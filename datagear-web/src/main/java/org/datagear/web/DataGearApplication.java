package org.datagear.web;

import org.datagear.web.config.WebConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = WebConfiguration.class)
public class DataGearApplication
{
	public static void main(String[] args)
	{
		SpringApplication.run(DataGearApplication.class, args);
	}
}
