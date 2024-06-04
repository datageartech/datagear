/*
 * Copyright 2018-present datagear.tech
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

package org.datagear.web.util;

import org.datagear.management.domain.AnalysisProject;
import org.datagear.management.domain.DataSetEntity;
import org.datagear.management.domain.FileSource;
import org.datagear.management.domain.HtmlChartWidgetEntity;
import org.datagear.management.domain.HtmlTplDashboardWidgetEntity;
import org.datagear.management.domain.DtbsSource;

/**
 * 授权资源元信息集。
 * 
 * @author datagear@163.com
 *
 */
public class AuthorizationResMetas
{
	private AuthorizationResMetaManager authorizationResMetaManager;

	public AuthorizationResMetas()
	{
		super();
	}

	public AuthorizationResMetas(AuthorizationResMetaManager authorizationResMetaManager)
	{
		super();
		this.authorizationResMetaManager = authorizationResMetaManager;
	}

	public AuthorizationResMetaManager getAuthorizationResMetaManager()
	{
		return authorizationResMetaManager;
	}

	public void setAuthorizationResMetaManager(AuthorizationResMetaManager authorizationResMetaManager)
	{
		this.authorizationResMetaManager = authorizationResMetaManager;
	}
	
	/**
	 * 注册所有授权资源元信息。
	 */
	public void register()
	{
		registerSchema();
		registerAnalysisProject();
		registerDataSet();
		registerFileSource();
		registerChart();
		registerDashboard();
	}
	
	/**
	 * 注册数据源授权资源元信息。
	 */
	protected void registerSchema()
	{
		this.authorizationResMetaManager.registerForReadTransfer(DtbsSource.AUTHORIZATION_RESOURCE_TYPE,
				"schema.auth.permission.read.desc", "schema.auth.permission.edit.desc",
				"schema.auth.permission.delete.desc");
	}
	
	/**
	 * 注册数据分析项目授权资源元信息。
	 */
	protected void registerAnalysisProject()
	{
		this.authorizationResMetaManager.registerForShare(AnalysisProject.AUTHORIZATION_RESOURCE_TYPE);
	}
	
	/**
	 * 注册数据集授权资源元信息。
	 */
	protected void registerDataSet()
	{
		this.authorizationResMetaManager.registerForShare(DataSetEntity.AUTHORIZATION_RESOURCE_TYPE);
	}

	/**
	 * 注册文件源授权资源元信息。
	 */
	protected void registerFileSource()
	{
		this.authorizationResMetaManager.registerForShare(FileSource.AUTHORIZATION_RESOURCE_TYPE);
	}
	
	/**
	 * 注册图表授权资源元信息。
	 */
	protected void registerChart()
	{
		this.authorizationResMetaManager.registerForShare(HtmlChartWidgetEntity.AUTHORIZATION_RESOURCE_TYPE);
	}
	
	/**
	 * 注册看板授权资源元信息。
	 */
	protected void registerDashboard()
	{
		this.authorizationResMetaManager.registerForShare(HtmlTplDashboardWidgetEntity.AUTHORIZATION_RESOURCE_TYPE);
	}
}