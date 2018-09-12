/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.vo;

import java.io.Serializable;

/**
 * 通知。
 * 
 * @author datagear@163.com
 *
 */
public class Notification implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String content;

	public Notification()
	{
		super();
	}

	public Notification(String content)
	{
		super();
		this.content = content;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [content=" + content + "]";
	}
}
