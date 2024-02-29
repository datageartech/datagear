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

package org.datagear.dataexchange;

import java.util.Set;

import org.datagear.util.resource.ConnectionFactory;

/**
 * 简单{@linkplain BatchDataExchange}。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleBatchDataExchange extends BatchDataExchange
{
	private Set<SubDataExchange> subDataExchanges;

	public SimpleBatchDataExchange()
	{
		super();
	}

	public SimpleBatchDataExchange(ConnectionFactory connectionFactory, Set<SubDataExchange> subDataExchanges)
	{
		super(connectionFactory);
		this.subDataExchanges = subDataExchanges;
	}

	@Override
	public Set<SubDataExchange> getSubDataExchanges()
	{
		return subDataExchanges;
	}

	public void setSubDataExchanges(Set<SubDataExchange> subDataExchanges)
	{
		this.subDataExchanges = subDataExchanges;
	}
}
