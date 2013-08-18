package com.github.davidmoten.logan.servlet;

import static com.github.davidmoten.logan.servlet.ServletUtil.getMandatoryDouble;
import static com.github.davidmoten.logan.servlet.ServletUtil.getMandatoryLong;
import static com.github.davidmoten.logan.servlet.ServletUtil.getMandatoryParameter;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.github.davidmoten.logan.BucketQuery;
import com.github.davidmoten.logan.Buckets;
import com.github.davidmoten.logan.Data;
import com.github.davidmoten.logan.Metric;
import com.github.davidmoten.logan.Util;

@WebServlet(urlPatterns = { "/data" })
public class DataServlet extends HttpServlet {

	private static Logger log = Logger.getLogger(DataServlet.class.getName());

	private static final long serialVersionUID = 1044384045444686984L;

	private final Data data = Data.instance();

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
		double interval = getMandatoryDouble(req, "interval");
		long numBuckets = getMandatoryLong(req, "buckets");
		String field = ServletUtil.getMandatoryParameter(req, "field");
		String source = req.getParameter("source");
		Metric metric = Metric.valueOf(getMandatoryParameter(req, "metric"));
		resp.setContentType("application/json");
		writeJson(data, field, source, startTime, interval, numBuckets, metric,
				resp.getWriter());
	}

	private void doRemote(HttpServletRequest req, HttpServletResponse resp)
			throws MalformedURLException, IOException {
		String url = Configuration.getLogServerBaseUrl() + "/data?field="
				+ req.getParameter("field") + "&start="
				+ req.getParameter("start") + "&interval="
				+ req.getParameter("interval") + "&buckets="
				+ req.getParameter("buckets") + "&metric="
				+ req.getParameter("metric");

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

	private static void writeJson(Data data, String field, String source,
			long startTime, double interval, long numBuckets, Metric metric,
			PrintWriter writer) {
		BucketQuery q = new BucketQuery(new Date(startTime), interval,
				numBuckets, field, source);
		Buckets buckets = data.execute(q);
		log.info("building json");
		Util.writeJson(buckets, metric, writer);
		log.info("built json");
	}

}
