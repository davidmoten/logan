package com.github.davidmoten.logan.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.davidmoten.logan.Data;
import com.github.davidmoten.logan.config.Configuration;
import com.github.davidmoten.logan.config.Marshaller;
import com.github.davidmoten.logan.util.PropertyReplacer;
import com.github.davidmoten.logan.watcher.Watcher;

@WebServlet(urlPatterns = { "/configuration" })
public class ConfigurationServlet extends HttpServlet {

	private static final long serialVersionUID = -1409220672745244218L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Marshaller m = new Marshaller();
		resp.setContentType("application/xml");
		m.marshal(State.instance().getConfiguration(), resp.getOutputStream());
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Marshaller m = new Marshaller();
		String xml = req.getParameter("configuration");
		Configuration configuration = m.unmarshal(PropertyReplacer
				.replaceSystemProperties(new ByteArrayInputStream(xml
						.getBytes())));
		Data data = new Data(configuration.maxSize, true);
		State.instance().getWatcher().stop();

		Watcher watcher = new Watcher(data, configuration);
		watcher.start();

		State.setInstance(new State(data, configuration, watcher));
		resp.setContentType("text/plain");
		resp.getWriter().print("New configuration loaded, watchers started");
	}
}
