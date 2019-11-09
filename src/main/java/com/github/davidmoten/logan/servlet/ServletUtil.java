package com.github.davidmoten.logan.servlet;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import com.github.davidmoten.logan.Data;
import com.github.davidmoten.logan.DataMemory;
import com.github.davidmoten.logan.DataPersistedBPlusTree;
import com.github.davidmoten.logan.DataPersistedH2;
import com.github.davidmoten.logan.Util;
import com.github.davidmoten.logan.config.Configuration;

public class ServletUtil {

    private static final int JDBC_BATCH_SIZE = 100;

    public static double getMandatoryDouble(HttpServletRequest req, String name) {
        if (req.getParameter(name) == null)
            throw new RuntimeException("parameter " + name + " is mandatory");
        else
            try {
                return Double.parseDouble(req.getParameter(name));
            } catch (NumberFormatException e) {
                throw new RuntimeException("parameter " + name + " parsing problem", e);
            }
    }

    public static Data getData(Configuration configuration) {
        Data data;
        if ("h2".equalsIgnoreCase(System.getProperty("persist"))) {
            data = new DataPersistedH2(new File("target/maindb"), JDBC_BATCH_SIZE);
        } else if ("bplustree".equalsIgnoreCase(System.getProperty("persist"))) {
            data = new DataPersistedBPlusTree("target/bplustree");
        } else {
            data = new DataMemory(configuration.maxSize);
        }
        Util.addDummyData(data);
        return data;
    }

    public static long getMandatoryLong(HttpServletRequest req, String name) {
        if (req.getParameter(name) == null)
            throw new RuntimeException("parameter '" + name + "' is mandatory");
        else
            try {
                return Long.parseLong(req.getParameter(name));
            } catch (NumberFormatException e) {
                throw new RuntimeException("parameter '" + name
                        + "' could not be parsed as a Long: " + req.getParameter(name), e);
            }
    }

    public static String getMandatoryParameter(HttpServletRequest req, String name) {
        if (req.getParameter(name) != null)
            return req.getParameter(name);
        else
            throw new RuntimeException("parameter " + name + " is mandatory");
    }

    public static String getParameter(HttpServletRequest req, String name, String defaultValue) {
        if (req.getParameter(name) != null)
            return req.getParameter(name);
        else
            return defaultValue;
    }

    public static long getLong(HttpServletRequest req, String name, long defaultValue) {
        String s = req.getParameter(name);
        if (s == null)
            return defaultValue;
        else
            return Long.parseLong(s);
    }
}
