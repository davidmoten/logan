package org.moten.david.log.ui;

public class Configuration {

	private static final String LOG_SERVER_BASE_URL_DEFAULT = "http://localhost:9191";

	private static final String logServerBaseUrl = System.getProperty(
			"log.server.url", LOG_SERVER_BASE_URL_DEFAULT);

	public static String getLogServerBaseUrl() {
		return logServerBaseUrl;
	}

}
