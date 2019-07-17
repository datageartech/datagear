/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.datagear.dataexchange.AbstractDevotedDataExchangeService;
import org.datagear.dataexchange.ConnectionFactory;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.DataImportListener;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.dataexchange.ExecuteDataImportSqlException;
import org.datagear.util.JdbcUtil;
import org.datagear.util.SqlScriptParser;
import org.datagear.util.SqlScriptParser.SqlStatement;

/**
 * SQL导入服务。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataImportService extends AbstractDevotedDataExchangeService<SqlDataImport>
{
	public SqlDataImportService()
	{
		super();
	}

	@Override
	public void exchange(SqlDataImport dataExchange) throws DataExchangeException
	{
		ExceptionResolve exceptionResolve = dataExchange.getImportOption().getExceptionResolve();
		ConnectionFactory connectionFactory = dataExchange.getConnectionFactory();

		DataImportListener listener = dataExchange.getListener();

		if (listener != null)
			listener.onStart();

		Reader reader = null;
		Connection cn = null;
		Statement st = null;

		try
		{
			reader = getResource(dataExchange.getReaderFactory());

			cn = connectionFactory.get();
			cn.setAutoCommit(false);

			st = cn.createStatement();

			executeSqlScripts(dataExchange, cn, st, reader);

			commit(cn);

			if (listener != null)
				listener.onSuccess();
		}
		catch (Throwable t)
		{
			handleExchangeThrowable(cn, exceptionResolve, t, listener);
		}
		finally
		{
			releaseResource(dataExchange.getReaderFactory(), reader);
			JdbcUtil.closeStatement(st);
			releaseResource(connectionFactory, cn);

			if (listener != null)
				listener.onFinish();
		}
	}

	/**
	 * 执行输入流的SQL语句。
	 * 
	 * @param cn
	 * @param st
	 * @param sqlReader
	 * @throws Throwable
	 */
	protected void executeSqlScripts(SqlDataImport dataExchange, Connection cn, Statement st, Reader sqlReader)
			throws Throwable
	{
		SqlScriptParser sqlScriptParser = createSqlScriptParser(sqlReader);

		SqlStatement sqlStatement = null;

		int index = 0;
		while ((sqlStatement = sqlScriptParser.parseNext()) != null)
		{
			executeSqlStatement(dataExchange, cn, st, sqlStatement, index);

			index++;
		}
	}

	/**
	 * 执行一条SQL语句。
	 * 
	 * @param dataExchange
	 * @param cn
	 * @param st
	 * @param sqlStatement
	 * @param sqlIndex
	 * @return
	 * @throws DataExchangeException
	 */
	protected boolean executeSqlStatement(SqlDataImport dataExchange, Connection cn, Statement st,
			SqlStatement sqlStatement, int sqlIndex) throws DataExchangeException
	{
		DataImportListener listener = dataExchange.getListener();

		SqlDataIndex dataIndex = SqlDataIndex.valueOf(sqlStatement);

		DataExchangeException exception = null;

		try
		{
			st.execute(sqlStatement.getSql());
		}
		catch (SQLException e)
		{
			exception = new ExecuteDataImportSqlException(dataIndex, e);
		}
		catch (Throwable t)
		{
			exception = wrapToDataExchangeException(t);
		}

		if (exception == null)
		{
			listener.onSuccess(dataIndex);
			return true;
		}
		else
		{
			if (ExceptionResolve.IGNORE.equals(dataExchange.getImportOption().getExceptionResolve()))
			{
				if (listener != null)
					listener.onIgnore(dataIndex, exception);

				return false;
			}
			else
				throw exception;
		}
	}

	/**
	 * 创建{@linkplain SqlScriptParser}。
	 * 
	 * @param reader
	 * @return
	 */
	protected SqlScriptParser createSqlScriptParser(Reader reader)
	{
		return new SqlScriptParser(reader);
	}
}
