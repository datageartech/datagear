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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.DtbsSource;
import org.datagear.meta.Table;
import org.datagear.meta.resolver.DBMetaResolver;
import org.datagear.persistence.support.NoColumnDefinedException;
import org.datagear.web.util.DtbsSourceTableCache;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 抽象数据库表连接控制器。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDtbsSourceConnTableController extends AbstractDtbsSourceConnController
{
	@Autowired
	private DBMetaResolver dbMetaResolver;

	@Autowired
	private DtbsSourceTableCache dtbsSourceTableCache;

	public AbstractDtbsSourceConnTableController()
	{
		super();
	}

	public DBMetaResolver getDbMetaResolver()
	{
		return dbMetaResolver;
	}

	public void setDbMetaResolver(DBMetaResolver dbMetaResolver)
	{
		this.dbMetaResolver = dbMetaResolver;
	}

	public DtbsSourceTableCache getDtbsSourceTableCache()
	{
		return dtbsSourceTableCache;
	}

	public void setDtbsSourceTableCache(DtbsSourceTableCache dtbsSourceTableCache)
	{
		this.dtbsSourceTableCache = dtbsSourceTableCache;
	}

	/**
	 * 抽象数据库表执行器。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected abstract class AbstractDtbsSourceConnTableExecutor extends AbstractDtbsSourceConnExecutor
	{
		private String tableName;

		public AbstractDtbsSourceConnTableExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String dtbsSourceId, String tableName, boolean readonly)
		{
			super(request, response, springModel, dtbsSourceId, readonly);
			this.tableName = tableName;
		}

		public AbstractDtbsSourceConnTableExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String dtbsSourceId, String tableName, boolean readonly,
				boolean customCommit)
		{
			super(request, response, springModel, dtbsSourceId, readonly, customCommit);
			this.tableName = tableName;
		}

		@Override
		protected void doExecute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, DtbsSource dtbsSource) throws Throwable
		{
			springModel.addAttribute("tableName", this.tableName);

			Table table = getDtbsSourceTableCache().get(dtbsSource.getId(), this.tableName);
			if (table == null)
			{
				table = getDbMetaResolver().getTable(getConnection(), this.tableName);
				getDtbsSourceTableCache().put(dtbsSource.getId(), table);
			}

			if (!table.hasColumn())
				throw new NoColumnDefinedException(table.getName());

			springModel.addAttribute("table", table);

			doExecute(request, response, springModel, dtbsSource, table);
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
		 * @param table
		 * @throws Throwable
		 */
		protected abstract void doExecute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, DtbsSource dtbsSource, Table table) throws Throwable;
	}

	/**
	 * 返回值{@linkplain AbstractDtbsSourceConnTableExecutor}。
	 * 
	 * @author datagear@163.com
	 *
	 * @param <T>
	 */
	protected abstract class ReturnDtbsSourceConnTableExecutor<T> extends AbstractDtbsSourceConnTableExecutor
	{
		private T returnValue;

		public ReturnDtbsSourceConnTableExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String dtbsSourceId, String tableName, boolean readonly)
		{
			super(request, response, springModel, dtbsSourceId, tableName, readonly);
		}

		public ReturnDtbsSourceConnTableExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String dtbsSourceId, String tableName, boolean readonly,
				boolean customCommit)
		{
			super(request, response, springModel, dtbsSourceId, tableName, readonly, customCommit);
		}

		public T execute() throws Throwable
		{
			doExecute();
			return returnValue;
		}

		@Override
		protected void doExecute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, DtbsSource dtbsSource, Table table) throws Throwable
		{
			this.returnValue = execute(request, response, springModel, dtbsSource, table);
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
		 * @param table
		 * @return
		 */
		protected abstract T execute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, DtbsSource dtbsSource, Table table) throws Throwable;
	}

	/**
	 * 无返回值{@linkplain AbstractDtbsSourceConnTableExecutor}。
	 * 
	 * @author datagear@163.com
	 *
	 * @param <T>
	 */
	protected abstract class VoidDtbsSourceConnTableExecutor extends AbstractDtbsSourceConnTableExecutor
	{
		public VoidDtbsSourceConnTableExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String dtbsSourceId, String tableName, boolean readonly)
		{
			super(request, response, springModel, dtbsSourceId, tableName, readonly);
		}

		public VoidDtbsSourceConnTableExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String dtbsSourceId, String tableName, boolean readonly,
				boolean customCommit)
		{
			super(request, response, springModel, dtbsSourceId, tableName, readonly, customCommit);
		}

		public void execute() throws Throwable
		{
			doExecute();
		}

		@Override
		protected void doExecute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, DtbsSource dtbsSource, Table table) throws Throwable
		{
			execute(request, response, springModel, dtbsSource, table);
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
		 * @param table
		 * @return
		 */
		protected abstract void execute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, DtbsSource dtbsSource, Table table) throws Throwable;
	}
}
