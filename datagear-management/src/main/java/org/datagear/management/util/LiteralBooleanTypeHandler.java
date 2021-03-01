/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.ibatis.type.BooleanTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * 使用字面值{@code 'true'}或{@code 'false'}存储{@linkplain Boolean}类型的MyBatis类型映射器。
 * <p>
 * {@linkplain BooleanTypeHandler}的{@linkplain #setNonNullParameter(PreparedStatement, int, Boolean, JdbcType)}
 * 使用的是{@linkplain PreparedStatement#setBoolean(int, boolean)}，对于不同数据库的驱动程序，当数据库列类型为VARCHAR时，
 * 存储的值可能为{@code 'true'}和{@code 'false'}，也可能为{@code '1'}和{@code '0'}，
 * 对于某些需要根据此列值进行判断处理的SQL语句，会出现数据库切换兼容问题。
 * </p>
 * <p>
 * 所以，这里定义此类，将{@linkplain Boolean}类型统一存储为{@code 'true'}和{@code 'false'}（要求数据库列类型为：VARCHAR）。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class LiteralBooleanTypeHandler extends BooleanTypeHandler
{
	public LiteralBooleanTypeHandler()
	{
		super();
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Boolean parameter, JdbcType jdbcType)
			throws SQLException
	{
		String value = (Boolean.TRUE.equals(parameter) ? "true" : "false");
		ps.setString(i, value);
	}
}
