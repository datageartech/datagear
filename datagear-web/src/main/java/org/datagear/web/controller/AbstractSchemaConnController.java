/*
 * Copyright 2018-2023 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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

import org.datagear.connection.ConnectionSource;
import org.datagear.connection.ConnectionSourceException;
import org.datagear.management.domain.Schema;
import org.datagear.management.domain.User;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.management.service.SchemaService;
import org.datagear.management.util.SchemaConnectionSupport;
import org.datagear.util.JdbcUtil;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;

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

	private SchemaConnectionSupport schemaConnectionSupport = new SchemaConnectionSupport();

	public AbstractSchemaConnController()
	{
		super();
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

	public SchemaConnectionSupport getSchemaConnectionSupport()
	{
		return schemaConnectionSupport;
	}

	public void setSchemaConnectionSupport(SchemaConnectionSupport schemaConnectionSupport)
	{
		this.schemaConnectionSupport = schemaConnectionSupport;
	}

	/**
	 * 获取指定用户有读权限的{@linkplain Schema}。
	 * 
	 * @param user
	 * @param schemaId
	 * @return
	 * @throws PermissionDeniedException
	 * @throws SchemaNotFoundException
	 */
	protected Schema getSchemaForUserNotNull(User user, String schemaId)
			throws PermissionDeniedException, SchemaNotFoundException
	{
		Schema schema = this.schemaService.getById(user, schemaId);

		if (schema == null)
			throw new SchemaNotFoundException(schemaId);

		return schema;
	}

	/**
	 * 获取当前用户有读权限的{@linkplain Schema}。
	 * 
	 * @param request
	 * @param response
	 * @param schemaId
	 * @return
	 * @throws PermissionDeniedException
	 * @throws SchemaNotFoundException
	 */
	protected Schema getSchemaForUserNotNull(HttpServletRequest request, HttpServletResponse response, String schemaId)
			throws PermissionDeniedException, SchemaNotFoundException
	{
		User user = WebUtils.getUser();

		Schema schema = this.schemaService.getById(user, schemaId);

		if (schema == null)
			throw new SchemaNotFoundException(schemaId);

		return schema;
	}

	/**
	 * 获取{@linkplain Schema}。
	 * 
	 * @param schemaId
	 * @return
	 * @throws SchemaNotFoundException
	 */
	protected Schema getSchemaNotNull(String schemaId) throws SchemaNotFoundException
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
		return this.schemaConnectionSupport.getSchemaConnection(this.connectionSource, schema);
	}

	protected void checkReadTableDataPermission(Schema schema, User user)
	{
		if (!Schema.canReadTableData(schema.getDataPermission()))
			throw new PermissionDeniedException();
	}

	protected void checkEditTableDataPermission(Schema schema, User user)
	{
		if (!Schema.canEditTableData(schema.getDataPermission()))
			throw new PermissionDeniedException();
	}

	protected void checkDeleteTableDataPermission(Schema schema, User user)
	{
		if (!Schema.canDeleteTableData(schema.getDataPermission()))
			throw new PermissionDeniedException();
	}

	/**
	 * 抽象模式连接执行器。
	 * <p>
	 * 注意：此类并非线程安全的。
	 * </p>
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
			try
			{
				this._schema = getSchemaForUserNotNull(request, response, schemaId);
				springModel.addAttribute("schema", this._schema);

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
				JdbcUtil.closeConnection(this._cn);
			}
		}

		/**
		 * 获取当前连接。
		 * <p>
		 * 注意：如果此方法在{@linkplain #doExecute(HttpServletRequest, HttpServletResponse, org.springframework.ui.Model, Schema)}内调用，
		 * 则不需关闭连接，否则，需自行关闭连接。
		 * </p>
		 * 
		 * @return
		 * @throws Exception
		 */
		protected Connection getConnection() throws Exception
		{
			if (this._cn == null)
			{
				this._cn = getSchemaConnection(this._schema);
				JdbcUtil.setAutoCommitIfSupports(this._cn, false);
				JdbcUtil.setReadonlyIfSupports(this._cn, this.readonly);
			}

			return this._cn;
		}

		protected void commitConnection() throws SQLException
		{
			if (this._cn == null)
				return;

			JdbcUtil.commitIfSupports(this._cn);
		}

		protected void rollbackConnection() throws SQLException
		{
			if (this._cn == null)
				return;

			JdbcUtil.rollbackIfSupports(this._cn);
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
		
		/**
		 * 获取{@linkplain Schema}。
		 * <p>
		 *  只有在执行完成后才不会返回{@code null}。
		 * </p>
		 * 
		 * @return
		 */
		public Schema getSchema()
		{
			return this._schema;
		}
	}

	/**
	 * 返回值{@linkplain AbstractSchemaConnExecutor}。
	 * <p>
	 * 注意：此类并非线程安全的。
	 * </p>
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
	 * <p>
	 * 注意：此类并非线程安全的。
	 * </p>
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

		// @Override
		@Override
		public Logger getParentLogger() throws SQLFeatureNotSupportedException
		{
			throw new SQLFeatureNotSupportedException();
		}
	}
}
