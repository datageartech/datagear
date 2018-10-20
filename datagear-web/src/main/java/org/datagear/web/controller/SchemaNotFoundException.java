/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import org.datagear.management.domain.Schema;

/**
 * 未找到指定{@linkplain Schema}异常。
 * 
 * @author datagear@163.com
 *
 */
public class SchemaNotFoundException extends ControllerException
{
	private static final long serialVersionUID = 1L;

	private String schemaId;

	public SchemaNotFoundException(String schemaId)
	{
		this(schemaId, null);
	}

	public SchemaNotFoundException(String schemaId, Throwable cause)
	{
		super("No Schema found for id [" + schemaId + "]", cause);
	}

	public String getSchemaId()
	{
		return schemaId;
	}
}
