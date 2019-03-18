/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cometd.bayeux.server.BayeuxServer;
import org.datagear.connection.ConnectionSource;
import org.datagear.management.domain.Schema;
import org.datagear.management.service.SchemaService;
import org.datagear.persistence.support.UUID;
import org.datagear.web.OperationMessage;
import org.datagear.web.convert.ClassDataConverter;
import org.datagear.web.sqlpad.SqlpadCometdService;
import org.datagear.web.sqlpad.SqlpadExecutionRunnable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
	@Autowired
	private BayeuxServer bayeuxServer;

	@Autowired
	private SqlpadCometdService sqlpadCometdService;

	public SqlpadController()
	{
		super();
	}

	public SqlpadController(MessageSource messageSource, ClassDataConverter classDataConverter,
			SchemaService schemaService, ConnectionSource connectionSource)
	{
		super(messageSource, classDataConverter, schemaService, connectionSource);
	}

	public BayeuxServer getBayeuxServer()
	{
		return bayeuxServer;
	}

	public void setBayeuxServer(BayeuxServer bayeuxServer)
	{
		this.bayeuxServer = bayeuxServer;
	}

	public SqlpadCometdService getSqlpadCometdService()
	{
		return sqlpadCometdService;
	}

	public void setSqlpadCometdService(SqlpadCometdService sqlpadCometdService)
	{
		this.sqlpadCometdService = sqlpadCometdService;
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

		String sqlpadChannelId = generateSqlpadChannelId(request, response);

		springModel.addAttribute("sqlpadChannelId", sqlpadChannelId);

		return "/sqlpad/sqlpad";
	}

	@RequestMapping(value = "/{schemaId}/execute", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> executeSql(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam("sqlpadChannelId") String sqlpadChannelId, @RequestParam("sql") String sql) throws Throwable
	{
		new VoidSchemaConnExecutor(request, response, springModel, schemaId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					Schema schema) throws Throwable
			{
			}
		}.execute();

		SqlpadExecutionRunnable sqlpadExecutionRunnable = new SqlpadExecutionRunnable(sqlpadCometdService,
				sqlpadChannelId, new StringReader(sql));
		sqlpadExecutionRunnable.init();

		new Thread(sqlpadExecutionRunnable).start();

		return buildOperationMessageSuccessEmptyResponseEntity();
	}

	/**
	 * 生成SQL工作台cometd通道ID。
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	protected String generateSqlpadChannelId(HttpServletRequest request, HttpServletResponse response)
	{
		return "/sqlpad/channel/" + UUID.gen();
	}
}
