/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

import java.io.Serializable;

import org.datagear.util.i18n.Label;

/**
 * 数据标记。
 * <p>
 * {@linkplain ChartPlugin}使用此类标记{@linkplain DataSetFactory}产生的数据，并依此进行图表绘制。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSign implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 名称 */
	private String name;

	/** 数据集是否必须有此标记 */
	private boolean occurRequired;

	/** 数据集是否可有多个此标记 */
	private boolean occurMultiple;

	/** 名称标签 */
	private Label nameLabel;

	/** 描述标签 */
	private Label descLabel;

	public DataSign()
	{
		super();
	}

	public DataSign(String name, boolean occurRequired, boolean occurMultiple)
	{
		super();
		this.name = name;
		this.occurRequired = occurRequired;
		this.occurMultiple = occurMultiple;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isOccurRequired()
	{
		return occurRequired;
	}

	public void setOccurRequired(boolean occurRequired)
	{
		this.occurRequired = occurRequired;
	}

	public boolean isOccurMultiple()
	{
		return occurMultiple;
	}

	public void setOccurMultiple(boolean occurMultiple)
	{
		this.occurMultiple = occurMultiple;
	}

	public boolean hasNameLabel()
	{
		return (this.nameLabel != null);
	}

	public Label getNameLabel()
	{
		return nameLabel;
	}

	public void setNameLabel(Label nameLabel)
	{
		this.nameLabel = nameLabel;
	}

	public boolean hasDescLabel()
	{
		return (this.descLabel != null);
	}

	public Label getDescLabel()
	{
		return descLabel;
	}

	public void setDescLabel(Label descLabel)
	{
		this.descLabel = descLabel;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", occurRequired=" + occurRequired + ", occurMultiple="
				+ occurMultiple + ", nameLabel=" + nameLabel + ", descLabel=" + descLabel + "]";
	}
}
