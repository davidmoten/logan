package com.github.davidmoten.logan.servlet;

import static com.github.davidmoten.logan.servlet.ServletUtil.getMandatoryLong;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.github.davidmoten.logan.Data;

@WebServlet(urlPatterns = { "/log" })
public class LogServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		if (Configuration.isRemote()) {
			doRemote(req, resp);
		} else {
			doLocal(req, resp);
		}
	}

	private void doLocal(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		long startTime = getMandatoryLong(req, "start");
		long finishTime = getMandatoryLong(req, "finish");
		resp.setContentType("text/plain");
		PrintWriter out = resp.getWriter();
		for (String line : Data.instance().getLogs(startTime, finishTime)) {
			if (line != null)
				out.println(line);
		}
	}

	private void doRemote(HttpServletRequest req, HttpServletResponse resp)
			throws MalformedURLException, IOException {
		String url = Configuration.getLogServerBaseUrl() + "/log?start="
				+ req.getParameter("start") + "&finish="
				+ req.getParameter("finish");

		url = url.replace(" ", "%20");

		URL u;
		try {
			u = new URI(url).toURL();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		InputStream is = u.openStream();
		String reply = IOUtils.toString(is);
		is.close();
		resp.setContentType("text/plain");
		resp.getWriter().print(reply);
	}
}
