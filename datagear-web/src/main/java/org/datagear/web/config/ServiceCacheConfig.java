/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.config;

import org.datagear.management.service.impl.ServiceCache;
import org.datagear.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Configuration;

/**
 * 服务缓存配置。
 * 
 * @author datagear@163.com
 *
 */
@Configuration
public class ServiceCacheConfig
{
	private ApplicationProperties applicationProperties;

	private CacheManager serviceCacheManager;

	@Autowired
	public ServiceCacheConfig(ApplicationProperties applicationProperties)
	{
		super();
		this.applicationProperties = applicationProperties;

		if (!this.applicationProperties.isServiceCacheDisabled())
			this.serviceCacheManager = createServiceCacheManager();
	}

	public ServiceCache getServiceCache(Class<?> serviceClass)
	{
		return getServiceCache(serviceClass.getSimpleName());
	}

	public ServiceCache getPermissionServiceCache(Class<?> serviceClass)
	{
		return getServiceCache(serviceClass.getSimpleName() + "Permission");
	}

	protected ServiceCache getServiceCache(String name)
	{
		ServiceCache serviceCache = new ServiceCache();
		
		serviceCache
				.setDisabled(this.applicationProperties.isServiceCacheDisabled() || this.serviceCacheManager == null);
		serviceCache.setSerialized(false);
		serviceCache.setShared(false);
		
		if(this.serviceCacheManager != null)
			serviceCache.setCache(this.serviceCacheManager.getCache(name));

		return serviceCache;
	}

	protected CacheManager createServiceCacheManager()
	{
		CaffeineCacheManager bean = new CaffeineCacheManager();

		if (!StringUtil.isEmpty(this.applicationProperties.getServiceCacheSpec()))
			bean.setCacheSpecification(this.applicationProperties.getServiceCacheSpec());

		return bean;
	}
}
