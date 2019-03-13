/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerSession;
import org.datagear.connection.ConnectionSource;
import org.datagear.management.domain.Schema;
import org.datagear.management.service.SchemaService;
import org.datagear.web.convert.ClassDataConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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

	@RequestMapping("/message")
	@ResponseBody
	public String message(HttpServletRequest request, HttpServletResponse response) throws Throwable
	{
		String channelName = "/sqlpad";

		this.bayeuxServer.createIfAbsent(channelName);

		ServerChannel channel = this.bayeuxServer.getChannel(channelName);
		Set<ServerSession> sessions = channel.getSubscribers();

		for (ServerSession serverSession : sessions)
			channel.publish(serverSession, "hello");

		return "ok";
	}
}
