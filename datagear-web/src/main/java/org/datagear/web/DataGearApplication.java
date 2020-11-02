/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web;

import org.datagear.web.config.WebMvcConfigurerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

/**
 * 应用入口。
 * 
 * @author datagear@163.com
 *
 */
@SpringBootApplication(scanBasePackageClasses = WebMvcConfigurerConfig.class, exclude = ErrorMvcAutoConfiguration.class)
public class DataGearApplication
{
	public static void main(String[] args)
	{
		SpringApplication.run(DataGearApplication.class, args);
	}
}
