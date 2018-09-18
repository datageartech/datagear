/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.webembd;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Log4jConfigurer;

/**
 * 主程序。
 * 
 * @author datagear@163.com
 *
 */
public class App
{
	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

	private static final String LOG4J_CONFIG_LOCATION = "classpath:log4j.properties";

	private static final String LOG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static final String APPLICATION_NAME = "application";

	private static final String INFO_WRAP_LINE = "-----------------------------------------";

	static
	{
		try
		{
			Log4jConfigurer.initLogging(LOG4J_CONFIG_LOCATION);
		}
		catch (FileNotFoundException e)
		{
		}
	}

	public static void main(String[] args) throws Exception
	{
		System.out.println(INFO_WRAP_LINE);
		System.out.println(
				"[" + new SimpleDateFormat(LOG_DATE_FORMAT).format(new Date()) + "] starting " + APPLICATION_NAME);
		System.out.println(INFO_WRAP_LINE);

		LOGGER.info(INFO_WRAP_LINE);
		LOGGER.info("starting " + APPLICATION_NAME);
		LOGGER.info(INFO_WRAP_LINE);

		ApplicationContext serverApplicationContext = buildServerApplicationContext();
		AppConfig appConfig = serverApplicationContext.getBean(AppConfig.class);

		Server server = new Server(appConfig.getServerPort());

		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setContextPath("/");
		webAppContext.setWar(appConfig.getWebappLocation());

		server.setHandler(webAppContext);

		server.start();

		while (!server.isStarted())
		{
			Thread.sleep(500);
		}

		System.out.println(INFO_WRAP_LINE);
		System.out.println(
				"[" + new SimpleDateFormat(LOG_DATE_FORMAT).format(new Date()) + "] started " + APPLICATION_NAME);
		System.out.println(INFO_WRAP_LINE);

		LOGGER.info(INFO_WRAP_LINE);
		LOGGER.info("started " + APPLICATION_NAME);
		LOGGER.info(INFO_WRAP_LINE);

		try
		{
			server.join();
		}
		catch (InterruptedException e)
		{
			System.out.println(INFO_WRAP_LINE);
			System.out.println(
					"[" + new SimpleDateFormat(LOG_DATE_FORMAT).format(new Date()) + "] stopped " + APPLICATION_NAME);
			System.out.println(INFO_WRAP_LINE);

			LOGGER.info(INFO_WRAP_LINE);
			LOGGER.info("stopped " + APPLICATION_NAME);
			LOGGER.info(INFO_WRAP_LINE);
		}

		Log4jConfigurer.shutdownLogging();
	}

	protected static ApplicationContext buildServerApplicationContext()
	{
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				new String[] { "classpath:server-applicationContext.xml" });

		return applicationContext;
	}
}
