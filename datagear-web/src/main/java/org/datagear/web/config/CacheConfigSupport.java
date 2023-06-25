/*
 * Copyright 2018-2023 datagear.tech
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

package org.datagear.web.config;

import org.datagear.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.NoOpCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存配置。
 * <p>
 * 子类应该添加如下注解：
 * </p>
 * 
 * <pre>
 * {@code @Configuration}
 * {@code @EnableCaching}
 * </pre>
 * <p>
 * Spring会递归处理{@linkplain Configuration @Configuration}类的父类，可能会导致某些非预期的父类配置被加载，
 * 所以此类没有添加{@linkplain Configuration @Configuration}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class CacheConfigSupport
{
	private ApplicationPropertiesConfigSupport applicationPropertiesConfig;

	@Autowired
	public CacheConfigSupport(ApplicationPropertiesConfigSupport applicationPropertiesConfig)
	{
		super();
		this.applicationPropertiesConfig = applicationPropertiesConfig;
	}
	
	@Bean
	public CacheManager cacheManager()
	{
		CaffeineCacheManager bean = new CaffeineCacheManager();

		String cacheSpec = getApplicationProperties().getCacheSpec();

		if (!StringUtil.isEmpty(cacheSpec))
			bean.setCacheSpecification(cacheSpec);

		return bean;
	}
	
	/**
	 * 创建{@linkplain Cache}。
	 * 
	 * @param cacheNameClass
	 * @return
	 */
	public Cache createCache(Class<?> cacheNameClass)
	{
		return createCache(cacheNameClass.getName());
	}

	/**
	 * 创建{@linkplain Cache}。
	 * 
	 * @param name
	 * @return
	 */
	public Cache createCache(String name)
	{
		ApplicationProperties applicationProperties = getApplicationProperties();

		if (applicationProperties.isCacheDisabled())
			return new NoOpCache(name);

		return this.cacheManager().getCache(name);
	}

	/**
	 * 创建进程内{@linkplain Cache}。
	 * 
	 * @param cacheNameClass
	 * @return
	 */
	public Cache createLocalCache(Class<?> cacheNameClass)
	{
		return createLocalCache(cacheNameClass.getName());
	}

	/**
	 * 创建进程内{@linkplain Cache}。
	 * 
	 * @param name
	 * @return
	 */
	public Cache createLocalCache(String name)
	{
		return createCache(name);
	}

	protected ApplicationProperties getApplicationProperties()
	{
		return this.applicationPropertiesConfig.applicationProperties();
	}
}
