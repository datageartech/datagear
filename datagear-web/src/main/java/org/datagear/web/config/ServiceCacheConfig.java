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
import org.springframework.core.env.Environment;

/**
 * 服务缓存配置。
 * 
 * @author datagear@163.com
 *
 */
@Configuration
public class ServiceCacheConfig
{
	private Environment environment;

	private CacheManager serviceCacheManager;

	private final boolean disabled;

	private final String cacheSpec;

	@Autowired
	public ServiceCacheConfig(Environment environment)
	{
		super();
		this.environment = environment;
		this.disabled = this.environment.getProperty("service.cache.disabled", Boolean.class, false);
		this.cacheSpec = this.environment.getProperty("service.cache.spec", "");

		if (!disabled)
			this.serviceCacheManager = createServiceCacheManager();
	}

	public Environment getEnvironment()
	{
		return environment;
	}

	public void setEnvironment(Environment environment)
	{
		this.environment = environment;
	}

	public boolean isDisabled()
	{
		return disabled;
	}

	public String getCacheSpec()
	{
		return cacheSpec;
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
		
		serviceCache.setDisabled(isDisabled() || this.serviceCacheManager == null);
		serviceCache.setSerialized(false);
		serviceCache.setShared(false);
		
		if(this.serviceCacheManager != null)
			serviceCache.setCache(this.serviceCacheManager.getCache(name));

		return serviceCache;
	}

	protected CacheManager createServiceCacheManager()
	{
		CaffeineCacheManager bean = new CaffeineCacheManager();

		if (!StringUtil.isEmpty(this.cacheSpec))
			bean.setCacheSpecification(this.cacheSpec);

		return bean;
	}
}
