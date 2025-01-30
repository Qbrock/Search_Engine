package edu.usfca.cs272;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.usfca.cs272.InvertedIndex.Result;

/**
 * This class was made to multi-thread building stemmed query results for search/file inputs
 * @Author Quinn Brockmyre
 */
public class MultiThreadedQueryBuilder implements QueryBuilderInterface {

	/** lock for this class. */
	private final MultiReaderLock lock;

	/** Logger to use for this class. */
	public static final Logger log = LogManager.getLogger();

	/** Total results and their keys (words) */
	private final TreeMap<String, ArrayList<Result>> results;

	/** either partial or exact search, this will be true if partial search, false if exact */
	private final boolean partial;

	/** The shared workQueue to use */
	private final WorkQueue queue;

	/** Function call to the search functions in index */
	private final Function<Set<String>, ArrayList<Result>> searchFunction;

	/**
	 * Constructor for MultiThreadedQueryBuilder class
	 *
	 * @param index the specific II used for this QueryBuilder instance
	 * @param queue the queue to use from driver
	 * @param partial the type of search being done
	 */
	public MultiThreadedQueryBuilder(ThreadSafeInvertedIndex index, WorkQueue queue, boolean partial) {
		this.queue = queue;
		this.results = new TreeMap<>();
		this.partial = partial;
		this.lock = new MultiReaderLock();
		this.searchFunction = partial ? index::partialSearch : index::exactSearch;

	}

	@Override
	public List<Result> get(String line) {
		String joined = stemmedJoin(line);
		lock.readLock().lock();
		try {
			var resultList = results.get(joined);
			return resultList == null ? Collections.emptyList() : Collections.unmodifiableList(resultList);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public NavigableSet<String> get() {
		lock.readLock().lock();
		try {
			return Collections.unmodifiableNavigableSet(results.navigableKeySet());
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean contains(String line) {
		String joined = stemmedJoin(line);
		lock.readLock().lock();
		try {
			return results.containsKey(joined);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String toString() {
		StringWriter writer = new StringWriter();
		try {
			lock.readLock().lock();
			try {
				JsonWriter.writeQueryArrays(results, writer, 0);
			} finally {
				lock.readLock().unlock();
			}
		}
		catch (IOException e) {
			return null;
		}
		return writer.toString();
	}

	@Override
	public boolean getPartial() {
		return this.partial;
	}

	@Override
	public void asJson(Path path) throws IOException {
		lock.readLock().lock();
		try {
			JsonWriter.writeQueryArrays(results, path);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Method we want to call when wanting to multi-thread reading files and stemming them
	 * Takes a file we will read each line of, creating and executing a new task per line
	 * This will finish the queue after creating tasks
	 *
	 * @param path path of the file
	 * @throws IOException throws IOE
	 */
	@Override
	public void build(Path path) throws IOException {
		QueryBuilderInterface.super.build(path);
		queue.finish();
	}

	/**
	 * This method does not shutdown nor join (finish) the queue, it only executes tasks
	 */
	@Override
	public void build(String line) {
		queue.execute(new Task(line));
	}

	/**
	 * Class for individual runnable tasks
	 */
	private class Task implements Runnable {

		/**The query line we want to stem and get results for*/
		private final String queryLine;

		/**
		 * @param Query the line we want to query
		 */
		private Task(String Query) {
			this.queryLine = Query;
		}

		/**
		 * This method will take the given line, stem it, join it if not empty
		 * if results already contains the line it will return early to save time
		 * else we will search and put the results of said search inside our DataSet
		 */
		@Override
		public void run() {
			//@CITE got help from peer about making unique Stemmer for each, did not know they weren't thread safe
			search(queryLine);
		}
	}

	@Override
	public List<Result> search(String line) {
		TreeSet<String> stemmedWords = FileStemmer.uniqueStems(line);
		if(!stemmedWords.isEmpty()) {
			String joined = String.join(" ", stemmedWords);

			lock.readLock().lock();
			try {
				if(results.containsKey(joined)) {
					return get(joined);
				}
			} finally {
				lock.readLock().unlock();
			}

			ArrayList<Result> queryResults = searchFunction.apply(stemmedWords);

			lock.writeLock().lock();
			try {
				results.put(joined, queryResults);
			} finally {
				lock.writeLock().unlock();
			}
			return queryResults;
		}
		return Collections.emptyList();
	}
}