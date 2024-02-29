/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.analysis.support.fmk;

import java.io.IOException;
import java.io.Writer;

import freemarker.core.CommonMarkupOutputFormat;
import freemarker.template.TemplateModelException;

/**
 * JSON输出格式。
 * <p>
 * 此类负责将JSON Freemarker模板中的字符串值转义为合法的JSON字符串。
 * </p>
 * <p>
 * 转义规则如下所示：
 * </p>
 * <p>
 * <table>
 * <tr>
 * <td>源字符</td>
 * <td>转义目标</td>
 * </tr>
 * <tr>
 * <td>"</td>
 * <td>\"</td>
 * </tr>
 * <tr>
 * <td>\</td>
 * <td>\\</td>
 * </tr>
 * <tr>
 * <td>回车</td>
 * <td>\r</td>
 * </tr>
 * <tr>
 * <td>换行</td>
 * <td>\n</td>
 * </tr>
 * </table>
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class JsonOutputFormat extends CommonMarkupOutputFormat<TemplateJsonOutputModel>
{
	public static final JsonOutputFormat INSTANCE = new JsonOutputFormat();

	protected JsonOutputFormat()
	{
		super();
	}

	@Override
	public String getName()
	{
		return "JSON";
	}

	@Override
	public String getMimeType()
	{
		return null;
	}

	@Override
	public void output(String textToEsc, Writer out) throws IOException, TemplateModelException
	{
		escapeJsonString(textToEsc, out);
	}

	@Override
	public String escapePlainText(String plainTextContent) throws TemplateModelException
	{
		StringBuilder sb = new StringBuilder();

		try
		{
			escapeJsonString(plainTextContent, sb);
		}
		catch(IOException e)
		{
			throw new TemplateModelException(e);
		}

		return sb.toString();
	}

	@Override
	public boolean isLegacyBuiltInBypassed(String builtInName) throws TemplateModelException
	{
		return builtInName.equals("json");
	}

	@Override
	protected TemplateJsonOutputModel newTemplateMarkupOutputModel(String plainTextContent, String markupContent)
			throws TemplateModelException
	{
		return new TemplateJsonOutputModel(plainTextContent, markupContent);
	}

	/**
	 * 转义JSON字符串。
	 * 
	 * @param plainText
	 * @param out
	 * @throws IOException
	 */
	protected void escapeJsonString(String plainText, Appendable out) throws IOException
	{
		char[] cs = plainText.toCharArray();

		for (int i = 0; i < cs.length; i++)
		{
			char c = cs[i];

			switch (c)
			{
				case '"':
				{
					out.append('\\');
					out.append('"');
					break;
				}
				case '\\':
				{
					out.append('\\');
					out.append('\\');
					break;
				}
				case '\r':
				{
					out.append('\\');
					out.append('r');
					break;
				}
				case '\n':
				{
					out.append('\\');
					out.append('n');
					break;
				}
				default:
				{
					out.append(c);
					break;
				}
			}
		}
	}
}
