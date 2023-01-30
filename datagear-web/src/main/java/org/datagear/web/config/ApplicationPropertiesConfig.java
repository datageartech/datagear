/*
 * Copyright 2018-2023 datagear.tech
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

package org.datagear.web.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 属性配置。
 * 
 * @author datagear@163.com
 */
@Configuration
@PropertySource(value = "classpath:org/datagear/web/application.properties", encoding = "UTF-8")
public class ApplicationPropertiesConfig
{
	public ApplicationPropertiesConfig()
	{
	}

	@Bean
	@ConfigurationProperties("sqldataset.invalidsqlkeywords")
	public Map<String, String> sqlDataSetInvalidSqlKeywords()
	{
		return new HashMap<String, String>();
	}

	@Bean
	@ConfigurationProperties("dsmanager.query.invalidsqlkeywords")
	public Map<String, String> dsmanagerQueryInvalidSqlKeywords()
	{
		return new HashMap<String, String>();
	}

	@Bean
	@ConfigurationProperties("dsmanager.imptsql.invalidsqlkeywords")
	public Map<String, String> dsmanagerImptsqlInvalidSqlKeywords()
	{
		return new HashMap<String, String>();
	}

	@Bean
	@ConfigurationProperties("dsmanager.sqlpad.read.invalidsqlkeywords")
	public Map<String, String> dsmanagerSqlpadReadInvalidSqlKeywords()
	{
		return new HashMap<String, String>();
	}

	@Bean
	@ConfigurationProperties("dsmanager.sqlpad.edit.invalidsqlkeywords")
	public Map<String, String> dsmanagerSqlpadEditInvalidSqlKeywords()
	{
		return new HashMap<String, String>();
	}

	@Bean
	@ConfigurationProperties("dsmanager.sqlpad.delete.invalidsqlkeywords")
	public Map<String, String> dsmanagerSqlpadDeleteInvalidSqlKeywords()
	{
		return new HashMap<String, String>();
	}

	@Bean
	public ApplicationProperties applicationProperties()
	{
		ApplicationPropertiesImpl bean = new ApplicationPropertiesImpl();

		bean.setSqlDataSetInvalidSqlKeywords(this.sqlDataSetInvalidSqlKeywords());
		bean.setDsmanagerQueryInvalidSqlKeywords(this.dsmanagerQueryInvalidSqlKeywords());
		bean.setDsmanagerImptsqlInvalidSqlKeywords(this.dsmanagerImptsqlInvalidSqlKeywords());
		bean.setDsmanagerSqlpadReadInvalidSqlKeywords(this.dsmanagerSqlpadReadInvalidSqlKeywords());
		bean.setDsmanagerSqlpadEditInvalidSqlKeywords(this.dsmanagerSqlpadEditInvalidSqlKeywords());
		bean.setDsmanagerSqlpadDeleteInvalidSqlKeywords(this.dsmanagerSqlpadDeleteInvalidSqlKeywords());

		return bean;
	}

	protected static class ApplicationPropertiesImpl extends ApplicationProperties
	{
		private static final long serialVersionUID = 1L;

		public ApplicationPropertiesImpl()
		{
			super();
		}
	}
}
