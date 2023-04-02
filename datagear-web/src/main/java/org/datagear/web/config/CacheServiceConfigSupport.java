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

import org.datagear.util.CacheService;
import org.datagear.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存服务配置。
 * <p>
 * 子类应该添加如下注解：
 * </p>
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
public class CacheServiceConfigSupport
{
	private ApplicationPropertiesConfigSupport applicationPropertiesConfig;

	@Autowired
	public CacheServiceConfigSupport(ApplicationPropertiesConfigSupport applicationPropertiesConfig)
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
		
		if (!applicationProperties.isCacheServiceDisabled())
			cacheService.setCache(this.cacheServiceCacheManager().getCache(name));

		return cacheService;
	}

	protected ApplicationProperties getApplicationProperties()
	{
		return this.applicationPropertiesConfig.applicationProperties();
	}
}
