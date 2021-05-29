/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.util.typehandlers;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.datagear.analysis.support.JsonSupport;

/**
 * 抽象JSON {@linkplain TypeHandler}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractJsonTypeHandler<T> extends BaseTypeHandler<T>
{
	public AbstractJsonTypeHandler()
	{
		super();
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException
	{
		String json = JsonSupport.generate(parameter, "");
		ps.setString(i, json);
	}

	@Override
	public T getNullableResult(ResultSet rs, String columnName) throws SQLException
	{
		String json = rs.getString(columnName);
		return fromJson(json, getJsonObjectType());
	}

	@Override
	public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException
	{
		String json = rs.getString(columnIndex);
		return fromJson(json, getJsonObjectType());
	}

	@Override
	public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException
	{
		String json = cs.getString(columnIndex);
		return fromJson(json, getJsonObjectType());
	}

	protected <TT> TT fromJson(String json, Class<TT> clazz)
	{
		if (json == null || json.isEmpty())
			return null;

		TT obj = JsonSupport.parse(json, clazz, null);

		return obj;
	}

	protected abstract Class<T> getJsonObjectType();
}
