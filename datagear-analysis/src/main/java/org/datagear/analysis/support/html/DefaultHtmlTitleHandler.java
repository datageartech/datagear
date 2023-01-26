/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import org.datagear.util.StringUtil;

/**
 * 默认{@linkplain HtmlTitleHandler}。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultHtmlTitleHandler implements HtmlTitleHandler
{
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
