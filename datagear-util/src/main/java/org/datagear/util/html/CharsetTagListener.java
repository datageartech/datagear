/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.html;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

/**
 * 解析HTML字符集的{@linkplain TagListener}。
 * <p>
 * 此类从HTML的<code>&lt;head&gt;...&lt;/head&gt;</code>标签中的 <br>
 * <code>&lt;meta charset=字符集&gt;</code><br>
 * 或者<br>
 * <code>&lt;meta content="...; charset=字符集; ..."&gt;</code><br>
 * 中解析字符集。
 * </p>
 * <p>
 * 在调用完{@linkplain HtmlFilter#filter(Reader, Writer, TagListener)}后，可通过{@linkplain #getCharset()}获取解析的字符集。
 * </p>
 * <p>
 * {@linkplain #setAbortIfResolved(boolean)}可设置解析到字符集后中止处理。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class CharsetTagListener extends DefaultTagListener
{
	/** 是否解析了字符集后中止 */
	private boolean abortIfResolved = false;

	/** 解析的字符集 */
	private String charset = null;

	public CharsetTagListener()
	{
		super();
	}

	public CharsetTagListener(boolean abortIfResolved)
	{
		super();
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
	public boolean isResolveTagAttrs(Reader in, Writer out, String tagName)
	{
		return "meta".equalsIgnoreCase(tagName);
	}

	@Override
	public void beforeTagEnd(Reader in, Writer out, String tagName, String tagEnd,
			Map<String, String> attrs) throws IOException
	{
		if ("meta".equalsIgnoreCase(tagName))
		{
			for(Map.Entry<String, String> entry : attrs.entrySet())
			{
				String name = entry.getKey();
				
				if("charset".equalsIgnoreCase(name))
				{
					this.charset = entry.getValue();
					break;
				}
				else if ("content".equalsIgnoreCase(name))
				{
					String value = entry.getValue();

					if (value != null)
					{
						String valueLower = value.toLowerCase();
						String token = "charset=";
						int tokenIdx = valueLower.indexOf(token);
						if (tokenIdx > -1)
						{
							this.charset = value.substring(tokenIdx + token.length());
							int stopIdx = this.charset.indexOf(';');
							if (stopIdx > 0)
								this.charset = this.charset.substring(0, stopIdx);

							break;
						}
					}
				}
			}
		}
	}

	@Override
	public boolean afterTagEnd(Reader in, Writer out, String tagName, String tagEnd) throws IOException
	{
		if (abortIfResolved && (this.charset != null || "/head".equalsIgnoreCase(tagName)))
			return true;
		else
			return false;
	}
}
