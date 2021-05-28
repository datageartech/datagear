/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.ResolvableDataSet;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.util.IOUtil;

/**
 * 抽象CSV数据集。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractCsvDataSet extends AbstractResolvableDataSet implements ResolvableDataSet
{
	/** 作为名称行的行号 */
	private int nameRow = -1;

	public AbstractCsvDataSet()
	{
		super();
	}

	public AbstractCsvDataSet(String id, String name)
	{
		super(id, name);
	}

	public AbstractCsvDataSet(String id, String name, List<DataSetProperty> properties)
	{
		super(id, name, properties);
	}

	/**
	 * 是否有名称行。
	 * 
	 * @return
	 */
	public boolean hasNameRow()
	{
		return (this.nameRow > 0);
	}

	/**
	 * 获取作为名称行的行号。
	 * 
	 * @return
	 */
	public int getNameRow()
	{
		return nameRow;
	}

	/**
	 * 设置作为名称行的行号。
	 * 
	 * @param nameRow
	 *            行号，小于{@code 1}则表示无名称行。
	 */
	public void setNameRow(int nameRow)
	{
		this.nameRow = nameRow;
	}

	/**
	 * 解析结果。
	 * <p>
	 * 如果{@linkplain #getCsvReader(DataSetQuery)}返回的{@linkplain TemplateResolvedSource#hasResolvedTemplate()}，
	 * 此方法将返回{@linkplain TemplateResolvedDataSetResult}。
	 * </p>
	 */
	@Override
	protected ResolvedDataSetResult resolveResult(DataSetQuery query, List<DataSetProperty> properties,
			boolean resolveProperties) throws DataSetException
	{
		TemplateResolvedSource<Reader> reader = null;

		try
		{
			reader = getCsvReader(query);

			ResolvedDataSetResult result = resolveResult(query, reader.getSource(), properties, resolveProperties);

			if (reader.hasResolvedTemplate())
				result = new TemplateResolvedDataSetResult(result.getResult(), result.getProperties(),
						reader.getResolvedTemplate());

			return result;
		}
		catch (DataSetException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetSourceParseException(t);
		}
		finally
		{
			if (reader != null)
				IOUtil.close(reader.getSource());
		}
	}

	/**
	 * 获取CSV输入流。
	 * <p>
	 * 实现方法应该返回实例级不变的输入流。
	 * </p>
	 * 
	 * @param query
	 * @return
	 * @throws Throwable
	 */
	protected abstract TemplateResolvedSource<Reader> getCsvReader(DataSetQuery query) throws Throwable;

	/**
	 * 解析结果。
	 * 
	 * @param query
	 * @param csvReader
	 * @param properties
	 *            允许为{@code null}
	 * @param resolveProperties
	 * @return
	 * @throws Throwable
	 */
	protected ResolvedDataSetResult resolveResult(DataSetQuery query, Reader csvReader,
			List<DataSetProperty> properties, boolean resolveProperties) throws Throwable
	{
		CSVParser csvParser = buildCSVParser(csvReader);
		List<CSVRecord> csvRecords = csvParser.getRecords();

		List<String> rawDataPropertyNames = resolvePropertyNames(csvRecords);
		List<Map<String, String>> rawData = resolveRawData(query, rawDataPropertyNames, csvRecords);

		if (resolveProperties)
		{
			List<DataSetProperty> resolvedProperties = resolveProperties(rawDataPropertyNames, rawData);
			mergeDataSetProperties(resolvedProperties, properties);
			properties = resolvedProperties;
		}

		return resolveResult(rawData, properties, query.getResultDataFormat());
	}

	/**
	 * 解析数据属性名列表。
	 * 
	 * @param csvRecords
	 * @return
	 * @throws Throwable
	 */
	protected List<String> resolvePropertyNames(List<CSVRecord> csvRecords) throws Throwable
	{
		List<String> propertyNames = null;

		for (int i = 0, len = csvRecords.size(); i < len; i++)
		{
			CSVRecord csvRecord = csvRecords.get(i);

			if (isNameRow(i))
			{
				int size = csvRecord.size();
				propertyNames = new ArrayList<String>(csvRecord.size());

				for (int j = 0; j < size; j++)
					propertyNames.add(csvRecord.get(j));

				break;
			}
			else
			{
				if (propertyNames == null)
				{
					int size = csvRecord.size();
					propertyNames = new ArrayList<String>(csvRecord.size());

					for (int j = 0; j < size; j++)
						propertyNames.add(Integer.toString(j + 1));
				}

				if (isAfterNameRow(i))
					break;
			}
		}

		if (propertyNames == null)
			propertyNames = Collections.emptyList();

		return propertyNames;
	}

	/**
	 * 解析{@linkplain DataSetProperty}。
	 * 
	 * @param rawDataPropertyNames
	 * @param rawData              允许为{@code null}
	 * @return
	 * @throws Throwable
	 */
	protected List<DataSetProperty> resolveProperties(List<String> rawDataPropertyNames,
			List<Map<String, String>> rawData)
			throws Throwable
	{
		int propertyLen = rawDataPropertyNames.size();
		List<DataSetProperty> properties = new ArrayList<>(propertyLen);

		for (String name : rawDataPropertyNames)
			properties.add(new DataSetProperty(name, DataSetProperty.DataType.STRING));

		// 根据数据格式，修订可能的数值类型：只有某一列的所有字符串都是数值格式，才认为是数值类型
		if (rawData != null && rawData.size() > 0)
		{
			boolean[] isNumbers = new boolean[propertyLen];
			Arrays.fill(isNumbers, true);

			for (Map<String, String> row : rawData)
			{
				for (int i = 0; i < propertyLen; i++)
				{
					if (!isNumbers[i])
						continue;

					String value = row.get(rawDataPropertyNames.get(i));
					isNumbers[i] = isNumberString(value);
				}
			}

			for (int i = 0; i < propertyLen; i++)
			{
				if (isNumbers[i])
					properties.get(i).setType(DataSetProperty.DataType.NUMBER);
			}
		}

		return properties;
	}

	/**
	 * 解析原始数据。
	 * 
	 * @param query
	 * @param propertyNames
	 * @param csvRecords
	 * @return
	 * @throws Throwable
	 */
	protected List<Map<String, String>> resolveRawData(DataSetQuery query, List<String> propertyNames,
			List<CSVRecord> csvRecords) throws Throwable
	{
		List<Map<String, String>> data = new ArrayList<>();

		for (int i = 0, len = csvRecords.size(); i < len; i++)
		{
			if(isNameRow(i))
				continue;

			if (isReachResultFetchSize(query, data.size()))
				break;

			Map<String, String> row = new HashMap<>();

			CSVRecord csvRecord = csvRecords.get(i);
			for (int j = 0, jlen = Math.min(csvRecord.size(), propertyNames.size()); j < jlen; j++)
			{
				String name = propertyNames.get(j);
				String value = csvRecord.get(j);

				row.put(name, value);
			}

			data.add(row);
		}

		return data;
	}

	/**
	 * 指定的CSV值是否可被当做数值类型。
	 * 
	 * @param value
	 * @return
	 */
	protected boolean isNumberString(String value)
	{
		if (value == null || value.isEmpty())
			return false;

		try
		{
			parseNumberString(value);

			return true;
		}
		catch (Throwable t)
		{
			return false;
		}
	}

	/**
	 * 解析数值字符串，{@linkplain #isNumberString(String)}应为{@code true}。
	 * 
	 * @param s
	 * @return
	 * @throws Throwable
	 */
	protected Number parseNumberString(String s) throws Throwable
	{
		return Double.parseDouble(s);
	}

	/**
	 * 是否名称行
	 * 
	 * @param rowIndex
	 *            行索引（以{@code 0}计数）
	 * @return
	 */
	protected boolean isNameRow(int rowIndex)
	{
		return ((rowIndex + 1) == this.nameRow);
	}

	/**
	 * 是否在名称行之后。
	 * <p>
	 * 如果没有名称行，应返回{@code true}。
	 * </p>
	 * 
	 * @param rowIndex
	 *            行索引（以{@code 0}计数）
	 * @return
	 */
	protected boolean isAfterNameRow(int rowIndex)
	{
		return ((rowIndex + 1) > this.nameRow);
	}

	/**
	 * 构建{@linkplain CSVParser}。
	 * 
	 * @param reader
	 * @return
	 * @throws Throwable
	 */
	protected CSVParser buildCSVParser(Reader reader) throws Throwable
	{
		return CSVFormat.DEFAULT.withIgnoreSurroundingSpaces().parse(reader);
	}
}
