package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import edu.usfca.cs272.InvertedIndex.Result;


/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using spaces.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2024
 */
public class JsonWriter {
	/**
	 * Indents the writer by the specified number of times. Does nothing if the
	 * indentation level is 0 or less.
	 *
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(Writer writer, int indent) throws IOException {
		while (indent-- > 0) {
			writer.write("  ");
		}
	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write(element);
	}

	/**
	 * Indents and then writes the Number element as a string.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(Number element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write(element.toString());
	}

	/**
	 * Indents and then writes the Result element as a string.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(Result element, Writer writer, int indent) throws IOException {
		writeIndent("\"count\": " + element.getCount(), writer, indent+1);
		writer.write(",\n");
		writeIndent("\"score\": " + String.format("%.8f", element.getScore()), writer, indent+1);
		writer.write(",\n");
		writeIndent("\"where\": \"" + element.getLocation() + "\"", writer, indent+1);
		writer.write("\n");
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "}
	 * quotation marks.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeQuote(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 * [
	 *  1,
	 *  2,
	 *  3
	 * ]
	 * @CITE I believe I used chatgpt here to some extent, writing this comment a week late because I forgot to
	 * I think I was asking how to use the writer class
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements,
			Writer writer, int indent) throws IOException {
		writer.write("[");
		var iterator = elements.iterator();
		if (iterator.hasNext()) {
			writer.write("\n");
			var element = iterator.next();
			writeIndent(element, writer, indent+1);
		}
		while(iterator.hasNext()) {
			var element = iterator.next();
			writer.write(",\n");
			writeIndent(element, writer, indent+1);
		}
		writer.write("\n");
		writeIndent("]", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements,
			Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static String writeArray(Collection<? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArray(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 * {
	 * 	path: 1,
	 * 	path: 2
	 * }
	 * @CITE I asked chatgpt what the best way to iterate through a map was
	 * it generated Map.Entry<String, ? extends Number> entry : map.entrySet()
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements,
			Writer writer, int indent) throws IOException {
		writer.write("{");
		var iterator = elements.entrySet().iterator();
		if (iterator.hasNext()) {
			writer.write("\n");
			var element = iterator.next();
			writeIndent(("\"" + element.getKey() + "\": " + element.getValue().toString()), writer, indent+1);
		}
		while(iterator.hasNext()) {
			var element = iterator.next();
			writer.write(",\n");
			writeIndent(("\"" + element.getKey() + "\": " + element.getValue().toString()), writer, indent+1);
		}
		writer.write("\n");
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObject(Map, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements,
			Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObject(Map, Writer, int)
	 */
	public static String writeObject(Map<String, ? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObject(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any
	 * type of nested collection of number objects.
	 *
	 *	{
	 *		path: [
	 *			1,
	 *			2
	 *		]
	 *	{
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeArray(Collection)
	 */
	public static void writeObjectArrays(
			Map<String, ? extends Collection<? extends Number>> elements,
					Writer writer, int indent) throws IOException {
		writer.write("{\n");
		var iterator = elements.entrySet().iterator();
		if (iterator.hasNext()) {
			var element = iterator.next();
			writeIndent(("\"" + element.getKey() + "\": "), writer, indent+1);
			writeArray(element.getValue(), writer, indent+1);
		}
		while(iterator.hasNext()) {
			var element = iterator.next();
			writer.write(",\n");
			writeIndent(("\"" + element.getKey() + "\": "), writer, indent+1);
			writeArray(element.getValue(), writer, indent+1);
		}
		writer.write("\n");
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static void writeObjectArrays(
			Map<String, ? extends Collection<? extends Number>> elements, Path path)
					throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjectArrays(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static String writeObjectArrays(
			Map<String, ? extends Collection<? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObjectArrays(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes to a .json file in inverse index form for project 1.1 tests
	 *
	 * @param elements the map containing all elements
	 * @param writer given writer
	 * @param indent indent to start at
	 * @throws IOException throws IOException
	 */
	public static void writeMapObjectArrays(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> elements, Writer writer, int indent) throws IOException {
		writer.write("{");
		var iterator = elements.entrySet().iterator();
		if (iterator.hasNext()) {
			var element = iterator.next();
			writer.write("\n");
			writeIndent(("\"" + element.getKey() + "\": "), writer, indent+1);
			writeObjectArrays(element.getValue(), writer, indent+1);
		}
		while(iterator.hasNext()) {
			var element = iterator.next();
			writer.write(",\n");
			writeIndent(("\"" + element.getKey() + "\": "), writer, indent+1);
			writeObjectArrays(element.getValue(), writer, indent+1);
		}
		writer.write("\n");
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes to a .json file in inverse index form for project 1.1 tests
	 *
	 * @param map the map containing all elements
	 * @param path Path to write to
	 * @throws IOException throws IOException
	 */
	public static void writeMapObjectArrays(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> map, Path path) throws IOException{
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeMapObjectArrays(map, writer, 0);
		}
	}

	/**
	 * Writes to a .json file in inverse index form for project 1.1 tests
	 *
	 * @param elements indexMap from InvertedIndex
	 * @return returns writer.toString
	 * @throws IOException throws IOException
	 */
	public static String writeMapObjectArrays(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> elements) throws IOException {
		try {
			StringWriter writer = new StringWriter();
			writeMapObjectArrays(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}
	/**
	 * Writes the elements as a pretty JSON array with nested objects. The generic
	 * notation used allows this method to be used for any type of collection with
	 * any type of nested map of String keys to number objects.
	 *
	 * [
	 * 	{
	 * 	 path: 1,
	 * 	 path: 2
	 *	}
	 *]
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeObject(Map)
	 */
	public static void writeArrayObjects(
			Collection<? extends Map<String, ? extends Number>> elements,
					Writer writer, int indent) throws IOException {
		writer.write("[");
		var iterator = elements.iterator();
		if (iterator.hasNext()) {
			writer.write("\n");
			var element = iterator.next();
			writeIndent(writer, indent+1);
			writeObject(element, writer, indent+1);
		}
		while(iterator.hasNext()) {
			var element = iterator.next();
			writer.write(",\n");
			writeIndent(writer, indent+1);
			writeObject(element, writer, indent+1);
		}
		writer.write("\n");
		writeIndent("]", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArrayObjects(Collection)
	 */
	public static void writeArrayObjects(
			Collection<? extends Map<String, ? extends Number>> elements, Path path)
					throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArrayObjects(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array with nested objects.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArrayObjects(Collection)
	 */
	public static String writeArrayObjects(
			Collection<? extends Map<String, ? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArrayObjects(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}


	/**
	 * @param elements query elements
	 * @param writer writer to use
	 * @param indent indent we are on
	 * @throws IOException throws IOE
	 */
	public static void writeQuerys(Collection<Result> elements, Writer writer, int indent) throws IOException {
		writer.write("[");
		var iterator = elements.iterator();
		if (iterator.hasNext()) {
			writer.write("\n");
			var element = iterator.next();
			writeIndent("{\n", writer, indent+1);
			writeIndent(element, writer, indent+1);
			writeIndent("}", writer, indent+1);
		}
		while(iterator.hasNext()) {
			var element = iterator.next();
			writer.write(",\n");
			writeIndent("{\n", writer, indent+1);
			writeIndent(element, writer, indent+1);
			writeIndent("}", writer, indent+1);
		}
		writer.write("\n");
		writeIndent("]", writer, indent);
	}

	/**
	 * Writes in query format for each query in a map
	 *
	 * @param elements the queries we are given
	 * @param writer the writer to use
	 * @param indent what indent we are at
	 * @throws IOException throws IOE
	 */
	public static void writeQueryArrays(Map<String, ? extends Collection<Result>> elements, Writer writer, int indent) throws IOException {
		writeIndent("{\n", writer, indent);
		var iterator = elements.entrySet().iterator();
		if (iterator.hasNext()) {
			var element = iterator.next();
			writeIndent(("\"" + element.getKey() + "\": "), writer, indent+1);
			writeQuerys(element.getValue(), writer, indent+1);
		}
		while(iterator.hasNext()) {
			var element = iterator.next();
			writer.write(",\n");
			writeIndent(("\"" + element.getKey() + "\": "), writer, indent+1);
			writeQuerys(element.getValue(), writer, indent+1);
		}
		writer.write("\n");
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes in query format for each query in a map
	 *
	 * @param elements the queries we are given
	 * @param path the path we need to get elems
	 * @throws IOException throws IOE
	 */
	public static void writeQueryArrays(Map<String, ? extends Collection<Result>> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeQueryArrays(elements, writer, 0);
		}
	}

	/**
	 * Writes in query format for each query in a map
	 *
	 * @param elements the queries we are given
	 * @return returns a String of what we are writing
	 * @throws IOException throws IOE
	 */
	public static String writeQueryArrays(Map<String, ? extends Collection<Result>> elements) throws IOException {
		try {
			StringWriter writer = new StringWriter();
			writeQueryArrays(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}
	/** Prevent instantiating this class of static methods. */
	private JsonWriter() {
	}
}
