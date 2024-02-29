/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.management.domain;

import java.util.Collection;
import java.util.Date;

import org.datagear.analysis.TplDashboardWidgetResManager;
import org.datagear.analysis.support.JsonSupport;
import org.datagear.analysis.support.html.HtmlTplDashboardWidget;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
import org.datagear.util.StringUtil;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@linkplain HtmlTplDashboardWidget}实体。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlTplDashboardWidgetEntity extends HtmlTplDashboardWidget
		implements CreateUserEntity<String>, DataPermissionEntity<String>, AnalysisProjectAwareEntity<String>,
		CloneableEntity
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

	private AnalysisProject analysisProject = null;

	public HtmlTplDashboardWidgetEntity()
	{
		super();
		this.createTime = new Date();
	}

	public HtmlTplDashboardWidgetEntity(String id, String template, HtmlTplDashboardWidgetRenderer renderer,
			TplDashboardWidgetResManager resManager,
			String name, User createUser)
	{
		super(id, template, renderer, resManager);
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

	@Override
	public AnalysisProject getAnalysisProject()
	{
		return analysisProject;
	}

	@Override
	public void setAnalysisProject(AnalysisProject analysisProject)
	{
		this.analysisProject = analysisProject;
	}

	@Override
	@JsonIgnore
	public String getFirstTemplate() throws IllegalStateException
	{
		return super.getFirstTemplate();
	}

	/**
	 * 返回{@linkplain #getTemplates()}的JSON。
	 * 
	 * @return
	 */
	@JsonIgnore
	public String getTemplatesJson()
	{
		String[] templates = getTemplates();

		if (templates == null)
			return "[]";

		return JsonSupport.generate(templates, "[]");
	}

	/**
	 * 设置{@linkplain #setTemplates(String...)}的JSON。
	 * 
	 * @param json
	 */
	public void setTemplatesJson(String json)
	{
		if (StringUtil.isEmpty(json))
			setTemplates(new String[0]);

		json = json.trim();

		// XXX 兼容非JSON格式，比如旧版数据库中已存储的格式
		if (!json.startsWith("[") && !json.endsWith("]"))
			setTemplates(splitTemplates(json));
		else
		{
			String[] templates = JsonSupport.parse(json, String[].class, null);
			if (templates == null)
				templates = new String[0];

			setTemplates(templates);
		}
	}

	/**
	 * 获取{@linkplain #getTemplates()}以{@linkplain #TEMPLATE_SPLITTER}分隔符合并后的字符串。
	 * 
	 * @return
	 */
	@JsonIgnore
	public String getTemplatesSplit()
	{
		return concatTemplates(getTemplates());
	}

	/**
	 * 将{@code template}以{@linkplain #TEMPLATE_SPLITTER}分割后，并调用{@linkplain #setTemplates(String...)}。
	 * 
	 * @param template
	 */
	public void setTemplateSplit(String template)
	{
		setTemplates(splitTemplates(template));
	}

	@Override
	public HtmlTplDashboardWidgetEntity clone()
	{
		HtmlTplDashboardWidgetEntity entity = new HtmlTplDashboardWidgetEntity();
		BeanUtils.copyProperties(this, entity);

		return entity;
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
