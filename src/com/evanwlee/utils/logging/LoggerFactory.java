package com.evanwlee.utils.logging;

import com.evanwlee.utils.properties.PropertyLoader;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.SimpleLayout;


public class LoggerFactory {
	public static Logger getLogger(
		String uniqueLoggerName,
		String outFile,
		String pattern,
		Level level) {
		Logger logger = Logger.getLogger(uniqueLoggerName);

		org.apache.log4j.Appender appender = null;
		org.apache.log4j.Layout layout = null;

		if ("".equals(pattern)) {
			layout = new SimpleLayout();
		} else {
			layout = new PatternLayout(pattern);
		}

		if ("".equals(outFile)) {
			try {
				appender = new FileAppender(layout, outFile, false);
			} catch (Exception e) {
			}
		} else {
			appender = new ConsoleAppender(layout);
		}

		logger.addAppender(appender);
		logger.setLevel(level);

		return logger;
	}

	public static Logger getLogger(String name, String config) {
		Logger logger = null;
		if ("".equals(name)) {
			logger = Logger.getRootLogger();
		} else {
			logger = Logger.getLogger(name);
		}
		PropertyConfigurator.configure(PropertyLoader.loadProperties(config));

		// PropertyConfigurator.configureAndWatch(config);
		return logger;
	}

	public static Logger getLogger(String name) {
		Logger logger = null;
		if ("".equals(name)) {
			logger = Logger.getRootLogger();
		} else {
			logger = Logger.getLogger(name);
		}

		//PropertyConfigurator.configure(PropertyLoader.getLog4JConfigProperty());
		PropertyConfigurator.configure(PropertyLoader.loadProperties("resources.log4j.properties"));
		return logger;
	}
}
