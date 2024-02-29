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
		super(new URLConnectionSensor(MySqlURLSensor.INSTANCE));
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
		
		//老版本的mysql返回的实际注释在';'之后，这里特殊处理
		int colonIdx = comment.indexOf(';');
		if (colonIdx > -1)
		{
			comment = comment.substring(0, colonIdx);
			st.setComment(comment);
		}
	}
}
