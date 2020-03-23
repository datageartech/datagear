/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.meta.resolver.support;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.datagear.connection.ConnectionSensor;
import org.datagear.connection.URLConnectionSensor;
import org.datagear.connection.support.MySqlURLSensor;
import org.datagear.meta.SimpleTable;
import org.datagear.meta.resolver.AbstractConnectionDevotedDBMetaResolver;
import org.datagear.meta.resolver.DevotedDBMetaResolver;
import org.datagear.util.StringUtil;

/**
 * MySQL {@linkplain DevotedDBMetaResolver}。
 * 
 * @author datagear@163.com
 *
 */
public class MySqlDevotedDBMetaResolver extends AbstractConnectionDevotedDBMetaResolver
{
	public MySqlDevotedDBMetaResolver()
	{
		super(new URLConnectionSensor(new MySqlURLSensor()));
	}

	@Override
	public void setConnectionSensor(ConnectionSensor connectionSensor)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected SimpleTable postProcessSimpleTable(Connection cn, DatabaseMetaData metaData, String schema,
			SimpleTable simpleTable) throws SQLException
	{
		resolveTableComment(simpleTable);
		return simpleTable;
	}

	protected void resolveTableComment(SimpleTable st)
	{
		String comment = st.getComment();

		if (StringUtil.isEmpty(comment))
			return;

		int colonIdx = comment.indexOf(';');

		if (colonIdx > -1)
		{
			comment = comment.substring(0, colonIdx);
			st.setComment(comment);
		}
		else
			st.setComment("");
	}
}
