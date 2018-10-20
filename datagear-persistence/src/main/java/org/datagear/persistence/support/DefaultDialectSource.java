/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.datagear.connection.ConnectionOption;
import org.datagear.connection.JdbcUtil;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;
import org.datagear.dbinfo.TableInfo;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.DialectBuilder;
import org.datagear.persistence.DialectException;
import org.datagear.persistence.DialectSource;
import org.datagear.persistence.Order;
import org.datagear.persistence.SqlBuilder;
import org.datagear.persistence.UnsupportedDialectException;

/**
 * 默认{@linkplain DialectSource}。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultDialectSource implements DialectSource
{
	protected static final String[] TABLE_TYPES = { "TABLE", "VIEW", "ALIAS" };

	protected static final String COLUMN_TABLE_NAME = "TABLE_NAME";

	protected static final String COLUMN_COLUMN_NAME = "COLUMN_NAME";

	private DatabaseInfoResolver databaseInfoResolver;

	private List<DialectBuilder> dialectBuilders;

	private boolean detection = true;

	private ConcurrentMap<String, DialectBuilder> dialectBuilderCache = new ConcurrentHashMap<String, DialectBuilder>();

	public DefaultDialectSource()
	{
		super();
	}

	public DefaultDialectSource(DatabaseInfoResolver databaseInfoResolver, List<DialectBuilder> dialectBuilders)
	{
		super();
		this.databaseInfoResolver = databaseInfoResolver;
		this.dialectBuilders = dialectBuilders;
	}

	public DatabaseInfoResolver getDatabaseInfoResolver()
	{
		return databaseInfoResolver;
	}

	public void setDatabaseInfoResolver(DatabaseInfoResolver databaseInfoResolver)
	{
		this.databaseInfoResolver = databaseInfoResolver;
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

		throw new UnsupportedDialectException(ConnectionOption.valueOf(cn));
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
			DatabaseMetaData databaseMetaData = cn.getMetaData();

			String cnUrl = databaseMetaData.getURL();

			DialectBuilder cached = this.dialectBuilderCache.get(cnUrl);

			if (cached != null)
				return cached.build(cn);
			else
			{
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
							catch (Exception e)
							{
								dialect = null;
							}

							// 试探能够成功的分页实现
							if (dialect != null && combinedDialectBuilder.getToPagingSqlDialectBuilder() == null)
							{
								try
								{
									if (testDialectToPagingSql(cn, databaseMetaData, testInfo, dialect))
										combinedDialectBuilder.setToPagingSqlDialectBuilder(dialectBuilder);
								}
								catch (Exception e)
								{
								}
							}
						}
					}
				}

				this.dialectBuilderCache.putIfAbsent(cnUrl, combinedDialectBuilder);

				return combinedDialectBuilder.build(cn);
			}
		}
		catch (SQLException e)
		{
			throw new DialectException(e);
		}
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
		TableInfo tableInfo = this.databaseInfoResolver.getRandomTableInfo(cn);

		if (tableInfo == null)
			return null;

		String tableName = tableInfo.getName();

		ColumnInfo columnInfo = this.databaseInfoResolver.getRandomColumnInfo(cn, tableName);

		if (columnInfo == null)
			return null;

		return new TestInfo(tableInfo.getName(), columnInfo.getName());
	}

	/**
	 * 测试{@linkplain Dialect#toPagingSql(SqlBuilder, SqlBuilder, Order[], long, int)}方法。
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

		SqlBuilder queryView = SqlBuilder.valueOf();
		queryView.sql("SELECT * FROM ").sql(tableQuote);

		SqlBuilder condition = SqlBuilder.valueOf();
		condition.sql(" 1=1 ");

		Order[] orders = Order.asArray(Order.valueOf(columnName, Order.ASC));

		SqlBuilder pagingQuerySql = dialect.toPagingSql(queryView, condition, orders, 1, 5);

		executeQuery(cn, pagingQuerySql);

		return true;
	}

	/**
	 * 执行查询。
	 * 
	 * @param cn
	 * @param query
	 * @return
	 */
	protected void executeQuery(Connection cn, SqlBuilder query) throws Exception
	{
		PreparedStatement pst = null;
		ResultSet rs = null;

		try
		{
			pst = cn.prepareStatement(query.getSqlString());

			Object[] args = query.getArgsArray();
			int[] argTypes = query.getArgTypesArray();

			for (int i = 0; i < args.length; i++)
				pst.setObject(i + 1, args[i], argTypes[i]);

			rs = pst.executeQuery();
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
			JdbcUtil.closeStatement(pst);
		}
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
		private Dialect toPagingSqlDialect;

		public CombinedDialect()
		{
			super();
		}

		public CombinedDialect(String identifierQuote)
		{
			super(identifierQuote);
		}

		public Dialect getToPagingSqlDialect()
		{
			return toPagingSqlDialect;
		}

		public void setToPagingSqlDialect(Dialect toPagingSqlDialect)
		{
			this.toPagingSqlDialect = toPagingSqlDialect;
		}

		@Override
		public boolean supportsPagingSql()
		{
			return (this.toPagingSqlDialect != null);
		}

		@Override
		public SqlBuilder toPagingSql(SqlBuilder queryView, SqlBuilder condition, Order[] orders, long startRow,
				int count)
		{
			if (this.toPagingSqlDialect == null)
				return null;

			return this.toPagingSqlDialect.toPagingSql(queryView, condition, orders, startRow, count);
		}
	}

	protected static class CombinedDialectBuilder extends AbstractDialectBuilder
	{
		private DialectBuilder toPagingSqlDialectBuilder;

		public CombinedDialectBuilder()
		{
			super();
		}

		public DialectBuilder getToPagingSqlDialectBuilder()
		{
			return toPagingSqlDialectBuilder;
		}

		public void setToPagingSqlDialectBuilder(DialectBuilder toPagingSqlDialectBuilder)
		{
			this.toPagingSqlDialectBuilder = toPagingSqlDialectBuilder;
		}

		@Override
		public Dialect build(Connection cn) throws DialectException
		{
			CombinedDialect dialect = new CombinedDialect();

			dialect.setIdentifierQuote(getIdentifierQuote(cn));

			if (this.toPagingSqlDialectBuilder != null)
				dialect.setToPagingSqlDialect(this.toPagingSqlDialectBuilder.build(cn));

			return dialect;
		}

		@Override
		public boolean supports(Connection cn)
		{
			return false;
		}
	}
}
