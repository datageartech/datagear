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

package org.datagear.persistence.support.dialect;

import java.sql.Connection;

import org.datagear.connection.URLSensor;
import org.datagear.connection.support.OracleURLSensor;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.DialectBuilder;
import org.datagear.persistence.support.AbstractURLSensedDialectBuilder;

/**
 * Oracle的{@linkplain DialectBuilder}。
 * 
 * @author datagear@163.com
 *
 */
public class OracleDialectBuilder extends AbstractURLSensedDialectBuilder
{
	public OracleDialectBuilder()
	{
		super(OracleURLSensor.INSTANCE);
	}

	@Override
	public void setUrlSensor(URLSensor urlSensor)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Dialect build(Connection cn)
	{
		return new OracleDialect(getIdentifierQuote(cn));
	}
}
