/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.domain;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.springframework.beans.BeanUtils;

/**
 * 用户实体。
 * 
 * @author datagear@163.com
 *
 */
public class User extends AbstractStringIdEntity implements CloneableEntity
{
	private static final long serialVersionUID = 1L;

	/** 默认管理员ID */
	public static final String ADMIN_USER_ID = "admin";

	/** 登录名 */
	private String name;

	/** 登录密码 */
	private String password;

	/** 姓名 */
	private String realName = "";

	/** 邮箱 */
	private String email = "";

	/** 是否管理员 */
	private boolean admin = false;

	/** 是否是匿名用户 */
	private boolean anonymous = false;

	/** 此模式的创建时间 */
	private Date createTime = new Date();

	/** 角色集 */
	private Set<Role> roles = Collections.emptySet();

	public User()
	{
		super();
	}

	public User(String id)
	{
		super(id);
	}

	public User(String id, String name, String password)
	{
		super(id);
		this.name = name;
		this.password = password;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getRealName()
	{
		return realName;
	}

	public void setRealName(String realName)
	{
		this.realName = realName;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public boolean isAdmin()
	{
		return admin;
	}

	public void setAdmin(boolean admin)
	{
		this.admin = admin;
	}

	public boolean isAnonymous()
	{
		return anonymous;
	}

	public void setAnonymous(boolean anonymous)
	{
		this.anonymous = anonymous;
	}

	public Date getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(Date createTime)
	{
		this.createTime = createTime;
	}

	public Set<Role> getRoles()
	{
		return roles;
	}

	public void setRoles(Set<Role> roles)
	{
		this.roles = roles;
	}

	/**
	 * 获取账号名称标签。
	 * 
	 * @return
	 */
	public String getNameLabel()
	{
		String nameLabel = "";

		if (this.realName != null && !this.realName.isEmpty())
			nameLabel += this.realName;

		if (this.name != null && !this.name.isEmpty())
		{
			if (nameLabel.isEmpty())
				nameLabel = this.name;
			else
				nameLabel = nameLabel + "(" + this.name + ")";
		}

		return nameLabel;
	}

	@Override
	public User clone()
	{
		User entity = new User();
		BeanUtils.copyProperties(this, entity);

		return entity;
	}

	/**
	 * 拷贝对象，排除密码。
	 * 
	 * @return
	 */
	public User cloneWithoutPassword()
	{
		User entity = clone();
		entity.setPassword(null);

		return entity;
	}

	/**
	 * 是否是内置管理员账号。
	 * 
	 * @param user
	 * @return
	 */
	public static boolean isAdminUser(User user)
	{
		return ADMIN_USER_ID.equals(user.getId());
	}

	/**
	 * 是否是内置管理员账号。
	 * 
	 * @param userId
	 * @return
	 */
	public static boolean isAdminUser(String userId)
	{
		return ADMIN_USER_ID.equals(userId);
	}
}
