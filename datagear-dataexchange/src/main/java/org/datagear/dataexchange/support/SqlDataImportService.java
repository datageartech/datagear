/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
import org.datagear.dataexchange.SqlValidationException;
import org.datagear.util.JdbcUtil;
import org.datagear.util.SqlScriptParser;
import org.datagear.util.SqlScriptParser.SqlStatement;
import org.datagear.util.sqlvalidator.DatabaseProfile;
import org.datagear.util.sqlvalidator.SqlValidation;
import org.datagear.util.sqlvalidator.SqlValidator;

/**
 * SQL导入服务。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataImportService extends AbstractDevotedDataExchangeService<SqlDataImport>
{
	private SqlValidator sqlValidator;

	public SqlDataImportService()
	{
		super();
	}

	public SqlValidator getSqlValidator()
	{
		return sqlValidator;
	}

	public void setSqlValidator(SqlValidator sqlValidator)
	{
		this.sqlValidator = sqlValidator;
	}

	@Override
	protected void exchange(SqlDataImport dataExchange, DataExchangeContext context) throws Throwable
	{
		Reader reader = getResource(dataExchange.getReaderFactory(), context);

		Connection cn = context.getConnection();
		JdbcUtil.setAutoCommitIfSupports(cn, false);
		JdbcUtil.setReadonlyIfSupports(cn, false);

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
		DatabaseProfile databaseProfile = DatabaseProfile.valueOf(cn);

		int index = 0;
		while ((sqlStatement = sqlScriptParser.parseNext()) != null)
		{
			executeSqlStatement(dataExchange, cn, st, sqlStatement, index, databaseProfile);

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
	 * @param databaseProfile
	 * @return
	 * @throws DataExchangeException
	 */
	protected boolean executeSqlStatement(SqlDataImport dataExchange, Connection cn, Statement st,
			SqlStatement sqlStatement, int sqlIndex, DatabaseProfile databaseProfile) throws DataExchangeException
	{
		DataImportListener listener = dataExchange.getListener();

		SqlDataIndex dataIndex = SqlDataIndex.valueOf(sqlStatement);

		DataExchangeException exception = null;

		if (this.sqlValidator != null)
		{
			SqlValidation validation = this.sqlValidator.validate(sqlStatement.getSql(), databaseProfile);
			if (!validation.isValid())
				exception = new SqlValidationException(sqlStatement.getSql(), validation);
		}

		if (exception == null)
		{
			try
			{
				st.execute(sqlStatement.getSql());
			}
			catch(SQLException e)
			{
				exception = new ExecuteDataImportSqlException(dataIndex, e);
			}
			catch(Throwable t)
			{
				exception = wrapToDataExchangeException(t);
			}
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
