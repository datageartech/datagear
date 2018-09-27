/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.webembd;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 主程序。
 * 
 * @author datagear@163.com
 *
 */
public class App
{
	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

	private static final String LOG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static final String INFO_WRAP_LINE = "-----------------------------------------";

	public static void main(String[] args) throws Throwable
	{
		System.out.println(INFO_WRAP_LINE);
		System.out.println("[" + new SimpleDateFormat(LOG_DATE_FORMAT).format(new Date()) + "] starting...");
		System.out.println(INFO_WRAP_LINE);

		LOGGER.info(INFO_WRAP_LINE);
		LOGGER.info("starting...");
		LOGGER.info(INFO_WRAP_LINE);

		Server server = null;

		try
		{
			AppConfigFactory appConfigFactory = new AppConfigFactory();
			AppConfig appConfig = appConfigFactory.get();

			server = new Server(appConfig.getServerPort());

			WebAppContext webAppContext = new WebAppContext();
			webAppContext.setContextPath("/");
			webAppContext.setWar(appConfig.getWebappLocation());

			server.setHandler(webAppContext);

			server.start();

			while (!server.isStarted())
			{
				Thread.sleep(500);
			}
		}
		catch (Throwable t)
		{
			LOGGER.error("starting failed", t);

			throw t;
		}

		System.out.println(INFO_WRAP_LINE);
		System.out.println("[" + new SimpleDateFormat(LOG_DATE_FORMAT).format(new Date()) + "] starting [OK]");
		System.out.println(INFO_WRAP_LINE);

		LOGGER.info(INFO_WRAP_LINE);
		LOGGER.info("starting [OK]");
		LOGGER.info(INFO_WRAP_LINE);

		try
		{
			server.join();
		}
		catch (InterruptedException e)
		{
			System.out.println(INFO_WRAP_LINE);
			System.out.println("[" + new SimpleDateFormat(LOG_DATE_FORMAT).format(new Date()) + "] stopping... ");
			System.out.println(INFO_WRAP_LINE);

			LOGGER.info(INFO_WRAP_LINE);
			LOGGER.info("stopping [OK]");
			LOGGER.info(INFO_WRAP_LINE);
		}
	}
}
