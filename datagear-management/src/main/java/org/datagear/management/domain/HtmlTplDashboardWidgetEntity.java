/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.domain;

import java.util.Date;

import org.datagear.analysis.support.html.HtmlRenderContext;
import org.datagear.analysis.support.html.HtmlTplDashboardWidget;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetFmkRenderer;

/**
 * {@linkplain HtmlTplDashboardWidget}实体。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlTplDashboardWidgetEntity extends HtmlTplDashboardWidget<HtmlRenderContext>
		implements CreateUserEntity<String>, DataPermissionEntity<String>
{
	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_TEMPLATE = "index.html";

	/** 授权资源类型 */
	public static final String AUTHORIZATION_RESOURCE_TYPE = "Dashboard";

	/** 名称 */
	private String name;

	/** 创建用户 */
	private User createUser;

	/** 创建时间 */
	private Date createTime;

	/** 权限 */
	private int dataPermission = PERMISSION_NOT_LOADED;

	public HtmlTplDashboardWidgetEntity()
	{
		super();
		this.createTime = new Date();
	}

	public HtmlTplDashboardWidgetEntity(String id, String template,
			HtmlTplDashboardWidgetFmkRenderer<HtmlRenderContext> renderer, String name, User createUser)
	{
		super(id, template, renderer);
		this.name = name;
		this.createUser = createUser;
		this.createTime = new Date();
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
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
