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

package org.datagear.util.html;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

/**
 * 解析HTML字符集的{@linkplain FilterHandler}。
 * <p>
 * 此类从HTML的<code>&lt;head&gt;...&lt;/head&gt;</code>标签中 <br>
 * <code>&lt;meta charset=字符集&gt;</code><br>
 * 或者<br>
 * <code>&lt;meta content="...; charset=字符集; ..."&gt;</code><br>
 * 中解析字符集。
 * </p>
 * <p>
 * {@linkplain #setAbortIfResolved(boolean)}可设置解析到字符集后中止处理。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class CharsetFilterHandler extends DefaultFilterHandler
{
	/** 是否解析了字符集后中止 */
	private boolean abortIfResolved = false;

	/** 解析的字符集 */
	private String charset = null;

	private boolean _aborted = false;

	/**
	 * 创建{@linkplain CharsetFilterHandler}，{@linkplain #getOut()}为{@linkplain NopWriter}，
	 * {@linkplain #isAbortIfResolved()}为{@code true}
	 */
	public CharsetFilterHandler()
	{
		this(NopWriter.NOP_WRITER);
		this.abortIfResolved = true;
	}

	public CharsetFilterHandler(Writer out)
	{
		super(out);
	}

	public CharsetFilterHandler(Writer out, boolean abortIfResolved)
	{
		super(out);
		this.abortIfResolved = abortIfResolved;
	}

	public boolean isAbortIfResolved()
	{
		return abortIfResolved;
	}

	public void setAbortIfResolved(boolean abortIfResolved)
	{
		this.abortIfResolved = abortIfResolved;
	}

	/**
	 * 获取解析的字符集。
	 * 
	 * @return 可能为{@code null}
	 */
	public String getCharset()
	{
		return (this.charset == null ? null : this.charset.trim());
	}

	@Override
	public boolean isAborted()
	{
		return this._aborted;
	}

	@Override
	public boolean isResolveTagAttrs(Reader in, String tagName)
	{
		return equalsIgnoreCase(tagName, "meta");
	}

	@Override
	public void beforeWriteTagEnd(Reader in, String tagName, String tagEnd, Map<String, String> attrs)
			throws IOException
	{
		if (this.charset == null && equalsIgnoreCase(tagName, "meta"))
			this.charset = resolveCharset(attrs);
	}

	@Override
	public void afterWriteTagEnd(Reader in, String tagName, String tagEnd, Map<String, String> attrs) throws IOException
	{
		if (this.abortIfResolved && (this.charset != null || equalsIgnoreCase(tagName, "/head")))
			this._aborted = true;
	}

	/**
	 * 从属性集中解析字符集。
	 * 
	 * @param attrs
	 * @return 字符集字符串、{@code null}
	 */
	protected String resolveCharset(Map<String, String> attrs)
	{
		String charset = null;

		for (Map.Entry<String, String> entry : attrs.entrySet())
		{
			String name = entry.getKey();

			if (equalsIgnoreCase(name, "charset"))
			{
				charset = entry.getValue();
				break;
			}
			else if (equalsIgnoreCase(name, "content"))
			{
				String value = entry.getValue();

				if (value != null)
				{
					String valueLower = value.toLowerCase();
					String token = "charset=";
					int tokenIdx = valueLower.indexOf(token);
					if (tokenIdx > -1)
					{
						charset = value.substring(tokenIdx + token.length());
						int stopIdx = charset.indexOf(';');
						if (stopIdx > 0)
							charset = charset.substring(0, stopIdx);

						break;
					}
				}
			}
		}

		return charset;
	}
}
