package com.github.davidmoten.logan.servlet;

import static com.github.davidmoten.logan.servlet.ServletUtil.getMandatoryDouble;
import static com.github.davidmoten.logan.servlet.ServletUtil.getMandatoryLong;
import static com.github.davidmoten.logan.servlet.ServletUtil.getMandatoryParameter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.davidmoten.logan.BucketQuery;
import com.github.davidmoten.logan.Buckets;
import com.github.davidmoten.logan.Data;
import com.github.davidmoten.logan.Metric;
import com.github.davidmoten.logan.Util;
import com.google.common.base.Optional;

@WebServlet(urlPatterns = { "/data" })
public class DataServlet extends HttpServlet {

	private static Logger log = Logger.getLogger(DataServlet.class.getName());

	private static final long serialVersionUID = 1044384045444686984L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		long startTime = getMandatoryLong(req, "start");
		double interval = getMandatoryDouble(req, "interval");
		long numBuckets = getMandatoryLong(req, "buckets");
		String field = req.getParameter("field");
		String text = req.getParameter("text");
		String source = req.getParameter("source");
		if ("*".equals(source))
			source = null;

		Metric metric = Metric.valueOf(getMandatoryParameter(req, "metric"));
		resp.setContentType("application/json");
		writeJson(State.instance().getData(), field, source, text, startTime,
				interval, numBuckets, metric, resp.getWriter());
	}

	private static void writeJson(Data data, String field, String source,
			String text, long startTime, double interval, long numBuckets,
			Metric metric, PrintWriter writer) {
		BucketQuery q = new BucketQuery(new Date(startTime), interval,
				numBuckets, Optional.fromNullable(field),
				Optional.fromNullable(source), Optional.fromNullable(text));
		Buckets buckets = data.execute(q);
		log.info("building json");
		Util.writeJson(buckets, metric, writer);
		log.info("built json");
	}
}
