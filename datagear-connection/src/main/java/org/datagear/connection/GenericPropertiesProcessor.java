/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.connection;

import java.sql.Driver;
import java.util.List;
import java.util.Properties;

/**
 * 通用{@linkplain PropertiesProcessor}。
 * 
 * @author datagear@163.com
 *
 */
public class GenericPropertiesProcessor implements PropertiesProcessor
{
	private List<DevotedPropertiesProcessor> devotedPropertiesProcessors;

	public GenericPropertiesProcessor()
	{
		super();
	}

	public List<DevotedPropertiesProcessor> getDevotedPropertiesProcessors()
	{
		return devotedPropertiesProcessors;
	}

	public void setDevotedPropertiesProcessors(List<DevotedPropertiesProcessor> devotedPropertiesProcessors)
	{
		this.devotedPropertiesProcessors = devotedPropertiesProcessors;
	}

	@Override
	public void process(Driver driver, Properties properties)
	{
		DevotedPropertiesProcessor processor = getDevotedPropertiesProcessor(driver, properties);

		if (processor == null)
			return;

		processor.process(driver, properties);
	}

	protected DevotedPropertiesProcessor getDevotedPropertiesProcessor(Driver driver, Properties properties)
	{
		if (this.devotedPropertiesProcessors == null)
			return null;

		for (DevotedPropertiesProcessor processor : this.devotedPropertiesProcessors)
		{
			if (processor.supports(driver, properties))
				return processor;
		}

		return null;
	}
}
