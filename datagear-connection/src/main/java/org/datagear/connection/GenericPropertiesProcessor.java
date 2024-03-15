/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
	public void process(Driver driver, String url, Properties properties)
	{
		DevotedPropertiesProcessor processor = getDevotedPropertiesProcessor(driver, url, properties);

		if (processor == null)
			return;

		processor.process(driver, url, properties);
	}

	protected DevotedPropertiesProcessor getDevotedPropertiesProcessor(Driver driver, String url, Properties properties)
	{
		if (this.devotedPropertiesProcessors == null)
			return null;

		for (DevotedPropertiesProcessor processor : this.devotedPropertiesProcessors)
		{
			if (processor.supports(driver, url, properties))
				return processor;
		}

		return null;
	}
}
