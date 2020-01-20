/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetExport;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataType;
import org.datagear.util.StringUtil;

/**
 * 宏{@linkplain DataSetExport}。
 * <p>
 * 此类用于通过宏定义{@linkplain #getExportValue(DataSetMeta, List)}逻辑。
 * </p>
 * <p>
 * 它支持的宏如下所示：
 * </p>
 * <ul>
 * <li>FIRST(<i>columnName</i>)：第一个<i>columnName</i>列值；</li>
 * <li>LAST(<i>columnName</i>)：最后一个<i>columnName</i>列值；</li>
 * <li>MAX(<i>columnName</i>)：最大<i>columnName</i>列值；</li>
 * <li>MIN(<i>columnName</i>)：最小<i>columnName</i>列值；</li>
 * <li>AVG(<i>columnName</i>)：平均<i>columnName</i>列值；</li>
 * <li>SUM(<i>columnName</i>)：总和<i>columnName</i>列值；</li>
 * </ul>
 * <p>
 * “FIRST”、“LAST”、“MAX”、“MIN”、“AVG”、“SUM”不区分大小写，<i>columnName</i>必须在所属的{@linkplain DataSet#getExports()}中。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class MacroDataSetExport extends DataSetExport
{
	private static final long serialVersionUID = 1L;

	public static final String LEFT_TOKEN = "(";

	public static final String RIGHT_TOKEN = ")";

	/** 宏：第一个 */
	public static final String MACRO_FIRST = "FIRST";

	/** 宏：最后一个 */
	public static final String MACRO_LAST = "LAST";

	/** 宏：最大值 */
	public static final String MACRO_MAX = "MAX";

	/** 宏：最小值 */
	public static final String MACRO_MIN = "MIN";

	/** 宏：平均值 */
	public static final String MACRO_AVG = "AVG";

	/** 宏：总和 */
	public static final String MACRO_SUM = "SUM";

	/** 宏 */
	private String macro;

	public MacroDataSetExport()
	{
	}

	public MacroDataSetExport(String name, DataType type, String macro)
	{
		super(name, type);
		this.macro = macro;
	}

	public String getMacro()
	{
		return macro;
	}

	public void setMacro(String macro)
	{
		this.macro = macro;
	}

	@Override
	public Object getExportValue(DataSet dataSet, DataSetResult dataSetResult) throws DataSetException
	{
		Macro macro = resolveMacro(getMacro());
		return getExportValue(dataSet, dataSetResult, macro, getMacro());
	}

	/**
	 * 获取指定宏的输出值。
	 * 
	 * @param dataSet
	 * @param dataSetResult
	 * @param macro
	 * @param macroString
	 * @return
	 * @throws DataSetException
	 */
	protected Object getExportValue(DataSet dataSet, DataSetResult dataSetResult, Macro macro, String macroString)
			throws DataSetException
	{
		String macroName = macro.getName();
		String propertyName = macro.getProperty();
		List<?> datas = dataSetResult.getDatas();
		boolean isEmptyData = (datas == null || datas.isEmpty());

		if (MACRO_FIRST.equalsIgnoreCase(macroName))
		{
			if (isEmptyData)
				return null;

			getDataSetPropertyNotNull(dataSet, propertyName);

			Object first = datas.get(0);
			return dataSetResult.getDataPropertyValue(first, propertyName);
		}
		else if (MACRO_LAST.equalsIgnoreCase(macroName))
		{
			if (isEmptyData)
				return null;

			getDataSetPropertyNotNull(dataSet, propertyName);

			Object last = datas.get(datas.size() - 1);
			return dataSetResult.getDataPropertyValue(last, propertyName);
		}
		else if (MACRO_MAX.equalsIgnoreCase(macroName))
		{
			if (isEmptyData)
				return null;

			DataSetProperty property = getDataSetPropertyNotNull(dataSet, propertyName);

			if (!DataType.isNumber(property.getType()))
				throw new MacroEvalException(macroString, "Property [" + propertyName + "] must be number type");

			BigDecimal max = null;

			for (int i = 0, len = datas.size(); i < len; i++)
			{
				Object row = datas.get(i);
				BigDecimal my = DataType.castBigDecimal(dataSetResult.getDataPropertyValue(row, propertyName));

				if (my == null)
					continue;

				max = (max == null ? my : max.max(my));
			}

			return max;
		}
		else if (MACRO_MIN.equalsIgnoreCase(macroName))
		{
			if (isEmptyData)
				return null;

			DataSetProperty property = getDataSetPropertyNotNull(dataSet, propertyName);

			if (!DataType.isNumber(property.getType()))
				throw new MacroEvalException(macroString, "Property [" + propertyName + "] must be number type");

			BigDecimal min = null;

			for (int i = 0, len = datas.size(); i < len; i++)
			{
				Object row = datas.get(i);
				BigDecimal my = DataType.castBigDecimal(dataSetResult.getDataPropertyValue(row, propertyName));

				if (my == null)
					continue;

				min = (min == null ? my : min.min(my));
			}

			return min;
		}
		else if (MACRO_AVG.equalsIgnoreCase(macroName))
		{
			if (isEmptyData)
				return null;

			DataSetProperty columnMeta = getDataSetPropertyNotNull(dataSet, propertyName);

			if (!DataType.isNumber(columnMeta.getType()))
				throw new MacroEvalException(macroString, "Property [" + propertyName + "] must be number type");

			BigDecimal avg = BigDecimal.valueOf(0);

			int len = datas.size();
			for (int i = 0; i < len; i++)
			{
				Object row = datas.get(i);
				BigDecimal my = DataType.castBigDecimal(dataSetResult.getDataPropertyValue(row, propertyName));

				if (my == null)
					continue;

				avg = avg.add(my);
			}

			avg = avg.divide(BigDecimal.valueOf(len));

			return avg;
		}
		else if (MACRO_SUM.equalsIgnoreCase(macroName))
		{
			if (isEmptyData)
				return null;

			DataSetProperty columnMeta = getDataSetPropertyNotNull(dataSet, propertyName);

			if (!DataType.isNumber(columnMeta.getType()))
				throw new MacroEvalException(macroString, "Property [" + propertyName + "] must be number type");

			BigDecimal sum = BigDecimal.valueOf(0);

			for (int i = 0, len = datas.size(); i < len; i++)
			{
				Object row = datas.get(i);
				BigDecimal my = DataType.castBigDecimal(dataSetResult.getDataPropertyValue(row, propertyName));

				if (my == null)
					continue;

				sum = sum.add(my);
			}

			return sum;
		}
		else
			return getExportValueExt(dataSet, dataSetResult, macro, macroString);
	}

	/**
	 * 获取扩展宏的输出值。
	 * 
	 * @param dataSet
	 * @param dataSetResult
	 * @param macro
	 * @param macroString
	 * @return
	 * @throws DataSetException
	 * @throws UnsupportedMacroException
	 */
	protected Object getExportValueExt(DataSet dataSet, DataSetResult dataSetResult, Macro macro, String macroString)
			throws DataSetException, UnsupportedMacroException
	{
		throw new UnsupportedMacroException(macroString);
	}

	/**
	 * 获取指定名称的{@linkplain ColumnMeta}，没有则抛出异常。
	 * 
	 * @param dataSet
	 * @param name
	 * @return
	 */
	protected DataSetProperty getDataSetPropertyNotNull(DataSet dataSet, String name)
			throws DataSetPropertyNotFoundException
	{
		DataSetProperty columnMeta = dataSet.getProperty(name);

		if (columnMeta == null)
			throw new DataSetPropertyNotFoundException(name);

		return columnMeta;
	}

	/**
	 * 解析{@linkplain Macro}对象。
	 * <p>
	 * 此方法不会返回{@code null}，如果格式非法，它返回的{@linkplain Macro#getName()}、{@linkplain Macro#getProperty()}将是空字符串。
	 * </p>
	 * 
	 * @param macro
	 * @return
	 */
	protected Macro resolveMacro(String macro)
	{
		String name = "";
		String column = "";

		if (!StringUtil.isEmpty(macro))
		{
			int leftBracketIdx = macro.indexOf(LEFT_TOKEN);
			int rightBracketIdx = (leftBracketIdx >= 0
					? macro.indexOf(LEFT_TOKEN + LEFT_TOKEN.length() + 1, leftBracketIdx + 1)
					: -1);

			if (leftBracketIdx > 0)
				name = macro.substring(0, leftBracketIdx);

			if (leftBracketIdx >= 0 && rightBracketIdx > leftBracketIdx + LEFT_TOKEN.length() + 1)
				column = macro.substring(leftBracketIdx + LEFT_TOKEN.length(), rightBracketIdx);
		}

		return new Macro(name.trim(), column.trim());
	}

	/**
	 * 构建宏。
	 * 
	 * @param macroName
	 * @param columnName
	 * @return
	 */
	public static String macroOf(String macroName, String columnName)
	{
		return macroName + LEFT_TOKEN + columnName + RIGHT_TOKEN;
	}

	protected static class Macro implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String name;

		private String property;

		public Macro()
		{
			super();
		}

		public Macro(String name, String property)
		{
			super();
			this.name = name;
			this.property = property;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getProperty()
		{
			return property;
		}

		public void setProperty(String property)
		{
			this.property = property;
		}

		@Override
		public String toString()
		{
			return "Macro [name=" + name + ", property=" + property + "]";
		}
	}
}
