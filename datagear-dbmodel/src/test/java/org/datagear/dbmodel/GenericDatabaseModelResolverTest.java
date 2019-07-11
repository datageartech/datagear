/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbmodel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.datagear.connection.ConnectionSensor;
import org.datagear.dbinfo.DatabaseInfoResolver;
import org.datagear.dbinfo.DevotedDatabaseInfoResolver;
import org.datagear.dbinfo.GenericDatabaseInfoResolver;
import org.datagear.dbinfo.WildcardDevotedDatabaseInfoResolver;
import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.support.DefaultDynamicBean;
import org.datagear.model.support.DefaultModelManager;
import org.datagear.persistence.DialectBuilder;
import org.datagear.persistence.DialectSource;
import org.datagear.persistence.PersistenceManager;
import org.datagear.persistence.features.TableName;
import org.datagear.persistence.support.DefaultDialectSource;
import org.datagear.persistence.support.DefaultPersistenceManager;
import org.datagear.persistence.support.dialect.MysqlDialectBuilder;
import org.datagear.util.JdbcUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * {@linkplain GenericDatabaseModelResolver}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class GenericDatabaseModelResolverTest extends DBTestSupport
{
	protected ConnectionSensor connectionSensor;

	protected PrimitiveModelResolver primitiveModelResolver;

	protected DatabaseInfoResolver databaseInfoResolver;

	protected PersistenceManager persistenceManager;

	protected GenericDatabaseModelResolver genericDatabaseModelResolver;

	public GenericDatabaseModelResolverTest()
	{
		super();

		this.primitiveModelResolver = new TypeMapPrimitiveModelResolver(new DatabasePrimitiveModelSource());

		List<DevotedDatabaseInfoResolver> devotedDatabaseInfoResolver = new ArrayList<DevotedDatabaseInfoResolver>();
		devotedDatabaseInfoResolver.add(new WildcardDevotedDatabaseInfoResolver());
		this.databaseInfoResolver = new GenericDatabaseInfoResolver(devotedDatabaseInfoResolver);

		List<DevotedDatabaseModelResolver> devotedDatabaseModelResolver = new ArrayList<DevotedDatabaseModelResolver>();
		devotedDatabaseModelResolver
				.add(new WildcardDevotedDatabaseModelResolver(this.databaseInfoResolver, this.primitiveModelResolver));
		this.genericDatabaseModelResolver = new GenericDatabaseModelResolver(devotedDatabaseModelResolver);

		List<DialectBuilder> dialectBuilders = new ArrayList<DialectBuilder>();
		dialectBuilders.add(new MysqlDialectBuilder());
		DialectSource dialectSource = new DefaultDialectSource(this.databaseInfoResolver, dialectBuilders);
		persistenceManager = new DefaultPersistenceManager(dialectSource, null);
	}

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void resolveTest() throws Exception
	{
		Connection cn = getConnection();

		int id = 999999;

		Model model = this.genericDatabaseModelResolver.resolve(cn, new DefaultModelManager(),
				new DefaultModelManager(), "T_ORDER");

		Assert.assertEquals("T_ORDER", ((TableName) model.getFeature(TableName.class)).getValue().toUpperCase());

		DefaultDynamicBean defaultDynamicBean = new DefaultDynamicBean();
		defaultDynamicBean.put("ID", id);
		defaultDynamicBean.put("NAME", "ORDER-0");

		try
		{
			persistenceManager.insert(cn, model, defaultDynamicBean);
		}
		finally
		{
			int deleteCount = persistenceManager.delete(cn, model, defaultDynamicBean);
			Assert.assertTrue(deleteCount == 1);

			JdbcUtil.closeConnection(cn);
		}
	}

	@Test
	public void resolveResultset() throws Exception
	{
		Connection cn = getConnection();

		try
		{
			Statement st = cn.createStatement();
			ResultSet rs = st.executeQuery("select 'sdf' as name, count(*) as count_ from T_ORDER");

			Model model = this.genericDatabaseModelResolver.resolve(cn, rs, "test");
			Property[] properties = model.getProperties();

			Assert.assertEquals("test", model.getName());
			Assert.assertEquals(2, properties.length);
			Assert.assertEquals("name", properties[0].getName());
			Assert.assertEquals(String.class, properties[0].getModel().getType());
			Assert.assertEquals("count_", properties[1].getName());
			Assert.assertEquals(Long.class, properties[1].getModel().getType());
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}
	}
}
