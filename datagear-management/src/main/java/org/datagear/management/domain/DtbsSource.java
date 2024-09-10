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

package org.datagear.management.domain;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.datagear.analysis.support.JsonSupport;
import org.datagear.connection.DriverEntity;
import org.datagear.util.StringUtil;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 数据源实体。
 * 
 * @author datagear@163.com
 *
 */
public class DtbsSource extends AbstractStringIdEntity
		implements CreateUserEntity, DataPermissionEntity, CloneableEntity
{
	private static final long serialVersionUID = 1L;

	/** 授权资源类型 */
	public static final String AUTHORIZATION_RESOURCE_TYPE = "DATA_SOURCE";

	/*------------------------------------------------------*/
	/*
	 * 从业务角度看，对数据源的授权不应是对其记录本身，而是它包含表中的数据。
	 * 所以，这里扩展了Authorization.PERMISSION_READ_START权限， 授权时，仅支持对数据源授予下面这些权限。
	 * 这样，即不会暴露数据源记录本身的编辑、删除权限，同时又能满足业务需求。
	 */

	/** 数据源内的表数据权限：读取 */
	public static final int PERMISSION_TABLE_DATA_READ = Authorization.PERMISSION_READ_TRANSFER_READ;

	/** 数据源内的表数据权限：编辑 */
	public static final int PERMISSION_TABLE_DATA_EDIT = Authorization.PERMISSION_READ_TRANSFER_EDIT;

	/** 数据源内的表数据权限：删除 */
	public static final int PERMISSION_TABLE_DATA_DELETE = Authorization.PERMISSION_READ_TRANSFER_DELETE;

	/*------------------------------------------------------*/

	public static final String PROPERTY_TITLE = "title";

	/** 标题 */
	private String title;

	/** 连接URL */
	private String url;

	/** 连接用户 */
	private String user;

	/** 连接密码 */
	private String password;

	/** 模式名，留空则默认 */
	private String schemaName = null;

	/** 此模式的创建用户 */
	private User createUser;

	/** 此模式的创建时间 */
	private Date createTime = null;

	/** 数据库驱动程序路径名 */
	private DriverEntity driverEntity = null;
	
	/**属性列表*/
	private List<DtbsSourceProperty> properties = null;

	/** 权限 */
	private int dataPermission = PERMISSION_NOT_LOADED;

	public DtbsSource()
	{
		super();
	}

	public DtbsSource(String id, String title, String url, String user, String password)
	{
		super(id);
		this.title = title;
		this.url = url;
		this.user = user;
		this.password = password;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getSchemaName()
	{
		return schemaName;
	}

	public void setSchemaName(String schemaName)
	{
		this.schemaName = schemaName;
	}

	public boolean hasCreateUser()
	{
		return (this.createUser != null);
	}

	@Override
	public User getCreateUser()
	{
		return createUser;
	}

	@Override
	public void setCreateUser(User createUser)
	{
		this.createUser = createUser;
	}

	@Override
	public Date getCreateTime()
	{
		return createTime;
	}

	@Override
	public void setCreateTime(Date createTime)
	{
		this.createTime = createTime;
	}
	
	public boolean hasProperty()
	{
		return (this.properties != null && !this.properties.isEmpty());
	}

	public List<DtbsSourceProperty> getProperties()
	{
		return properties;
	}

	public void setProperties(List<DtbsSourceProperty> properties)
	{
		this.properties = properties;
	}

	public boolean hasDriverEntity()
	{
		if (this.driverEntity == null)
			return false;

		String driverEntityId = this.driverEntity.getId();

		return (driverEntityId != null && !driverEntityId.isEmpty());
	}

	public DriverEntity getDriverEntity()
	{
		return driverEntity;
	}

	public void setDriverEntity(DriverEntity driverEntity)
	{
		this.driverEntity = driverEntity;
	}

	@Override
	public int getDataPermission()
	{
		return dataPermission;
	}

	@Override
	public void setDataPermission(int dataPermission)
	{
		this.dataPermission = dataPermission;
	}

	/**
	 * 返回{@linkplain #getProperties()}的JSON。
	 * 
	 * @return
	 */
	@JsonIgnore
	public String getPropertiesJson()
	{
		if (this.properties == null)
			return "[]";

		return JsonSupport.generate(this.properties, "[]");
	}

	/**
	 * 设置{@linkplain #setProperties(List)}的JSON。
	 * 
	 * @param json
	 */
	public void setPropertiesJson(String json)
	{
		if (StringUtil.isEmpty(json))
		{
			setProperties(null);
		}
		else
		{
			DtbsSourceProperty[] properties = JsonSupport.parse(json, DtbsSourceProperty[].class, null);
			setProperties(Arrays.asList(properties));
		}
	}

	@Override
	public DtbsSource clone()
	{
		DtbsSource entity = new DtbsSource();
		BeanUtils.copyProperties(this, entity);

		return entity;
	}

	/**
	 * 清除敏感信息，包括：
	 * <p>
	 * {@linkplain #getUrl()}、{@linkplain #getUser()}、{@linkplain #getPassword()}、
	 * {@linkplain #getProperties()}、{@linkplain #getSchemaName()}。
	 * </p>
	 */
	public void clearSensitiveInfo()
	{
		// 敏感信息不应传输至客户端
		setUrl(null);
		setUser(null);
		setPassword(null);
		setProperties(null);
		setSchemaName(null);
	}

	/**
	 * 清除敏感信息：密码。
	 */
	public void clearPassword()
	{
		this.password = null;
	}

	public static boolean isReadTableDataPermission(int permission)
	{
		return permission >= PERMISSION_TABLE_DATA_READ && permission < PERMISSION_TABLE_DATA_EDIT;
	}

	public static boolean isEditTableDataPermission(int permission)
	{
		return permission >= PERMISSION_TABLE_DATA_EDIT && permission < PERMISSION_TABLE_DATA_DELETE;
	}

	public static boolean isDeleteTableDataPermission(int permission)
	{
		return permission >= PERMISSION_TABLE_DATA_DELETE;
	}

	public static boolean canReadTableData(int permission)
	{
		return permission >= PERMISSION_TABLE_DATA_READ;
	}

	public static boolean canEditTableData(int permission)
	{
		return permission >= PERMISSION_TABLE_DATA_EDIT;
	}

	public static boolean canDeleteTableData(int permission)
	{
		return permission >= PERMISSION_TABLE_DATA_DELETE;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [title=" + title + ", url=" + url + ", user=" + user + ", schemaName="
				+ schemaName + ", createUser=" + createUser + ", createTime=" + createTime + ", driverEntity="
				+ driverEntity + "]";
	}
}
