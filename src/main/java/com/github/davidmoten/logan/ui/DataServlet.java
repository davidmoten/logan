package com.github.davidmoten.logan.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

@WebServlet(urlPatterns = { "/query" })
public class DataServlet extends HttpServlet {

	private static final long serialVersionUID = 1044384045444686984L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String url = Configuration.getLogServerBaseUrl() + "/query?field="
				+ req.getParameter("field") + "&start="
				+ req.getParameter("start") + "&interval="
				+ req.getParameter("interval") + "&buckets="
				+ req.getParameter("buckets") + "&metric="
				+ req.getParameter("metric");
		if (req.getParameter("text") != null)
			url += "&text=" + req.getParameter("text");

		url = url.replace(" ", "%20");

		URL u;
		try {
			u = new URI(url).toURL();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		InputStream is = u.openStream();
		String json = IOUtils.toString(is);
		is.close();
		resp.setContentType("application/json");
		resp.getWriter().print(json);
	}
}
