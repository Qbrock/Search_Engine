package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.net.URI;
import java.util.HashSet;
import java.util.List;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * WebCrawler Class made for multi-threading web crawling for Http(s) and Html
 * @Author Quinn Brockmyre
 */
public class WebCrawler {

	/** The custom work queue used for multi-threading */
	private final WorkQueue queue;

	/** A thread-safe Inverted Index to add our elements to */
	private final ThreadSafeInvertedIndex index;

	/** A HashSet to keep track of URI's we have already been to */
	private final HashSet<URI> visited;

	/** The total number of URL's to crawl */
	private final int total;

	/**
	 * Constructor class for the Web Crawler
	 *
	 * @param queue the queue to use
	 * @param index the Inverted Index to use
	 * @param total the total number of URL's to crawl
	 */
	public WebCrawler(WorkQueue queue, ThreadSafeInvertedIndex index, int total) {
		this.queue = queue;
		this.index = index;
		this.visited = new HashSet<URI>();
		this.total = total;
	}

	/**
	 * Method that starts the multi-threading process, cleaning the seed and adding it to visited off the bat
	 * Since we want to recursively call execute we only call it once here and then finish, we only join in the driver
	 *
	 * @param seed takes a URI seed
	 */
	public void crawl(URI seed) {
		seed = LinkFinder.clean(seed);
		visited.add(seed);
		queue.execute(new Task(seed));
		queue.finish();
	}

	/**
	 * Class for multi-threading individual runnable tasks
	 */
	private class Task implements Runnable {

		/** The seed to use */
		private final URI seed;

		/**
		 * Constructor for the Task Class
		 *
		 * @param seed the given seed
		 */
		private Task(URI seed) {
			this.seed = seed;
		}

		/**
		 * Since we use fetch here with the seed, and we use the seed to indicate any other links visited,
		 * we still associate the final response with the original seed. We also only fetch a single time and use that
		 * html twice for either method. The method fetch itself will not actually fetch the html unless it's status code
		 * 200 and an html
		 */
		@Override
		public void run() {
			String html = HtmlFetcher.fetch(seed, 3);
			if(html != null) {
				html = HtmlCleaner.stripBlockElements(html);
				processLinks(html);
				html = HtmlCleaner.stripHtml(html);
				addToIndex(html);
			}
		}

		/**
		 * Method that allows us to take a fetched html that has not been stripped, strips it and adds the contents to our index
		 *
		 * @param html the non-stripped fetched html string
		 */
		public void addToIndex(String html) {
			InvertedIndex local = new InvertedIndex();
			Stemmer stemmer = new SnowballStemmer(ENGLISH);
			int wordNumber = 1;
			String[] parsedLine = FileStemmer.parse(html);
			String seedString = seed.toString();
			for(String word : parsedLine) {
				String stem = stemmer.stem(word).toString();
				local.add(stem, seedString, wordNumber++);
			}
			index.addAll(local);
		}

		/**
		 * This is a recursive method that recursively creates a task to multi-thread. We take a non-stripped html
		 * and strip it of all block elements to leave the anchor tag, then we find all anchor tags that have valid
		 * href references, putting them into a list to go through in order, iterating through them as many times as
		 * indicated by the given total, and executing if and ONLY if they do not exist in visited. This also means that if they do,
		 * they will not have counted as part of the total links visited.
		 *
		 * This means that this: "A web page is considered crawled if its headers are fetched from a web server,
		 * even if it does not result in a 200 OK status code. If using the work queue properly and the maximum crawl limit is 50 URLs,
		 * then the first 49 unique URIs on the seed page (plus the seed URI itself) will be part of the crawl."
		 *
		 * Will also be done since we are executing any webpage that does not exist in visited, we do not exclude any page that has a 404 status code,
		 * that seed will be considered visited.
		 *
		 * @param html the non-stripped html
		 */
		public void processLinks(String html) {
			List<URI> links = LinkFinder.listUris(seed, html);
			var iterator = links.iterator();
			synchronized(visited) {
				while(iterator.hasNext() && visited.size() < total) {
					URI link = iterator.next();
					if(!visited.contains(link)) {
						visited.add(link);
						queue.execute(new Task(link));
					}
				}
			}
		}
	}
}