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
public class DGSchedulingConfiguration
{
	private DGCoreConfiguration dGCoreConfiguration;

	@Autowired
	public DGSchedulingConfiguration(DGCoreConfiguration dGCoreConfiguration)
	{
		this.dGCoreConfiguration = dGCoreConfiguration;
	}

	public DGCoreConfiguration getCoreConfiguration()
	{
		return dGCoreConfiguration;
	}

	public void setCoreConfiguration(DGCoreConfiguration dGCoreConfiguration)
	{
		this.dGCoreConfiguration = dGCoreConfiguration;
	}

	@Bean
	public DeleteExpiredFileJob deleteTempFileJob()
	{
		DeleteExpiredFileJob bean = new DeleteExpiredFileJob(this.dGCoreConfiguration.tempDirectory(), 1440);
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
