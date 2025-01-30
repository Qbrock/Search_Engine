package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import edu.usfca.cs272.InvertedIndex.Result;

/**
 * Interface for Query Builders
 */
public interface QueryBuilderInterface {

	/**
	 * gets the size of a specific files result returns 0 if the keyLine doesn't exist
	 *
	 * @param line the word we want to see the size of query result of
	 * @return returns Integer size
	 */
	public default Integer size(String line)  {
		var resultSet = get(line);
		return resultSet == null ? 0 : resultSet.size();
	}

	/**
	 * returns the key-size of the results
	 *
	 * @return returns the size of results keys
	 */
	public default Integer size()  {
		return get().size();
	}


	/**
	 * Stems the line and returns the joined result of the stemmed set
	 *
	 * @param line the line of text to stem and join
	 * @return returns a joined String of stems
	 */
	public default String stemmedJoin(String line) {
		TreeSet<String> stemmedWords = FileStemmer.uniqueStems(line);
		return String.join(" ", stemmedWords);
	}

	/**
	 * Returns the List of Results of a specific keyLine
	 *
	 * @param line line we want to get results of
	 * @return returns result list
	 */
	public List<Result> get(String line);

	/**
	 * Returns a Set view of the keys in results
	 *
	 * @return returns Result's Keys
	 */
	public NavigableSet<String> get();

	/**
	 * Gets the partial boolean value for this class instance
	 *
	 * @return returns partial's boolean value
	 */
	public boolean getPartial();

	/**
	 * returns whether the given line exists in the results key set
	 *
	 * @param line the keyLine to find the result map
	 * @return returns boolean of whether the key exists
	 */
	public boolean contains(String line);

	/**
	 * Writes results as a JSON object to the path
	 *
	 * @param path The path we will be writing to
	 * @throws IOException throws IOE
	 */
	public void asJson(Path path) throws IOException;

	/**
	 * Reads file from given path
	 * Takes lines of queries and calls helper build function
	 *
	 * @param path path of query file
	 * @throws IOException throws IOException
	 * @CITE a friend told me about the idea of having a default build, not the actual implementation just the idea
	 */
	public default void build(Path path) throws IOException{
		try (BufferedReader reader = Files.newBufferedReader(path, UTF_8)) {
			String line;
			while((line = reader.readLine()) != null) {
				build(line);
			}
		}
	}

	/**
	 * Builds a single query and puts it into local dataSet using either partial
	 * or exact search based on class member "partial"
	 *
	 * @param line the line read from the file
	 */
	public void build(String line);

	/**
	 * @param line query
	 * @return returns query results
	 */
	public List<Result> search(String line);
}
