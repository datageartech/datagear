/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
