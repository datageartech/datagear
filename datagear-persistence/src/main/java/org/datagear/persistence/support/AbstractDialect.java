/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.sql.Types;
import java.util.List;

import org.datagear.dbinfo.SqlTypeInfo.SearchableType;
import org.datagear.model.Model;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.Order;
import org.datagear.persistence.Query;
import org.datagear.persistence.QueryColumnMetaInfo;
import org.datagear.persistence.SqlBuilder;
import org.datagear.util.JDBCCompatiblity;
import org.datagear.util.JdbcUtil;

/**
 * 抽象{@linkplain Dialect}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDialect implements Dialect
{
	/** 标识引用符 */
	private String identifierQuote;

	public AbstractDialect()
	{
		super();
	}

	public AbstractDialect(String identifierQuote)
	{
		super();
		this.identifierQuote = identifierQuote;
	}

	@Override
	public String getIdentifierQuote()
	{
		return identifierQuote;
	}

	public void setIdentifierQuote(String identifierQuote)
	{
		this.identifierQuote = identifierQuote;
	}

	@Override
	public String quote(String name)
	{
		return this.identifierQuote + name + this.identifierQuote;
	}

	@Override
	public boolean isQuoted(String name)
	{
		return (name.length() > this.identifierQuote.length() * 2 && name.startsWith(this.identifierQuote)
				&& name.endsWith(this.identifierQuote));
	}

	@Override
	public String unquote(String name)
	{
		if (!isQuoted(name))
			return name;

		int iqLen = this.identifierQuote.length();
		return name.substring(iqLen, name.length() - iqLen);
	}

	@Override
	public SqlBuilder toKeywordQueryCondition(Model model, Query query,
			List<? extends QueryColumnMetaInfo> queryColumnMetaInfos)
	{
		SqlBuilder keywordCondition = SqlBuilder.valueOf();

		if (!query.hasKeyword())
			return keywordCondition;

		String joinOpt = (query.isNotLike() ? " AND " : " OR ");
		String likeOpt = (query.isNotLike() ? " NOT LIKE " : " LIKE ");
		String equalOpt = (query.isNotLike() ? " != " : " = ");

		for (QueryColumnMetaInfo queryColumnMetaInfo : queryColumnMetaInfos)
		{
			if (!queryColumnMetaInfo.isToken())
				continue;

			int sqlType = queryColumnMetaInfo.getColumnSqlType();

			@JDBCCompatiblity("很多驱动程序的值为SearchableType.ALL但实际并不支持LIKE语法（比如：PostgreSQL JDBC 42.2.5），"
					+ "这里为了兼容，不采用SearchableType逻辑")
			SearchableType searchableType = null;
			String myOperator = null;
			Object myKeyword = null;
			// PropertyPath propertyPath =
			// PropertyPath.valueOf(queryColumnMetaInfo.getPropertyPath());
			// PropertyPathInfo propertyPathInfo =
			// PropertyPathInfo.valueOf(model, propertyPath);
			// Property tailProperty = propertyPathInfo.getPropertyTail();
			// Searchable searchable =
			// tailProperty.getFeature(Searchable.class);
			// searchableType = (searchable == null ? null :
			// searchable.getValue());

			if (SearchableType.NO.equals(searchableType))
				;
			else if (SearchableType.ONLY_LIKE.equals(searchableType) || SearchableType.ALL.equals(searchableType))
			{
				myOperator = likeOpt;
				myKeyword = wrapLikeKeyword(query.getKeyword());
			}
			else
			{
				// SearchableType.EXPCEPT_LIKE、或者无SearchableType

				if (isLikeableSqlType(sqlType))
				{
					myOperator = likeOpt;
					myKeyword = wrapLikeKeyword(query.getKeyword());
				}
				else
				{
					Number number = JdbcUtil.parseToNumber(query.getKeyword(), sqlType);

					if (number != null)
					{
						myOperator = equalOpt;
						myKeyword = number;
					}
				}
			}

			if (myOperator != null && myKeyword != null)
			{
				if (!keywordCondition.isEmpty())
					keywordCondition.sql(joinOpt);

				keywordCondition.sql(queryColumnMetaInfo.getColumnPath() + myOperator + "?", myKeyword);
			}
		}

		return keywordCondition;
	}

	@Override
	@JDBCCompatiblity("某些驱动程序对有些类型不支持排序（比如Oracle对于BLOB类型）")
	public boolean isSortable(int sqlType)
	{
		if (Types.BIGINT == sqlType || Types.BIT == sqlType || Types.BOOLEAN == sqlType || Types.CHAR == sqlType
				|| Types.DATE == sqlType || Types.DECIMAL == sqlType || Types.DOUBLE == sqlType
				|| Types.FLOAT == sqlType || Types.INTEGER == sqlType || Types.NCHAR == sqlType
				|| Types.NUMERIC == sqlType || Types.NVARCHAR == sqlType || Types.REAL == sqlType
				|| Types.SMALLINT == sqlType || Types.TIME == sqlType || Types.TIMESTAMP == sqlType
				|| Types.TINYINT == sqlType || Types.VARCHAR == sqlType)
			return true;

		return false;
	}

	@Override
	public String getPagingQueryOrderName(QueryColumnMetaInfo queryColumnMetaInfo)
	{
		return quote(queryColumnMetaInfo.getColumnAlias());
	}

	/**
	 * 指定SQL类型是否可用作LIKE条件。
	 * 
	 * @param sqlType
	 * @return
	 */
	protected boolean isLikeableSqlType(int sqlType)
	{
		return (Types.CHAR == sqlType || Types.VARCHAR == sqlType || Types.NCHAR == sqlType
				|| Types.NVARCHAR == sqlType);
	}

	/**
	 * 转换为排序SQL。
	 * 
	 * @param orders
	 * @return
	 * @see Order#toOrderSql(Order...)
	 */
	protected SqlBuilder toOrderSql(Order... orders)
	{
		return Order.toOrderSql(orders);
	}

	/**
	 * SQL是否为空。
	 * 
	 * @param sql
	 * @return
	 */
	protected boolean isEmptySql(SqlBuilder sql)
	{
		return sql == null || sql.isEmpty();
	}

	/**
	 * 包裹Like关键字。
	 * 
	 * @param keyword
	 * @return
	 */
	protected String wrapLikeKeyword(String keyword)
	{
		if (keyword == null || keyword.isEmpty())
			return keyword;

		char first = keyword.charAt(0), last = keyword.charAt(keyword.length() - 1);

		if (first != '%' && first != '_' && last != '%' && last != '_')
			return "%" + keyword + "%";

		return keyword;
	}
}
