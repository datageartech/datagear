/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.datagear.connection.ConnectionOption;
import org.datagear.connection.ConnectionSource;
import org.datagear.connection.ConnectionSourceException;
import org.datagear.connection.DriverEntity;
import org.datagear.management.domain.Schema;
import org.datagear.management.service.SchemaService;
import org.datagear.util.JdbcUtil;
import org.datagear.web.convert.ClassDataConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

/**
 * 抽象{@linkplain Schema}连接控制器。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractSchemaConnController extends AbstractController
{
	@Autowired
	private SchemaService schemaService;

	@Autowired
	private ConnectionSource connectionSource;

	public AbstractSchemaConnController()
	{
		super();
	}

	public AbstractSchemaConnController(MessageSource messageSource, ClassDataConverter classDataConverter,
			SchemaService schemaService, ConnectionSource connectionSource)
	{
		super(messageSource, classDataConverter);
		this.schemaService = schemaService;
		this.connectionSource = connectionSource;
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
	 * @throws ConnectionSourceException
	 */
	protected Connection getSchemaConnection(Schema schema) throws ConnectionSourceException
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
	 * 抽象模式连接执行器。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected abstract class AbstractSchemaConnExecutor
	{
		private HttpServletRequest request;

		private HttpServletResponse response;

		private org.springframework.ui.Model springModel;

		private String schemaId;

		private Schema _schema;

		private Connection _cn;

		private boolean readonly;

		private boolean customCommit = false;

		public AbstractSchemaConnExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String schemaId, boolean readonly)
		{
			super();
			this.request = request;
			this.response = response;
			this.springModel = springModel;
			this.schemaId = schemaId;
			this.readonly = readonly;
			this.customCommit = false;
		}

		public AbstractSchemaConnExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String schemaId, boolean readonly, boolean customCommit)
		{
			super();
			this.request = request;
			this.response = response;
			this.springModel = springModel;
			this.schemaId = schemaId;
			this.readonly = readonly;
			this.customCommit = customCommit;
		}

		protected void doExecute() throws Throwable
		{
			this._schema = getSchemaNotNull(request, response, schemaId);

			springModel.addAttribute("schema", this._schema);

			try
			{
				doExecute(request, response, springModel, this._schema);

				if (!customCommit)
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
		 * @throws Throwable
		 */
		protected abstract void doExecute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, Schema schema) throws Throwable;
	}

	/**
	 * 返回值{@linkplain AbstractSchemaConnExecutor}。
	 * 
	 * @author datagear@163.com
	 *
	 * @param <T>
	 */
	protected abstract class ReturnSchemaConnExecutor<T> extends AbstractSchemaConnExecutor
	{
		private T returnValue;

		public ReturnSchemaConnExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String schemaId, boolean readonly)
		{
			super(request, response, springModel, schemaId, readonly);
		}

		public ReturnSchemaConnExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String schemaId, boolean readonly, boolean customCommit)
		{
			super(request, response, springModel, schemaId, readonly, customCommit);
		}

		public T execute() throws Throwable
		{
			doExecute();
			return returnValue;
		}

		@Override
		protected void doExecute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, Schema schema) throws Throwable
		{
			this.returnValue = execute(request, response, springModel, schema);
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
		 * @return
		 */
		protected abstract T execute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, Schema schema) throws Throwable;
	}

	/**
	 * 无返回值{@linkplain AbstractSchemaConnExecutor}。
	 * 
	 * @author datagear@163.com
	 *
	 * @param <T>
	 */
	protected abstract class VoidSchemaConnExecutor extends AbstractSchemaConnExecutor
	{
		public VoidSchemaConnExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String schemaId, boolean readonly)
		{
			super(request, response, springModel, schemaId, readonly);
		}

		public VoidSchemaConnExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String schemaId, boolean readonly, boolean customCommit)
		{
			super(request, response, springModel, schemaId, readonly, customCommit);
		}

		public void execute() throws Throwable
		{
			doExecute();
		}

		@Override
		protected void doExecute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, Schema schema) throws Throwable
		{
			execute(request, response, springModel, schema);
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
		 * @return
		 */
		protected abstract void execute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, Schema schema) throws Throwable;
	}

	/**
	 * 模式数据源。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected class SchemaDataSource implements DataSource
	{
		private Schema schema;

		public SchemaDataSource()
		{
			super();
		}

		public SchemaDataSource(Schema schema)
		{
			super();
			this.schema = schema;
		}

		public Schema getSchema()
		{
			return schema;
		}

		public void setSchema(Schema schema)
		{
			this.schema = schema;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T unwrap(Class<T> iface) throws SQLException
		{
			if (iface.isInstance(this))
			{
				return (T) this;
			}

			throw new SQLException("DataSource of type [" + getClass().getName() + "] cannot be unwrapped as ["
					+ iface.getName() + "]");
		}

		@Override
		public boolean isWrapperFor(Class<?> iface) throws SQLException
		{
			return iface.isInstance(this);
		}

		@Override
		public PrintWriter getLogWriter() throws SQLException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void setLogWriter(PrintWriter out) throws SQLException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void setLoginTimeout(int seconds) throws SQLException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public int getLoginTimeout() throws SQLException
		{
			return 0;
		}

		@Override
		public Connection getConnection() throws SQLException
		{
			return getSchemaConnection(this.schema);
		}

		@Override
		public Connection getConnection(String username, String password) throws SQLException
		{
			throw new UnsupportedOperationException();
		}
		
		//@Override
		public Logger getParentLogger() throws SQLFeatureNotSupportedException
		{
			throw new SQLFeatureNotSupportedException();
		}
	}
}
