package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

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

import edu.usfca.cs272.InvertedIndex.Result;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * This class was made to stem and build query results for search/file inputs
 */
public class QueryBuilder implements QueryBuilderInterface {

	/**
	 * Total results and their keys (lines or single words)
	 */
	private final TreeMap<String, ArrayList<Result>> results;

	/**
	 * Either partial or exact search, this will be true if partial search, false if exact
	 */
	private final boolean partial;

	/**
	 * Shared stemmer, since we are single threaded this is ok to use so we don't have to create more
	 */
	private final Stemmer stemmer;

	/**
	 * Function call to the search functions in index
	 */
	private final Function<Set<String>, ArrayList<Result>> searchFunction;

	/**
	 * Constructor for QueryBuilder class
	 *
	 * @param index the specific InvertedIndex used for this QueryBuilder instance
	 * @param partial the type of search being done
	 */
	public QueryBuilder(InvertedIndex index, boolean partial) {
		this.results = new TreeMap<>();
		this.partial = partial;
		this.stemmer = new SnowballStemmer(ENGLISH);
		this.searchFunction = partial ? index::partialSearch : index::exactSearch;
	}

	@Override
	public List<Result> get(String line) {
		var resultList = results.get(stemmedJoin(line));
		return resultList == null ? Collections.emptyList() : Collections.unmodifiableList(resultList);
	}

	@Override
	public NavigableSet<String> get() {
		return Collections.unmodifiableNavigableSet(results.navigableKeySet());
	}

	@Override
	public boolean getPartial() {
		return this.partial;
	}

	@Override
	public boolean contains(String line) {
		return results.containsKey(stemmedJoin(line));
	}

	@Override
	public String toString() {
		StringWriter writer = new StringWriter();
		try {
			JsonWriter.writeQueryArrays(results, writer, 0);
		}
		catch (IOException e) {
			return null;
		}
		return writer.toString();
	}

	@Override
	public void asJson(Path path) throws IOException {
		JsonWriter.writeQueryArrays(results, path);
	}

	@Override
	public String stemmedJoin(String line) {
		TreeSet<String> stemmedWords = FileStemmer.uniqueStems(line, stemmer);
		return String.join(" ", stemmedWords);
	}

	@Override
	public void build(String line) {
		search(line);
	}

	@Override
	public List<Result> search(String line) {
		TreeSet<String> stemmedWords = FileStemmer.uniqueStems(line, stemmer);
		String joined = String.join(" ", stemmedWords);
		if(!stemmedWords.isEmpty() && !results.containsKey(joined)) {
			var result = searchFunction.apply(stemmedWords);
			results.put(joined, result);
			return result;
		}
		return Collections.emptyList();
	}
}