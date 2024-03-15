/*
 * Copyright 2018-present datagear.tech
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

package org.datagear.management.util.typehandlers;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BooleanTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.mybatis.spring.SqlSessionFactoryBean;

/**
 * 使用字面值{@code 'true'}或{@code 'false'}存储{@linkplain Boolean}类型的MyBatis类型映射器。
 * <p>
 * {@linkplain BooleanTypeHandler}的{@linkplain #setNonNullParameter(PreparedStatement, int, Boolean, JdbcType)}
 * 使用的是{@linkplain PreparedStatement#setBoolean(int, boolean)}，对于不同数据库的驱动程序，当数据库列类型为VARCHAR时，
 * 存储的值可能为{@code 'true'}和{@code 'false'}，也可能为{@code '1'}和{@code '0'}，
 * 对于某些需要根据此列值进行判断处理的SQL语句，会出现数据库切换兼容问题。
 * </p>
 * <p>
 * 所以，这里定义此类，将{@linkplain Boolean}类型统一存储为{@code 'true'}和{@code 'false'}字符串（要求数据库列类型为：VARCHAR）。
 * </p>
 * <p>
 * 注意：此类必须添加下面的{@linkplain MappedTypes}注解，不然使用{@linkplain SqlSessionFactoryBean}执行初始化时无法注册处理{@code boolean}类型。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
@MappedTypes({ Boolean.class, boolean.class })
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

	@Override
	public Boolean getNullableResult(ResultSet rs, String columnName) throws SQLException
	{
		return stringToBoolean(rs.getString(columnName));
	}

	@Override
	public Boolean getNullableResult(ResultSet rs, int columnIndex) throws SQLException
	{
		return stringToBoolean(rs.getString(columnIndex));
	}

	@Override
	public Boolean getNullableResult(CallableStatement cs, int columnIndex) throws SQLException
	{
		return stringToBoolean(cs.getString(columnIndex));
	}

	protected String booleanToString(Boolean b)
	{
		return (Boolean.TRUE.equals(b) ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
	}

	protected boolean stringToBoolean(String s)
	{
		if (Boolean.TRUE.toString().equalsIgnoreCase(s))
		{
			return true;
		}
		else if ("1".equals(s))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
