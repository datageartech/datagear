/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
