/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.connection.ConnectionOption;
import org.datagear.connection.ConnectionSource;
import org.datagear.connection.DriverEntity;
import org.datagear.connection.JdbcUtil;
import org.datagear.dbmodel.CachedDbModelFactory;
import org.datagear.management.domain.Schema;
import org.datagear.management.service.SchemaService;
import org.datagear.model.Model;
import org.datagear.web.convert.ClassDataConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

/**
 * 抽象{@linkplain Schema}、{@linkplain Model}
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractSchemaModelController extends AbstractController
{
	@Autowired
	private SchemaService schemaService;

	@Autowired
	private ConnectionSource connectionSource;

	@Autowired
	private CachedDbModelFactory cachedDbModelFactory;

	public AbstractSchemaModelController()
	{
		super();
	}

	public AbstractSchemaModelController(MessageSource messageSource, ClassDataConverter classDataConverter,
			SchemaService schemaService, ConnectionSource connectionSource, CachedDbModelFactory cachedDbModelFactory)
	{
		super(messageSource, classDataConverter);
		this.schemaService = schemaService;
		this.connectionSource = connectionSource;
		this.cachedDbModelFactory = cachedDbModelFactory;
	}

	public SchemaService getSchemaService()
	{
		return schemaService;
	}

	public void setSchemaService(SchemaService schemaService)
	{
		this.schemaService = schemaService;
	}

	public ConnectionSource getConnectionSource()
	{
		return connectionSource;
	}

	public void setConnectionSource(ConnectionSource connectionSource)
	{
		this.connectionSource = connectionSource;
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
	 * 根据ID查找{@linkplain Schema}。
	 * 
	 * @param request
	 * @param response
	 * @param schemaId
	 * @return
	 * @throws SchemaNotFoundException
	 */
	protected Schema getSchemaNotNull(HttpServletRequest request, HttpServletResponse response, String schemaId)
			throws SchemaNotFoundException
	{
		Schema schema = this.schemaService.getById(schemaId);

		if (schema == null)
			throw new SchemaNotFoundException(schemaId);

		return schema;
	}

	/**
	 * 获取指定{@linkplain Schema}的{@linkplain Connection}。
	 * 
	 * @param schema
	 * @return
	 * @throws Exception
	 */
	protected Connection getSchemaConnection(Schema schema) throws Exception
	{
		Connection cn = null;

		ConnectionOption connectionOption = ConnectionOption.valueOf(schema.getUrl(), schema.getUser(),
				schema.getPassword());

		if (schema.hasDriverEntity())
		{
			DriverEntity driverEntity = schema.getDriverEntity();

			cn = this.connectionSource.getConnection(driverEntity, connectionOption);
		}
		else
		{
			cn = this.connectionSource.getConnection(connectionOption);
		}

		return cn;
	}

	/**
	 * 抽象模式、模型相关执行器。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected abstract class AbstractSchemaModelExecutor
	{
		private HttpServletRequest request;

		private HttpServletResponse response;

		private org.springframework.ui.Model springModel;

		private String schemaId;

		private String tableName;

		private Schema _schema;

		private Connection _cn;

		private boolean readonly;

		public AbstractSchemaModelExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String schemaId, String tableName, boolean readonly)
		{
			super();
			this.request = request;
			this.response = response;
			this.springModel = springModel;
			this.schemaId = schemaId;
			this.tableName = tableName;
			this.readonly = readonly;
		}

		protected void doExecute() throws Throwable
		{
			this._schema = getSchemaNotNull(request, response, schemaId);

			springModel.addAttribute("schema", this._schema);

			Model model = getCachedDbModelFactory().getCachedModel(schemaId, tableName);

			if (model == null)
			{
				try
				{
					model = getCachedDbModelFactory().getModel(getConnection(), schemaId, tableName);
					springModel.addAttribute("model", model);

					doExecute(request, response, springModel, this._schema, model);

					commitConnection();
				}
				catch (Throwable e)
				{
					rollbackConnection();

					throw e;
				}
				finally
				{
					if (this._cn != null)
						JdbcUtil.closeConnection(this._cn);
				}
			}
			else
			{
				springModel.addAttribute("model", model);

				try
				{
					doExecute(request, response, springModel, this._schema, model);

					commitConnection();
				}
				catch (Exception e)
				{
					rollbackConnection();

					throw e;
				}
				finally
				{
					if (this._cn != null)
						JdbcUtil.closeConnection(this._cn);
				}
			}
		}

		protected Connection getConnection() throws Exception
		{
			if (this._cn == null)
			{
				this._cn = getSchemaConnection(this._schema);
				this._cn.setAutoCommit(false);
				this._cn.setReadOnly(this.readonly);
			}

			return this._cn;
		}

		protected void commitConnection() throws SQLException
		{
			if (this._cn == null)
				return;

			this._cn.commit();
		}

		protected void rollbackConnection() throws SQLException
		{
			if (this._cn == null)
				return;

			this._cn.rollback();
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
	 * 带有返回值的模式、模型相关执行器。
	 * 
	 * @author datagear@163.com
	 *
	 * @param <T>
	 */
	protected abstract class ReturnExecutor<T> extends AbstractSchemaModelExecutor
	{
		private T returnValue;

		public ReturnExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String schemaId, String tableName, boolean readonly)
		{
			super(request, response, springModel, schemaId, tableName, readonly);
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
	 * 无返回值的模式、模型相关执行器。
	 * 
	 * @author datagear@163.com
	 *
	 * @param <T>
	 */
	protected abstract class VoidExecutor extends AbstractSchemaModelExecutor
	{
		public VoidExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String schemaId, String tableName, boolean readonly)
		{
			super(request, response, springModel, schemaId, tableName, readonly);
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
