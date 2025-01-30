package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * A specialized version of {@link HttpsFetcher} that follows redirects and
 * returns HTML content if possible.
 *
 * @see HttpsFetcher
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2024
 */
public class HtmlFetcher {
	/**
	 * Returns {@code true} if and only if there is a "content-type" header (assume
	 * lowercase) and the first value of that header starts with the value
	 * "text/html" (case-insensitive).
	 *
	 * @param headers the HTTP/1.1 headers to parse (keys must be lowercase)
	 * @return {@code true} if the headers indicate the content type is HTML
	 *
	 * @see HttpsFetcher#processHttpHeaders(BufferedReader)
	 * @CITE got help from peer and Amelia
	 */
	public static boolean isHtml(Map<String, List<String>> headers) {
		List<String> contentHeader = headers.get("content-type");
		if(contentHeader != null && contentHeader.get(0).toLowerCase().startsWith("text/html")) {
			return true;
		}
		return false;
	}

	/**
	 * Parses the HTTP status code from the provided HTTP headers, assuming the
	 * status line is stored under the {@code null} key.
	 *
	 * @param headers the HTTP/1.1 headers to parse (keys must be lowercase)
	 * @return the HTTP status code or -1 if unable to parse for any reasons
	 *
	 * @see HttpsFetcher#processHttpHeaders(BufferedReader)
	 */
	public static int getStatusCode(Map<String, List<String>> headers) {
		String nullHeader = headers.get(null).get(0);
		try {
			return Integer.valueOf(nullHeader.substring(9, 12));
		} catch(NullPointerException e) {
			return -1;
		}
	}

	/**
	 * If the HTTP status code is between 300 and 399 (inclusive) indicating a
	 * redirect, returns the first redirect location if it is provided. Otherwise
	 * returns {@code null}.
	 *
	 * @param headers the HTTP/1.1 headers to parse (keys must be lowercase)
	 * @return the first redirected location if the headers indicate a redirect
	 *
	 * @see HttpsFetcher#processHttpHeaders(BufferedReader)
	 */
	public static String getRedirect(Map<String, List<String>> headers) {
		int statusCode = getStatusCode(headers);
		if(statusCode >= 300 && statusCode < 400) {
			List<String> locationHeader = headers.get("location");
			if(locationHeader != null && !locationHeader.isEmpty()) {
				return locationHeader.get(0);
			}
		}
		return null;
	}

	/**
	 * Efficiently fetches HTML using HTTP/1.1 and sockets.
	 *
	 * <p>
	 * The HTTP body will only be fetched and processed if the status code is 200
	 * and the content-type is HTML. In that case, the HTML will be returned as a
	 * single joined String using the {@link System#lineSeparator}.
	 *
	 * <p>
	 * Otherwise, the HTTP body will not be fetched. However, if the status code is
	 * a redirect, then the location of the redirect will be recursively followed up
	 * to the specified number of times. Once the number of redirects falls to 0 or
	 * lower, then redirects will no longer be followed.
	 *
	 * <p>
	 * If valid HTML cannot be fetched within the specified number of redirects,
	 * then {@code null} is returned.
	 *
	 * @param uri the URI to fetch
	 * @param redirects the number of times to follow redirects
	 * @return the HTML or {@code null} if unable to fetch valid HTML
	 *
	 * @see HttpsFetcher#openConnection(URI)
	 * @see HttpsFetcher#printGetRequest(PrintWriter, URI)
	 * @see HttpsFetcher#processHttpHeaders(BufferedReader)
	 *
	 * @see String#join(CharSequence, CharSequence...)
	 * @see System#lineSeparator()
	 *
	 * @see #isHtml(Map)
	 * @see #getRedirect(Map)
	 * @CITE got help from Amelia and a peer
	 */
	public static String fetch(URI uri, int redirects) {
		String html = null;

		try (
				Socket socket = HttpsFetcher.openConnection(uri);
				PrintWriter request = new PrintWriter(socket.getOutputStream());
				InputStreamReader input = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
				BufferedReader response = new BufferedReader(input);
				) {
			HttpsFetcher.printGetRequest(request, uri);
			Map<String, List<String>> headers = HttpsFetcher.processHttpHeaders(response);
			if(isHtml(headers) && getStatusCode(headers) == 200) {
				// read everything remaining in socket reader as the content
				List<String> content = response.lines().toList();
				headers.put("content", content);
				html = String.join(System.lineSeparator(), headers.get("content"));
			} else {
				String redirect = getRedirect(headers);
				if(redirect != null && redirects > 0) {
					html = fetch(redirect, redirects-1);
				}
			}
		}
		catch (IOException e) {
			html = null;
		}
		return html;
	}

	/**
	 * Converts the {@link String} into a {@link URI} object and then calls
	 * {@link #fetch(URI, int)}.
	 *
	 * @param uri the URI to fetch
	 * @param redirects the number of times to follow redirects
	 * @return the HTML or {@code null} if unable to fetch valid HTML
	 *
	 * @see #fetch(URI, int)
	 */
	public static String fetch(String uri, int redirects) {
		try {
			return fetch(new URI(uri), redirects);
		}
		catch (NullPointerException | URISyntaxException e) {
			return null;
		}
	}

	/**
	 * Converts the {@link String} url into a {@link URL} object and then calls
	 * {@link #fetch(URI, int)} with 0 redirects.
	 *
	 * @param uri the URI to fetch
	 * @return the HTML or {@code null} if unable to fetch valid HTML
	 *
	 * @see #fetch(URI, int)
	 */
	public static String fetch(String uri) {
		return fetch(uri, 0);
	}

	/**
	 * Calls {@link #fetch(URI, int)} with 0 redirects.
	 *
	 * @param uri the URI to fetch
	 * @return the HTML or {@code null} if unable to fetch valid HTML
	 */
	public static String fetch(URI uri) {
		return fetch(uri, 0);
	}

	/** Prevent instantiating this class of static methods. */
	private HtmlFetcher() {
	}
}
