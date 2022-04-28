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
import org.datagear.analysis.support.fmk.CsvOutputFormat;
import org.datagear.util.IOUtil;

/**
 * 抽象CSV数据集。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractCsvDataSet extends AbstractResolvableDataSet implements ResolvableDataSet
{
	private static final long serialVersionUID = 1L;

	public static final DataSetFmkTemplateResolver CSV_TEMPLATE_RESOLVER = new DataSetFmkTemplateResolver(
			CsvOutputFormat.INSTANCE);

	/**
	 * CSV解析器。
	 */
	public static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder().setIgnoreSurroundingSpaces(true).build();

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
	 * 如果{@linkplain #getCsvResource(DataSetQuery)}返回有{@linkplain CsvTemplateResolvedResource#hasResolvedTemplate()}，
	 * 此方法将返回{@linkplain TemplateResolvedDataSetResult}。
	 * </p>
	 */
	@Override
	protected ResolvedDataSetResult resolveResult(DataSetQuery query, List<DataSetProperty> properties,
			boolean resolveProperties) throws DataSetException
	{
		CsvTemplateResolvedResource resource = null;

		try
		{
			resource = getCsvResource(query);
			CsvResourceData csvResourceData = resolveCsvResourceData(resource);

			ResolvedDataSetResult result = resolveResult(query, csvResourceData, properties, resolveProperties);

			if (resource.hasResolvedTemplate())
				result = new TemplateResolvedDataSetResult(result.getResult(), result.getProperties(),
						resource.getResolvedTemplate());

			return result;
		}
		catch (DataSetException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetSourceParseException(t, (resource == null ? null : resource.getResolvedTemplate()));
		}
	}

	/**
	 * 获取{@linkplain CsvTemplateResolvedResource}。
	 * 
	 * @param query
	 * @return
	 * @throws Throwable
	 */
	protected abstract CsvTemplateResolvedResource getCsvResource(DataSetQuery query)
			throws Throwable;

	/**
	 * 解析CSV数据。
	 * 
	 * @param resource
	 * @return
	 * @throws Throwable
	 */
	protected CsvResourceData resolveCsvResourceData(CsvTemplateResolvedResource resource) throws Throwable
	{
		Reader reader = null;

		try
		{
			reader = resource.getResource();

			CSVParser csvParser = buildCSVParser(reader);
			List<CSVRecord> csvRecords = csvParser.getRecords();

			List<String> propertyNames = resolvePropertyNames(resource, csvRecords);
			List<Map<String, String>> data = resolveData(resource, propertyNames, csvRecords);
			List<DataSetProperty> properties = resolveProperties(propertyNames, data);

			return new CsvResourceData(data, properties);
		}
		finally
		{
			IOUtil.close(reader);
		}
	}

	/**
	 * 解析属性名。
	 * 
	 * @param resource
	 * @param csvRecords
	 * @return
	 * @throws Throwable
	 */
	protected List<String> resolvePropertyNames(CsvTemplateResolvedResource resource,
			List<CSVRecord> csvRecords) throws Throwable
	{
		List<String> propertyNames = null;

		for (int i = 0, len = csvRecords.size(); i < len; i++)
		{
			CSVRecord csvRecord = csvRecords.get(i);

			if (isNameRow(resource.getNameRow(), i))
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

				if (isAfterNameRow(resource.getNameRow(), i))
					break;
			}
		}

		if (propertyNames == null)
			propertyNames = Collections.emptyList();

		return propertyNames;
	}

	/**
	 * 解析数据。
	 * 
	 * @param resource
	 * @param propertyNames
	 * @param csvRecords
	 * @return
	 * @throws Throwable
	 */
	protected List<Map<String, String>> resolveData(CsvTemplateResolvedResource resource, List<String> propertyNames,
			List<CSVRecord> csvRecords) throws Throwable
	{
		List<Map<String, String>> data = new ArrayList<>();

		for (int i = 0, len = csvRecords.size(); i < len; i++)
		{
			if (isNameRow(resource.getNameRow(), i))
				continue;

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
	 * 解析{@linkplain DataSetProperty}。
	 * 
	 * @param propertyNames
	 * @param data              允许为{@code null}
	 * @return
	 * @throws Throwable
	 */
	protected List<DataSetProperty> resolveProperties(List<String> propertyNames,
			List<Map<String, String>> data)
			throws Throwable
	{
		int propertyLen = propertyNames.size();
		List<DataSetProperty> properties = new ArrayList<>(propertyLen);
	
		for (String name : propertyNames)
			properties.add(new DataSetProperty(name, DataSetProperty.DataType.STRING));
	
		// 根据数据格式，修订可能的数值类型：只有某一列的所有字符串都是数值格式，才认为是数值类型
		if (data != null && data.size() > 0)
		{
			boolean[] isNumbers = new boolean[propertyLen];
			Arrays.fill(isNumbers, true);
	
			for (Map<String, String> row : data)
			{
				for (int i = 0; i < propertyLen; i++)
				{
					if (!isNumbers[i])
						continue;
	
					String value = row.get(propertyNames.get(i));
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
	 * 解析结果。
	 * 
	 * @param query
	 * @param csvResourceData
	 * @param properties        允许为{@code null}
	 * @param resolveProperties
	 * @return
	 * @throws Throwable
	 */
	protected ResolvedDataSetResult resolveResult(DataSetQuery query, CsvResourceData csvResourceData,
			List<DataSetProperty> properties, boolean resolveProperties) throws Throwable
	{
		List<DataSetProperty> resProperties = csvResourceData.getProperties();
		List<Map<String, String>> resData = csvResourceData.getData();

		if (resolveProperties)
			properties = mergeDataSetProperties(resProperties, properties);

		return resolveResult(resData, properties, query.getResultFetchSize(), query.getResultDataFormat());
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
	 * 是否名称行。
	 * 
	 * @param nameRow  名称行号（以{@code 1}计数）
	 * @param rowIndex 行索引（以{@code 0}计数）
	 * @return
	 */
	protected boolean isNameRow(int nameRow, int rowIndex)
	{
		return ((rowIndex + 1) == nameRow);
	}

	/**
	 * 是否在名称行之后。
	 * <p>
	 * 如果没有名称行，应返回{@code true}。
	 * </p>
	 * 
	 * @param nameRow  名称行号（以{@code 1}计数）
	 * @param rowIndex 行索引（以{@code 0}计数）
	 * @return
	 */
	protected boolean isAfterNameRow(int nameRow, int rowIndex)
	{
		return ((rowIndex + 1) > nameRow);
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
		return CSV_FORMAT.parse(reader);
	}

	/**
	 * 将指定CSV文本作为模板解析。
	 * 
	 * @param csv
	 * @param query
	 * @return
	 */
	protected String resolveCsvAsTemplate(String csv, DataSetQuery query)
	{
		return resolveTextAsTemplate(CSV_TEMPLATE_RESOLVER, csv, query);
	}

	protected static abstract class CsvTemplateResolvedResource extends TemplateResolvedResource<Reader>
	{
		private static final long serialVersionUID = 1L;

		private int nameRow;

		public CsvTemplateResolvedResource(String resolvedTemplate, int nameRow)
		{
			super();
			super.setResolvedTemplate(resolvedTemplate);
			this.nameRow = nameRow;
		}

		public int getNameRow()
		{
			return nameRow;
		}

		@Override
		public boolean isIdempotent()
		{
			return true;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + nameRow;
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			CsvTemplateResolvedResource other = (CsvTemplateResolvedResource) obj;
			if (nameRow != other.nameRow)
				return false;
			return true;
		}
	}

	protected static class CsvResourceData
	{
		private final List<Map<String, String>> data;

		private final List<DataSetProperty> properties;

		public CsvResourceData(List<Map<String, String>> data,
				List<DataSetProperty> properties)
		{
			super();
			this.data = (data == null ? Collections.emptyList() : Collections.unmodifiableList(data));
			this.properties = (properties == null ? Collections.emptyList()
					: Collections.unmodifiableList(properties));
		}

		/**
		 * 获取数据列表。
		 * <p>
		 * 返回值及其内容不应被修改，因为可能会缓存。
		 * </p>
		 * 
		 * @return
		 */
		public List<Map<String, String>> getData()
		{
			return data;
		}

		/**
		 * 获取属性列表。
		 * <p>
		 * 返回值及其内容不应被修改，因为可能会缓存。
		 * </p>
		 * 
		 * @return
		 */
		public List<DataSetProperty> getProperties()
		{
			return properties;
		}
	}
}
