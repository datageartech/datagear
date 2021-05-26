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
import org.datagear.analysis.support.DataFormat;
import org.datagear.analysis.support.JsonSupport;

/**
 * {@linkplain DataFormat}的Mybatis {@linkplain TypeHandler}。 
 * 
 * @author datagear@163.com
 *
 */
public class DataFormatTypeHandler extends BaseTypeHandler<DataFormat>
{
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, DataFormat parameter, JdbcType jdbcType)
			throws SQLException
	{
		String json = JsonSupport.generate(parameter, "");
		ps.setString(i, json);
	}

	@Override
	public DataFormat getNullableResult(ResultSet rs, String columnName) throws SQLException
	{
		String json = rs.getString(columnName);
		return fromJson(json);
	}

	@Override
	public DataFormat getNullableResult(ResultSet rs, int columnIndex) throws SQLException
	{
		String json = rs.getString(columnIndex);
		return fromJson(json);
	}

	@Override
	public DataFormat getNullableResult(CallableStatement cs, int columnIndex) throws SQLException
	{
		String json = cs.getString(columnIndex);
		return fromJson(json);
	}
	
	protected DataFormat fromJson(String json)
	{
		if(json == null || json.isEmpty())
			return null;
		
		DataFormat dataFormat = JsonSupport.parse(json, DataFormat.class, null);
		
		return dataFormat;
	}
}
