/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.model.support;

import java.util.HashMap;
import java.util.Map;

import org.datagear.model.Model;

/**
 * 默认{@linkplain PrimitiveModelSource}。
 * <p>
 * 此类会默认添加如下类型：
 * </p>
 * 
 * <pre>
 * Class.class
 * boolean.class
 * Boolean.class
 * byte.class
 * Byte.class
 * byte[].class
 * char.class
 * Character.class
 * double.class
 * Double.class
 * float.class
 * Float.class
 * int.class
 * Integer.class
 * long.class
 * Long.class
 * short.class
 * Short.class
 * String.class
 * java.math.BigDecimal.class
 * java.math.BigInteger.class
 * java.net.URI.class
 * java.net.URL.class
 * java.sql.Date.class
 * java.sql.Time.class
 * java.sql.Timestamp.class
 * java.util.Calendar.class
 * java.util.Date.class
 * java.util.GregorianCalendar.class
 * java.util.Locale.class
 * java.util.UUID.class
 * java.io.File.class
 * </pre>
 * 
 * @author datagear@163.com
 *
 */
public class DefaultPrimitiveModelSource implements PrimitiveModelSource
{
	private Map<Class<?>, Model> primitiveModels = new HashMap<Class<?>, Model>();

	private PrimitiveModelBuilder primitiveModelBuilder = new PrimitiveModelBuilder();

	public DefaultPrimitiveModelSource()
	{
		super();
		initDefaultPrimitiveModels();
	}

	public DefaultPrimitiveModelSource(Map<Class<?>, Model> primitiveModels)
	{
		super();
		initDefaultPrimitiveModels();
		this.primitiveModels.putAll(primitiveModels);
	}

	/**
	 * 初始化默认基本{@linkplain Model}。
	 */
	protected void initDefaultPrimitiveModels()
	{
		addPrimitiveModel(Class.class);

		addPrimitiveModel(boolean.class);
		addPrimitiveModel(Boolean.class);
		addPrimitiveModel(byte.class);
		addPrimitiveModel(Byte.class);
		addPrimitiveModel(byte[].class);
		addPrimitiveModel(char.class);
		addPrimitiveModel(Character.class);
		addPrimitiveModel(double.class);
		addPrimitiveModel(Double.class);
		addPrimitiveModel(float.class);
		addPrimitiveModel(Float.class);
		addPrimitiveModel(int.class);
		addPrimitiveModel(Integer.class);
		addPrimitiveModel(long.class);
		addPrimitiveModel(Long.class);
		addPrimitiveModel(short.class);
		addPrimitiveModel(Short.class);
		addPrimitiveModel(String.class);
		addPrimitiveModel(java.math.BigDecimal.class);
		addPrimitiveModel(java.math.BigInteger.class);
		addPrimitiveModel(java.net.URI.class);
		addPrimitiveModel(java.net.URL.class);
		addPrimitiveModel(java.sql.Date.class);
		addPrimitiveModel(java.sql.Time.class);
		addPrimitiveModel(java.sql.Timestamp.class);
		addPrimitiveModel(java.util.Calendar.class);
		addPrimitiveModel(java.util.Date.class);
		addPrimitiveModel(java.util.GregorianCalendar.class);
		addPrimitiveModel(java.util.Locale.class);
		addPrimitiveModel(java.util.UUID.class);
		addPrimitiveModel(java.io.File.class);
	}

	public PrimitiveModelBuilder getPrimitiveModelBuilder()
	{
		return primitiveModelBuilder;
	}

	public void setPrimitiveModelBuilder(PrimitiveModelBuilder primitiveModelBuilder)
	{
		this.primitiveModelBuilder = primitiveModelBuilder;
	}

	@Override
	public boolean contains(Class<?> type)
	{
		return this.primitiveModels.containsKey(type);
	}

	@Override
	public Model get(Class<?> type)
	{
		return this.primitiveModels.get(type);
	}

	@Override
	public Map<Class<?>, Model> toMap()
	{
		return new HashMap<Class<?>, Model>(this.primitiveModels);
	}

	/**
	 * 添加基本模型。
	 * 
	 * @param type
	 */
	protected void addPrimitiveModel(Class<?> type)
	{
		this.primitiveModels.put(type, this.primitiveModelBuilder.build(type));
	}
}
