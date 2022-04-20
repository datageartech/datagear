/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.connection.support;

import java.sql.Driver;
import java.util.Properties;

import org.datagear.connection.AbstractDriverClassNameDevotedPropertiesProcessor;
import org.datagear.connection.DevotedPropertiesProcessor;

/**
 * MySQL {@linkplain DevotedPropertiesProcessor}。
 * 
 * @author datagear@163.com
 *
 */
public class MySqlDevotedPropertiesProcessor extends AbstractDriverClassNameDevotedPropertiesProcessor
{
	public MySqlDevotedPropertiesProcessor()
	{
		super("mysql", true);
	}

	@Override
	public void process(Driver driver, String url, Properties properties)
	{
		// 元信息返回注释
		properties.setProperty("useInformationSchema", "true");

		// 默认连接超时30秒
		if (!isPropertyPresent(url, properties, "connectTimeout"))
			properties.setProperty("connectTimeout", "30000");
	}
}
