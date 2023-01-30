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

package org.datagear.util.html;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

/**
 * 可识别<code>"&lt;head&gt;&lt;/head&gt;"</code>、<code>"&lt;body&gt;&lt;/body&gt;"</code>标签范围的{@linkplain FilterHandler}。
 * <p>
 * 子类在应在重写方法内的适当位置调用{@linkplain #beforeWriteTagStart(Reader, String)}、{@linkplain #afterWriteTagEnd(Reader, String, String)}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class HeadBodyAwareFilterHandler extends DefaultFilterHandler
{
	private boolean inHeadTag = false;

	private boolean inBodyTag = false;

	public HeadBodyAwareFilterHandler()
	{
		super();
	}

	public HeadBodyAwareFilterHandler(Writer out)
	{
		super(out);
	}

	/**
	 * 是否在<code>&lt;head&gt;</code>与<code>&lt;/head&gt;</code>之间。
	 * 
	 * @return
	 */
	public boolean isInHeadTag()
	{
		return inHeadTag;
	}

	protected void setInHeadTag(boolean inHeadTag)
	{
		this.inHeadTag = inHeadTag;
		this.onSetInHeadTag(inHeadTag);
	}

	/**
	 * 是否在<code>&lt;body&gt;</code>与<code>&lt;/body&gt;</code>之间。
	 * 
	 * @return
	 */
	public boolean isInBodyTag()
	{
		return inBodyTag;
	}

	protected void setInBodyTag(boolean inBodyTag)
	{
		this.inBodyTag = inBodyTag;
		this.onSetInBodyTag(inBodyTag);
	}

	@Override
	public void beforeWriteTagStart(Reader in, String tagName) throws IOException
	{
		if (this.inHeadTag && !this.inBodyTag && equalsIgnoreCase(tagName, "/head"))
		{
			this.setInHeadTag(false);
		}
		else if (!this.inHeadTag && this.inBodyTag && equalsIgnoreCase(tagName, "/body"))
		{
			this.setInBodyTag(false);
		}
	}

	@Override
	public void afterWriteTagEnd(Reader in, String tagName, String tagEnd, Map<String, String> attrs) throws IOException
	{
		if (!this.inHeadTag && !this.inBodyTag && equalsIgnoreCase(tagName, "head"))
		{
			this.setInHeadTag(!isSelfCloseTagEnd(tagEnd));
		}
		else if (!this.inHeadTag && !this.inBodyTag && equalsIgnoreCase(tagName, "body"))
		{
			this.setInBodyTag(!isSelfCloseTagEnd(tagEnd));
		}
	}

	/**
	 * 设置是否在<code>&lt;head&gt;</code>与<code>&lt;/head&gt;</code>之间的回调方法。
	 * <p>
	 * 默认实空方法，子类可以重写以实现特定逻辑。
	 * </p>
	 * 
	 * @param in
	 */
	protected void onSetInHeadTag(boolean in)
	{

	}

	/**
	 * 设置是否在<code>&lt;body&gt;</code>与<code>&lt;/body&gt;</code>之间的回调方法。
	 * <p>
	 * 默认实空方法，子类可以重写以实现特定逻辑。
	 * </p>
	 * 
	 * @param in
	 */
	protected void onSetInBodyTag(boolean in)
	{

	}
}
