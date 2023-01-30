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

package org.datagear.analysis;

import java.io.Serializable;

/**
 * 数据集参数。
 * <p>
 * 此类描述{@linkplain DataSet#getResult(DataSetQuery)}的{@linkplain DataSetQuery#setParamValues(java.util.Map)}的参数元信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetParam extends AbstractNameTypeAware implements NameTypeInputAware, Serializable
{
	private static final long serialVersionUID = 1L;

	/** 是否必须 */
	private boolean required;

	/** 参数描述 */
	private String desc;

	/** 界面输入框类型 */
	private String inputType;

	/** 界面输入框载荷，比如：输入框为下拉选择时，定义选项内容JSON；输入概况为日期时，定义日期格式 */
	private String inputPayload;

	public DataSetParam()
	{
		super();
	}

	public DataSetParam(String name, String type, boolean required)
	{
		super(name, type);
		this.required = required;
	}

	@Override
	public boolean isRequired()
	{
		return required;
	}

	public void setRequired(boolean required)
	{
		this.required = required;
	}

	public boolean hasDesc()
	{
		return (this.desc != null && !this.desc.isEmpty());
	}

	public String getDesc()
	{
		return desc;
	}

	public void setDesc(String desc)
	{
		this.desc = desc;
	}

	@Override
	public String getInputType()
	{
		return inputType;
	}

	public void setInputType(String inputType)
	{
		this.inputType = inputType;
	}

	@Override
	public String getInputPayload()
	{
		return inputPayload;
	}

	public void setInputPayload(String inputPayload)
	{
		this.inputPayload = inputPayload;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [required=" + required + ", desc=" + desc + ", inputType=" + inputType
				+ ", inputPayload=" + inputPayload + "]";
	}

	/**
	 * {@linkplain DataSetParam#getType()}枚举。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class DataType
	{
		/** 字符串 */
		public static final String STRING = "STRING";

		/** 布尔值 */
		public static final String BOOLEAN = "BOOLEAN";

		/** 数值 */
		public static final String NUMBER = "NUMBER";
	}

	/**
	 * 常用的{@linkplain DataSetParam#getInputType()}枚举。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class InputType
	{
		/** 文本框 */
		public static final String TEXT = "text";

		/** 下拉框 */
		public static final String SELECT = "select";

		/** 单选框 */
		public static final String RADIO = "radio";

		/** 复选框 */
		public static final String CHECKBOX = "checkbox";

		/** 文本域 */
		public static final String TEXTAREA = "textarea";
		
		/** 日期 */
		public static final String DATE = "date";

		/** 时间 */
		public static final String TIME = "time";

		/** 日期时间 */
		public static final String DATETIME = "datetime";
	}
}
