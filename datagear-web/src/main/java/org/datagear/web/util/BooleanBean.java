package org.datagear.web.util;

import java.io.Serializable;

/**
 * 布尔值Bean。
 * 
 * @author datagear@163.com
 *
 */
public class BooleanBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	private boolean value = false;

	public BooleanBean()
	{
		super();
	}

	public BooleanBean(boolean value)
	{
		super();
		this.value = value;
	}

	public boolean isValue()
	{
		return value;
	}

	public void setValue(boolean value)
	{
		this.value = value;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [value=" + value + "]";
	}
}
