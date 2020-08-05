/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSet;

/**
 * JSON {@linkplain DataSet}支持类。
 * 
 * @author datagear@163.com
 *
 */
public class JsonDataSetSupport extends JsonSupport
{
	public JsonDataSetSupport()
	{
		super();
	}

	/**
	 * 解析结果数据。
	 * 
	 * @param jsonValue
	 * @return
	 * @throws IOException
	 */
	public List<Map<String, ?>> resolveResultDatas(String jsonValue) throws IOException
	{
		return resolveResultDatas(jsonValue, null);
	}

	/**
	 * 解析结果数据。
	 * 
	 * @param jsonValue
	 * @param path
	 *            结果数据属性路径，为{@code null}表示使用原始数据
	 * @return
	 * @throws IOException
	 */
	public List<Map<String, ?>> resolveResultDatas(String jsonValue, String path) throws IOException
	{
		StringReader reader = new StringReader(jsonValue);
		return resolveResultDatas(reader, path);
	}

	/**
	 * 解析结果数据。
	 * 
	 * @param jsonReader
	 * @return
	 * @throws IOException
	 */
	public List<Map<String, ?>> resolveResultDatas(Reader jsonReader) throws IOException
	{
		return resolveResultDatas(jsonReader, null);
	}

	/**
	 * 解析结果数据。
	 * 
	 * @param jsonReader
	 * @param path
	 *            结果数据属性路径，为{@code null}表示使用原始数据
	 * @return
	 * @throws IOException
	 */
	public List<Map<String, ?>> resolveResultDatas(Reader jsonReader, String path) throws IOException
	{
		// TODO
		return null;
	}
}
