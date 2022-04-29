/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.config;

import org.datagear.util.CacheService;
import org.datagear.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存服务配置。
 * 
 * @author datagear@163.com
 *
 */
@Configuration
public class CacheServiceConfig
{
	private ApplicationProperties applicationProperties;

	private CacheManager cacheManager;

	@Autowired
	public CacheServiceConfig(ApplicationProperties applicationProperties)
	{
		super();
		this.applicationProperties = applicationProperties;

		if (!this.applicationProperties.isCacheServiceDisabled())
			this.cacheManager = createCacheManager();
	}

	public CacheService createCacheService(Class<?> cacheNameClass)
	{
		return createCacheService(cacheNameClass.getName());
	}

	public CacheService createPermissionCacheService(Class<?> cacheNameClass)
	{
		return createCacheService(cacheNameClass.getName() + "Permission");
	}

	public CacheService createCacheService(String name)
	{
		CacheService cacheService = new CacheService();
		
		cacheService
				.setDisabled(this.applicationProperties.isCacheServiceDisabled() || this.cacheManager == null);
		cacheService.setSerialized(false);
		cacheService.setShared(false);
		
		if (this.cacheManager != null)
			cacheService.setCache(this.cacheManager.getCache(name));

		return cacheService;
	}

	protected CacheManager createCacheManager()
	{
		CaffeineCacheManager bean = new CaffeineCacheManager();

		if (!StringUtil.isEmpty(this.applicationProperties.getCacheServiceSpec()))
			bean.setCacheSpecification(this.applicationProperties.getCacheServiceSpec());

		return bean;
	}
}
