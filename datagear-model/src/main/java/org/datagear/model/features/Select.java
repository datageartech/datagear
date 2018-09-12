/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.model.features;

import java.util.List;

import org.datagear.model.Label;
import org.datagear.model.PropertyFeature;

/**
 * 选项集特性。
 * <p>
 * 此特性用于限定属性值只能在特定的选项集范围内。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class Select implements PropertyFeature
{
	private List<Option> options;

	public Select()
	{
		super();
	}

	public Select(List<Option> options)
	{
		super();
		this.options = options;
	}

	/**
	 * 获取选项值列表。
	 * 
	 * @return
	 */
	public List<Option> getOptions()
	{
		return options;
	}

	/**
	 * 设置{@linkplain Option 选项值}列表。
	 * 
	 * @param options
	 */
	public void setOptions(List<Option> options)
	{
		this.options = options;
	}

	/**
	 * 选项值。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class Option
	{
		private Object value;

		private Label nameLabel;

		private Label descLabel;

		public Option()
		{
			super();
		}

		public Option(Object value)
		{
			super();
			this.value = value;
		}

		public Option(Object value, Label nameLabel)
		{
			super();
			this.value = value;
			this.nameLabel = nameLabel;
		}

		public Option(Object value, Label nameLabel, Label descLabel)
		{
			super();
			this.value = value;
			this.nameLabel = nameLabel;
			this.descLabel = descLabel;
		}

		/**
		 * 获取值。
		 * 
		 * @return
		 */
		public Object getValue()
		{
			return value;
		}

		/**
		 * 设置值。
		 * 
		 * @param value
		 */
		public void setValue(Object value)
		{
			this.value = value;
		}

		/**
		 * 获取名称标签。
		 * 
		 * @return
		 */
		public Label getNameLabel()
		{
			return nameLabel;
		}

		/**
		 * 设置名称标签。
		 * 
		 * @param nameLabel
		 */
		public void setNameLabel(Label nameLabel)
		{
			this.nameLabel = nameLabel;
		}

		/**
		 * 获取描述标签。
		 * 
		 * @return
		 */
		public Label getDescLabel()
		{
			return descLabel;
		}

		/**
		 * 设置描述标签。
		 * 
		 * @param descLabel
		 */
		public void setDescLabel(Label descLabel)
		{
			this.descLabel = descLabel;
		}
	}
}
