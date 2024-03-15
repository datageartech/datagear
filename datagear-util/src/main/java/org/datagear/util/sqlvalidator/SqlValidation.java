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
