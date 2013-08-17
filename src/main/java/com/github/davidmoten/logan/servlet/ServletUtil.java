package com.github.davidmoten.logan.servlet;

import javax.servlet.http.HttpServletRequest;

public class ServletUtil {

	public static double getMandatoryDouble(HttpServletRequest req, String name) {
		if (req.getParameter(name) == null)
			throw new RuntimeException("parameter " + name + " is mandatory");
		else
			try {
				return Double.parseDouble(req.getParameter(name));
			} catch (NumberFormatException e) {
				throw new RuntimeException("parameter " + name
						+ " parsing problem", e);
			}
	}

	public static long getMandatoryLong(HttpServletRequest req, String name) {
		if (req.getParameter(name) == null)
			throw new RuntimeException("parameter '" + name + "' is mandatory");
		else
			try {
				return Long.parseLong(req.getParameter(name));
			} catch (NumberFormatException e) {
				throw new RuntimeException("parameter '" + name
						+ "' could not be parsed as a Long: "
						+ req.getParameter(name), e);
			}
	}

	public static String getMandatoryParameter(HttpServletRequest req,
			String name) {
		if (req.getParameter(name) != null)
			return req.getParameter(name);
		else
			throw new RuntimeException("parameter " + name + " is mandatory");
	}

	public static String getParameter(HttpServletRequest req, String name,
			String defaultValue) {
		if (req.getParameter(name) != null)
			return req.getParameter(name);
		else
			return defaultValue;
	}

	public static long getLong(HttpServletRequest req, String name,
			long defaultValue) {
		String s = req.getParameter(name);
		if (s == null)
			return defaultValue;
		else
			return Long.parseLong(s);
	}
}
