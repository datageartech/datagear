/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.convert;

/**
 * 非法原值转换异常。
 * 
 * @author datagear@163.com
 *
 */
public class IllegalSourceValueException extends ConverterException
{
	private static final long serialVersionUID = 1L;

	private String propertyPath;

	private Object sourceValue;

	private Class<?> targetType;

	public IllegalSourceValueException(String propertyPath, Object sourceValue, Class<?> targetType)
	{
		super();
		this.propertyPath = propertyPath;
		this.sourceValue = sourceValue;
		this.targetType = targetType;
	}

	public String getPropertyPath()
	{
		return propertyPath;
	}

	public void setPropertyPath(String propertyPath)
	{
		this.propertyPath = propertyPath;
	}

	public Object getSourceValue()
	{
		return sourceValue;
	}

	public void setSourceValue(Object sourceValue)
	{
		this.sourceValue = sourceValue;
	}

	public Class<?> getTargetType()
	{
		return targetType;
	}

	public void setTargetType(Class<?> targetType)
	{
		this.targetType = targetType;
	}
}
