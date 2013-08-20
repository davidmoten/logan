package com.github.davidmoten.logan.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = { "/sources" })
public class SourcesServlet extends HttpServlet {

	private static final long serialVersionUID = 1044384045444686984L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doLocal(resp);
	}

	private void doLocal(HttpServletResponse resp) throws IOException {
		StringBuilder s = new StringBuilder();
		for (String key : State.instance().getData().getSources()) {
			if (s.length() > 0)
				s.append(",");
			s.append("\"");
			s.append(key);
			s.append("\"");
		}
		resp.setContentType("application/json");
		resp.getWriter().print("{ \"sources\": [" + s.toString() + "] }");
	}

}
