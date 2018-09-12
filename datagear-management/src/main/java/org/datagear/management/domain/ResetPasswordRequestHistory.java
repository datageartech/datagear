/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.management.domain;

import java.util.Date;

import org.datagear.model.support.Entity;

/**
 * 重设密码请求历史。
 * 
 * @author datagear@163.com
 *
 */
public class ResetPasswordRequestHistory implements Entity<String>
{
	private static final long serialVersionUID = 1L;

	private ResetPasswordRequest resetPasswordRequest;

	/** 生效时间 */
	private Date effectiveTime;

	/** 此记录创建时间 */
	private Date createTime;

	public ResetPasswordRequestHistory()
	{
		super();
	}

	public ResetPasswordRequestHistory(ResetPasswordRequest resetPasswordRequest, Date effectiveTime, Date createTime)
	{
		super();
		this.resetPasswordRequest = resetPasswordRequest;
		this.effectiveTime = effectiveTime;
		this.createTime = createTime;
	}

	public ResetPasswordRequest getResetPasswordRequest()
	{
		return resetPasswordRequest;
	}

	public void setResetPasswordRequest(ResetPasswordRequest resetPasswordRequest)
	{
		this.resetPasswordRequest = resetPasswordRequest;
	}

	public Date getEffectiveTime()
	{
		return effectiveTime;
	}

	public void setEffectiveTime(Date effectiveTime)
	{
		this.effectiveTime = effectiveTime;
	}

	public Date getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(Date createTime)
	{
		this.createTime = createTime;
	}

	@Override
	public String getId()
	{
		return this.resetPasswordRequest.getId();
	}

	@Override
	public void setId(String id)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resetPasswordRequest == null) ? 0 : resetPasswordRequest.hashCode());
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
		ResetPasswordRequestHistory other = (ResetPasswordRequestHistory) obj;
		if (resetPasswordRequest == null)
		{
			if (other.resetPasswordRequest != null)
				return false;
		}
		else if (!resetPasswordRequest.equals(other.resetPasswordRequest))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [resetPasswordRequest=" + resetPasswordRequest + ", effectiveTime="
				+ effectiveTime + ", createTime=" + createTime + "]";
	}
}
