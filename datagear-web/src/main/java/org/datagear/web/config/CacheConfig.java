/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 缓存配置。
 * 
 * @author datagear@163.com
 *
 */
@Configuration
public class CacheConfig
{
	private Environment environment;

	@Autowired
	public CacheConfig(Environment environment)
	{
		super();
		this.environment = environment;
	}

	public Environment getEnvironment()
	{
		return environment;
	}

	public void setEnvironment(Environment environment)
	{
		this.environment = environment;
	}

	@Bean
	public CacheManager cacheManager()
	{
		CaffeineCacheManager bean = new CaffeineCacheManager();

		return bean;
	}

	public Cache getCache(String name)
	{
		return this.cacheManager().getCache(name);
	}

	public Cache getEntityCacheBySimpleName(Class<?> clazz)
	{
		return getCache(clazz.getSimpleName());
	}

	public Cache getPermissionCacheBySimpleName(Class<?> clazz)
	{
		return getCache(clazz.getSimpleName() + "Permission");
	}
}
