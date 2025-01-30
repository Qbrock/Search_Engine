package edu.usfca.cs272;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Quinn Brockmyre
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2024
 */
public class Driver {
	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments/flags. This includes (but is not limited to) how to build or search an
	 * inverted index as well as whether or not we are multi-threading.
	 *
	 * @param args flag/value pairs used to start this program
	 * @CITE asked chatgpt for a better way of getting threads
	 * @CITE asked Par about getting rid of the casting
	 */
	public static void main(String[] args) {
		// store initial start time
		Instant start = Instant.now();
		ArgumentParser parser = new ArgumentParser();
		parser.parse(args);
		boolean multiThread = parser.hasFlag("-threads") || parser.hasFlag("-html") || parser.hasFlag("-server");
		boolean partial = parser.hasFlag("-partial");

		InvertedIndex index = null;
		ThreadSafeInvertedIndex safe = null;
		WorkQueue queue = null;
		QueryBuilderInterface queries = null;
		WebCrawler crawler = null;

		if(multiThread) {
			int threads = multiThread ? Math.max(1, parser.getInteger("-threads", 5)) : 1;
			safe = new ThreadSafeInvertedIndex();
			index = safe;
			queue = new WorkQueue(threads);
			queries = new MultiThreadedQueryBuilder(safe, queue, partial);
			int total = parser.getInteger("-crawl", 1);
			crawler = new WebCrawler(queue, safe, total);
		} else {
			index = new InvertedIndex();
			queries = new QueryBuilder(index, partial);
		}

		if(parser.hasFlag("-text")) {
			Path textPath = parser.getPath("-text");
			try {
				if(safe != null) {
					MultiThreadedTextFileIndexer.indexDirectory(textPath, safe, queue);
				} else {
					TextFileIndexer.indexDirectory(textPath, index);
				}
			}
			catch (IOException | NullPointerException e) {
				System.out.println("Unable to index the files at path: " + textPath);
			}
		}

		if(parser.hasFlag("-html")) {
			String seed = parser.getString("-html");
			try {
				if(seed != null) {
					crawler.crawl(new URI(seed));
				} else {
					System.out.println("Something it seems the given URI is null: " + seed);
				}
			}
			catch (URISyntaxException e) {
				System.out.println("Something went wrong with the given URI: " + seed);
			}
		}

		if(parser.hasFlag("-query")) {
			Path path = parser.getPath("-query");
			try {
				queries.build(path);
			}
			catch (IOException | NullPointerException e) {
				System.out.println("Unable to query items in this file: " + path);
			}
		}

		if(parser.hasFlag("-server")) {
			int port = parser.getInteger("-server", 8080);
			ServerHandler server = new ServerHandler(port, queries);
			try {
				server.run();
			} catch(Exception e) {
				e.getStackTrace();
			}
		}

		if(queue != null) {
			queue.shutdown();
		}

		if(parser.hasFlag("-counts")) {
			Path path = parser.getPath("-counts", Path.of("counts.json"));
			try {
				JsonWriter.writeObject(index.viewCounts(), path);
			}
			catch (IOException e) {
				System.out.println("Counts problem with output file: " + path);
			}
		}

		if(parser.hasFlag("-index")) {
			Path indexPath = parser.getPath("-index", Path.of("index.json"));
			try {
				index.asJson(indexPath);
			}
			catch (Exception e) {
				System.out.println("Indexing problem with output file: " + indexPath);
			}
		}

		if(parser.hasFlag("-results")) {
			Path path = parser.getPath("-results", Path.of("results.json"));
			try {
				queries.asJson(path);
			}
			catch (IOException e) {
				System.out.println("Results problem with output file: " + path);
			}
		}

		if(queue != null) {
			queue.join();
		}

		System.out.println("Working Directory: " + Path.of(".").toAbsolutePath().normalize());
		System.out.println("Arguments: " + Arrays.toString(args));
		long elapsed = Duration.between(start, Instant.now()).toMillis();
		double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}
}