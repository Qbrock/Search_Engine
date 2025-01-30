package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Builder Class for Inverted index dataSets
 */
public class TextFileIndexer {

	/**
	 * Calls necessary functions to get all paths from user input
	 * and fills data sets for InvertedIndex
	 *
	 * @param input the user path input
	 * @param index the InvertedIndex class from driver
	 * @throws IOException throws IOException
	 */
	public static void indexDirectory(Path input, InvertedIndex index) throws IOException {
		for(Path path : DirectoryTraverser.getPaths(input)) {
			indexFile(path, index);
		}
	}

	/**
	 * Adds stems from the text file to the provided index.
	 *
	 * @param path the file to stem and index
	 * @param index InvertedIndex Class to fill
	 * @throws IOException throws IOE
	 */
	public static void indexFile(Path path, InvertedIndex index) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(path, UTF_8)) {
			String document = path.toString();
			String line;
			int wordNumber = 1;
			Stemmer stemmer = new SnowballStemmer(ENGLISH);
			while((line = reader.readLine()) != null) {
				String[] parsedLine = FileStemmer.parse(line);
				for(String word : parsedLine) {
					String stem = stemmer.stem(word).toString();
					index.add(stem, document, wordNumber++);
				}
			}
		}
	}
}
