package com.github.davidmoten.logan.servlet;

import static com.github.davidmoten.logan.servlet.ServletUtil.getMandatoryLong;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.davidmoten.logan.Data;

@WebServlet(urlPatterns = { "/log" })
public class LogServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		doLocal(req, resp);
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

}
