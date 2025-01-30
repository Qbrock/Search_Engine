package edu.usfca.cs272;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * InvertedIndex Class
 * Holds data sets for Inverted Index and a counts map to keep track of file sizes
 */
public class InvertedIndex {

	/**
	 * CountMap, holds file names and their size in words as an Integer
	 */
	private final TreeMap<String, Integer> counts;

	/**
	 * index, keys are words in each file, value is a map, where the keys are the file paths as
	 * strings and value is an TreeSet of Integers of each location the word appears in the file
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;

	/**
	 * Constructor for InvertedIndex Class
	 */
	public InvertedIndex() {
		this.counts = new TreeMap<>();
		this.index = new TreeMap<>();
	}

	/**
	 * Returns the countMap in view only mode
	 *
	 * @return returns countMap
	 */
	public Map<String, Integer> viewCounts() {
		return Collections.unmodifiableMap(counts);
	}

	/**
	 * Retrieves the word count for a given file.
	 * @CITE Method signature generated with chatgpt
	 *
	 * @param location the file path
	 * @return the word count for the file, or null if the file is not in the map
	 */
	public Integer wordCount(String location) {
		return counts.getOrDefault(location, 0);
	}

	/**
	 * Returns the total number of unique files in the index.
	 * @CITE Method signature generated with chatgpt
	 *
	 * @return the number of files
	 */
	public int numLocations() {
		return counts.size();
	}

	/**
	 * returns the size of the a specific value in the index based on which keys are given
	 * returns 0 if null pointer or if word doesn't have inner map
	 *
	 * @param word the key we use to get the maps size
	 * @param location the specific file we want to
	 * @return returns the size of the Inner set inside the index based on the given word (key) and a location (file) inside
	 */
	public Integer size(String word, String location) {
		var inner = index.get(word);
		if (inner != null) {
			var positions = inner.get(location);
			return positions == null ? 0 : positions.size();
		}
		return 0;
	}

	/**
	 * returns the size of the a specific value in the index based on which key is given
	 *
	 * @param word the key we use to get the maps size
	 * @return returns the size of the Inner map inside the index based on the given word (key)
	 */
	public Integer size(String word) {
		var locations = index.get(word);
		return locations == null ? 0 : locations.size();
	}

	/**
	 * returns the key-size of the index
	 *
	 * @return returns an Integer
	 */
	public Integer size() {
		return index.size();
	}

	/**
	 * Gets the positions of a word in a specific file for index.
	 * @CITE Method signature generated with chatgpt
	 *
	 * @param word the word to look for
	 * @param location the location of the file
	 * @return a set of positions, or an empty set if the word or file is not present
	 */
	public Set<Integer> get(String word, String location) {
		var inner = index.get(word);
		if (inner != null) {
			var positions = inner.get(location);
			return positions == null ? Collections.emptySet() : Collections.unmodifiableSet(positions);
		}
		return Collections.emptySet();
	}

	/**
	 * Retrieves all files where the word appears.
	 * @CITE had to look up how to return an empty set here
	 * @CITE Method signature generated with chatgpt
	 *
	 * @param word the word to look for
	 * @return a set of file paths, or null if the word is not present
	 */
	public Set<String> get(String word) {
		var inner = index.get(word);
		return inner != null ? Collections.unmodifiableSet(inner.keySet()) : Collections.emptySet();
	}

	/**
	 * gets us a encapsulated view of index's keyset
	 *
	 * @return return unmodifiable view of the index.keyset
	 */
	public NavigableSet<String> get() {
		return Collections.unmodifiableNavigableSet(index.navigableKeySet());
	}

	/**
	 * Add function to add a single word to the index
	 *
	 * @CITE looked up better way to do this and got reminded of computeIfAbsent
	 * @CITE asked chatgpt which BiFunction could help me get the max of an existing value vs given one
	 * @CITE https://docs.oracle.com/javase/8/docs/api/?java/util/function/BiFunction.html - used this but didn't get much from it
	 *
	 * @param word the word itself
	 * @param location the location to the file
	 * @param wordNumber the number of word we are currently at
	 */
	public void add(String word, String location, int wordNumber) {
		index.computeIfAbsent(word, i -> new TreeMap<>())
		.computeIfAbsent(location, i -> new TreeSet<>())
		.add(wordNumber);
		counts.merge(location, wordNumber, Math::max);
	}

	/**
	 * Adds all words of the location in the inverted index
	 *
	 * @param words words to add to index's keys
	 * @param location the location (file) that the words occur in
	 */
	public void addAll(List<String> words, String location) {
		int position = 1;
		for(String word : words) {
			add(word, location, position++);
		}
	}

	/**
	 * Takes an InvertedIndex instance and transfers all elements from it into this instance
	 * @CITE used chatgpt to help with the name bufferIndex
	 *
	 * @param bufferIndex the index we are transferring elements from
	 */
	public void addAll(InvertedIndex bufferIndex) {
		for (var otherEntry : bufferIndex.index.entrySet()) {
			String word = otherEntry.getKey();
			var thisEntry = this.index.get(word);
			var otherValue = otherEntry.getValue();

			if (thisEntry == null) {
				this.index.put(word, otherValue);
			}	else {
				for (var otherInnerEntry : otherValue.entrySet()) {
					String location = otherInnerEntry.getKey();
					var thisInnerSet = thisEntry.get(location);
					var otherInnerSet = otherInnerEntry.getValue();

					if (thisInnerSet == null) {
						thisEntry.put(location, otherInnerSet);
					} else {
						thisInnerSet.addAll(otherInnerSet);
					}
				}
			}
		}
		//counts update
		for (var otherEntry : bufferIndex.counts.entrySet()) {
			String location = otherEntry.getKey();
			var otherValue = otherEntry.getValue();
			counts.merge(location, otherValue, Math::max);
		}
	}

	/**
	 * Checks if the index contains the word at a specific position in a file.
	 * @CITE Original Method signature generated with chatgpt
	 *
	 * @param word the word to look for
	 * @param location the location of the file
	 * @param position the position in the file to check (specifically the exact word position)
	 * @return true if the word exists in the specified file, false otherwise
	 */
	public boolean contains(String word, String location, int position) {
		var inner = index.get(word);
		if (inner != null) {
			var positions = inner.get(location);
			return positions != null && positions.contains(position);
		}
		return false;
	}


	/**
	 * Checks if the index contains the word in a specific file.
	 * @CITE Method signature generated with chatgpt
	 *
	 * @param word the word to look for
	 * @param location the location of the file
	 * @return true if the word exists in the specified file, false otherwise
	 */
	public boolean contains(String word, String location) {
		var inner = index.get(word);
		return inner != null && inner.containsKey(location);
	}

	/**
	 * Checks if the index contains the specified word.
	 * @CITE Original Method signature generated with chatgpt
	 *
	 * @param word the word look for
	 * @return true if the word exists in the specified file, false otherwise
	 */
	public boolean contains(String word) {
		return index.containsKey(word);
	}

	@Override
	public String toString() {
		StringWriter writer = new StringWriter();
		try {
			JsonWriter.writeMapObjectArrays(index, writer, 0);
			JsonWriter.writeObject(counts, writer, 0);
		}
		catch (IOException e) {
			return null;
		}
		return writer.toString();
	}

	/**
	 * Writes index as a JSON object to the path
	 *
	 * @param path The path we will be writing to
	 * @throws IOException throws IOE
	 */
	public void asJson(Path path) throws IOException {
		JsonWriter.writeMapObjectArrays(index, path);
	}

	/**
	 * Will return the amount of words in a file, made for Result, this is only for partial searches
	 * @CITE ChatGpt was consulted when making the TreeMap of queries
	 *
	 * For the binarySearch loop, I convert index into a map that starts with a word that has
	 * the stemmedWord as a part of it, basically it will start on a key that is valid,
	 * then it will go through all keys that have that stem, once it has one that is not valid
	 * it breaks out of the loop
	 *
	 * @param stemmedWords words to look for
	 * @return returns results
	 */
	public ArrayList<Result> partialSearch(Set<String> stemmedWords) {
		HashMap<String, Result> lookup = new HashMap<>();
		ArrayList<Result> results = new ArrayList<>();

		for (String word : stemmedWords) {
			for(var tailMap : index.tailMap(word).entrySet()) {
				if(tailMap.getKey().startsWith(word)) {
					var wordsMap = tailMap.getValue();
					searchWords(wordsMap, lookup, results);
				} else {
					// once we are done starting with the keyWord we wont need to search anymore
					break;
				}
			}
		}
		Collections.sort(results);
		return results;
	}

	/**
	 * Will return the amount of words in a file, made for Result, this is only for exact searches
	 * @CITE ChatGpt was consulted when making the TreeMap of queries
	 *
	 * @param stemmedWords words to look for
	 * @return returns results
	 */
	public ArrayList<Result> exactSearch(Set<String> stemmedWords) {
		HashMap<String, Result> lookup = new HashMap<>();
		ArrayList<Result> results = new ArrayList<>();

		for (String word : stemmedWords) {
			var wordsMap = index.get(word);
			if(wordsMap == null) {
				continue;
			}
			searchWords(wordsMap, lookup, results);
		}
		Collections.sort(results);
		return results;

	}

	/**
	 * Will return the amount of words in a file, made for Result
	 * @CITE ChatGpt was consulted when making the TreeMap of queries
	 *
	 * @param stemmedWords words to look for
	 * @param partial determines whether we partial search
	 * @return returns queries
	 */
	public ArrayList<Result> searchQueries(Set<String> stemmedWords, boolean partial) {
		if(partial) {
			return partialSearch(stemmedWords);
		} else {
			return exactSearch(stemmedWords);
		}
	}

	/**
	 * helper function for searchPartial and searchExact
	 * creates a new query if needed, otherwise adds locations and count to existing query
	 *
	 * @param wordsMap the words we want to go through
	 * @param lookup the lookup map to add to
	 * @param results the result list we will modify
	 */
	private void searchWords(TreeMap<String, TreeSet<Integer>> wordsMap, HashMap<String, Result> lookup, ArrayList<Result> results) {
		for (var entry : wordsMap.entrySet()) {
			String location = entry.getKey();
			int matches = entry.getValue().size();
			Result result = lookup.get(location);

			if (result == null) {
				result = new Result(location);
				lookup.put(location, result);
				results.add(result);
			}
			result.increment(matches);
		}
	}

	/**
	 * Built to hold data for the query tests in project 2.0
	 */
	public class Result implements Comparable<Result> {
		// need to store the location, total word count of the location, and the number of matches for that location

		/**
		 * path to file location
		 */
		private final String location;

		/**
		 * total word count for the location we get from counts map
		 */
		private int count;

		/**
		 * total matches in the file location for the word we query for
		 */
		private double score;

		/**
		 * Constructor for this class
		 *
		 * @param location location of the file
		 */
		public Result(String location) {
			this.location = location;
			this.count = 0; // these are set to 0 at first since we will only increment them with the increment method through the searches
			this.score = 0;
		}

		/**
		 * @return returns this location
		 */
		public String getLocation() {
			return this.location;
		}

		/**
		 * @return returns this score
		 */
		public double getScore() {
			return this.score;
		}

		/**
		 * @return returns this count
		 */
		public int getCount() {
			return this.count;
		}


		@Override
		public String toString() {
			StringWriter writer = new StringWriter();

			try {
				JsonWriter.writeIndent(this, writer, 0);
			}
			catch (IOException e) {
				System.out.println("Unable to write query toString.");
			}

			return writer.toString();
		}

		/**
		 * Use this when incrementing to an already established query
		 *
		 * @param increment the amount to increment
		 */
		private void increment(int increment) {
			this.count += increment;
			this.score = count / (double) wordCount(location);
		}

		/**
		 * returns 1 if this query should come before the next, 0 if equal to, -1 if after
		 */
		@Override
		public int compareTo(Result o) {
			var comparedScore = Double.compare(o.score, this.score);
			if(comparedScore != 0) {
				return comparedScore;
			}

			var comparedCount = Integer.compare(o.count, this.count);
			if(comparedCount != 0) {
				return comparedCount;
			}

			var comparedLocation = String.CASE_INSENSITIVE_ORDER.compare(this.location, o.location);
			if(comparedLocation != 0) {
				return comparedLocation;
			}
			return 0;
		}
	}
}