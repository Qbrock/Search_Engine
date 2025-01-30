package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.lang3.exception.UncheckedException;

/**
 * Builder Class for multi-threaded Inverted index dataSets
 */
public class MultiThreadedTextFileIndexer {

	/**
	 * Calls necessary functions to get all paths from user input
	 * and fills data sets for InvertedIndex
	 *
	 * @param input the user path input
	 * @param index the InvertedIndex class from driver
	 * @param queue the queue to use
	 * @throws IOException throws IOException
	 */
	public static void indexDirectory(Path input, ThreadSafeInvertedIndex index, WorkQueue queue) throws IOException {
		for(Path path : DirectoryTraverser.getPaths(input)) {
			queue.execute(new Task(path, index));
		}
		queue.finish();
	}

	/**
	 * Class for individual runnable tasks
	 */
	private static class Task implements Runnable {

		/**The path to use*/
		private final Path path;

		/**The index to use*/
		private final ThreadSafeInvertedIndex index;

		/**
		 * Constructor for the task class, needs a path input and Inverted Index to add to
		 *
		 * @param path the file path of the file we want to stem/was input
		 * @param index the index we want to add to, thread-safe for multi-threading
		 */
		private Task(Path path, ThreadSafeInvertedIndex index) {
			this.path = path;
			this.index = index;
		}

		@Override
		public void run() {
			try {
				InvertedIndex local = new InvertedIndex();
				TextFileIndexer.indexFile(path, local);
				index.addAll(local);
			}
			catch (IOException e) {
				throw new UncheckedException(e);
			}
		}
	}
}
