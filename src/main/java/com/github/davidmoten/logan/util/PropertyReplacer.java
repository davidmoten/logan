package com.github.davidmoten.logan.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

public class PropertyReplacer {

	private PropertyReplacer() {
		// prevent instantiation
	}

	public static InputStream replaceSystemProperties(InputStream is) {
		try {
			List<String> lines = IOUtils.readLines(is);
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			PrintWriter out = new PrintWriter(bytes);
			Pattern p = Pattern.compile("\\$\\{[^\\$]*\\}");
			boolean firstLine = true;
			for (String line : lines) {
				if (!firstLine)
					out.println();
				Matcher m = p.matcher(line);
				while (m.find()) {
					System.out.println("matched " + m.group());
					String name = m.group()
							.substring(2, m.group().length() - 1);
					System.out.println(name);
					String property = System.getProperty(name);
					if (property != null)
						line = line.replace(m.group(), property);
				}
				out.print(line);
				firstLine = false;
			}
			out.close();
			return new ByteArrayInputStream(bytes.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
