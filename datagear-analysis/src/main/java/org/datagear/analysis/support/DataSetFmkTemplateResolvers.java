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

package org.datagear.analysis.support;

import java.util.Map;

import org.datagear.analysis.support.fmk.CsvOutputFormat;
import org.datagear.analysis.support.fmk.JsonOutputFormat;
import org.datagear.analysis.support.fmk.SqlOutputFormat;
import org.datagear.analysis.support.fmk.XmlOutputFormat;

/**
 * 常用{@linkplain DataSetFmkTemplateResolver}工具。
 * 
 * @author datagear@163.com
 */
public final class DataSetFmkTemplateResolvers
{
	/**
	 * 普通文本。
	 * <p>
	 * 不做任何转义处理。
	 * </p>
	 */
	public static final DataSetFmkTemplateResolver PLAIN = new DataSetFmkTemplateResolver();

	/**
	 * CSV。
	 * <p>
	 * 转义CSV特殊字符。
	 * </p>
	 */
	public static final DataSetFmkTemplateResolver CSV = new DataSetFmkTemplateResolver(CsvOutputFormat.INSTANCE);

	/**
	 * JSON。
	 * <p>
	 * 转义JSON特殊字符。
	 * </p>
	 */
	public static final DataSetFmkTemplateResolver JSON = new DataSetFmkTemplateResolver(JsonOutputFormat.INSTANCE);

	/**
	 * SQL。
	 * <p>
	 * 转义SQL特殊字符。
	 * </p>
	 */
	public static final DataSetFmkTemplateResolver SQL = new DataSetFmkTemplateResolver(SqlOutputFormat.INSTANCE);

	/**
	 * XML。
	 * <p>
	 * 转义XML特殊字符。
	 * </p>
	 */
	public static final DataSetFmkTemplateResolver XML = new DataSetFmkTemplateResolver(XmlOutputFormat.INSTANCE);

	/**
	 * 解析普通文本。
	 * 
	 * @param text
	 * @param params
	 * @return
	 */
	public static String resolvePlain(String text, Map<String, ?> params)
	{
		return resolveNullable(PLAIN, text, params);
	}

	/**
	 * 解析普通文本。
	 * 
	 * @param text
	 * @param context
	 * @return
	 */
	public static String resolvePlain(String text, TemplateContext context)
	{
		return resolveNullable(PLAIN, text, context);
	}

	/**
	 * 解析CSV。
	 * 
	 * @param text
	 * @param params
	 * @return
	 */
	public static String resolveCsv(String text, Map<String, ?> params)
	{
		return resolveNullable(CSV, text, params);
	}

	/**
	 * 解析CSV。
	 * 
	 * @param text
	 * @param context
	 * @return
	 */
	public static String resolveCsv(String text, TemplateContext context)
	{
		return resolveNullable(CSV, text, context);
	}

	/**
	 * 解析JSON。
	 * 
	 * @param text
	 * @param params
	 * @return
	 */
	public static String resolveJson(String text, Map<String, ?> params)
	{
		return resolveNullable(JSON, text, params);
	}

	/**
	 * 解析CSV。
	 * 
	 * @param text
	 * @param context
	 * @return
	 */
	public static String resolveJson(String text, TemplateContext context)
	{
		return resolveNullable(JSON, text, context);
	}

	/**
	 * 解析SQL。
	 * 
	 * @param text
	 * @param params
	 * @return
	 */
	public static String resolveSql(String text, Map<String, ?> params)
	{
		return resolveNullable(SQL, text, params);
	}

	/**
	 * 解析SQL。
	 * 
	 * @param text
	 * @param context
	 * @return
	 */
	public static String resolveSql(String text, TemplateContext context)
	{
		return resolveNullable(SQL, text, context);
	}

	/**
	 * 解析XML。
	 * 
	 * @param text
	 * @param params
	 * @return
	 */
	public static String resolveXml(String text, Map<String, ?> params)
	{
		return resolveNullable(XML, text, params);
	}

	/**
	 * 解析XML。
	 * 
	 * @param text
	 * @param context
	 * @return
	 */
	public static String resolveXml(String text, TemplateContext context)
	{
		return resolveNullable(XML, text, context);
	}

	/**
	 * 解析模板。
	 * 
	 * @param resolver
	 * @param text
	 * @param params
	 * @return
	 */
	protected static String resolveNullable(DataSetFmkTemplateResolver resolver, String text, Map<String, ?> params)
	{
		if (text == null)
			return null;

		return resolver.resolve(text, params);
	}

	/**
	 * 解析模板。
	 * 
	 * @param resolver
	 * @param text
	 * @param context
	 * @return
	 */
	protected static String resolveNullable(DataSetFmkTemplateResolver resolver, String text, TemplateContext context)
	{
		if (text == null)
			return null;

		return resolver.resolve(text, context);
	}
}
