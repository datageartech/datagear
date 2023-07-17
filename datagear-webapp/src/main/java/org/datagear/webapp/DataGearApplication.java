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

package org.datagear.webapp;

import org.datagear.web.util.ApplicationBanner;
import org.datagear.webapp.config.WebMvcConfigurerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

/**
 * 应用入口。
 * 
 * @author datagear@163.com
 *
 */
@SpringBootApplication(scanBasePackageClasses = WebMvcConfigurerConfig.class, exclude = {
		// 错误页面完全自定义
		ErrorMvcAutoConfiguration.class,
		// Freemarker完全自定义
		FreeMarkerAutoConfiguration.class,
		// 上传文件完全自定义为CommonsMultipartResolver，需禁用此项，不然可能会出现上传文件为null的情况
		MultipartAutoConfiguration.class
		//
})
public class DataGearApplication
{
	public static void main(String[] args)
	{
		SpringApplication springApplication = new SpringApplication(DataGearApplication.class);
		springApplication.setBanner(new ApplicationBanner());
		springApplication.run(args);
	}
}
