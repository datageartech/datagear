/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 抽象{@linkplain Query}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractQuery implements Query
{
	public AbstractQuery()
	{
		super();
	}

	/**
	 * 执行SQL查询。
	 * 
	 * @param cn
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	protected ResultSet executeQuery(Connection cn, String sql) throws SQLException
	{
		Statement st = cn.createStatement();

		return st.executeQuery(sql);
	}
}
