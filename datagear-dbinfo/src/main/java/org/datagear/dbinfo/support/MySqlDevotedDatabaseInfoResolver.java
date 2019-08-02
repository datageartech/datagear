/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo.support;

import org.datagear.connection.ConnectionSensor;
import org.datagear.connection.URLConnectionSensor;
import org.datagear.connection.support.MySqlURLSensor;
import org.datagear.dbinfo.AbstractConnectionDevotedDatabaseInfoResolver;
import org.datagear.dbinfo.DevotedDatabaseInfoResolver;
import org.datagear.dbinfo.TableInfo;

/**
 * MySQL {@linkplain DevotedDatabaseInfoResolver}。
 * 
 * @author datagear@163.com
 *
 */
public class MySqlDevotedDatabaseInfoResolver extends AbstractConnectionDevotedDatabaseInfoResolver
{
	public MySqlDevotedDatabaseInfoResolver()
	{
		super(new URLConnectionSensor(new MySqlURLSensor()));
	}

	@Override
	public void setConnectionSensor(ConnectionSensor connectionSensor)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected void postProcessTableInfo(TableInfo[] tableInfos)
	{
		if (tableInfos == null)
			return;

		for (TableInfo tableInfo : tableInfos)
			resolveTableComment(tableInfo);
	}

	protected void resolveTableComment(TableInfo tableInfo)
	{
		String comment = tableInfo.getComment();

		if (comment == null || comment.isEmpty())
			return;

		int colonIdx = comment.indexOf(';');

		if (colonIdx > -1)
		{
			comment = comment.substring(0, colonIdx);
			tableInfo.setComment(comment);
		}
		else
			tableInfo.setComment("");
	}
}
