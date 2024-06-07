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

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.datagear.analysis.DataSetField;
import org.datagear.analysis.ResolvableDataSet;
import org.datagear.analysis.support.AbstractCsvDataSet.CsvDataSetResource;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;

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

	public AbstractCsvDataSet(String id, String name, List<DataSetField> fields)
	{
		super(id, name, fields);
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
	protected ResourceData resolveResourceData(T resource, boolean resolveFields) throws Throwable
	{
		Reader reader = null;

		try
		{
			reader = resource.getReader();

			CSVParser csvParser = buildCSVParser(reader);
			List<CSVRecord> csvRecords = csvParser.getRecords();

			List<String> fieldNames = resolveFieldNames(resource, csvRecords);
			List<Map<String, String>> data = resolveData(resource, fieldNames, csvRecords);

			List<DataSetField> fields = null;

			if (resolveFields)
				fields = resolveFields(fieldNames, data);

			return new ResourceData(data, fields);
		}
		finally
		{
			IOUtil.close(reader);
		}
	}

	/**
	 * 解析字段名。
	 * 
	 * @param resource
	 * @param csvRecords
	 * @return
	 * @throws Throwable
	 */
	protected List<String> resolveFieldNames(CsvDataSetResource resource,
			List<CSVRecord> csvRecords) throws Throwable
	{
		List<String> fieldNames = null;

		for (int i = 0, len = csvRecords.size(); i < len; i++)
		{
			CSVRecord csvRecord = csvRecords.get(i);

			if (resource.isNameRow(i))
			{
				int size = csvRecord.size();
				fieldNames = new ArrayList<String>(csvRecord.size());

				for (int j = 0; j < size; j++)
					fieldNames.add(csvRecord.get(j));

				break;
			}
			else
			{
				if (fieldNames == null)
				{
					int size = csvRecord.size();
					fieldNames = new ArrayList<String>(csvRecord.size());

					for (int j = 0; j < size; j++)
						fieldNames.add(Integer.toString(j + 1));
				}

				if (resource.isAfterNameRow(i))
					break;
			}
		}

		if (fieldNames == null)
			fieldNames = Collections.emptyList();

		return fieldNames;
	}

	/**
	 * 解析数据。
	 * 
	 * @param resource
	 * @param fieldNames
	 * @param csvRecords
	 * @return
	 * @throws Throwable
	 */
	protected List<Map<String, String>> resolveData(CsvDataSetResource resource, List<String> fieldNames,
			List<CSVRecord> csvRecords) throws Throwable
	{
		List<Map<String, String>> data = new ArrayList<>();

		for (int i = 0, len = csvRecords.size(); i < len; i++)
		{
			if (resource.isNameRow(i))
				continue;

			Map<String, String> row = new HashMap<>();

			CSVRecord csvRecord = csvRecords.get(i);
			for (int j = 0, jlen = Math.min(csvRecord.size(), fieldNames.size()); j < jlen; j++)
			{
				String name = fieldNames.get(j);
				String value = csvRecord.get(j);

				row.put(name, value);
			}

			data.add(row);
		}

		return data;
	}

	/**
	 * 解析{@linkplain DataSetField}。
	 * 
	 * @param fieldNames
	 * @param data              允许为{@code null}
	 * @return
	 * @throws Throwable
	 */
	protected List<DataSetField> resolveFields(List<String> fieldNames, List<Map<String, String>> data)
			throws Throwable
	{
		int fieldLen = fieldNames.size();
		List<DataSetField> fields = new ArrayList<>(fieldLen);
	
		for (String name : fieldNames)
			fields.add(new DataSetField(name, DataSetField.DataType.STRING));
	
		// 根据数据格式，修订可能的数值类型：
		// 如果某一列至少有一个非空字符串、且非空字符串都是数值格式，才认为是数值类型
		if (data != null && data.size() > 0)
		{
			Boolean[] isNumbers = new Boolean[fieldLen];
	
			for (Map<String, String> row : data)
			{
				for (int i = 0; i < fieldLen; i++)
				{
					if (Boolean.FALSE.equals(isNumbers[i]))
						continue;
	
					String value = row.get(fieldNames.get(i));

					if (StringUtil.isEmpty(value))
						continue;

					isNumbers[i] = isNumberString(value);
				}
			}
	
			for (int i = 0; i < fieldLen; i++)
			{
				if (Boolean.TRUE.equals(isNumbers[i]))
					fields.get(i).setType(DataSetField.DataType.NUMBER);
			}
		}
	
		return fields;
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
