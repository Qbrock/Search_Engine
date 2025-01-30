package edu.usfca.cs272;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;

import edu.usfca.cs272.InvertedIndex.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * ServerHandler class that handles servlets etc
 * @CITE got A LOT of help from a peer
 */
public class ServerHandler {
	/** The hard-coded port to run this server. */
	public final int port;

	/**The dataSet for the query results we have */
	public final QueryBuilderInterface queries;

	/**
	 * @param port port (default 8080)
	 * @param queries queries
	 */
	public ServerHandler(int port, QueryBuilderInterface queries) {
		this.port = port;
		this.queries = queries;
	}

	/**
	 * Run method for Driver
	 *
	 * @throws Exception throws Exception
	 */
	public void run() throws Exception {
		Server server = new Server(port);

		ServletContextHandler handler = new ServletContextHandler();
		handler.addServlet(new SearchEngineServlet(), "/");
		server.setHandler(handler);
		server.start();
		server.join();
	}

	/**
	 * Gets the query value from the user and returns us the value to use for the query handler
	 *
	 * @param queryString the complete queryString from Http request
	 * @param key the key we are looking for (q)
	 * @return returns the String or null if not found
	 * @CITE got help from a peer for this function and understanding why and what it was doing
	 */
	public static String getQueryValue(String queryString, String key) {
		String[] pairs = queryString.split("&");
		for(String pair : pairs) {
			String[] keyValuePair = pair.split("=");
			if(key.equals(keyValuePair[0])) {
				return keyValuePair[1].replaceAll("\\+", " ");
			}
		}
		return null;
	}

	/**
	 * Formatting html method for the Servlet
	 *
	 * @param results results gotten from QueryBuilder
	 * @return returns the html
	 */
	public static String formatResult(List<Result> results) {
		StringBuilder html = new StringBuilder();
		for(Result result : results) {
			html.append("<a href=\"")
			.append(result.getLocation())
			.append("\"><h1>")
			.append(result.getLocation())
			.append("</h1><span>Score: ")
			.append(String.format("%.8f", result.getScore()))
			.append("</span><span>\tCount: ")
			.append(result.getCount())
			.append("</span></a>");
		}
		return html.toString();
	}

	/**
	 * Outputs and responds to HTML form.
	 * @CITE got help from same peer for the query string and some of the html formatting
	 */
	public class SearchEngineServlet extends HttpServlet {
		/**
		 * eclipse generated serialID
		 */
		private static final long serialVersionUID = 1L;
		/** The title to use for this webpage. */
		private static final String TITLE = "Search";

		/** Creates a new instance of this class. */
		public SearchEngineServlet() {}

		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {

			String query = null;
			List<Result> result = null;

			if(request.getQueryString() != null) {
				query = getQueryValue(request.getQueryString(), "q");
				result = queries.search(query);
			}

			String html = """
					<!DOCTYPE html>
					<html lang="en">

					<head>
					  <meta charset="utf-8">
					  <title>%1$s</title>
					</head>

					<body>
					<h1>%1$s</h1>

					<form method="get" action="/">
					  <p>
					    <input type="text" name="q" size="50"></input>
					  </p>

					  <p>
					    <button>Search</button>
					  </p>
					</form>
					<pre>%2$s</pre>
					</body>
					</html>
					""";

			PrintWriter out = response.getWriter();
			out.printf(html, TITLE, formatResult(result));

			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
		}
	}
}