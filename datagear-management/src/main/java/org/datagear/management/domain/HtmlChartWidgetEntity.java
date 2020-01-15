/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.domain;

import java.util.Date;

import org.datagear.analysis.DataSetFactory;
import org.datagear.analysis.support.ChartWidget;
import org.datagear.analysis.support.SqlDataSetFactory;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlChartWidget;
import org.datagear.analysis.support.html.HtmlRenderContext;

/**
 * HTML {@linkplain ChartWidget}实体。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartWidgetEntity extends HtmlChartWidget<HtmlRenderContext>
		implements CreateUserEntity<String>, DataPermissionEntity<String>
{
	private static final long serialVersionUID = 1L;

	/** 授权资源类型 */
	public static final String AUTHORIZATION_RESOURCE_TYPE = "Chart";

	/** 创建用户 */
	private User createUser;

	/** 创建时间 */
	private Date createTime;

	/** 权限 */
	private int dataPermission = PERMISSION_NOT_LOADED;

	public HtmlChartWidgetEntity()
	{
		super();
		super.setDataSetFactories(new SqlDataSetFactoryEntity[0]);
		this.createTime = new Date();
	}

	public HtmlChartWidgetEntity(String id, String name, HtmlChartPlugin<HtmlRenderContext> chartPlugin,
			SqlDataSetFactory[] dataSetFactories, User createUser)
	{
		super(id, name, chartPlugin, dataSetFactories);
		this.createUser = createUser;
		this.createTime = new Date();
	}

	public HtmlChartPlugin<HtmlRenderContext> getHtmlChartPlugin()
	{
		return getChartPlugin();
	}

	public void setHtmlChartPlugin(HtmlChartPlugin<HtmlRenderContext> htmlChartPlugin)
	{
		setChartPlugin(htmlChartPlugin);
	}

	@Override
	public SqlDataSetFactoryEntity[] getDataSetFactories()
	{
		return (SqlDataSetFactoryEntity[]) super.getDataSetFactories();
	}

	@Override
	public void setDataSetFactories(DataSetFactory[] dataSetFactories)
	{
		if (dataSetFactories != null && !(dataSetFactories instanceof SqlDataSetFactoryEntity[]))
			throw new IllegalArgumentException();

		super.setDataSetFactories(dataSetFactories);
	}

	public SqlDataSetFactoryEntity[] getSqlDataSetFactoryEntities()
	{
		return getDataSetFactories();
	}

	public void setSqlDataSetFactoryEntities(SqlDataSetFactoryEntity[] sqlDataSetFactoryEntities)
	{
		setDataSetFactories(sqlDataSetFactoryEntities);
	}

	@Override
	public User getCreateUser()
	{
		return createUser;
	}

	@Override
	public void setCreateUser(User createUser)
	{
		this.createUser = createUser;
	}

	public Date getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(Date createTime)
	{
		this.createTime = createTime;
	}

	@Override
	public int getDataPermission()
	{
		return dataPermission;
	}

	@Override
	public void setDataPermission(int dataPermission)
	{
		this.dataPermission = dataPermission;
	}
}
