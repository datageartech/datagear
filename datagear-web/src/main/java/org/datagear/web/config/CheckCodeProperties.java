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

package org.datagear.web.config;

import java.awt.Font;
import java.io.Serializable;

/**
 * 校验码配置。
 * 
 * @author datagear@163.com
 *
 */
public class CheckCodeProperties implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * 候选字体名，为空表示自动计算
	 */
	private String[] fontNames = new String[0];

	/**
	 * 字体类型：{@linkplain Font#PLAIN}、{@linkplain Font#BOLD}、{@linkplain Font#ITALIC}
	 */
	private int fontStyle = Font.BOLD;

	/**
	 * 字体尺寸
	 */
	private int fontSize = 16;

	/**
	 * 校验码图片宽度，小于{@code 0}表示自动计算
	 */
	private int imageWidth = -1;

	/**
	 * 校验码图片高度，小于{@code 0}表示自动计算
	 */
	private int imageHeight = -1;

	/**
	 * 校验码图片中校验码的X坐标，小于{@code 0}表示自动计算
	 */
	private int codeLeft = -1;

	/**
	 * 校验码图片中校验码的Y坐标，小于{@code 0}表示自动计算
	 */
	private int codeTop = -1;

	/**
	 * 校验码图片格式名称
	 */
	private String imageFormatName = "png";

	public CheckCodeProperties()
	{
		super();
	}

	public String[] getFontNames()
	{
		return fontNames;
	}

	public void setFontNames(String[] fontNames)
	{
		this.fontNames = fontNames;
	}

	public int getFontStyle()
	{
		return fontStyle;
	}

	public void setFontStyle(int fontStyle)
	{
		this.fontStyle = fontStyle;
	}

	public int getFontSize()
	{
		return fontSize;
	}

	public void setFontSize(int fontSize)
	{
		this.fontSize = fontSize;
	}

	public int getImageWidth()
	{
		return imageWidth;
	}

	public void setImageWidth(int imageWidth)
	{
		this.imageWidth = imageWidth;
	}

	public int getImageHeight()
	{
		return imageHeight;
	}

	public void setImageHeight(int imageHeight)
	{
		this.imageHeight = imageHeight;
	}

	public int getCodeLeft()
	{
		return codeLeft;
	}

	public void setCodeLeft(int codeLeft)
	{
		this.codeLeft = codeLeft;
	}

	public int getCodeTop()
	{
		return codeTop;
	}

	public void setCodeTop(int codeTop)
	{
		this.codeTop = codeTop;
	}

	public String getImageFormatName()
	{
		return imageFormatName;
	}

	public void setImageFormatName(String imageFormatName)
	{
		this.imageFormatName = imageFormatName;
	}
}
