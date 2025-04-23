/*
 * Copyright 2018-present datagear.tech
 */

package org.datagear.analysis.support;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.analysis.support.datasetres.JsonDataSetResource;

/**
 * 映射表数据集结果包装器。
 * <p>
 * 此类可将数据包装处理为符合给定{@linkplain DataSet}的结果数据。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetResultWrapper
{
	/**
	 * 基本类型的数据转换至规范数据集结果数据类型的{@linkplain Map}时的属性名。
	 */
	public static final String PRIMITIVE_TO_STD_DATA_PROP_NAME = "value";

	private DataSet dataSet;

	public DataSetResultWrapper()
	{
		super();
	}

	public DataSetResultWrapper(DataSet dataSet)
	{
		super();
		this.dataSet = dataSet;
	}

	public DataSet getDataSet()
	{
		return dataSet;
	}

	public void setDataSet(DataSet dataSet)
	{
		this.dataSet = dataSet;
	}

	/**
	 * 获取指定数据对象的{@linkplain DataSetResult}。
	 * 
	 * @param data
	 *            允许{@code null}，应是基本类型、{@linkplain Map}对象，或者是它们的数组、集合。
	 * @return
	 */
	public DataSetResult getResult(Object data)
	{
		data = wrapData(data);
		InternalJsonDataSet dataSet = createInternalDataSet(data);

		return dataSet.getResult(new DataSetQuery());
	}

	/**
	 * 解析指定数据对象的{@linkplain ResolvedDataSetResult}。
	 * 
	 * @param data
	 *            允许{@code null}，应是基本类型、{@linkplain Map}对象，或者是它们的数组、集合。
	 * @return
	 */
	public ResolvedDataSetResult resolveResult(Object data)
	{
		data = wrapData(data);
		InternalJsonDataSet dataSet = createInternalDataSet(data);

		return dataSet.resolve(new DataSetQuery());
	}

	protected InternalJsonDataSet createInternalDataSet(Object data)
	{
		data = wrapData(data);
		return new InternalJsonDataSet(this.dataSet, data);
	}

	/**
	 * 包装数据，使其符合{@linkplain DataSetResult#getData()}规范。
	 * 
	 * @param data
	 *            允许{@code null}
	 * @return
	 */
	protected Object wrapData(Object data)
	{
		Object re = data;

		if (isPrimitive(data))
		{
			re = wrapAsMap(data);
		}
		else if (data instanceof Collection<?>)
		{
			Collection<?> collection = (Collection<?>) data;
			Object firstNonNull = null;

			for (Object ele : collection)
			{
				if (ele != null)
				{
					firstNonNull = ele;
					break;
				}
			}

			if (isPrimitive(firstNonNull))
			{
				re = wrapAsMapList(collection);
			}
		}
		else if (data instanceof Object[])
		{
			Object[] array = (Object[]) data;
			Object firstNonNull = null;

			for (Object ele : array)
			{
				if (ele != null)
				{
					firstNonNull = ele;
					break;
				}
			}

			if (isPrimitive(firstNonNull))
			{
				re = wrapAsMapList(array);
			}
		}

		return re;
	}

	/**
	 * 是否是需要包装的基本类型。
	 * 
	 * @param o
	 * @return
	 */
	protected boolean isPrimitive(Object o)
	{
		if (o == null)
		{
			return false;
		}
		else if (o instanceof String)
		{
			return true;
		}
		else if (o instanceof byte[])
		{
			return true;
		}
		else if (o instanceof Number || o instanceof Boolean || o instanceof Character)
		{
			return true;
		}
		else if (o instanceof java.util.Date)
		{
			return true;
		}
		else if (o instanceof java.time.temporal.Temporal)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * 将指定对象包装为{@linkplain Map}。
	 * <p>
	 * 返回{@linkplain Map}仅包含一个元素，其主键是{@linkplain #PRIMITIVE_TO_STD_DATA_PROP_NAME}。
	 * </p>
	 * 
	 * @param data
	 *            允许{@code null}
	 * @return 可能{@code null}
	 */
	public static Map<String, Object> wrapAsMap(Object data)
	{
		if (data == null)
			return null;

		return Collections.singletonMap(PRIMITIVE_TO_STD_DATA_PROP_NAME, data);
	}

	/**
	 * 将指定集合包装为{@linkplain Map}列表。
	 * <p>
	 * 返回列表的每一个元素都符合{@linkplain #wrapAsMap(Object)}规范。
	 * </p>
	 * 
	 * @param data
	 *            允许{@code null}
	 * @return 可能{@code null}
	 */
	public static List<Map<String, Object>> wrapAsMapList(Collection<?> data)
	{
		if (data == null)
			return null;

		List<Map<String, Object>> re = new ArrayList<>(data.size());
		
		for (Object ele : data)
		{
			re.add(wrapAsMap(ele));
		}

		return re;
	}

	/**
	 * 将指定数组包装为{@linkplain Map}列表。
	 * <p>
	 * 返回数组的每一个元素都符合{@linkplain #wrapAsMap(Object)}规范。
	 * </p>
	 * 
	 * @param data
	 *            允许{@code null}
	 * @return 可能{@code null}
	 */
	public static List<Map<String, Object>> wrapAsMapList(Object[] data)
	{
		if (data == null)
			return null;

		List<Map<String, Object>> re = new ArrayList<>(data.length);

		for (Object ele : data)
		{
			re.add(wrapAsMap(ele));
		}

		return re;
	}

	protected static class InternalJsonDataSet extends AbstractJsonDataSet<InternalJsonDataSetResource>
	{
		private static final long serialVersionUID = 1L;
		
		private static final InternalJsonDataSetResource DATA_SET_RESOURCE = new InternalJsonDataSetResource();

		private final Object data;

		public InternalJsonDataSet(DataSet dataSet, Object data)
		{
			super(dataSet.getId(), dataSet.getName(), dataSet.getFields());
			setMutableModel(dataSet.isMutableModel());
			setParams(dataSet.getParams());
			if (dataSet instanceof AbstractDataSet)
				setDataFormat(((AbstractDataSet) dataSet).getDataFormat());

			this.data = data;
		}

		public Object getData()
		{
			return data;
		}

		@Override
		protected Object resolveData(Reader jsonReader, String dataJsonPath) throws ReadJsonDataPathException, Throwable
		{
			return this.data;
		}

		@Override
		protected InternalJsonDataSetResource getResource(DataSetQuery query) throws Throwable
		{
			return DATA_SET_RESOURCE;
		}

		@Override
		protected void checkRequiredParamValues(DataSetQuery query) throws DataSetParamValueRequiredException
		{
			// 无需校验参数，此方法应什么也不做
			return;
		}
	}

	protected static class InternalJsonDataSetResource extends JsonDataSetResource
	{
		private static final long serialVersionUID = 1L;

		private Reader reader = new StringReader("");

		public InternalJsonDataSetResource()
		{
			super("", "");
		}

		@Override
		public Reader getReader() throws Throwable
		{
			return this.reader;
		}

		@Override
		public boolean isIdempotent()
		{
			// 这里应始终返回false
			return false;
		}
	}
}
