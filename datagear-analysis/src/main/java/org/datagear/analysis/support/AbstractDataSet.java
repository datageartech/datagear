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

package org.datagear.analysis.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.AbstractIdentifiable;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetField;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.NameAwareUtil;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.analysis.ResultDataFormat;
import org.datagear.analysis.support.DataSetFieldExpEvaluator.FieldValueAccessor;
import org.datagear.analysis.support.datasettpl.DataSetFmkTemplateResolvers;
import org.datagear.analysis.support.datasettpl.SqlTemplateResult;
import org.datagear.analysis.support.datasettpl.TemplateContext;
import org.datagear.analysis.support.datasettpl.TemplateResult;

/**
 * 抽象{@linkplain DataSet}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDataSet extends AbstractIdentifiable implements DataSet, Serializable
{
	private static final long serialVersionUID = 1L;

	private String name;

	private boolean mutableModel = false;

	private List<DataSetField> fields = Collections.emptyList();

	private List<DataSetParam> params = Collections.emptyList();

	/** 底层数据转换格式 */
	private DataFormat dataFormat = DataFormat.DEFAULT;

	/** 描述 */
	private String description = "";

	public AbstractDataSet()
	{
		super();
	}

	public AbstractDataSet(String id, String name, List<DataSetField> fields)
	{
		super(id);
		this.name = name;
		this.fields = fields;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public boolean isMutableModel()
	{
		return mutableModel;
	}

	public void setMutableModel(boolean mutableModel)
	{
		this.mutableModel = mutableModel;
	}

	@Override
	public List<DataSetField> getFields()
	{
		return fields;
	}

	public void setFields(List<DataSetField> fields)
	{
		this.fields = fields;
	}

	@Override
	public DataSetField getField(String name)
	{
		return NameAwareUtil.find(this.fields, name);
	}

	public boolean hasParam()
	{
		return (this.params != null && !this.params.isEmpty());
	}

	@Override
	public List<DataSetParam> getParams()
	{
		return params;
	}

	public void setParams(List<DataSetParam> params)
	{
		this.params = params;
	}

	@Override
	public DataSetParam getParam(String name)
	{
		return NameAwareUtil.find(this.params, name);
	}

	/**
	 * 获取底层数据转换格式。
	 * 
	 * @return
	 */
	public DataFormat getDataFormat()
	{
		return dataFormat;
	}

	/**
	 * 设置底层数据转换格式。
	 * <p>
	 * 当数据集字段{@linkplain #getFields()}的{@linkplain DataSetField#getType()}与底层数据源（数据库、CSV、JSON等）不匹配时，
	 * 可设置此数据格式，用于支持类型转换。
	 * </p>
	 * 
	 * @param dataFormat
	 */
	public void setDataFormat(DataFormat dataFormat)
	{
		this.dataFormat = dataFormat;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * 校验{@linkplain DataSetQuery#getParamValues()}是否有缺失的必填项。
	 * 
	 * @param query
	 * @throws DataSetParamValueRequiredException
	 */
	protected void checkRequiredParamValues(DataSetQuery query) throws DataSetParamValueRequiredException
	{
		if (!hasParam())
			return;

		List<DataSetParam> params = getParams();
		Map<String, ?> paramValues = query.getParamValues();
		
		for (DataSetParam param : params)
		{
			if (param.isRequired() && !paramValues.containsKey(param.getName()))
				throw new DataSetParamValueRequiredException(
						"Parameter [" + param.getName() + "] 's value is required");
		}
	}

	/**
	 * 解析结果。
	 * 
	 * @param rawResult
	 * @param fields
	 * @param fetchSize
	 *            获取条数，小于{@code 0}表示全部
	 * @param format
	 *            允许为{@code null}
	 * @return
	 * @throws Throwable
	 * @see {@link #resolveResultData(Object, List, int, ResultDataFormat)}
	 */
	protected ResolvedDataSetResult resolveResult(DataSetResult rawResult, List<DataSetField> fields, int fetchSize,
			ResultDataFormat format) throws Throwable
	{
		Object data = resolveResultData(rawResult.getData(), fields, fetchSize, format);
		DataSetResult resolvedResult = toDataSetResult(data, rawResult.getAdditions());

		return new ResolvedDataSetResult(resolvedResult, fields);
	}
	
	/**
	 * 解析结果数据。
	 * 
	 * @param rawData
	 *            {@code Collection<Map<String, ?>>}、{@code Object[]}（元素需为{@code Map<String, ?>}）、{@code Map<String, ?>}、{@code null}
	 * @param fields
	 * @param fetchSize
	 *            获取条数，小于{@code 0}表示全部
	 * @param format
	 *            允许为{@code null}
	 * @return {@code List<Map<String, ?>>}、{@code Object[]}、{@code Map<String, ?>}、{@code null}
	 * @throws Throwable
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Object resolveResultData(Object rawData, List<DataSetField> fields,
			int fetchSize, ResultDataFormat format) throws Throwable
	{
		Object data = null;

		DataSetFieldValueConverter converter = createDataSetFieldValueConverter();
		ResultDataFormatter formatter = (format == null ? null : new ResultDataFormatter(format));

		if (rawData == null)
		{
			data = null;
		}
		else if (rawData instanceof Collection<?>)
		{
			Collection<Map<String, ?>> rawCollection = (Collection<Map<String, ?>>) rawData;
			data = convertRawDataToResult(rawCollection, fields, fetchSize, converter, formatter);
		}
		else if (rawData instanceof Object[])
		{
			Object[] rawArray = (Object[]) rawData;
			List<Map<String, ?>> rawCollection = (List) Arrays.asList(rawArray);
			List<Map<String, Object>> dataList = convertRawDataToResult(rawCollection, fields, fetchSize, converter,
					formatter);

			data = dataList.toArray(new Object[dataList.size()]);
		}
		else if (rawData instanceof Map<?, ?>)
		{
			Map<?, ?> rawMap = (Map<?, ?>) rawData;
			List<Map<String, ?>> rawCollection = (List) Arrays.asList(rawMap);
			List<Map<String, Object>> dataList = convertRawDataToResult(rawCollection, fields, -1, converter,
					formatter);

			data = dataList.get(0);
		}
		else
			throw new UnsupportedOperationException(
					"Unsupported raw data type : " + rawData.getClass().getSimpleName());

		return data;
	}

	/**
	 * 转换原始数据。
	 * 
	 * @param rawData
	 * @param fields
	 * @param fetchSize
	 *            获取条数，小于{@code 0}表示全部
	 * @param converter
	 * @param formatter
	 *            允许为{@code null}
	 * @return
	 * @throws Throwable
	 */
	protected List<Map<String, Object>> convertRawDataToResult(Collection<? extends Map<String, ?>> rawData,
			List<DataSetField> fields, int fetchSize, DataSetFieldValueConverter converter,
			ResultDataFormatter formatter) throws Throwable
	{
		int dataSize = (fetchSize >= 0 ? fetchSize : rawData.size());
		List<Map<String, Object>> data = new ArrayList<>(dataSize);

		for (Map<String, ?> rowRaw : rawData)
		{
			if (data.size() >= dataSize)
				break;

			Map<String, Object> row = converter.convert(rowRaw, fields);
			data.add(row);
		}

		// 计算表达式
		evalResultData(data, fields, converter);
		// 格式化，应是最后步骤
		formatResultData(data, fields, formatter);

		return data;
	}
	
	protected void evalResultData(Object data, List<DataSetField> fields,
			DataSetFieldValueConverter converter)
	{
		DataSetFieldExpEvaluator evaluator = getDataSetFieldExpEvaluator();
		
		evaluator.eval(data, fields, new FieldValueAccessor()
		{
			@SuppressWarnings("unchecked")
			@Override
			public void set(Object row, DataSetField field, Object value)
			{
				value = converter.convert(value, field);
				((Map<String, Object>) row).put(field.getName(), value);
			}

			@SuppressWarnings("unchecked")
			@Override
			public Object get(Object row, DataSetField field)
			{
				return ((Map<String, Object>) row).get(field.getName());
			}
		});
	}
	
	/**
	 * 格式化结果数据。
	 * 
	 * @param data
	 *            {@code Collection<Map<String, ?>>}、{@code Object[]}（元素需为{@code Map<String, ?>}）、{@code Map<String, ?>}、{@code null}
	 * @param fields
	 *            允许{@code null}
	 * @param formatter
	 *            允许{@code null}
	 */
	@SuppressWarnings("unchecked")
	protected void formatResultData(Object data, List<DataSetField> fields,
			ResultDataFormatter formatter)
	{
		if (data == null || fields == null || fields.isEmpty() || formatter == null)
			return;

		if (data instanceof Collection<?>)
		{
			Collection<?> datas = (Collection<?>) data;

			for (Object row : datas)
				formatResultDataRow((Map<String, Object>) row, fields, formatter);
		}
		else if (data instanceof Object[])
		{
			Object[] datas = (Object[]) data;

			for (Object row : datas)
				formatResultDataRow((Map<String, Object>) row, fields, formatter);
		}
		else if (data instanceof Map<?, ?>)
		{
			Map<String, Object> map = (Map<String, Object>) data;
			formatResultDataRow(map, fields, formatter);
		}
		else
		{
			// 什么也不做，不必抛出异常
		}
	}

	protected void formatResultDataRow(Map<String, Object> row, List<DataSetField> fields,
			ResultDataFormatter formatter)
	{
		if (formatter == null)
			return;

		for (int i = 0, len = fields.size(); i < len; i++)
		{
			DataSetField field = fields.get(i);
			String name = field.getName();
			Object value = row.get(name);

			if (DataSetField.DataType.isObjectType(field))
			{
				formatResultData(value, field.getFields(), formatter);
			}
			else if (formatter.canFormat(field))
			{
				Object fv = formatter.format(value);
				row.put(name, fv);
			}
		}
	}

	protected DataSetFieldExpEvaluator getDataSetFieldExpEvaluator()
	{
		return DataSetFieldExpEvaluator.DEFAULT;
	}

	/**
	 * 给定数目是否已到达{@linkplain DataSetQuery#getResultFetchSize()}。
	 * 
	 * @param query
	 *            允许为{@code null}
	 * @param count
	 * @return
	 */
	protected boolean isReachResultFetchSize(DataSetQuery query, int count)
	{
		if (query == null)
			return false;

		int maxCount = query.getResultFetchSize();

		if (maxCount < 0)
			return false;

		return count >= maxCount;
	}

	/**
	 * 转换为{@linkplain DataSetResult}。
	 * 
	 * @param data
	 *            允许{@code null}
	 * @param additions
	 *            允许{@code null}
	 * @return
	 */
	protected DataSetResult toDataSetResult(Object data)
	{
		return toDataSetResult(data, null);
	}

	/**
	 * 转换为{@linkplain DataSetResult}。
	 * 
	 * @param data
	 *            允许{@code null}
	 * @param additions
	 *            允许{@code null}
	 * @return
	 */
	protected DataSetResult toDataSetResult(Object data, Map<String, ?> additions)
	{
		DataSetResult re = new DataSetResult(data);
		re.setAdditions(additions);

		return re;
	}

	/**
	 * 查找与名称数组对应的{@linkplain DataSetField}列表。
	 * <p>
	 * 如果{@code names}某元素没有对应的{@linkplain DataSetField}，返回列表对应元素位置将为{@code null}。
	 * </p>
	 * 
	 * @param fields
	 * @param names
	 * @return
	 */
	protected List<DataSetField> findDataSetFields(List<DataSetField> fields, String[] names)
	{
		return findDataSetFields(fields, Arrays.asList(names));
	}

	/**
	 * 查找与名称数组对应的{@linkplain DataSetField}列表。
	 * <p>
	 * 如果{@code names}某元素没有对应的{@linkplain DataSetField}，返回列表对应元素位置将为{@code null}。
	 * </p>
	 * 
	 * @param fields
	 * @param names
	 * @return
	 */
	protected List<DataSetField> findDataSetFields(List<DataSetField> fields, List<String> names)
	{
		return NameAwareUtil.finds(fields, names);
	}

	/**
	 * 创建一个{@linkplain DataSetFieldValueConverter}实例。
	 * <p>
	 * 由于{@linkplain DataSetFieldValueConverter}不是线程安全的，所以每次使用时要手动创建。
	 * </p>
	 * 
	 * @return
	 */
	protected DataSetFieldValueConverter createDataSetFieldValueConverter()
	{
		DataFormat dataFormat = getDataFormat();
		if (dataFormat == null)
			dataFormat = DataFormat.DEFAULT;

		DataSetFieldValueConverter converter = new DataSetFieldValueConverter(dataFormat);

		// 易变模型要保留全部数据
		converter.setStrictForMap(!isMutableModel());
		// 这里应设为true，可避免精度丢失
		converter.setIgnoreBigIntegerToInteger(true);

		return converter;
	}

	/**
	 * 解析{@linkplain DataSetField#getType()}。
	 * 
	 * @param value
	 * @return
	 */
	protected String resolveFieldDataType(Object value)
	{
		return DataSetField.DataType.resolveDataType(value);
	}
	
	/**
	 * 解析{@linkplain DataSetField#setType(String)}、{@linkplain DataSetField#setArray(boolean)}。
	 * 
	 * @param field
	 * @param value
	 */
	protected void resolveFieldDataType(DataSetField field, Object value)
	{
		if (DataSetField.DataType.isLikeArray(value))
		{
			field.setArray(true);
			field.setType(DataSetField.DataType.resolveArrayEleDataType(value));
		}
		else
		{
			field.setArray(false);
			field.setType(resolveFieldDataType(value));
		}
	}

	@SuppressWarnings("unchecked")
	protected List<Map<String, Object>> listRowsToMapRows(List<List<Object>> data, List<DataSetField> fields)
	{
		if (data == null)
			return Collections.EMPTY_LIST;

		int plen = fields.size();

		List<Map<String, Object>> maps = new ArrayList<>(data.size());

		for (List<Object> row : data)
		{
			Map<String, Object> map = new HashMap<>();

			for (int i = 0; i < Math.min(plen, row.size()); i++)
			{
				String name = fields.get(i).getName();
				map.put(name, row.get(i));
			}

			maps.add(map);
		}

		return maps;
	}

	/**
	 * 解析模板：普通文本。
	 * 
	 * @param text
	 *            允许{@code null}
	 * @param query
	 * @return
	 * @see {@linkplain #resolveTemplateResultPlain(String, DataSetQuery)}
	 */
	protected String resolveTemplatePlain(String text, DataSetQuery query)
	{
		return resolveTemplateResultPlain(text, query).getResult();
	}

	/**
	 * 解析模板：普通文本。
	 * <p>
	 * 注意：无论数据集是否有定义参数（{@linkplain #hasParam()}为{@code false}），此方法也必须将{@code text}作为模板解析，因为存在如下应用场景：
	 * </p>
	 * <ol>
	 * <li>用户编写了无需参数的模板内容；</li>
	 * <li>用户不定义数据集参数，但却定义模板内容，之后用户自行在{@linkplain DataSet#getResult(DataSetQuery)}参数映射表中传递模板内容所须的参数值；</li>
	 * </ol>
	 * 
	 * @param text
	 *            允许{@code null}
	 * @param query
	 * @return
	 */
	protected TemplateResult resolveTemplateResultPlain(String text, DataSetQuery query)
	{
		return DataSetFmkTemplateResolvers.resolvePlain(text, toTemplateContext(query));
	}

	/**
	 * 解析模板：CSV。
	 * 
	 * @param text
	 *            允许{@code null}
	 * @param query
	 * @return
	 * @see {@linkplain #resolveTemplateResultCsv(String, DataSetQuery)}
	 */
	protected String resolveTemplateCsv(String text, DataSetQuery query)
	{
		return resolveTemplateResultCsv(text, query).getResult();
	}

	/**
	 * 解析模板：CSV。
	 * <p>
	 * 注意：无论数据集是否有定义参数（{@linkplain #hasParam()}为{@code false}），此方法也必须将{@code text}作为模板解析，因为存在如下应用场景：
	 * </p>
	 * <ol>
	 * <li>用户编写了无需参数的模板内容；</li>
	 * <li>用户不定义数据集参数，但却定义模板内容，之后用户自行在{@linkplain DataSet#getResult(DataSetQuery)}参数映射表中传递模板内容所须的参数值；</li>
	 * </ol>
	 * 
	 * @param text
	 *            允许{@code null}
	 * @param query
	 * @return
	 */
	protected TemplateResult resolveTemplateResultCsv(String text, DataSetQuery query)
	{
		return DataSetFmkTemplateResolvers.resolveCsv(text, toTemplateContext(query));
	}

	/**
	 * 解析模板：JSON。
	 * 
	 * @param text
	 *            允许{@code null}
	 * @param query
	 * @return
	 * @see {@linkplain #resolveTemplateResultJson(String, DataSetQuery)}
	 */
	protected String resolveTemplateJson(String text, DataSetQuery query)
	{
		return resolveTemplateResultJson(text, query).getResult();
	}

	/**
	 * 解析模板：JSON。
	 * <p>
	 * 注意：无论数据集是否有定义参数（{@linkplain #hasParam()}为{@code false}），此方法也必须将{@code text}作为模板解析，因为存在如下应用场景：
	 * </p>
	 * <ol>
	 * <li>用户编写了无需参数的模板内容；</li>
	 * <li>用户不定义数据集参数，但却定义模板内容，之后用户自行在{@linkplain DataSet#getResult(DataSetQuery)}参数映射表中传递模板内容所须的参数值；</li>
	 * </ol>
	 * 
	 * @param text
	 *            允许{@code null}
	 * @param query
	 * @return
	 */
	protected TemplateResult resolveTemplateResultJson(String text, DataSetQuery query)
	{
		return DataSetFmkTemplateResolvers.resolveJson(text, toTemplateContext(query));
	}

	/**
	 * 解析模板：SQL。
	 * 
	 * @param text
	 *            允许{@code null}
	 * @param query
	 * @return
	 * @see {@linkplain #resolveTemplateResultSql(String, DataSetQuery)}
	 */
	protected String resolveTemplateSql(String text, DataSetQuery query)
	{
		return resolveTemplateResultSql(text, query).getResult();
	}

	/**
	 * 解析模板：SQL。
	 * <p>
	 * 注意：无论数据集是否有定义参数（{@linkplain #hasParam()}为{@code false}），此方法也必须将{@code text}作为模板解析，因为存在如下应用场景：
	 * </p>
	 * <ol>
	 * <li>用户编写了无需参数的模板内容；</li>
	 * <li>用户不定义数据集参数，但却定义模板内容，之后用户自行在{@linkplain DataSet#getResult(DataSetQuery)}参数映射表中传递模板内容所须的参数值；</li>
	 * </ol>
	 * 
	 * @param text
	 *            允许{@code null}
	 * @param query
	 * @return
	 */
	protected SqlTemplateResult resolveTemplateResultSql(String text, DataSetQuery query)
	{
		return DataSetFmkTemplateResolvers.resolveSql(text, toTemplateContext(query));
	}

	/**
	 * 解析模板：XML。
	 * 
	 * @param text
	 *            允许{@code null}
	 * @param query
	 * @return
	 * @see {@linkplain #resolveTemplateResultXml(String, DataSetQuery)}
	 */
	protected String resolveTemplateXml(String text, DataSetQuery query)
	{
		return resolveTemplateResultXml(text, query).getResult();
	}

	/**
	 * 解析模板：XML。
	 * <p>
	 * 注意：无论数据集是否有定义参数（{@linkplain #hasParam()}为{@code false}），此方法也必须将{@code text}作为模板解析，因为存在如下应用场景：
	 * </p>
	 * <ol>
	 * <li>用户编写了无需参数的模板内容；</li>
	 * <li>用户不定义数据集参数，但却定义模板内容，之后用户自行在{@linkplain DataSet#getResult(DataSetQuery)}参数映射表中传递模板内容所须的参数值；</li>
	 * </ol>
	 * 
	 * @param text
	 *            允许{@code null}
	 * @param query
	 * @return
	 */
	protected TemplateResult resolveTemplateResultXml(String text, DataSetQuery query)
	{
		return DataSetFmkTemplateResolvers.resolveXml(text, toTemplateContext(query));
	}

	/**
	 * 将{@linkplain DataSetQuery}转换为{@linkplain TemplateContext}。
	 * 
	 * @param query
	 * @return
	 */
	protected TemplateContext toTemplateContext(DataSetQuery query)
	{
		Map<String, ?> values = query.getParamValues();
		return new TemplateContext(values);
	}

	protected static class EvaludatedFieldsInfo
	{
		private final List<DataSetField> fields;
		private final List<Object> defaultValues;

		public EvaludatedFieldsInfo(List<DataSetField> fields, List<Object> defaultValues)
		{
			super();
			this.fields = fields;
			this.defaultValues = defaultValues;
		}

		public List<DataSetField> getFields()
		{
			return fields;
		}

		public List<Object> getDefaultValues()
		{
			return defaultValues;
		}
	}
}
