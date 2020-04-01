/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.datagear.connection.ConnectionIdentity;
import org.datagear.connection.ConnectionOption;
import org.datagear.connection.DatabaseIdentity;
import org.datagear.meta.Column;
import org.datagear.meta.SimpleTable;
import org.datagear.meta.resolver.DBMetaResolver;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.DialectBuilder;
import org.datagear.persistence.DialectException;
import org.datagear.persistence.DialectSource;
import org.datagear.persistence.Order;
import org.datagear.persistence.support.dialect.MysqlDialectBuilder;
import org.datagear.persistence.support.dialect.OracleDialectBuilder;
import org.datagear.persistence.support.dialect.SqlServerDialectBuilder;
import org.datagear.util.Sql;

/**
 * 默认{@linkplain DialectSource}。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultDialectSource extends PersistenceSupport implements DialectSource
{
	private DBMetaResolver dbMetaResolver;

	private List<DialectBuilder> dialectBuilders;

	private boolean detection = true;

	private ConcurrentMap<Object, DialectBuilder> dialectBuilderCache = new ConcurrentHashMap<>();

	public DefaultDialectSource()
	{
		this(null);
	}

	public DefaultDialectSource(DBMetaResolver dbMetaResolver)
	{
		super();
		this.dbMetaResolver = dbMetaResolver;
		this.dialectBuilders = new ArrayList<>();

		this.dialectBuilders.add(new MysqlDialectBuilder());
		this.dialectBuilders.add(new OracleDialectBuilder());
		this.dialectBuilders.add(new SqlServerDialectBuilder());
	}

	public DBMetaResolver getDbMetaResolver()
	{
		return dbMetaResolver;
	}

	public void setDbMetaResolver(DBMetaResolver dbMetaResolver)
	{
		this.dbMetaResolver = dbMetaResolver;
	}

	public List<DialectBuilder> getDialectBuilders()
	{
		return dialectBuilders;
	}

	public void setDialectBuilders(List<DialectBuilder> dialectBuilders)
	{
		this.dialectBuilders = dialectBuilders;
	}

	public boolean isDetection()
	{
		return detection;
	}

	public void setDetection(boolean detection)
	{
		this.detection = detection;
	}

	@Override
	public Dialect getDialect(Connection cn) throws DialectException
	{
		if (this.dialectBuilders != null)
		{
			for (DialectBuilder dialectBuilder : this.dialectBuilders)
			{
				if (dialectBuilder.supports(cn))
					return dialectBuilder.build(cn);
			}
		}

		if (this.detection)
		{
			Dialect detectiveDialect = getDetectiveDialect(cn);

			if (detectiveDialect != null)
				return detectiveDialect;
		}

		throw new UnsupportedDialectException(ConnectionOption.valueOfNonNull(cn));
	}

	/**
	 * 试探{@linkplain Dialect}。
	 * 
	 * @param cn
	 * @return
	 * @throws DialectException
	 */
	protected Dialect getDetectiveDialect(Connection cn) throws DialectException
	{
		try
		{
			Object cacheKey = getDialectBuilderCacheKey(cn);

			DialectBuilder cached = (cacheKey == null ? null : this.dialectBuilderCache.get(cacheKey));

			if (cached != null)
				return cached.build(cn);
			else
			{
				DatabaseMetaData databaseMetaData = cn.getMetaData();

				CombinedDialectBuilder combinedDialectBuilder = new CombinedDialectBuilder();

				if (this.dialectBuilders != null)
				{
					TestInfo testInfo = buildTestInfo(cn, databaseMetaData);

					if (testInfo != null)
					{
						for (DialectBuilder dialectBuilder : this.dialectBuilders)
						{
							Dialect dialect = null;
							try
							{
								dialect = dialectBuilder.build(cn);
							}
							catch(Throwable e)
							{
								dialect = null;
							}

							// 试探能够成功的分页实现
							if (dialect != null && combinedDialectBuilder.getToPagingQuerySqlDialectBuilder() == null)
							{
								try
								{
									if (testDialectToPagingSql(cn, databaseMetaData, testInfo, dialect))
										combinedDialectBuilder.setToPagingQuerySqlDialectBuilder(dialectBuilder);
								}
								catch(Throwable e)
								{
								}
							}
						}
					}
				}

				if (cacheKey != null)
					this.dialectBuilderCache.putIfAbsent(cacheKey, combinedDialectBuilder);

				return combinedDialectBuilder.build(cn);
			}
		}
		catch (SQLException e)
		{
			throw new DialectException(e);
		}
	}

	/**
	 * 获取连接的缓存KEY。
	 * 
	 * @param cn
	 * @return 返回{@code null}表示无法构建
	 */
	protected Object getDialectBuilderCacheKey(Connection cn)
	{
		// 先使用数据库级的关键字，因为数据库级的Dialect都相同
		Object key = DatabaseIdentity.valueOf(cn);

		// 否则，采用连接级的关键字
		if (key == null)
		{
			ConnectionOption connectionOption = ConnectionOption.valueOf(cn);

			if (connectionOption != null)
				key = ConnectionIdentity.valueOf(connectionOption);
		}

		return key;
	}

	/**
	 * 构建测试信息。
	 * <p>
	 * 如果数据库中没有表存在，此方法将返回{@code null}。
	 * </p>
	 * 
	 * @param cn
	 * @param databaseMetaData
	 * @return
	 */
	protected TestInfo buildTestInfo(Connection cn, DatabaseMetaData databaseMetaData)
	{
		SimpleTable table = this.dbMetaResolver.getRandomSimpleTable(cn);

		if (table == null)
			return null;

		Column column = this.dbMetaResolver.getRandomColumn(cn, table.getName());

		if (column == null)
			return null;

		return new TestInfo(table.getName(), column.getName());
	}

	/**
	 * 测试{@linkplain Dialect#toPagingSql(Sql, Sql, Order[], long, int)}方法。
	 * 
	 * @param cn
	 * @param databaseMetaData
	 * @param testInfo
	 * @param dialect
	 * @return
	 * @throws Exception
	 */
	protected boolean testDialectToPagingSql(Connection cn, DatabaseMetaData databaseMetaData, TestInfo testInfo,
			Dialect dialect) throws Exception
	{
		String identifierQuote = databaseMetaData.getIdentifierQuoteString();

		String tableQuote = identifierQuote + testInfo.getTableName() + identifierQuote;
		String columnName = identifierQuote + testInfo.getOrderColumnName() + identifierQuote;

		Sql query = Sql.valueOf();
		query.sql("SELECT ").sql(columnName).sql(" FROM ").sql(tableQuote);

		Order[] orders = Order.asArray(Order.valueOf(columnName, Order.ASC));

		Sql pagingQuerySql = dialect.toPagingQuerySql(query, orders, 1, 5);

		executeQuery(cn, pagingQuerySql, ResultSet.TYPE_FORWARD_ONLY);

		return true;
	}

	protected static class TestInfo
	{
		private String tableName;

		private String orderColumnName;

		public TestInfo()
		{
			super();
		}

		public TestInfo(String tableName, String orderColumnName)
		{
			super();
			this.tableName = tableName;
			this.orderColumnName = orderColumnName;
		}

		public String getTableName()
		{
			return tableName;
		}

		public void setTableName(String tableName)
		{
			this.tableName = tableName;
		}

		public String getOrderColumnName()
		{
			return orderColumnName;
		}

		public void setOrderColumnName(String orderColumnName)
		{
			this.orderColumnName = orderColumnName;
		}
	}

	protected static class CombinedDialect extends AbstractDialect
	{
		private Dialect toPagingQuerySqlDialect;

		public CombinedDialect()
		{
			super();
		}

		public CombinedDialect(String identifierQuote)
		{
			super(identifierQuote);
		}

		public Dialect getToPagingQuerySqlDialect()
		{
			return toPagingQuerySqlDialect;
		}

		public void setToPagingQuerySqlDialect(Dialect toPagingQuerySqlDialect)
		{
			this.toPagingQuerySqlDialect = toPagingQuerySqlDialect;
		}

		@Override
		public boolean supportsPagingSql()
		{
			return (this.toPagingQuerySqlDialect != null);
		}

		@Override
		public Sql toPagingQuerySql(Sql query, Order[] orders, long startRow, int count)
		{
			if (this.toPagingQuerySqlDialect == null)
				return null;

			return this.toPagingQuerySqlDialect.toPagingQuerySql(query, orders, startRow, count);
		}
	}

	protected static class CombinedDialectBuilder extends AbstractDialectBuilder
	{
		private DialectBuilder toPagingQuerySqlDialectBuilder;

		public CombinedDialectBuilder()
		{
			super();
		}

		public DialectBuilder getToPagingQuerySqlDialectBuilder()
		{
			return toPagingQuerySqlDialectBuilder;
		}

		public void setToPagingQuerySqlDialectBuilder(DialectBuilder toPagingQuerySqlDialectBuilder)
		{
			this.toPagingQuerySqlDialectBuilder = toPagingQuerySqlDialectBuilder;
		}

		@Override
		public Dialect build(Connection cn) throws DialectException
		{
			CombinedDialect dialect = new CombinedDialect();

			dialect.setIdentifierQuote(getIdentifierQuote(cn));

			if (this.toPagingQuerySqlDialectBuilder != null)
				dialect.setToPagingQuerySqlDialect(this.toPagingQuerySqlDialectBuilder.build(cn));

			return dialect;
		}

		@Override
		public boolean supports(Connection cn)
		{
			return false;
		}
	}
}
