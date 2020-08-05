/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetProperty;

/**
 * 抽象Freemarker模板{@linkplain DataSet}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractFmkTemplateDataSet extends AbstractDataSet
{
	public static final DataSetFmkTemplateResolver TEMPLATE_RESOLVER = new DataSetFmkTemplateResolver();

	public AbstractFmkTemplateDataSet()
	{
		super();
	}

	public AbstractFmkTemplateDataSet(String id, String name, List<DataSetProperty> properties)
	{
		super(id, name, properties);
	}

	protected String resolveTemplate(String template, Map<String, ?> paramValues)
	{
		return TEMPLATE_RESOLVER.resolve(template, paramValues);
	}
}
