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

package org.datagear.web.config.support;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.datagear.util.IOUtil;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * 支持设置编码的{@linkplain PropertySourceLoader}。
 * <p>
 * 默认的{@linkplain PropertySourceLoader}实现类{@linkplain PropertiesPropertySourceLoader}仅支持读取{@code ISO-8859-1}的{@code *.properties}配置文件，
 * 会出现中文乱码问题。
 * </p>
 * <p>
 * 此类可以解决上述问题。
 * </p>
 * <p>
 * 注意：要使此类起作用，需要{@code src/main/resources/META-INF/spring.factories}配置。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class EncodingPropertySourceLoader extends PropertiesPropertySourceLoader
{
	private String encoding = IOUtil.CHARSET_UTF_8;

	public EncodingPropertySourceLoader()
	{
		super();
	}

	public String getEncoding()
	{
		return encoding;
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	@Override
	public List<PropertySource<?>> load(String name, Resource resource) throws IOException
	{
		Properties properties = loadProperties(resource);

		if (properties.isEmpty())
		{
			return Collections.emptyList();
		}
		else
		{
			return Collections.singletonList(
					new OriginTrackedMapPropertySource(name, Collections.unmodifiableMap(properties), true));
		}
	}

	protected Properties loadProperties(Resource resource) throws IOException
	{
		EncodedResource er = new EncodedResource(resource, this.encoding);
		return PropertiesLoaderUtils.loadProperties(er);
	}
}
