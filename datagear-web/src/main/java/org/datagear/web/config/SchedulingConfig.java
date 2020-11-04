/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.web.config;

import org.datagear.web.scheduling.DeleteExpiredFileJob;
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
	public DeleteExpiredFileJob deleteTempFileJob()
	{
		int expiredMinutes = this.environment.getProperty("clearTempDirectory.expiredMinutes", Integer.class);

		DeleteExpiredFileJob bean = new DeleteExpiredFileJob(this.coreConfig.tempDirectory(), expiredMinutes);
		return bean;
	}

	/**
	 * 定时清理系统的临时目录。
	 */
	@Scheduled(cron = "${clearTempDirectory.interval}")
	public void deleteTempFile()
	{
		this.deleteTempFileJob().delete();
	}
}
