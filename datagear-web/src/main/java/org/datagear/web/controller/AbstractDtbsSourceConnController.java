/*
 * Copyright 2018-present datagear.tech
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
import org.datagear.management.domain.DtbsSource;
import org.datagear.management.domain.User;
import org.datagear.management.service.DtbsSourceService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.management.util.DtbsSourceConnectionSupport;
import org.datagear.util.JdbcUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 抽象{@linkplain DtbsSource}连接控制器。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDtbsSourceConnController extends AbstractController
{
	@Autowired
	private DtbsSourceService dtbsSourceService;

	@Autowired
	private ConnectionSource connectionSource;

	private DtbsSourceConnectionSupport dtbsSourceConnectionSupport = new DtbsSourceConnectionSupport();

	public AbstractDtbsSourceConnController()
	{
		super();
	}

	public DtbsSourceService getDtbsSourceService()
	{
		return dtbsSourceService;
	}

	public void setDtbsSourceService(DtbsSourceService dtbsSourceService)
	{
		this.dtbsSourceService = dtbsSourceService;
	}

	public ConnectionSource getConnectionSource()
	{
		return connectionSource;
	}

	public void setConnectionSource(ConnectionSource connectionSource)
	{
		this.connectionSource = connectionSource;
	}

	public DtbsSourceConnectionSupport getDtbsSourceConnectionSupport()
	{
		return dtbsSourceConnectionSupport;
	}

	public void setDtbsSourceConnectionSupport(DtbsSourceConnectionSupport dtbsSourceConnectionSupport)
	{
		this.dtbsSourceConnectionSupport = dtbsSourceConnectionSupport;
	}

	/**
	 * 获取指定用户有读权限的{@linkplain DtbsSource}。
	 * 
	 * @param user
	 * @param dtbsSourceId
	 * @return
	 * @throws PermissionDeniedException
	 * @throws DtbsSourceNotFoundException
	 */
	protected DtbsSource getDtbsSourceForUserNotNull(User user, String dtbsSourceId)
			throws PermissionDeniedException, DtbsSourceNotFoundException
	{
		DtbsSource dtbsSource = this.dtbsSourceService.getById(user, dtbsSourceId);

		if (dtbsSource == null)
			throw new DtbsSourceNotFoundException(dtbsSourceId);

		return dtbsSource;
	}

	/**
	 * 获取当前用户有读权限的{@linkplain DtbsSource}。
	 * 
	 * @param request
	 * @param response
	 * @param dtbsSourceId
	 * @return
	 * @throws PermissionDeniedException
	 * @throws DtbsSourceNotFoundException
	 */
	protected DtbsSource getDtbsSourceForUserNotNull(HttpServletRequest request, HttpServletResponse response,
			String dtbsSourceId)
			throws PermissionDeniedException, DtbsSourceNotFoundException
	{
		User user = getCurrentUser();

		DtbsSource dtbsSource = this.dtbsSourceService.getById(user, dtbsSourceId);

		if (dtbsSource == null)
			throw new DtbsSourceNotFoundException(dtbsSourceId);

		return dtbsSource;
	}

	/**
	 * 获取{@linkplain DtbsSource}。
	 * 
	 * @param dtbsSourceId
	 * @return
	 * @throws DtbsSourceNotFoundException
	 */
	protected DtbsSource getDtbsSourceNotNull(String dtbsSourceId) throws DtbsSourceNotFoundException
	{
		DtbsSource dtbsSource = this.dtbsSourceService.getById(dtbsSourceId);

		if (dtbsSource == null)
			throw new DtbsSourceNotFoundException(dtbsSourceId);

		return dtbsSource;
	}

	/**
	 * 获取指定{@linkplain DtbsSource}的{@linkplain Connection}。
	 * 
	 * @param dtbsSource
	 * @return
	 * @throws ConnectionSourceException
	 */
	protected Connection getDtbsSourceConnection(DtbsSource dtbsSource) throws ConnectionSourceException
	{
		return this.dtbsSourceConnectionSupport.getDtbsSourceConnection(this.connectionSource, dtbsSource);
	}

	protected void checkReadTableDataPermission(DtbsSource dtbsSource, User user)
	{
		if (!DtbsSource.canReadTableData(dtbsSource.getDataPermission()))
			throw new PermissionDeniedException();
	}

	protected void checkEditTableDataPermission(DtbsSource dtbsSource, User user)
	{
		if (!DtbsSource.canEditTableData(dtbsSource.getDataPermission()))
			throw new PermissionDeniedException();
	}

	protected void checkDeleteTableDataPermission(DtbsSource dtbsSource, User user)
	{
		if (!DtbsSource.canDeleteTableData(dtbsSource.getDataPermission()))
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
	protected abstract class AbstractDtbsSourceConnExecutor
	{
		private HttpServletRequest request;

		private HttpServletResponse response;

		private org.springframework.ui.Model springModel;

		private String dtbsSourceId;

		private DtbsSource _dtbsSource;

		private Connection _cn;

		private boolean readonly;

		private boolean customCommit = false;

		public AbstractDtbsSourceConnExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String dtbsSourceId, boolean readonly)
		{
			super();
			this.request = request;
			this.response = response;
			this.springModel = springModel;
			this.dtbsSourceId = dtbsSourceId;
			this.readonly = readonly;
			this.customCommit = false;
		}

		public AbstractDtbsSourceConnExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String dtbsSourceId, boolean readonly, boolean customCommit)
		{
			super();
			this.request = request;
			this.response = response;
			this.springModel = springModel;
			this.dtbsSourceId = dtbsSourceId;
			this.readonly = readonly;
			this.customCommit = customCommit;
		}
		
		protected void doExecute() throws Throwable
		{
			try
			{
				this._dtbsSource = getDtbsSourceForUserNotNull(request, response, dtbsSourceId);
				springModel.addAttribute("dtbsSource", this._dtbsSource);

				doExecute(request, response, springModel, this._dtbsSource);

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
		 * 注意：如果此方法在{@linkplain #doExecute(HttpServletRequest, HttpServletResponse, org.springframework.ui.Model, DtbsSource)}内调用，
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
				this._cn = getDtbsSourceConnection(this._dtbsSource);
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
		 * @param dtbsSource
		 * @throws Throwable
		 */
		protected abstract void doExecute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, DtbsSource dtbsSource) throws Throwable;
		
		/**
		 * 获取{@linkplain DtbsSource}。
		 * <p>
		 *  只有在执行完成后才不会返回{@code null}。
		 * </p>
		 * 
		 * @return
		 */
		public DtbsSource getDtbsSource()
		{
			return this._dtbsSource;
		}
	}

	/**
	 * 返回值{@linkplain AbstractDtbsSourceConnExecutor}。
	 * <p>
	 * 注意：此类并非线程安全的。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 * @param <T>
	 */
	protected abstract class ReturnDtbsSourceConnExecutor<T> extends AbstractDtbsSourceConnExecutor
	{
		private T returnValue;

		public ReturnDtbsSourceConnExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String dtbsSourceId, boolean readonly)
		{
			super(request, response, springModel, dtbsSourceId, readonly);
		}

		public ReturnDtbsSourceConnExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String dtbsSourceId, boolean readonly, boolean customCommit)
		{
			super(request, response, springModel, dtbsSourceId, readonly, customCommit);
		}

		public T execute() throws Throwable
		{
			doExecute();
			return returnValue;
		}

		@Override
		protected void doExecute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, DtbsSource dtbsSource) throws Throwable
		{
			this.returnValue = execute(request, response, springModel, dtbsSource);
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
		 * @param dtbsSource
		 * @return
		 */
		protected abstract T execute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, DtbsSource dtbsSource) throws Throwable;
	}

	/**
	 * 无返回值{@linkplain AbstractDtbsSourceConnExecutor}。
	 * <p>
	 * 注意：此类并非线程安全的。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 * @param <T>
	 */
	protected abstract class VoidDtbsSourceConnExecutor extends AbstractDtbsSourceConnExecutor
	{
		public VoidDtbsSourceConnExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String dtbsSourceId, boolean readonly)
		{
			super(request, response, springModel, dtbsSourceId, readonly);
		}

		public VoidDtbsSourceConnExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String dtbsSourceId, boolean readonly, boolean customCommit)
		{
			super(request, response, springModel, dtbsSourceId, readonly, customCommit);
		}

		public void execute() throws Throwable
		{
			doExecute();
		}

		@Override
		protected void doExecute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, DtbsSource dtbsSource) throws Throwable
		{
			execute(request, response, springModel, dtbsSource);
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
		 * @param dtbsSource
		 * @return
		 */
		protected abstract void execute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, DtbsSource dtbsSource) throws Throwable;
	}

	/**
	 * 模式数据源。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected class DtbsSourceDataSource implements DataSource
	{
		private DtbsSource dtbsSource;

		public DtbsSourceDataSource()
		{
			super();
		}

		public DtbsSourceDataSource(DtbsSource dtbsSource)
		{
			super();
			this.dtbsSource = dtbsSource;
		}

		public DtbsSource getDtbsSource()
		{
			return dtbsSource;
		}

		public void setDtbsSource(DtbsSource dtbsSource)
		{
			this.dtbsSource = dtbsSource;
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
			return getDtbsSourceConnection(this.dtbsSource);
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
