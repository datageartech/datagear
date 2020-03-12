/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.meta.resolver.support;

import java.util.List;

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
	protected List<SimpleTable> postProcessSimpleTables(List<SimpleTable> simpleTables)
	{
		if (simpleTables != null)
		{
			for (SimpleTable st : simpleTables)
				resolveTableComment(st);
		}

		return simpleTables;
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
