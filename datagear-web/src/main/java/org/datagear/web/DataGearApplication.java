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

package org.datagear.web;

import java.io.PrintStream;

import org.datagear.util.Global;
import org.datagear.web.config.WebMvcConfigurerConfig;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.core.env.Environment;

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
		FreeMarkerAutoConfiguration.class })
public class DataGearApplication
{
	public static void main(String[] args)
	{
		SpringApplication springApplication = new SpringApplication(DataGearApplication.class);
		springApplication.setBanner(new DataGearBanner());
		springApplication.run(args);
	}

	public static class DataGearBanner implements Banner
	{
		@Override
		public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out)
		{
			out.println("  ____        _         ____                 ");
			out.println(" |  _ \\  __ _| |_ __ _ / ___| ___  __ _ _ __ ");
			out.println(" | | | |/ _` | __/ _` | |  _ / _ \\/ _` | '__|");
			out.println(" | |_| | (_| | |_ (_| | |_| |  __/ (_| | |   ");
			out.println(" |____/ \\__,_|\\__\\__,_|\\____|\\___|\\__,_|_|   ");
			out.println("");
			out.println("  " + Global.PRODUCT_NAME_EN + "-v" + Global.VERSION + "  " + Global.WEB_SITE);
			out.println("");
		}
	}
}
