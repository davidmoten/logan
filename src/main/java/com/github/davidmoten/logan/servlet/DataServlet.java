package com.github.davidmoten.logan.servlet;

import static com.github.davidmoten.logan.servlet.ServletUtil.getMandatoryDouble;
import static com.github.davidmoten.logan.servlet.ServletUtil.getMandatoryLong;
import static com.github.davidmoten.logan.servlet.ServletUtil.getMandatoryParameter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.davidmoten.logan.BucketQuery;
import com.github.davidmoten.logan.Buckets;
import com.github.davidmoten.logan.Data;
import com.github.davidmoten.logan.Metric;
import com.github.davidmoten.logan.Util;
import com.google.common.base.Optional;

//@WebServlet(urlPatterns = { "/data" })
public class DataServlet extends HttpServlet {

	private static final String WILDCARD = "*";

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
		if (WILDCARD.equals(text)
				|| (text != null && text.trim().length() == 0))
			text = null;
		String source = req.getParameter("source");
		if (WILDCARD.equals(source)
				|| (source != null && source.trim().length() == 0))
			source = null;
		String scanString = req.getParameter("scan");
		final Integer scan;
		if (scanString == null || scanString.equals(""))
			scan = null;
		else
			scan = Integer.parseInt(scanString);

		Metric metric = Metric.valueOf(getMandatoryParameter(req, "metric"));
		resp.setContentType("application/json");

		writeJson(State.instance().getData(), field, source, text, scan,
				startTime, interval, numBuckets, metric, resp.getWriter(),
				State.instance().getConfiguration().scanDelimiterPattern);
	}

	private static void writeJson(Data data, String field, String source,
			String text, Integer scan, long startTime, double interval,
			long numBuckets, Metric metric, PrintWriter writer,
			String scanDelimiterPattern) {
		BucketQuery q = new BucketQuery(startTime, interval, numBuckets,
				Optional.fromNullable(field), Optional.fromNullable(source),
				Optional.fromNullable(text), Optional.fromNullable(scan),
				Optional.<String> fromNullable(scanDelimiterPattern));
		Buckets buckets = data.execute(q);
		log.info("building json");
		Util.writeJson(buckets, metric, writer);
		log.info("built json");
	}
}
