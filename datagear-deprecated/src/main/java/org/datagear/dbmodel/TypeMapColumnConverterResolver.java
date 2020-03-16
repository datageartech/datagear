/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbmodel;

import java.sql.Connection;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.EntireTableInfo;
import org.datagear.model.Model;
import org.datagear.persistence.columnconverter.BlobColumnConverter;
import org.datagear.persistence.columnconverter.BytesColumnConverter;
import org.datagear.persistence.columnconverter.ClobColumnConverter;
import org.datagear.persistence.columnconverter.StringColumnConverter;
import org.datagear.persistence.features.ColumnConverter;

/**
 * 基于类型映射表的{@linkplain ColumnConverterResolver}。
 * <p>
 * 此类的{@linkplain #getJdbcTypeMap()}主键是{@linkplain Types}类的JDBC类型字段，值是对应的{@linkplain Model#getType()}。
 * </p>
 * <p>
 * 此类会默认添加如下类型映射：
 * </p>
 * 
 * <pre>
 * Types.BINARY -> new BytesColumnConverter()
 * Types.BLOB -> new BlobColumnConverter()
 * Types.CLOB -> new ClobColumnConverter()
 * Types.LONGNVARCHAR -> new StringColumnConverter()
 * Types.LONGVARBINARY -> new BytesColumnConverter()
 * Types.LONGVARCHAR -> new StringColumnConverter()
 * Types.NCLOB -> new ClobColumnConverter()
 * Types.VARBINARY -> new BytesColumnConverter()
 * </pre>
 * 
 * @author datagear@163.com
 *
 */
public class TypeMapColumnConverterResolver extends JdbcTypeMapSupport implements ColumnConverterResolver
{
	private Map<Integer, ColumnConverter> columnConverterMap = new HashMap<Integer, ColumnConverter>();

	public TypeMapColumnConverterResolver()
	{
		super();
		initDefaultColumnConverterMap();
	}

	/**
	 * 初始化默认类型映射表。
	 */
	public void initDefaultColumnConverterMap()
	{
		this.columnConverterMap.put(Types.BINARY, new BytesColumnConverter());
		this.columnConverterMap.put(Types.BLOB, new BlobColumnConverter());
		this.columnConverterMap.put(Types.CLOB, new ClobColumnConverter());
		this.columnConverterMap.put(Types.LONGNVARCHAR, new StringColumnConverter());
		this.columnConverterMap.put(Types.LONGVARBINARY, new BytesColumnConverter());
		this.columnConverterMap.put(Types.LONGVARCHAR, new StringColumnConverter());
		this.columnConverterMap.put(Types.NCLOB, new ClobColumnConverter());
		this.columnConverterMap.put(Types.VARBINARY, new BytesColumnConverter());
	}

	public Map<Integer, ColumnConverter> getColumnConverterMap()
	{
		return columnConverterMap;
	}

	public void setColumnConverterMap(Map<Integer, ColumnConverter> columnConverterMap)
	{
		this.columnConverterMap = columnConverterMap;
	}

	/**
	 * 设置字面型映射表。
	 * <p>
	 * 它的主键是{@linkplain Types}类的JDBC类型字段名，此方法会自动将它们转换为对应的字段值。
	 * </p>
	 * <p>
	 * 此方法可以在XML配置中使用，使其更友好。
	 * </p>
	 * 
	 * @param literalColumnConverterMap
	 */
	public void setLiteralColumnConverterMap(Map<String, ColumnConverter> literalColumnConverterMap)
	{
		Map<Integer, ColumnConverter> jdbcTypeMap = literalMapToValueMap(literalColumnConverterMap);
		this.columnConverterMap.putAll(jdbcTypeMap);
	}

	@Override
	public ColumnConverter resolve(Connection cn, EntireTableInfo entireTableInfo, ColumnInfo columnInfo)
	{
		int jdbcType = columnInfo.getType();
		ColumnConverter columnConverter = this.columnConverterMap.get(jdbcType);

		return columnConverter;
	}
}
