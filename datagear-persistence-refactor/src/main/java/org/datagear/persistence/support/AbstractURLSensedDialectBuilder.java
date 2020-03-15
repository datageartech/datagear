/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.sql.Connection;

import org.datagear.connection.URLConnectionSensor;
import org.datagear.connection.URLSensor;
import org.datagear.persistence.DialectBuilder;

/**
 * 抽象URL敏感的{@linkplain DialectBuilder}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractURLSensedDialectBuilder extends AbstractDialectBuilder
{
	private URLSensor urlSensor;

	private transient URLConnectionSensor _urlConnectionSensor;

	public AbstractURLSensedDialectBuilder()
	{
		super();
	}

	public AbstractURLSensedDialectBuilder(URLSensor urlSensor)
	{
		super();
		this.urlSensor = urlSensor;
		this._urlConnectionSensor = new URLConnectionSensor(this.urlSensor);
	}

	public URLSensor getUrlSensor()
	{
		return urlSensor;
	}

	public void setUrlSensor(URLSensor urlSensor)
	{
		this.urlSensor = urlSensor;
		this._urlConnectionSensor = new URLConnectionSensor(this.urlSensor);
	}

	@Override
	public boolean supports(Connection cn)
	{
		return this._urlConnectionSensor.supports(cn);
	}
}
