/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dbmodel;

/**
 * 名字处理器支持类。
 * 
 * @author datagear@163.com
 *
 */
public class NameResolverSupport
{
	public NameResolverSupport()
	{
		super();
	}

	/**
	 * 作为标识符处理。
	 * <p>
	 * 标识符仅包含{@code 0-9}、{@code a-z}、{@code A-Z}和{@code '_'}字符，且不以数字开头。
	 * </p>
	 * <p>
	 * 此方法会保留合规的字符，对于不合规的字符，将转换为它的Unicode码值。
	 * </p>
	 * 
	 * @param name
	 * @return
	 */
	public String resolveForIndentifier(String name)
	{
		if (name == null || name.isEmpty())
			throw new IllegalArgumentException("[name] must not be null nor empty");

		StringBuilder sb = new StringBuilder();

		char[] cs = name.toCharArray();

		for (int i = 0; i < cs.length; i++)
		{
			char c = cs[i];

			if (isValidIdentifierChar(c))
				sb.append(c);
			else
			{
				int codePoint = Character.codePointAt(cs, i);

				if (sb.length() == 0)
					sb.append('_');

				sb.append(codePoint);

				if (Character.isHighSurrogate(c))
					i += 1;
			}
		}

		return sb.toString();
	}

	/**
	 * 判断字符是否符合标识符规范。
	 * 
	 * @param c
	 * @return
	 */
	public boolean isValidIdentifierChar(char c)
	{
		return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_');
	}
}
