/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 
 */
package org.datagear.web.util;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.datagear.analysis.support.JsonSupport;
import org.datagear.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库JDBC驱动信息。
 * 
 * @author datagear@163.com
 *
 */
public class DriverInfo implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(DriverInfo.class);

	public static final String COMMON_DRIVER_INFO_CLASS_PATH = "org/datagear/web/commonDriverInfos.json";

	public static final String DRIVER_CLASS_FILE_SUFFIX = ".class";

	private static final List<DriverInfo> COMMON_DRIVER_INFOS;
	static
	{
		COMMON_DRIVER_INFOS = parseCommonInDriverInfos();
	}

	/** 数据库名 */
	private String name;

	/** JDBC驱动类名 */
	private String[] driverClassNames;

	/** JDBC连接URL模板 */
	private UrlTemplate urlTemplate;

	public DriverInfo()
	{
		super();
	}

	public DriverInfo(String name, String[] driverClassNames, UrlTemplate urlTemplate)
	{
		super();
		this.name = name;
		this.driverClassNames = driverClassNames;
		this.urlTemplate = urlTemplate;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String[] getDriverClassNames()
	{
		return driverClassNames;
	}

	public void setDriverClassNames(String[] driverClassNames)
	{
		this.driverClassNames = driverClassNames;
	}

	public UrlTemplate getUrlTemplate()
	{
		return urlTemplate;
	}

	public void setUrlTemplate(UrlTemplate urlTemplate)
	{
		this.urlTemplate = urlTemplate;
	}

	/**
	 * 将驱动类名转换为文件路径。
	 * <p>
	 * 例如：{@code a.b.c.XxxDriver}至：{@code a/b/c/XxxDriver.class}。
	 * </p>
	 * 
	 * @param driverClassName
	 * @return
	 */
	public static String toDriverClassFilePath(String driverClassName)
	{
		return driverClassName.replace('.', '/') + DRIVER_CLASS_FILE_SUFFIX;
	}

	/**
	 * 将驱动文件路径转换为驱动类名。
	 * <p>
	 * 例如：{@code a/b/c/XxxDriver.class}至：{@code a.b.c.XxxDriver}。
	 * </p>
	 * 
	 * @param driverClassFilePath
	 * @return
	 */
	public static String toDriverClassName(String driverClassFilePath)
	{
		return driverClassFilePath.replace('/', '.').substring(0,
				driverClassFilePath.lastIndexOf(DRIVER_CLASS_FILE_SUFFIX));
	}

	/**
	 * 获取驱动类名集合。
	 * 
	 * @param driverInfos
	 * @return
	 */
	public static List<String> getDriverClassNames(List<DriverInfo> driverInfos)
	{
		List<String> list = new ArrayList<>();

		if (driverInfos == null)
			return list;

		for (DriverInfo driverInfo : driverInfos)
		{
			String[] names = driverInfo.getDriverClassNames();

			if (names == null)
				continue;

			for (String name : names)
				list.add(name);
		}

		return list;
	}

	/**
	 * 获取常用{@linkplain DriverInfo}的不可变列表。
	 * 
	 * @return
	 */
	public static List<DriverInfo> getCommonInDriverInfos()
	{
		return COMMON_DRIVER_INFOS;
	}

	/**
	 * 解析常用{@linkplain DriverInfo}。
	 * 
	 * @return
	 */
	protected static List<DriverInfo> parseCommonInDriverInfos()
	{
		List<DriverInfo> driverInfos = null;

		InputStream in = null;
		try
		{
			in = DriverInfo.class.getClassLoader().getResourceAsStream(COMMON_DRIVER_INFO_CLASS_PATH);
			String json = IOUtil.readString(in, IOUtil.CHARSET_UTF_8, false);

			DriverInfo[] driverInfoAry = JsonSupport.parseNonStardand(json, DriverInfo[].class);
			driverInfos = Arrays.asList(driverInfoAry);
		}
		catch (Exception e)
		{
			LOGGER.warn("pase builtin jdbc driver info error", e);
		}
		finally
		{
			IOUtil.close(in);
		}

		return (driverInfos == null ? Collections.emptyList() : Collections.unmodifiableList(driverInfos));
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", driverClassNames=" + Arrays.toString(driverClassNames)
				+ ", urlTemplate=" + urlTemplate + "]";
	}

	public static class UrlTemplate implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String template = "";

		private DefaultValue defaultValue;

		public UrlTemplate()
		{
			super();
		}

		public UrlTemplate(String template)
		{
			super();
			this.template = template;
		}

		public String getTemplate()
		{
			return template;
		}

		public void setTemplate(String template)
		{
			this.template = template;
		}

		public boolean hasDefaultValue()
		{
			return (this.defaultValue != null);
		}

		public DefaultValue getDefaultValue()
		{
			return defaultValue;
		}

		public void setDefaultValue(DefaultValue defaultValue)
		{
			this.defaultValue = defaultValue;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [template=" + template + ", defaultValue=" + defaultValue + "]";
		}
	}

	public static class DefaultValue implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String host = "";

		private String port = "";

		private String name = "";

		public DefaultValue()
		{
			super();
		}

		public DefaultValue(DefaultValue defaultValue)
		{
			super();
			this.host = defaultValue.host;
			this.port = defaultValue.port;
			this.name = defaultValue.name;
		}

		public String getHost()
		{
			return host;
		}

		public void setHost(String host)
		{
			this.host = host;
		}

		public String getPort()
		{
			return port;
		}

		public void setPort(String port)
		{
			this.port = port;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [host=" + host + ", port=" + port + ", name=" + name + "]";
		}
	}
}
