/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.datagear.dataexchange.AbstractDevotedDataExchangeService;
import org.datagear.dataexchange.DataExchangeContext;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.DataImportListener;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.dataexchange.ExecuteDataImportSqlException;
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
	protected void exchange(SqlDataImport dataExchange, DataExchangeContext context) throws Throwable
	{
		Reader reader = getResource(dataExchange.getReaderFactory(), context);

		Connection cn = context.getConnection();
		cn.setAutoCommit(false);

		Statement st = cn.createStatement();

		executeSqlScripts(dataExchange, cn, st, reader);

		commit(cn);
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
