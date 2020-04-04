/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.util.i18n;

/**
 * 抽象可标签对象。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractLabeled implements Labeled
{
	private Label nameLabel;

	private Label descLabel;

	public AbstractLabeled()
	{
		super();
	}

	public boolean hasNameLabel()
	{
		return (this.nameLabel != null);
	}

	@Override
	public Label getNameLabel()
	{
		return nameLabel;
	}

	@Override
	public void setNameLabel(Label nameLabel)
	{
		this.nameLabel = nameLabel;
	}

	public boolean hasDescLabel()
	{
		return (this.descLabel != null);
	}

	@Override
	public Label getDescLabel()
	{
		return descLabel;
	}

	@Override
	public void setDescLabel(Label descLabel)
	{
		this.descLabel = descLabel;
	}
}
