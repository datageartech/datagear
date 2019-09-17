/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.sqlpad;

import org.datagear.management.domain.Schema;
import org.datagear.management.domain.User;
import org.datagear.util.SqlScriptParser;
import org.datagear.util.SqlScriptParser.SqlStatement;

/**
 * SQL执行权限检查器。
 * 
 * @author datagear@163.com
 *
 */
public class SqlPermissionChecker
{
	public SqlPermissionChecker()
	{
		super();
	}

	/**
	 * 检查用户是否有执行指定SQL的权限。
	 * 
	 * @param user
	 * @param schema
	 * @param sqlStatement
	 * @return
	 */
	public boolean hasPermission(User user, Schema schema, SqlStatement sqlStatement)
	{
		int permission = schema.getDataPermission();

		if (Schema.isDeleteTableDataPermission(permission))
			return true;

		String sqlAction = resolveSqlAction(sqlStatement.getSql());

		if (Schema.isReadTableDataPermission(permission))
		{
			if ("SELECT".equalsIgnoreCase(sqlAction))
				return true;
		}
		else if (Schema.isEditTableDataPermission(permission))
		{
			if ("SELECT".equalsIgnoreCase(sqlAction) || "UPDATE".equalsIgnoreCase(sqlAction))
				return true;
		}

		return false;
	}

	/**
	 * 解析SQL命令标识符，比如：“select”、“update”、"delete"。
	 * 
	 * @param sql
	 * @return
	 */
	protected String resolveSqlAction(String sql)
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0, len = sql.length(); i < len && i >= 0;)
		{
			char c = sql.charAt(i);
			char cn = (i < len - 1 ? sql.charAt(i + 1) : 0);

			if (c == '-' && cn == '-')
			{
				i = sql.indexOf(SqlScriptParser.LINE_SEPARATOR, i + 2) + SqlScriptParser.LINE_SEPARATOR.length() - 1;
			}
			else if (c == '/' && cn == '*')
			{
				i = sql.indexOf("*/", i + 2) + "*/".length();
			}
			else if (c == '/' && cn == '/')
			{
				i = sql.indexOf(SqlScriptParser.LINE_SEPARATOR, i + 2) + SqlScriptParser.LINE_SEPARATOR.length() - 1;
			}
			else if (Character.isWhitespace(c))
			{
				if (sb.length() > 0)
					break;
				else
					;

				i += 1;
			}
			else
			{
				sb.append(c);
				i += 1;
			}
		}

		return sb.toString();
	}
}
