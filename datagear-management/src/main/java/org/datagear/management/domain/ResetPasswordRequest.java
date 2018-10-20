/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.domain;

import java.util.Date;

import org.datagear.model.support.AbstractStringIdEntity;

/**
 * 重设密码请求。
 * 
 * @author datagear@163.com
 *
 */
public class ResetPasswordRequest extends AbstractStringIdEntity
{
	private static final long serialVersionUID = 1L;

	/** 重设用户 */
	private User user;

	/** 重设密码 */
	private String password;

	/** 重设请求时间 */
	private Date time;

	/** 重设请求人（比如IP） */
	private String principal;

	public ResetPasswordRequest()
	{
		super();
	}

	public ResetPasswordRequest(String id, User user, String password, Date time, String principal)
	{
		super(id);
		this.user = user;
		this.password = password;
		this.time = time;
		this.principal = principal;
	}

	public User getUser()
	{
		return user;
	}

	public void setUser(User user)
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

	public Date getTime()
	{
		return time;
	}

	public void setTime(Date time)
	{
		this.time = time;
	}

	public String getPrincipal()
	{
		return principal;
	}

	public void setPrincipal(String principal)
	{
		this.principal = principal;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [id= " + getId() + ", user=" + user + ", password=" + password + ", time="
				+ time + ", principal=" + principal + "]";
	}
}
