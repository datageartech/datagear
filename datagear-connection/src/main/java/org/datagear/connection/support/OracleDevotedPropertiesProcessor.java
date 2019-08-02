/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
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
	public void process(Driver driver, Properties properties)
	{
		properties.put("remarksReporting", "true");
	}
}
