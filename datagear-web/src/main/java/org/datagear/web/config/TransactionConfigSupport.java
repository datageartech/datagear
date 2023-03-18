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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 事务配置。
 * <p>
 * 子类应该添加如下注解：
 * </p>
 * <pre>
 * {@code @Configuration}
 * {@code @ImportResource(TransactionConfigSupport.CONFIG_RESOURCE_PATH)}
 * </pre>
 * <p>
 * Spring会递归处理{@linkplain Configuration @Configuration}类的父类，可能会导致某些非预期的父类配置被加载，
 * 所以此类没有添加{@linkplain Configuration @Configuration}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class TransactionConfigSupport
{
	public static final String CONFIG_RESOURCE_PATH = "classpath:org/datagear/web/transactionConfig.xml";
	/** transactionConfig.xml中使用此Bean名 */
	public static final String TX_MANAGER_BEAN = "txManager";

	private DataSourceConfigSupport dataSourceConfig;

	@Autowired
	public TransactionConfigSupport(DataSourceConfigSupport dataSourceConfig)
	{
		this.dataSourceConfig = dataSourceConfig;
	}

	public DataSourceConfigSupport getDataSourceConfig()
	{
		return dataSourceConfig;
	}

	public void setDataSourceConfig(DataSourceConfigSupport dataSourceConfig)
	{
		this.dataSourceConfig = dataSourceConfig;
	}

	@Bean(TX_MANAGER_BEAN)
	public PlatformTransactionManager txManager()
	{
		PlatformTransactionManager bean = new DataSourceTransactionManager(this.dataSourceConfig.dataSource());
		return bean;
	}
}
