/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.connection.ConnectionSource;
import org.datagear.management.domain.Schema;
import org.datagear.management.service.SchemaService;
import org.datagear.web.convert.ClassDataConverter;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SQL工作台控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/sqlpad")
public class SqlpadController extends AbstractSchemaConnController
{
	public SqlpadController()
	{
		super();
	}

	public SqlpadController(MessageSource messageSource, ClassDataConverter classDataConverter,
			SchemaService schemaService, ConnectionSource connectionSource)
	{
		super(messageSource, classDataConverter, schemaService, connectionSource);
	}

	@RequestMapping("/{schemaId}")
	public String index(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId) throws Throwable
	{
		new VoidSchemaConnExecutor(request, response, springModel, schemaId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					Schema schema) throws Throwable
			{
			}
		}.execute();

		return "/sqlpad/sqlpad";
	}
}
