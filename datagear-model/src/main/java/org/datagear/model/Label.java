/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model;

import java.util.Locale;
import java.util.Map;

/**
 * 标签。
 * 
 * @author datagear@163.com
 *
 */
public class Label extends org.datagear.util.i18n.Label
{
	private static final long serialVersionUID = 1L;

	public Label()
	{
		super();
	}

	public Label(String value)
	{
		super(value);
	}

	public Label(String value, Map<Locale, String> localeValues)
	{
		super(value, localeValues);
	}
}
