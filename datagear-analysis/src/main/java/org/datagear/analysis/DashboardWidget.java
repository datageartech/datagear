/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis;

/**
 * 看板部件。
 * <p>
 * 它可在{@linkplain RenderContext}中渲染自己所描述的{@linkplain Dashboard}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class DashboardWidget<T extends RenderContext> extends AbstractIdentifiable
{
	/** 名称 */
	private String name;

	public DashboardWidget()
	{
		super();
	}

	public DashboardWidget(String id, String name)
	{
		super(id);
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * 渲染{@linkplain Dashboard}。
	 * 
	 * @param renderContext
	 * @return
	 * @throws RenderException
	 */
	public abstract Dashboard render(T renderContext) throws RenderException;
}
