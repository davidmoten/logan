package com.github.davidmoten.logan.servlet;

import java.io.IOException;
import java.io.InputStream;
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

@WebServlet(urlPatterns = { "/keys" })
public class KeysServlet extends HttpServlet {

	private static final long serialVersionUID = 1044384045444686984L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (Configuration.isRemote()) {
			doRemote(req, resp);
		} else {
			doLocal(resp);
		}
	}

	private void doLocal(HttpServletResponse resp) throws IOException {
		StringBuilder s = new StringBuilder();
		for (String key : Data.instance().getKeys()) {
			if (s.length() > 0)
				s.append(",");
			s.append("\"");
			s.append(key);
			s.append("\"");
		}
		resp.setContentType("application/json");
		resp.getWriter().print("{ \"keys\": [" + s.toString() + "] }");
	}

	private void doRemote(HttpServletRequest req, HttpServletResponse resp)
			throws MalformedURLException, IOException {
		String url = Configuration.getLogServerBaseUrl() + "/keys?table="
				+ req.getParameter("table");

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
