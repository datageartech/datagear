/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 
 */
package org.datagear.management.domain;

import java.util.Collection;
import java.util.Date;

import org.datagear.analysis.support.JsonSupport;
import org.datagear.analysis.support.html.HtmlTplDashboardWidget;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetHtmlRenderer;
import org.datagear.util.StringUtil;

/**
 * {@linkplain HtmlTplDashboardWidget}实体。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlTplDashboardWidgetEntity extends HtmlTplDashboardWidget
		implements CreateUserEntity<String>, DataPermissionEntity<String>, AnalysisProjectAwareEntity<String>
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

	public HtmlTplDashboardWidgetEntity(String id, String template, HtmlTplDashboardWidgetHtmlRenderer renderer,
			String name, User createUser)
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

	/**
	 * 返回{@linkplain #getTemplates()}的JSON。
	 * 
	 * @return
	 */
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
