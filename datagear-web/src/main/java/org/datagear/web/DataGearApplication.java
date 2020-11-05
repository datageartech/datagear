/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.datagear.util.Global;
import org.datagear.web.config.WebMvcConfigurerConfig;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.core.env.Environment;

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
		SpringApplication springApplication = new SpringApplication(DataGearApplication.class);
		springApplication.setBanner(new DataGearBanner());
		springApplication.run(args);
	}

	public static class DataGearBanner implements Banner
	{
		public static final String BORDER = "-----------------------------------------";

		@Override
		public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out)
		{
			List<String> lines = buildBannerLines("", Global.PRODUCT_NAME_EN + " - v" + Global.VERSION, Global.WEB_SITE,
					"", "(^_^)");

			for (String line : lines)
				out.println(line);
		}

		protected List<String> buildBannerLines(String... contents)
		{
			int maxLen = 0;
			for (String content : contents)
				maxLen = Math.max(maxLen, content.length());

			String border = BORDER;
			while (border.length() <= maxLen)
				border = border + BORDER;

			List<String> lines = new ArrayList<>();
			lines.add("+" + border + "+");

			// 每一行都居中显示
			for (String content : contents)
			{
				int spaces = border.length() - content.length();
				int spacesBefore = spaces / 2;
				int spacesAfter = spaces - spacesBefore;

				StringBuilder sb = new StringBuilder();

				sb.append('|');
				for (int i = 0; i < spacesBefore; i++)
					sb.append(' ');
				sb.append(content);
				for (int i = 0; i < spacesAfter; i++)
					sb.append(' ');
				sb.append('|');

				lines.add(sb.toString());
			}

			lines.add("+" + border + "+");

			return lines;
		}
	}
}
