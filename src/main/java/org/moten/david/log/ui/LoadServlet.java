package org.moten.david.log.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class LoadServlet extends HttpServlet {

	private static final long serialVersionUID = 8469220097421061495L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		boolean configure = "true".equals(req.getParameter("configure"));

		String url = Configuration.getLogServerBaseUrl() + "/load?n="
				+ req.getParameter("n") + "&configure=" + configure;

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
