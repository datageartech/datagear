/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.management.domain;

import java.io.Serializable;

/**
 * 全局设置。
 * 
 * @author datagear@163.com
 *
 */
public class GlobalSetting implements Serializable
{
	private static final long serialVersionUID = 1L;

	private SmtpSetting smtpSetting;

	public GlobalSetting()
	{
		super();
	}

	public boolean hasSmtpSetting()
	{
		return (this.smtpSetting != null);
	}

	public SmtpSetting getSmtpSetting()
	{
		return smtpSetting;
	}

	public void setSmtpSetting(SmtpSetting smtpSetting)
	{
		this.smtpSetting = smtpSetting;
	}
}
