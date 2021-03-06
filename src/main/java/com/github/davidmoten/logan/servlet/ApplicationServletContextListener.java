package com.github.davidmoten.logan.servlet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.github.davidmoten.logan.Data;
import com.github.davidmoten.logan.config.Configuration;
import com.github.davidmoten.logan.watcher.Watcher;

@WebListener
public class ApplicationServletContextListener implements ServletContextListener {

    private static final Logger log = Logger
            .getLogger(ApplicationServletContextListener.class.getName());

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        try {
            State.instance().getData().close();
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage(), e);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        setupLogging();
        Configuration configuration = Configuration.getConfiguration();
        Data data = ServletUtil.getData(configuration);
        Watcher w = new Watcher(data, configuration);
        w.start();
        State.setInstance(new State(data, configuration, w));
        log.info("state set");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void setupLogging() {
        try {
            LogManager.getLogManager().readConfiguration(ApplicationServletContextListener.class
                    .getResourceAsStream("/my-logging.properties"));
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
