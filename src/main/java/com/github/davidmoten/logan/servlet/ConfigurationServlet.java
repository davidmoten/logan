package com.github.davidmoten.logan.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.davidmoten.logan.Data;
import com.github.davidmoten.logan.LogEntry;
import com.github.davidmoten.logan.LogFile;
import com.github.davidmoten.logan.LogFile.SampleResult;
import com.github.davidmoten.logan.config.Configuration;
import com.github.davidmoten.logan.config.Marshaller;
import com.github.davidmoten.logan.util.PropertyReplacer;
import com.github.davidmoten.logan.watcher.FileTailerSampling;
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
		if (req.getParameter("reload") != null) {
			reload(req, resp);
		} else
			sample(req, resp);
	}

	private void sample(HttpServletRequest req, HttpServletResponse resp) {
		Marshaller m = new Marshaller();
		String xml = req.getParameter("configuration");
		Configuration configuration = m.unmarshal(PropertyReplacer
				.replaceSystemProperties(new ByteArrayInputStream(xml
						.getBytes())));
		Data data = new Data(configuration.maxSize, true);
		FileTailerSampling sampler = FileTailerSampling.Singleton.INSTANCE
				.instance();
		Watcher watcher = new Watcher(data, configuration, sampler);
		watcher.start();
		resp.setContentType("text/html");
		PrintWriter out;
		try {
			out = resp.getWriter();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		out.println("<html>");
		for (Entry<LogFile, SampleResult> en : sampler.getSamples().entrySet()) {
			out.println("<p><b>File: " + en.getKey().getFile() + "</b></p>");
			for (Entry<LogEntry, List<String>> info : en.getValue()
					.getEntries().entrySet()) {
				String colour;
				if (info.getValue().size() > 1)
					colour = "red";
				else
					colour = "green";
				out.print("<pre style=\"color:" + colour + ";\">");
				for (String line : info.getValue()) {
					out.println(line);
				}
				out.println("</pre>");
				out.println("<p style=\"margin-left:50px;\">" + info.getKey()
						+ "</p>");
			}
		}
		out.println("</html>");
	}

	private void reload(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
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
