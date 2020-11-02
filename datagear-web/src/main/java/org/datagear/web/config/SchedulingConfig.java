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

	@Autowired
	public SchedulingConfig(CoreConfig coreConfig)
	{
		this.coreConfig = coreConfig;
	}

	public CoreConfig getCoreConfig()
	{
		return coreConfig;
	}

	public void setCoreConfig(CoreConfig coreConfig)
	{
		this.coreConfig = coreConfig;
	}

	@Bean
	public DeleteExpiredFileJob deleteTempFileJob()
	{
		DeleteExpiredFileJob bean = new DeleteExpiredFileJob(this.coreConfig.tempDirectory(), 1440);
		return bean;
	}

	/**
	 * 定时清理系统的临时目录。
	 */
	@Scheduled(cron = "0 0 1 * * ?")
	public void deleteTempFile()
	{
		this.deleteTempFileJob().delete();
	}
}
