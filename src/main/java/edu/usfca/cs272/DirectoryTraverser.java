package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;


/**
 * Directory Traversing class, goes through a directory and handles each valid file
 * If File given handles the file itself
 */
public class DirectoryTraverser {

	/**
	 * Checks if a file is a text file
	 *
	 * @param file the file to check
	 * @return if the file is a .txt or .text file
	 */
	public static boolean isTextFile(Path file) {
		String text = file.toString().toLowerCase();
		return text.endsWith(".txt") || text.endsWith(".text");
	}

	/**
	 * Gets all the file paths for each text file in a directory
	 * Fills both maps with necessary data
	 *
	 * @param input input path
	 * @return returns HashSet of possible paths
	 * @throws IOException throws IOexception
	 */
	public static HashSet<Path> getPaths(Path input) throws IOException {
		HashSet<Path> paths = new HashSet<>();
		getPaths(input, paths);
		return paths;
	}

	/**
	 * Gets all the file paths for each text file in a directory
	 * Fills both maps with necessary data
	 *
	 * @CITE I used chatgpt trying to figure out the best way to iterate through the files in
	 * the directory. As well as using the regular web, java.nio.File is the best I could find.
	 *
	 * @param input the input Path, could be a directory or a file
	 * @param paths the list to put the text files in
	 * @throws IOException throws IOExeption
	 */
	public static void getPaths(Path input, Collection<Path> paths) throws IOException {
		if(Files.isRegularFile(input)) {
			paths.add(input);
			return;
		}
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(input)) {
			if (Files.isDirectory(input)) {
				for (Path file : stream) {
					if (Files.isDirectory(file)) {
						getPaths(file, paths);
					} else if(isTextFile(file)) {
						paths.add(file);
					}
				}
			}
		}
	}

	/** Prevent instantiating this class of static methods. */
	private DirectoryTraverser() {
	}
}
