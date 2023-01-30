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

package org.datagear.util.sqlvalidator;

import java.sql.Connection;

import org.datagear.util.JdbcUtil;
import org.datagear.util.StringUtil;

/**
 * SQL校验数据库信息。
 * 
 * @author datagear@163.com
 *
 */
public class DatabaseProfile
{
	/** 名称 */
	private String name = null;

	/** 连接URL */
	private String url = null;

	/** 标识引用符 */
	private String identifierQuote = null;

	public DatabaseProfile()
	{
		super();
	}

	public DatabaseProfile(String name, String url, String identifierQuote)
	{
		super();
		this.name = name;
		this.url = url;
		this.identifierQuote = identifierQuote;
	}

	public boolean hasName()
	{
		return !StringUtil.isEmpty(this.name);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean hasUrl()
	{
		return !StringUtil.isEmpty(this.url);
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public boolean hasIdentifierQuote()
	{
		return !StringUtil.isEmpty(this.identifierQuote);
	}

	public String getIdentifierQuote()
	{
		return identifierQuote;
	}

	public void setIdentifierQuote(String identifierQuote)
	{
		this.identifierQuote = identifierQuote;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifierQuote == null) ? 0 : identifierQuote.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DatabaseProfile other = (DatabaseProfile) obj;
		if (identifierQuote == null)
		{
			if (other.identifierQuote != null)
				return false;
		}
		else if (!identifierQuote.equals(other.identifierQuote))
			return false;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (url == null)
		{
			if (other.url != null)
				return false;
		}
		else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", url=" + url + ", identifierQuote=" + identifierQuote
				+ "]";
	}

	/**
	 * 由数据库连接构建{@linkplain DatabaseProfile}。
	 * 
	 * @param cn
	 * @return
	 */
	public static DatabaseProfile valueOf(Connection cn)
	{
		DatabaseProfile p = new DatabaseProfile();

		p.setName(JdbcUtil.getDatabaseProductNameIfSupports(cn));
		p.setUrl(JdbcUtil.getURLIfSupports(cn));
		p.setIdentifierQuote(JdbcUtil.getIdentifierQuote(cn));

		return p;
	}
}
