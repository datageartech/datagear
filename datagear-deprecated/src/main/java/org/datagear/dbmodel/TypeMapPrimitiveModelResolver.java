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
import org.datagear.model.support.PrimitiveModelSource;

/**
 * 基于类型映射表的{@linkplain PrimitiveModelResolver}。
 * <p>
 * 此类的{@linkplain #getModelTypeMap()}主键是{@linkplain Types}类的JDBC类型字段，值是对应的{@linkplain Model#getType()}。
 * </p>
 * <p>
 * 此类会默认添加如下类型映射：
 * </p>
 * 
 * <pre>
 * Types.BIGINT -> Long.class
 * Types.BINARY -> java.io.File.class
 * Types.BIT -> Boolean.class
 * Types.BLOB -> java.io.File.class
 * Types.BOOLEAN -> Boolean.class
 * Types.CHAR -> String.class
 * Types.CLOB -> String.class
 * Types.DATE -> java.sql.Date.class
 * Types.DECIMAL -> java.math.BigDecimal.class
 * Types.DOUBLE -> Double.class
 * Types.FLOAT -> Float.class
 * Types.INTEGER -> Integer.class
 * Types.LONGNVARCHAR -> String.class
 * Types.LONGVARBINARY -> java.io.File.class
 * Types.LONGVARCHAR -> String.class
 * Types.NCHAR -> String.class
 * Types.NCLOB -> String.class
 * Types.NUMERIC -> java.math.BigDecimal.class
 * Types.NVARCHAR -> String.class
 * Types.REAL -> Float.class
 * Types.SMALLINT -> Short.class
 * Types.TIME -> java.sql.Time.class
 * Types.TIMESTAMP -> java.sql.Timestamp.class
 * Types.TINYINT -> Byte.class
 * Types.VARBINARY -> java.io.File.class
 * Types.VARCHAR -> String.class
 * </pre>
 * 
 * @author datagear@163.com
 *
 */
public class TypeMapPrimitiveModelResolver extends JdbcTypeMapSupport implements PrimitiveModelResolver
{
	private PrimitiveModelSource primitiveModelSource;

	private Map<Integer, Class<?>> modelTypeMap = new HashMap<Integer, Class<?>>();

	public TypeMapPrimitiveModelResolver()
	{
		super();
		initDefaultModelTypeMap();
	}

	public TypeMapPrimitiveModelResolver(PrimitiveModelSource primitiveModelSource)
	{
		super();
		this.primitiveModelSource = primitiveModelSource;
		initDefaultModelTypeMap();
	}

	/**
	 * 初始化默认JDBC类型值映射表。
	 */
	protected void initDefaultModelTypeMap()
	{
		this.modelTypeMap.put(Types.BIGINT, Long.class);
		this.modelTypeMap.put(Types.BINARY, java.io.File.class);
		this.modelTypeMap.put(Types.BIT, Boolean.class);
		this.modelTypeMap.put(Types.BLOB, java.io.File.class);
		this.modelTypeMap.put(Types.BOOLEAN, Boolean.class);
		this.modelTypeMap.put(Types.CHAR, String.class);
		this.modelTypeMap.put(Types.CLOB, String.class);
		this.modelTypeMap.put(Types.DATE, java.sql.Date.class);
		this.modelTypeMap.put(Types.DECIMAL, java.math.BigDecimal.class);
		this.modelTypeMap.put(Types.DOUBLE, Double.class);
		this.modelTypeMap.put(Types.FLOAT, Float.class);
		this.modelTypeMap.put(Types.INTEGER, Integer.class);
		this.modelTypeMap.put(Types.LONGNVARCHAR, String.class);
		this.modelTypeMap.put(Types.LONGVARBINARY, java.io.File.class);
		this.modelTypeMap.put(Types.LONGVARCHAR, String.class);
		this.modelTypeMap.put(Types.NCHAR, String.class);
		this.modelTypeMap.put(Types.NCLOB, String.class);
		this.modelTypeMap.put(Types.NUMERIC, java.math.BigDecimal.class);
		this.modelTypeMap.put(Types.NVARCHAR, String.class);
		this.modelTypeMap.put(Types.REAL, Float.class);
		this.modelTypeMap.put(Types.SMALLINT, Short.class);
		this.modelTypeMap.put(Types.TIME, java.sql.Time.class);
		this.modelTypeMap.put(Types.TIMESTAMP, java.sql.Timestamp.class);
		this.modelTypeMap.put(Types.TINYINT, Byte.class);
		this.modelTypeMap.put(Types.VARBINARY, java.io.File.class);
		this.modelTypeMap.put(Types.VARCHAR, String.class);
	}

	public PrimitiveModelSource getPrimitiveModelSource()
	{
		return primitiveModelSource;
	}

	public void setPrimitiveModelSource(PrimitiveModelSource primitiveModelSource)
	{
		this.primitiveModelSource = primitiveModelSource;
	}

	public Map<Integer, Class<?>> getModelTypeMap()
	{
		return modelTypeMap;
	}

	public void setModelTypeMap(Map<Integer, Class<?>> modelTypeMap)
	{
		this.modelTypeMap = modelTypeMap;
	}

	/**
	 * 设置字面类型映射表。
	 * <p>
	 * 它的主键是{@linkplain Types}类的JDBC类型字段名，此方法会自动将它们转换为对应的字段值。
	 * </p>
	 * <p>
	 * 此方法可以在XML配置中使用，使其更友好。
	 * </p>
	 * 
	 * @param literalModelTypeMap
	 */
	public void setLiteralModelTypeMap(Map<String, Class<?>> literalModelTypeMap)
	{
		Map<Integer, Class<?>> jdbcTypeMap = literalMapToValueMap(literalModelTypeMap);
		this.modelTypeMap.putAll(jdbcTypeMap);
	}

	@Override
	public Model resolve(Connection cn, EntireTableInfo entireTableInfo, ColumnInfo columnInfo)
	{
		int jdbcType = columnInfo.getType();
		Class<?> modelType = this.modelTypeMap.get(jdbcType);

		Model model = (modelType == null ? null : this.primitiveModelSource.get(modelType));

		return model;
	}
}
