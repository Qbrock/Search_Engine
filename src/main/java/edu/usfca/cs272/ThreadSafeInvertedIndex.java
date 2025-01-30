package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

/**
 * A ThreadSafe Inverted Index DataSet class for multi-threading
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {
	/** The lock used to protect concurrent access to the underlying set. */
	private final MultiReaderLock lock;

	/**
	 * Initializes a thread-safe indexed set.
	 */
	public ThreadSafeInvertedIndex() {
		super();
		lock = new MultiReaderLock();
	}

	/**
	 * Returns the identity hashcode of the lock object. Not particularly useful.
	 *
	 * @return the identity hashcode of the lock object
	 */
	public int lockCode() {
		return System.identityHashCode(lock);
	}

	@Override
	public Map<String, Integer> viewCounts() {
		lock.readLock().lock();
		try {
			return super.viewCounts();
		}
		finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Integer wordCount(String location) {
		lock.readLock().lock();
		try {
			return super.wordCount(location);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int numLocations() {
		lock.readLock().lock();
		try {
			return super.numLocations();
		}
		finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Integer size(String word, String location) {
		lock.readLock().lock();
		try {
			return super.size(word, location);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Integer size(String word) {
		lock.readLock().lock();
		try {
			return super.size(word);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Integer size() {
		lock.readLock().lock();
		try {
			return super.size();
		}
		finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<Integer> get(String word, String location) {
		lock.readLock().lock();
		try {
			return super.get(word, location);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<String> get(String word) {
		lock.readLock().lock();
		try {
			return super.get(word);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public NavigableSet<String> get() {
		lock.readLock().lock();
		try {
			return super.get();
		}
		finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void add(String word, String location, int wordNumber) {
		lock.writeLock().lock();
		try {
			super.add(word, location, wordNumber);
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void addAll(List<String> words, String location) {
		lock.writeLock().lock();
		try {
			super.addAll(words, location);
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void addAll(InvertedIndex bufferIndex) {
		lock.writeLock().lock();
		try {
			super.addAll(bufferIndex);
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public boolean contains(String word, String location, int position) {
		lock.readLock().lock();
		try {
			return super.contains(word, location, position);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean contains(String word, String location) {
		lock.readLock().lock();
		try {
			return super.contains(word, location);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean contains(String word) {
		lock.readLock().lock();
		try {
			return super.contains(word);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String toString() {
		lock.readLock().lock();

		try {
			return super.toString();
		}
		finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void asJson(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.asJson(path);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public ArrayList<Result> partialSearch(Set<String> stemmedWords) {
		lock.readLock().lock();
		try {
			return super.partialSearch(stemmedWords);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public ArrayList<Result> exactSearch(Set<String> stemmedWords) {
		lock.readLock().lock();
		try {
			return super.exactSearch(stemmedWords);
		}
		finally {
			lock.readLock().unlock();
		}
	}
}
