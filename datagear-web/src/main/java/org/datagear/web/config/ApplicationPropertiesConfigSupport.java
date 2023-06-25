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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 属性配置支持类。
 * <p>
 * 子类应该添加如下注解：
 * </p>
 * <pre>
 * {@code @Configuration}
 * {@code @PropertySource(value = ApplicationPropertiesConfigSupport.PROPERTY_SOURCE_PATH, encoding = "UTF-8")}
 * </pre>
 * <p>
 * Spring会递归处理{@linkplain Configuration @Configuration}类的父类，可能会导致某些非预期的父类配置被加载，
 * 所以此类没有添加{@linkplain Configuration @Configuration}。
 * </p>
 * 
 * @author datagear@163.com
 */
public class ApplicationPropertiesConfigSupport
{
	public static final String PROPERTY_SOURCE_PATH = "classpath:org/datagear/web/application.properties";
	
	public ApplicationPropertiesConfigSupport()
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
	@ConfigurationProperties("cors")
	public List<CrossOriginPropertiesImpl> crossOriginPropertiess()
	{
		return new ArrayList<CrossOriginPropertiesImpl>();
	}

	@Bean
	public ApplicationProperties applicationProperties()
	{
		ApplicationProperties bean = createApplicationProperties();

		bean.setSqlDataSetInvalidSqlKeywords(this.sqlDataSetInvalidSqlKeywords());
		bean.setDsmanagerQueryInvalidSqlKeywords(this.dsmanagerQueryInvalidSqlKeywords());
		bean.setDsmanagerImptsqlInvalidSqlKeywords(this.dsmanagerImptsqlInvalidSqlKeywords());
		bean.setDsmanagerSqlpadReadInvalidSqlKeywords(this.dsmanagerSqlpadReadInvalidSqlKeywords());
		bean.setDsmanagerSqlpadEditInvalidSqlKeywords(this.dsmanagerSqlpadEditInvalidSqlKeywords());
		bean.setDsmanagerSqlpadDeleteInvalidSqlKeywords(this.dsmanagerSqlpadDeleteInvalidSqlKeywords());
		bean.setCrossOriginPropertiess(this.crossOriginPropertiess());

		return bean;
	}
	
	protected ApplicationProperties createApplicationProperties()
	{
		return new ApplicationPropertiesImpl();
	}

	protected static class ApplicationPropertiesImpl extends ApplicationProperties
	{
		private static final long serialVersionUID = 1L;

		public ApplicationPropertiesImpl()
		{
			super();
		}
	}

	protected static class CrossOriginPropertiesImpl extends CrossOriginProperties
	{
		private static final long serialVersionUID = 1L;

		public CrossOriginPropertiesImpl()
		{
			super();
		}

		@Override
		public void setPaths(String[] paths)
		{
			super.setPaths(paths);
		}

		@Override
		public void setAllowedOriginPatterns(String[] allowedOriginPatterns)
		{
			super.setAllowedOriginPatterns(allowedOriginPatterns);
		}

		@Override
		public void setAllowedMethods(String[] allowedMethods)
		{
			super.setAllowedMethods(allowedMethods);
		}

		@Override
		public void setAllowedHeaders(String[] allowedHeaders)
		{
			super.setAllowedHeaders(allowedHeaders);
		}

		@Override
		public void setExposedHeaders(String[] exposedHeaders)
		{
			super.setExposedHeaders(exposedHeaders);
		}

		@Override
		public void setAllowCredentials(boolean allowCredentials)
		{
			super.setAllowCredentials(allowCredentials);
		}

		@Override
		public void setMaxAge(Long maxAge)
		{
			super.setMaxAge(maxAge);
		}
	}
}
