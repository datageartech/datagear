/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.connection.ConnectionSource;
import org.datagear.dbmodel.CachedDbModelFactory;
import org.datagear.management.domain.Schema;
import org.datagear.management.service.SchemaService;
import org.datagear.model.Model;
import org.datagear.web.convert.ClassDataConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

/**
 * 抽象{@linkplain Schema}、{@linkplain Model}连接控制器。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractSchemaModelConnController extends AbstractSchemaConnController
{
	@Autowired
	private CachedDbModelFactory cachedDbModelFactory;

	public AbstractSchemaModelConnController()
	{
		super();
	}

	public AbstractSchemaModelConnController(MessageSource messageSource, ClassDataConverter classDataConverter,
			SchemaService schemaService, ConnectionSource connectionSource, CachedDbModelFactory cachedDbModelFactory)
	{
		super(messageSource, classDataConverter, schemaService, connectionSource);
		this.cachedDbModelFactory = cachedDbModelFactory;
	}

	public CachedDbModelFactory getCachedDbModelFactory()
	{
		return cachedDbModelFactory;
	}

	public void setCachedDbModelFactory(CachedDbModelFactory cachedDbModelFactory)
	{
		this.cachedDbModelFactory = cachedDbModelFactory;
	}

	/**
	 * 抽象模式、模型连接执行器。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected abstract class AbstractSchemaModelConnExecutor extends AbstractSchemaConnExecutor
	{
		private String tableName;

		public AbstractSchemaModelConnExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String schemaId, String tableName, boolean readonly)
		{
			super(request, response, springModel, schemaId, readonly);
			this.tableName = tableName;
		}

		public AbstractSchemaModelConnExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String schemaId, String tableName, boolean readonly,
				boolean customCommit)
		{
			super(request, response, springModel, schemaId, readonly, customCommit);
			this.tableName = tableName;
		}

		@Override
		protected void doExecute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, Schema schema) throws Throwable
		{
			springModel.addAttribute("tableName", this.tableName);

			String schemaId = schema.getId();

			Model model = getCachedDbModelFactory().getCachedModel(schemaId, tableName);
			if (model == null)
				model = getCachedDbModelFactory().getModel(getConnection(), schemaId, tableName);

			springModel.addAttribute("model", model);

			doExecute(request, response, springModel, schema, model);
		}

		/**
		 * 执行。
		 * <p>
		 * 此方法内可以调用{@linkplain #getConnection()}直接使用，而不需要关闭。
		 * </p>
		 * 
		 * @param request
		 * @param response
		 * @param springModel
		 * @param schema
		 * @param model
		 * @throws Throwable
		 */
		protected abstract void doExecute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable;
	}

	/**
	 * 返回值{@linkplain AbstractSchemaModelConnExecutor}。
	 * 
	 * @author datagear@163.com
	 *
	 * @param <T>
	 */
	protected abstract class ReturnSchemaModelConnExecutor<T> extends AbstractSchemaModelConnExecutor
	{
		private T returnValue;

		public ReturnSchemaModelConnExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String schemaId, String tableName, boolean readonly)
		{
			super(request, response, springModel, schemaId, tableName, readonly);
		}

		public ReturnSchemaModelConnExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String schemaId, String tableName, boolean readonly,
				boolean customCommit)
		{
			super(request, response, springModel, schemaId, tableName, readonly, customCommit);
		}

		public T execute() throws Throwable
		{
			doExecute();
			return returnValue;
		}

		@Override
		protected void doExecute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
		{
			this.returnValue = execute(request, response, springModel, schema, model);
		}

		/**
		 * 执行。
		 * <p>
		 * 此方法内可以调用{@linkplain #getConnection()}直接使用，而不需要关闭。
		 * </p>
		 * 
		 * @param request
		 * @param response
		 * @param springModel
		 * @param schema
		 * @param model
		 * @return
		 */
		protected abstract T execute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable;
	}

	/**
	 * 无返回值{@linkplain AbstractSchemaModelConnExecutor}。
	 * 
	 * @author datagear@163.com
	 *
	 * @param <T>
	 */
	protected abstract class VoidSchemaModelConnExecutor extends AbstractSchemaModelConnExecutor
	{
		public VoidSchemaModelConnExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String schemaId, String tableName, boolean readonly)
		{
			super(request, response, springModel, schemaId, tableName, readonly);
		}

		public VoidSchemaModelConnExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String schemaId, String tableName, boolean readonly,
				boolean customCommit)
		{
			super(request, response, springModel, schemaId, tableName, readonly, customCommit);
		}

		public void execute() throws Throwable
		{
			doExecute();
		}

		@Override
		protected void doExecute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
		{
			execute(request, response, springModel, schema, model);
		}

		/**
		 * 执行。
		 * <p>
		 * 此方法内可以调用{@linkplain #getConnection()}直接使用，而不需要关闭。
		 * </p>
		 * 
		 * @param request
		 * @param response
		 * @param springModel
		 * @param schema
		 * @param model
		 * @return
		 */
		protected abstract void execute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable;
	}
}
