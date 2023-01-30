/*
 * Copyright 2018-2023 datagear.tech
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

package org.datagear.connection;

import java.sql.Connection;
import java.sql.SQLException;

import org.datagear.util.JdbcUtil;

/**
 * 使用URL进行判断的{@linkplain ConnectionSensor}。
 * 
 * @author datagear@163.com
 *
 */
public class URLConnectionSensor implements ConnectionSensor
{
	private URLSensor urlSensor;

	public URLConnectionSensor()
	{
		super();
	}

	public URLConnectionSensor(URLSensor urlSensor)
	{
		super();
		this.urlSensor = urlSensor;
	}

	public URLSensor getUrlSensor()
	{
		return urlSensor;
	}

	public void setUrlSensor(URLSensor urlSensor)
	{
		this.urlSensor = urlSensor;
	}

	@Override
	public boolean supports(Connection cn)
	{
		String url = getURL(cn);

		return (url == null ? false : this.urlSensor.supports(url));
	}

	/**
	 * 获取指定{@linkplain Connection}的连接URL。
	 * <p>
	 * 如果出现{@linkplain SQLException}，此方法将返回{@code null}。
	 * </p>
	 * 
	 * @param cn
	 * @return
	 */
	protected String getURL(Connection cn)
	{
		return JdbcUtil.getURLIfSupports(cn);
	}
}
