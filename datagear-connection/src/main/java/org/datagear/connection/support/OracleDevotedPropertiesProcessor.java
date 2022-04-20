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
 * Oracle {@linkplain DevotedPropertiesProcessor}。
 * 
 * @author datagear@163.com
 *
 */
public class OracleDevotedPropertiesProcessor extends AbstractDriverClassNameDevotedPropertiesProcessor
{
	public OracleDevotedPropertiesProcessor()
	{
		super("oracle", true);
	}

	@Override
	public void process(Driver driver, String url, Properties properties)
	{
		// 元信息返回注释
		properties.setProperty("remarksReporting", "true");
	}
}
