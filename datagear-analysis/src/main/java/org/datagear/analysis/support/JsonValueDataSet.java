/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetOption;
import org.datagear.analysis.DataSetProperty;
import org.datagear.util.IOUtil;

/**
 * JSON字符串值数据集。
 * <p>
 * 此类的{@linkplain #getValue()}支持<code>Freemarker</code>模板语言。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class JsonValueDataSet extends AbstractJsonDataSet
{
	private String value;

	public JsonValueDataSet()
	{
		super();
	}

	public JsonValueDataSet(String id, String name, String value)
	{
		super(id, name);
		this.value = value;
	}

	public JsonValueDataSet(String id, String name, List<DataSetProperty> properties, String value)
	{
		super(id, name, properties);
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	@Override
	public TemplateResolvedDataSetResult resolve(Map<String, ?> paramValues, DataSetOption dataSetOption)
			throws DataSetException
	{
		return (TemplateResolvedDataSetResult) resolveResult(paramValues, null, dataSetOption);
	}

	@Override
	protected TemplateResolvedSource<Reader> getJsonReader(Map<String, ?> paramValues) throws Throwable
	{
		String json = resolveAsFmkTemplate(this.value, paramValues);
		return new TemplateResolvedSource<>(IOUtil.getReader(json), json);
	}
}
