package com.github.davidmoten.logan.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlElement;

import com.github.davidmoten.logan.util.PropertyReplacer;
import com.github.davidmoten.logan.watcher.Main;
import com.google.common.collect.Lists;

/**
 * Configuration for log watching.
 * 
 */
public class Configuration {

    private static final String DEFAULT_CONFIGURATION_LOCATION = "/logan-configuration.xml";

    private static Logger log = Logger.getLogger(Configuration.class.getName());

    /**
     * Maximum number of log entries to keep in memory. Oldest are discarded
     * first once size reaches maxSize.
     */
    @XmlElement(required = false, defaultValue = "1000000")
    public int maxSize;

    @XmlElement(required = false)
    public String scanDelimiterPattern;
    /**
     * Default parser for log lines for all log files where the parser is not
     * specified.
     */
    @XmlElement(required = false)
    public Parser parser;
    /**
     * Groups of Parser and Log items.
     */
    @XmlElement(required = true)
    public List<Group> group = Lists.newArrayList();

    /**
     * Constructor.
     * 
     * @param parser
     *            parser
     * @param group
     *            group
     */
    public Configuration(Parser parser, List<Group> group) {
        super();
        this.parser = parser;
        this.group = group;
    }

    /**
     * Constructor.
     */
    public Configuration() {
        // no-args constructor required by jaxb
    }

    /**
     * <p>
     * Returns {@link Configuration} based on the value of the system property
     * <code>logan.config</code>.
     * </p>
     * 
     * <p>
     * If <code>logan.config</code> property not set then assumed to be
     * <code>/logan-configuration.xml</code>.
     * </p>
     * 
     * <p>
     * Configuration is loaded from the file pointed to by
     * <code>logan.config</code>. The classpath is checked first then the
     * filesystem. If the file is not found in neither path then a
     * {@link RuntimeException} is thrown.
     * </p>
     * 
     * <p>
     * Any properties specified in the configuration.xml file in the format
     * <code>${property.name}</code> will be replaced with system properties if
     * set.
     * </p>
     * 
     * @return loaded configuration
     */

    public static synchronized Configuration getConfiguration() {
        String configLocation = System.getProperty("logan.config", DEFAULT_CONFIGURATION_LOCATION);
        log.info("config=" + configLocation);
        InputStream is = Main.class.getResourceAsStream(configLocation);
        if (is == null) {
            File file = new File(configLocation);
            if (file.exists())
                try {
                    is = new FileInputStream(configLocation);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            else
                throw new RuntimeException(
                        "configuration xml not found. Set property logan.config to a file on classpath or filesystem.");
        }
        InputStream is2 = PropertyReplacer.replaceSystemProperties(is);
        Configuration configuration = new Marshaller().unmarshal(is2);
        return configuration;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Configuration [");
        builder.append("parser=");
        builder.append(parser);
        builder.append("maxSize=");
        builder.append(maxSize);
        builder.append(", group=");
        builder.append(group);
        builder.append("]");
        return builder.toString();
    }

}
