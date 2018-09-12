/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.model.support;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.datagear.model.PropertyAccessException;

/**
 * Bean属性值访问器。
 * 
 * @author datagear@163.com
 *
 */
public class BeanPropertyValueAccessor implements PropertyValueAccessor
{
	protected static final Object[] EMPTY_OBJS_ARG = new Object[0];

	/** 读方法 */
	private Method readMethod;

	/** 写方法 */
	private Method writeMethod;

	/** 字段 */
	private Field field;

	public BeanPropertyValueAccessor()
	{
		super();
	}

	public BeanPropertyValueAccessor(Field field)
	{
		super();
		this.field = field;
	}

	public BeanPropertyValueAccessor(Method readMethod, Method writeMethod)
	{
		super();
		this.readMethod = readMethod;
		this.writeMethod = writeMethod;
	}

	public Method getReadMethod()
	{
		return readMethod;
	}

	public void setReadMethod(Method readMethod)
	{
		this.readMethod = readMethod;
	}

	public Method getWriteMethod()
	{
		return writeMethod;
	}

	public void setWriteMethod(Method writeMethod)
	{
		this.writeMethod = writeMethod;
	}

	public Field getField()
	{
		return field;
	}

	public void setField(Field field)
	{
		this.field = field;
	}

	@Override
	public Object get(Object obj) throws PropertyAccessException
	{
		if (readMethod != null)
		{
			if (!readMethod.isAccessible())
				readMethod.setAccessible(true);

			try
			{
				return readMethod.invoke(obj, EMPTY_OBJS_ARG);
			}
			catch (Exception e)
			{
				throw new PropertyAccessException(e);
			}
		}
		else if (field != null)
		{
			if (!field.isAccessible())
				field.setAccessible(true);

			try
			{
				return field.get(obj);
			}
			catch (Exception e)
			{
				throw new PropertyAccessException(e);
			}
		}
		else
			throw new IllegalStateException("Null [readMethod] and [field], can not read property value");
	}

	@Override
	public void set(Object obj, Object propertyValue) throws PropertyAccessException
	{
		if (writeMethod != null)
		{
			if (!writeMethod.isAccessible())
				writeMethod.setAccessible(true);

			try
			{
				writeMethod.invoke(obj, propertyValue);
			}
			catch (Exception e)
			{
				throw new PropertyAccessException(e);
			}
		}
		else if (field != null)
		{
			if (!field.isAccessible())
				field.setAccessible(true);

			try
			{
				field.set(obj, propertyValue);
			}
			catch (Exception e)
			{
				throw new PropertyAccessException(e);
			}
		}
		else
			throw new IllegalStateException("Null [readMethod] and [field], can not read property value");
	}

}
