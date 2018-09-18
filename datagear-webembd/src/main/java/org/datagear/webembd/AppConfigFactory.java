/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.webembd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * {@linkplain AppConfig}工厂类。
 * 
 * @author datagear@163.com
 *
 */
public class AppConfigFactory
{
	public static final String CONFIG_FILE_PATH = "config/datagear.properties";

	public static final String CONFIG_FILE_ENCODING = "UTF-8";

	public static final String CONFIG_PROPERTY_SERVER_PORT = "server.port";

	public static final String CONFIG_PROPERTY_WEBAPP_LOCATION = "webapp.location";

	public AppConfigFactory()
	{
		super();
	}

	/**
	 * 获取{@linkplain AppConfig}。
	 * 
	 * @return
	 * @throws AppConfigFactoryException
	 */
	public AppConfig get() throws AppConfigFactoryException
	{
		Properties properties = loadAppConfigProperties(CONFIG_FILE_PATH, CONFIG_FILE_ENCODING);

		return readAppConfig(properties);
	}

	/**
	 * 读取{@linkplain AppConfig}。
	 * 
	 * @param properties
	 * @return
	 * @throws AppConfigFactoryException
	 */
	protected AppConfig readAppConfig(Properties properties) throws AppConfigFactoryException
	{
		String portStr = properties.getProperty(CONFIG_PROPERTY_SERVER_PORT);
		String location = properties.getProperty(CONFIG_PROPERTY_WEBAPP_LOCATION);

		if (portStr == null || portStr.isEmpty())
			throw new AppConfigFactoryException("configuration property [" + CONFIG_PROPERTY_SERVER_PORT
					+ "] not found in file [" + CONFIG_FILE_PATH + "]");

		if (location == null || location.isEmpty())
			throw new AppConfigFactoryException("configuration property [" + CONFIG_PROPERTY_WEBAPP_LOCATION
					+ "] not found in file [" + CONFIG_FILE_PATH + "]");

		int port = 0;

		try
		{
			port = Integer.parseInt(portStr);
		}
		catch (NumberFormatException e)
		{
			throw new AppConfigFactoryException("configuration property [" + CONFIG_PROPERTY_WEBAPP_LOCATION
					+ "] value [" + portStr + "] is not legal number");
		}

		return new AppConfig(port, location);
	}

	/**
	 * 加载配置{@linkplain Properties}。
	 * 
	 * @param configFilePath
	 * @param configFileEncoding
	 * @return
	 * @throws AppConfigFactoryException
	 */
	protected Properties loadAppConfigProperties(String configFilePath, String configFileEncoding)
			throws AppConfigFactoryException
	{
		File file = new File(configFilePath);

		if (!file.exists())
			throw new AppConfigFactoryException("configuration file [" + configFilePath + "] not found");

		Reader in = null;

		try
		{
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file), configFileEncoding));
		}
		catch (FileNotFoundException e)
		{
			throw new AppConfigFactoryException("configuration file [" + configFilePath + "] not found");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new AppConfigFactoryException(e);
		}

		Properties properties = new Properties();

		try
		{
			properties.load(in);
		}
		catch (IOException e)
		{
			throw new AppConfigFactoryException(e);
		}
		finally
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (Throwable t)
				{
				}
			}
		}

		return properties;
	}
}
