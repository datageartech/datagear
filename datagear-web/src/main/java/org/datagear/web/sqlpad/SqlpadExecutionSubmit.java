/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.web.sqlpad;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.datagear.management.domain.Schema;
import org.datagear.management.domain.User;
import org.datagear.persistence.RowMapper;
import org.datagear.util.SqlScriptParser.SqlStatement;
import org.datagear.web.sqlpad.SqlpadExecutionService.CommitMode;
import org.datagear.web.sqlpad.SqlpadExecutionService.ExceptionHandleMode;

/**
 * SQL工作台执行提交。
 * 
 * @author datagear@163.com
 *
 */
public class SqlpadExecutionSubmit
{
	/** 暂停允许最大分钟数 */
	public static final int MAX_PAUSE_OVER_TIME_THREASHOLD_MINUTES = 60;

	private User user;

	private Schema schema;

	private String sqlpadId;

	private File sqlpadFileDirectory;

	private List<SqlStatement> sqlStatements;

	private CommitMode commitMode;

	private ExceptionHandleMode exceptionHandleMode;

	/** 暂停超时过期分钟数 */
	private int overTimeThreashold;

	private int resultsetFetchSize;

	private RowMapper resultsetRowMapper;

	private Locale locale;

	public SqlpadExecutionSubmit()
	{
	}

	public SqlpadExecutionSubmit(SqlpadExecutionSubmit from)
	{
		this(from.user, from.schema, from.sqlpadId, from.sqlpadFileDirectory, from.sqlStatements, from.commitMode,
				from.exceptionHandleMode, from.overTimeThreashold, from.resultsetFetchSize, from.resultsetRowMapper,
				from.locale);
	}

	public SqlpadExecutionSubmit(User user, Schema schema, String sqlpadId, File sqlpadFileDirectory,
			List<SqlStatement> sqlStatements, CommitMode commitMode, ExceptionHandleMode exceptionHandleMode,
			Integer overTimeThreashold, int resultsetFetchSize, RowMapper resultsetRowMapper, Locale locale)
	{
		super();
		this.user = user;
		this.schema = schema;
		this.sqlpadId = sqlpadId;
		this.sqlpadFileDirectory = sqlpadFileDirectory;
		this.sqlStatements = sqlStatements;
		this.commitMode = commitMode;
		this.exceptionHandleMode = exceptionHandleMode;
		setOverTimeThreashold(overTimeThreashold);
		this.resultsetFetchSize = resultsetFetchSize;
		this.resultsetRowMapper = resultsetRowMapper;
		this.locale = locale;
	}

	public User getUser()
	{
		return user;
	}

	public void setUser(User user)
	{
		this.user = user;
	}

	public Schema getSchema()
	{
		return schema;
	}

	public void setSchema(Schema schema)
	{
		this.schema = schema;
	}

	public String getSqlpadId()
	{
		return sqlpadId;
	}

	public void setSqlpadId(String sqlpadId)
	{
		this.sqlpadId = sqlpadId;
	}

	public File getSqlpadFileDirectory()
	{
		return sqlpadFileDirectory;
	}

	public void setSqlpadFileDirectory(File sqlpadFileDirectory)
	{
		this.sqlpadFileDirectory = sqlpadFileDirectory;
	}

	public List<SqlStatement> getSqlStatements()
	{
		return sqlStatements;
	}

	public void setSqlStatements(List<SqlStatement> sqlStatements)
	{
		this.sqlStatements = sqlStatements;
	}

	public CommitMode getCommitMode()
	{
		return commitMode;
	}

	public void setCommitMode(CommitMode commitMode)
	{
		this.commitMode = commitMode;
	}

	public ExceptionHandleMode getExceptionHandleMode()
	{
		return exceptionHandleMode;
	}

	public void setExceptionHandleMode(ExceptionHandleMode exceptionHandleMode)
	{
		this.exceptionHandleMode = exceptionHandleMode;
	}

	public int getOverTimeThreashold()
	{
		return overTimeThreashold;
	}

	public void setOverTimeThreashold(Integer overTimeThreashold)
	{
		if (overTimeThreashold == null)
			overTimeThreashold = 10;
		else if (overTimeThreashold < 1)
			overTimeThreashold = 1;
		else if (overTimeThreashold > MAX_PAUSE_OVER_TIME_THREASHOLD_MINUTES)
			overTimeThreashold = MAX_PAUSE_OVER_TIME_THREASHOLD_MINUTES;

		this.overTimeThreashold = overTimeThreashold;
	}

	public int getResultsetFetchSize()
	{
		return resultsetFetchSize;
	}

	public void setResultsetFetchSize(int resultsetFetchSize)
	{
		this.resultsetFetchSize = resultsetFetchSize;
	}

	public RowMapper getResultsetRowMapper()
	{
		return resultsetRowMapper;
	}

	public void setResultsetRowMapper(RowMapper resultsetRowMapper)
	{
		this.resultsetRowMapper = resultsetRowMapper;
	}

	public Locale getLocale()
	{
		return locale;
	}

	public void setLocale(Locale locale)
	{
		this.locale = locale;
	}
}
