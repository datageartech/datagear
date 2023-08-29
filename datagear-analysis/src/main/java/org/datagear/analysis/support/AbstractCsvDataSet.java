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
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.ResolvableDataSet;
import org.datagear.analysis.support.AbstractCsvDataSet.CsvDataSetResource;
import org.datagear.util.IOUtil;

/**
 * 抽象CSV数据集。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractCsvDataSet<T extends CsvDataSetResource> extends AbstractResolvableResourceDataSet<T>
		implements ResolvableDataSet
{
	private static final long serialVersionUID = 1L;

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

	@Override
	protected ResourceData resolveResourceData(T resource) throws Throwable
	{
		Reader reader = null;

		try
		{
			reader = resource.getReader();

			CSVParser csvParser = buildCSVParser(reader);
			List<CSVRecord> csvRecords = csvParser.getRecords();

			List<String> propertyNames = resolvePropertyNames(resource, csvRecords);
			List<Map<String, String>> data = resolveData(resource, propertyNames, csvRecords);
			List<DataSetProperty> properties = resolveProperties(propertyNames, data);

			return new ResourceData(data, properties);
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
	protected List<String> resolvePropertyNames(CsvDataSetResource resource,
			List<CSVRecord> csvRecords) throws Throwable
	{
		List<String> propertyNames = null;

		for (int i = 0, len = csvRecords.size(); i < len; i++)
		{
			CSVRecord csvRecord = csvRecords.get(i);

			if (resource.isNameRow(i))
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

				if (resource.isAfterNameRow(i))
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
	protected List<Map<String, String>> resolveData(CsvDataSetResource resource, List<String> propertyNames,
			List<CSVRecord> csvRecords) throws Throwable
	{
		List<Map<String, String>> data = new ArrayList<>();

		for (int i = 0, len = csvRecords.size(); i < len; i++)
		{
			if (resource.isNameRow(i))
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
	 * CSV数据集资源。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static abstract class CsvDataSetResource extends DataSetResource
	{
		private static final long serialVersionUID = 1L;
		
		private int nameRow;

		public CsvDataSetResource()
		{
			super();
		}

		public CsvDataSetResource(String resolvedTemplate, int nameRow)
		{
			super(resolvedTemplate);
			this.nameRow = nameRow;
		}

		public int getNameRow()
		{
			return nameRow;
		}

		public void setNameRow(int nameRow)
		{
			this.nameRow = nameRow;
		}

		/**
		 * 是否名称行。
		 * 
		 * @param rowIndex 行索引（以{@code 0}计数）
		 * @return
		 */
		public boolean isNameRow(int rowIndex)
		{
			return ((rowIndex + 1) == this.nameRow);
		}

		/**
		 * 是否在名称行之后。
		 * <p>
		 * 如果没有名称行，应返回{@code true}。
		 * </p>
		 * 
		 * @param rowIndex 行索引（以{@code 0}计数）
		 * @return
		 */
		public boolean isAfterNameRow(int rowIndex)
		{
			return ((rowIndex + 1) > this.nameRow);
		}

		/**
		 * 获取CSV输入流。
		 * <p>
		 * 输入流应该在此方法内创建，而不应该在实例内创建，因为采用缓存后不会每次都调用此方法。
		 * </p>
		 * 
		 * @return
		 * @throws Throwable
		 */
		public abstract Reader getReader() throws Throwable;

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
			CsvDataSetResource other = (CsvDataSetResource) obj;
			if (nameRow != other.nameRow)
				return false;
			return true;
		}
	}
}
