/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.config;

import org.datagear.web.config.support.DeliverContentTypeExceptionHandlerExceptionResolver;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

/**
 * Web配置。
 * 
 * @author datagear@163.com
 *
 */
@Configuration
public class WebMvcRegistrationsConfig implements WebMvcRegistrations
{
	@Override
	public ExceptionHandlerExceptionResolver getExceptionHandlerExceptionResolver()
	{
		return new DeliverContentTypeExceptionHandlerExceptionResolver();
	}
}
