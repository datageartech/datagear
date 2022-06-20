/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.sqlvalidator;

import java.io.Serializable;

/**
 * SQL校验结果。
 * 
 * @author datagear@163.com
 *
 */
public class SqlValidation implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 是否验证通过 */
	private boolean valid;

	/** 验证未通过的SQL内容 */
	private String invalidValue;

	public SqlValidation(boolean valid)
	{
		super();
		this.valid = valid;
	}

	public SqlValidation(String invalidValue)
	{
		super();
		this.valid = false;
		this.invalidValue = invalidValue;
	}

	public boolean isValid()
	{
		return valid;
	}

	public void setValid(boolean valid)
	{
		this.valid = valid;
	}

	public String getInvalidValue()
	{
		return invalidValue;
	}

	public void setInvalidValue(String invalidValue)
	{
		this.invalidValue = invalidValue;
	}

	public String getInvalidMessage()
	{
		return "Invalid SQL : " + getInvalidValue();
	}
}
