/*
 * Copyright 2018-2024 datagear.tech
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
