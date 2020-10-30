package org.datagear.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("org.datagear.web.config")
public class DataGearApplication
{
	public static void main(String[] args)
	{
		SpringApplication.run(DataGearApplication.class, args);
	}
}
