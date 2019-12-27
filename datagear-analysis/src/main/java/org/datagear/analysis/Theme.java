/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

import java.io.Serializable;

/**
 * 主题。
 * 
 * @author datagear@163.com
 *
 */
public class Theme implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 背景色 */
	private String backgroundColor;

	/** 前景色 */
	private String foregroundColor;

	/** 边框宽度 */
	private String borderWidth = "0";

	/** 边框颜色 */
	private String borderColor;

	public Theme()
	{
		super();
	}

	public Theme(String backgroundColor, String foregroundColor)
	{
		super();
		this.backgroundColor = backgroundColor;
		this.foregroundColor = foregroundColor;
	}

	public String getBackgroundColor()
	{
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor)
	{
		this.backgroundColor = backgroundColor;
	}

	public String getForegroundColor()
	{
		return foregroundColor;
	}

	public void setForegroundColor(String foregroundColor)
	{
		this.foregroundColor = foregroundColor;
	}

	public String getBorderColor()
	{
		return borderColor;
	}

	public void setBorderColor(String borderColor)
	{
		this.borderColor = borderColor;
	}

	public String getBorderWidth()
	{
		return borderWidth;
	}

	public void setBorderWidth(String borderWidth)
	{
		this.borderWidth = borderWidth;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [backgroundColor=" + backgroundColor + ", foregroundColor="
				+ foregroundColor + ", borderWidth=" + borderWidth + ", borderColor=" + borderColor + "]";
	}
}
