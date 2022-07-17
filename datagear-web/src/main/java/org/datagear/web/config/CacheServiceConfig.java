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
import org.springframework.context.annotation.Bean;
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
	private ApplicationPropertiesConfig applicationPropertiesConfig;

	@Autowired
	public CacheServiceConfig(ApplicationPropertiesConfig applicationPropertiesConfig)
	{
		super();
		this.applicationPropertiesConfig = applicationPropertiesConfig;
	}
	
	@Bean
	public CacheManager cacheServiceCacheManager()
	{
		CaffeineCacheManager bean = new CaffeineCacheManager();

		if (!StringUtil.isEmpty(getApplicationProperties().getCacheServiceSpec()))
			bean.setCacheSpecification(getApplicationProperties().getCacheServiceSpec());

		return bean;
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
		ApplicationProperties applicationProperties = getApplicationProperties();
		
		cacheService
				.setDisabled(applicationProperties.isCacheServiceDisabled());
		cacheService.setSerialized(false);
		cacheService.setShared(false);
		
		if (!applicationProperties.isCacheServiceDisabled())
			cacheService.setCache(this.cacheServiceCacheManager().getCache(name));

		return cacheService;
	}

	protected ApplicationProperties getApplicationProperties()
	{
		return this.applicationPropertiesConfig.applicationProperties();
	}
}
