/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
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
	public void process(Driver driver, Properties properties)
	{
		// 元信息返回注释
		properties.put("useInformationSchema", "true");
	}
}
