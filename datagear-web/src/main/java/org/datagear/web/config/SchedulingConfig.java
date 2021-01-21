/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.config;

import org.datagear.web.util.DirectoryCleaner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 计划任务配置。
 * 
 * @author datagear@163.com
 *
 */
@Configuration
@EnableScheduling
public class SchedulingConfig
{
	private CoreConfig coreConfig;

	private Environment environment;

	@Autowired
	public SchedulingConfig(CoreConfig coreConfig, Environment environment)
	{
		this.coreConfig = coreConfig;
		this.environment = environment;
	}

	public CoreConfig getCoreConfig()
	{
		return coreConfig;
	}

	public void setCoreConfig(CoreConfig coreConfig)
	{
		this.coreConfig = coreConfig;
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
	public DirectoryCleaner tempDirectoryCleaner()
	{
		int expiredMinutes = this.environment.getProperty("cleanTempDirectory.expiredMinutes", Integer.class);

		DirectoryCleaner bean = new DirectoryCleaner(this.coreConfig.tempDirectory(), expiredMinutes);
		return bean;
	}

	@Scheduled(cron = "${cleanTempDirectory.interval}")
	public void cleanTempDirectory()
	{
		this.tempDirectoryCleaner().clean();
	}
}
