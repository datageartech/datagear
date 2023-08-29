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

package org.datagear.analysis.support.html;

import java.io.Serializable;

import org.datagear.util.StringUtil;

/**
 * 默认{@linkplain HtmlTitleHandler}。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultHtmlTitleHandler implements HtmlTitleHandler, Serializable
{
	private static final long serialVersionUID = 1L;

	private String suffix = "";

	private String suffixForBlank = "";

	public DefaultHtmlTitleHandler()
	{
		super();
	}

	public DefaultHtmlTitleHandler(String suffix)
	{
		super();
		this.suffix = suffix;
	}

	public DefaultHtmlTitleHandler(String suffix, String suffixForBlank)
	{
		super();
		this.suffix = suffix;
		this.suffixForBlank = suffixForBlank;
	}

	public String getSuffix()
	{
		return suffix;
	}

	public void setSuffix(String suffix)
	{
		this.suffix = suffix;
	}

	public String getSuffixForBlank()
	{
		return suffixForBlank;
	}

	public void setSuffixForBlank(String suffixForBlank)
	{
		this.suffixForBlank = suffixForBlank;
	}

	@Override
	public String suffix(String title)
	{
		String suffix = this.suffix;

		if (StringUtil.isBlank(title) && !StringUtil.isEmpty(this.suffixForBlank))
			suffix = this.suffixForBlank;

		return suffix;
	}
}
