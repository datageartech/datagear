/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.domain;

import java.util.Collection;
import java.util.Date;

import org.datagear.analysis.support.html.HtmlRenderContext;
import org.datagear.analysis.support.html.HtmlTplDashboardWidget;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetFmkRenderer;
import org.datagear.util.StringUtil;

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

	public static final String TEMPLATE_SPLITTER = ",";

	public static final String[] DEFAULT_TEMPLATES = { "index.html" };

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

	/**
	 * 获取{@linkplain #getTemplates()}以{@linkplain #TEMPLATE_SPLITTER}分隔符合并后的字符串。
	 * 
	 * @return
	 */
	public String getTemplate()
	{
		return concatTemplates(getTemplates());
	}

	/**
	 * 将{@code template}以{@linkplain #TEMPLATE_SPLITTER}分割后，并调用{@linkplain #setTemplates(String...)}。
	 * 
	 * @param template
	 */
	public void setTemplate(String template)
	{
		setTemplates(splitTemplates(template));
	}

	/**
	 * 返回{@linkplain #TEMPLATE_SPLITTER}分隔符合并后的字符串。
	 * 
	 * @param templates
	 * @return
	 */
	public static String concatTemplates(String... templates)
	{
		return StringUtil.concat(templates, TEMPLATE_SPLITTER);
	}

	/**
	 * 返回{@linkplain #TEMPLATE_SPLITTER}分隔符合并后的字符串。
	 * 
	 * @param templates
	 * @return
	 */
	public static String concatTemplates(Collection<String> templates)
	{
		if (templates == null)
			return "";

		String[] strs = new String[templates.size()];
		templates.toArray(strs);

		return StringUtil.concat(strs, TEMPLATE_SPLITTER);
	}

	/**
	 * 以{@linkplain #TEMPLATE_SPLITTER}分割。
	 * 
	 * @param template
	 * @return
	 */
	public static String[] splitTemplates(String template)
	{
		if (StringUtil.isEmpty(template))
			return new String[0];

		String[] templates = StringUtil.split(template, TEMPLATE_SPLITTER, true);
		return templates;
	}
}
