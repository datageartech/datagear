/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.util;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletContext;

import org.datagear.connection.IOUtil;
import org.datagear.connection.XmlDriverEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextResource;

/**
 * {@linkplain XmlDriverEntityManager}初始化器。
 * <p>
 * 它先将相对于{@linkplain ServletContext}路径下指定的ZIP文件解压到{@linkplain XmlDriverEntityManager#getRootDirectory()}目录下，
 * 然后调用{@linkplain XmlDriverEntityManager#init()}方法。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class XmlDriverEntityManagerInitializer implements ServletContextAware
{
	private static final Logger LOGGER = LoggerFactory.getLogger(XmlDriverEntityManagerInitializer.class);

	public static final String DEFAULT_BUILT_IN_FILE_DRIVER_ENTITY_ZIP_PATH = "/WEB-INF/builtInDriverEntity.zip";

	private XmlDriverEntityManager xmlDriverEntityManager;

	private ServletContext servletContext;

	private String builtInFileDriverEntityZipPath = DEFAULT_BUILT_IN_FILE_DRIVER_ENTITY_ZIP_PATH;

	public XmlDriverEntityManagerInitializer()
	{
		super();
	}

	public XmlDriverEntityManagerInitializer(XmlDriverEntityManager xmlDriverEntityManager,
			ServletContext servletContext)
	{
		super();
		this.xmlDriverEntityManager = xmlDriverEntityManager;
		this.servletContext = servletContext;
	}

	public XmlDriverEntityManager getXmlDriverEntityManager()
	{
		return xmlDriverEntityManager;
	}

	public void setXmlDriverEntityManager(XmlDriverEntityManager xmlDriverEntityManager)
	{
		this.xmlDriverEntityManager = xmlDriverEntityManager;
	}

	public ServletContext getServletContext()
	{
		return servletContext;
	}

	@Override
	public void setServletContext(ServletContext servletContext)
	{
		this.servletContext = servletContext;
	}

	public String getBuiltInFileDriverEntityZipPath()
	{
		return builtInFileDriverEntityZipPath;
	}

	public void setBuiltInFileDriverEntityZipPath(String builtInFileDriverEntityZipPath)
	{
		this.builtInFileDriverEntityZipPath = builtInFileDriverEntityZipPath;
	}

	/**
	 * 初始化。
	 */
	public void init() throws IOException
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug(
					"start initializing " + XmlDriverEntityManager.class.getSimpleName() + " with built-in drivers");

		if (isDriverEntityInfoFileExists())
		{
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("built-in drivers initialization is skipped, it is been done before");
		}
		else
		{
			ServletContextResource resource = new ServletContextResource(this.servletContext,
					this.builtInFileDriverEntityZipPath);

			if (!resource.exists())
			{
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("built-in drivers initialization is skipped, resource ["
							+ this.builtInFileDriverEntityZipPath + "] not found");
			}
			else
			{
				ZipInputStream in = new ZipInputStream(resource.getInputStream());

				IOUtil.unzip(in, this.xmlDriverEntityManager.getRootDirectory());
			}
		}

		this.xmlDriverEntityManager.init();

		if (LOGGER.isDebugEnabled())
			LOGGER.debug(
					"finish initializing " + XmlDriverEntityManager.class.getSimpleName() + " with built-in drivers");
	}

	protected boolean isDriverEntityInfoFileExists()
	{
		File file = this.xmlDriverEntityManager.getDriverEntityInfoFile();

		return (file != null && file.exists());
	}
}
