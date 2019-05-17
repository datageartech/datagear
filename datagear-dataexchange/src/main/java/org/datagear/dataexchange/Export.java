/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.sql.Connection;

import org.datagear.model.Model;

/**
 * 导出端。
 * 
 * @author datagear@163.com
 *
 */
public abstract class Export
{
	private Connection connection;

	private Model model;

	public Export()
	{
		super();
	}

	public Export(Connection connection, Model model)
	{
		super();
		this.connection = connection;
		this.model = model;
	}

	public Connection getConnection()
	{
		return connection;
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	public Model getModel()
	{
		return model;
	}

	public void setModel(Model model)
	{
		this.model = model;
	}
}
