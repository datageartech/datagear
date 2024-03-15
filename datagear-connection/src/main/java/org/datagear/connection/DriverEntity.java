/*
 * Copyright 2018-present datagear.tech
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

import java.io.Serializable;
import java.util.List;

/**
 * JDBC驱动程序实体。
 * 
 * @author datagear@163.com
 *
 */
public class DriverEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** ID */
	private String id;

	/** 驱动类名 */
	private String driverClassName;

	/** 展示名称 */
	private String displayName;

	/** 展示描述 */
	private String displayDesc;

	/** 驱动程序的最低JRE版本 */
	private String jreVersion;

	/** 驱动程序支持的数据库名称 */
	private String databaseName;

	/** 驱动程序支持的数据库版本 */
	private List<String> databaseVersions;

	public DriverEntity()
	{
		super();
	}

	public DriverEntity(String id, String driverClassName)
	{
		super();
		this.id = id;
		this.driverClassName = driverClassName;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getDriverClassName()
	{
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName)
	{
		this.driverClassName = driverClassName;
	}

	public boolean hasDisplayName()
	{
		return (this.displayName != null && !this.displayName.isEmpty());
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public boolean hasDisplayDesc()
	{
		return (this.displayDesc != null && !this.displayDesc.isEmpty());
	}

	public String getDisplayDesc()
	{
		return displayDesc;
	}

	public void setDisplayDesc(String displayDesc)
	{
		this.displayDesc = displayDesc;
	}

	public String getDisplayText()
	{
		if (hasDisplayName())
			return getDisplayName();
		else
			return getDriverClassName();
	}

	public boolean hasJreVersion()
	{
		return (this.jreVersion != null && !this.jreVersion.isEmpty());
	}

	public String getJreVersion()
	{
		return jreVersion;
	}

	public void setJreVersion(String jreVersion)
	{
		this.jreVersion = jreVersion;
	}

	public boolean hasDatabaseName()
	{
		return (this.databaseName != null && !this.databaseName.isEmpty());
	}

	public String getDatabaseName()
	{
		return databaseName;
	}

	public void setDatabaseName(String databaseName)
	{
		this.databaseName = databaseName;
	}

	public boolean hasDatabaseVersions()
	{
		return (this.databaseVersions != null && !this.databaseName.isEmpty());
	}

	public List<String> getDatabaseVersions()
	{
		return databaseVersions;
	}

	public void setDatabaseVersions(List<String> databaseVersions)
	{
		this.databaseVersions = databaseVersions;
	}

	public String getDisplayDescMore()
	{
		if (this.displayDesc != null && !this.displayDesc.isEmpty())
			return this.displayDesc;
		else
		{
			StringBuilder sb = new StringBuilder();

			if (this.databaseName != null && !this.databaseName.isEmpty())
			{
				if (sb.length() != 0)
					sb.append(", ");

				sb.append("DB: " + this.databaseName);
			}

			if (this.databaseVersions != null && !this.databaseVersions.isEmpty())
			{
				if (sb.length() != 0)
					sb.append(", ");

				sb.append("Versions: " + this.databaseVersions.toString());
			}

			if (this.jreVersion != null && !this.jreVersion.isEmpty())
			{
				if (sb.length() != 0)
					sb.append(", ");

				sb.append("JRE: " + this.jreVersion);
			}

			return sb.toString();
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		DriverEntity other = (DriverEntity) obj;
		if (id == null)
		{
			if (other.id != null)
				return false;
		}
		else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [id=" + id + ", driverClassName=" + driverClassName + ", displayName="
				+ displayName + ", displayDesc=" + displayDesc + ", jreVersion=" + jreVersion + ", databaseName="
				+ databaseName + ", databaseVersions=" + databaseVersions + "]";
	}

	/**
	 * 构建{@linkplain DriverEntity}。
	 * 
	 * @param id
	 * @param driverClassName
	 * @return
	 */
	public static DriverEntity valueOf(String id, String driverClassName)
	{
		return new DriverEntity(id, driverClassName);
	}
}
