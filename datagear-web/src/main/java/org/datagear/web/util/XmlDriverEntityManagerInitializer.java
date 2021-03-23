/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.datagear.connection.DriverEntity;
import org.datagear.connection.XmlDriverEntityManager;
import org.datagear.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@linkplain XmlDriverEntityManager}初始化器。
 * <p>
 * 它先将{@linkplain #BUILT_IN_DRIVER_ENTITY_ZIP_CLASS_PATH}
 * 的ZIP文件解压到{@linkplain XmlDriverEntityManager#getRootDirectory()}目录下，
 * 然后调用{@linkplain XmlDriverEntityManager#init()}方法。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class XmlDriverEntityManagerInitializer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(XmlDriverEntityManagerInitializer.class);

	public static final String BUILT_IN_DRIVER_ENTITY_ZIP_CLASS_PATH = "org/datagear/web/builtInDriverEntity.zip";

	private XmlDriverEntityManager xmlDriverEntityManager;

	public XmlDriverEntityManagerInitializer()
	{
		super();
	}

	public XmlDriverEntityManagerInitializer(XmlDriverEntityManager xmlDriverEntityManager)
	{
		super();
		this.xmlDriverEntityManager = xmlDriverEntityManager;
	}

	public XmlDriverEntityManager getXmlDriverEntityManager()
	{
		return xmlDriverEntityManager;
	}

	public void setXmlDriverEntityManager(XmlDriverEntityManager xmlDriverEntityManager)
	{
		this.xmlDriverEntityManager = xmlDriverEntityManager;
	}

	/**
	 * 初始化。
	 */
	public void init() throws IOException
	{
		if (LOGGER.isInfoEnabled())
			LOGGER.info("start init {}", XmlDriverEntityManager.class.getSimpleName());

		// 仅在首次初始化内置驱动，避免升级至使用新驱动的新版本时导致已有数据源功能不可用
		if (isDriverEntityInfoFileExists())
		{
			if (LOGGER.isInfoEnabled())
				LOGGER.info("init built-in driver is skipped, it will be done only if the directory is empty");
		}
		else
		{
			InputStream in = null;

			try
			{
				in = getClass().getClassLoader().getResourceAsStream(BUILT_IN_DRIVER_ENTITY_ZIP_CLASS_PATH);
				ZipInputStream zipIn = new ZipInputStream(in);

				IOUtil.unzip(zipIn, this.xmlDriverEntityManager.getRootDirectory());
			}
			finally
			{
				IOUtil.close(in);
			}

			if (LOGGER.isInfoEnabled())
				LOGGER.info("init built-in driver is done");
		}

		this.xmlDriverEntityManager.init();

		if (LOGGER.isInfoEnabled())
		{
			List<DriverEntity> driverEntities = this.xmlDriverEntityManager.getAll();

			for (DriverEntity driverEntity : driverEntities)
				LOGGER.info("init {}", driverEntity.toString());

			LOGGER.info("finish init {}", XmlDriverEntityManager.class.getSimpleName());
		}
	}

	protected boolean isDriverEntityInfoFileExists()
	{
		File file = this.xmlDriverEntityManager.getDriverEntityInfoFile();

		return (file != null && file.exists());
	}
}
