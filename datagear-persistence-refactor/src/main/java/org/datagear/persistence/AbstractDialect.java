/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import org.datagear.meta.Column;
import org.datagear.meta.SearchableType;
import org.datagear.meta.Table;
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

	/** 作为关键字查询的列数 */
	private int keywordQueryColumnCount = Dialect.DEFAULT_KEYWORD_QUERY_COLUMN_COUNT;

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
	public int getKeywordQueryColumnCount()
	{
		return keywordQueryColumnCount;
	}

	public void setKeywordQueryColumnCount(int keywordQueryColumnCount)
	{
		this.keywordQueryColumnCount = keywordQueryColumnCount;
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
	public Sql toKeywordQueryCondition(Table table, Query query)
	{
		Sql sql = Sql.valueOf();

		if (!query.hasKeyword())
			return sql;

		String joinOpt = (query.isNotLike() ? " AND " : " OR ");
		String likeOpt = (query.isNotLike() ? " NOT LIKE " : " LIKE ");
		String equalOpt = (query.isNotLike() ? " != " : " = ");

		Column[] columns=table.getColumns();
		for (int i=0; i< columns.length; i++)
		{
			if (i >= this.keywordQueryColumnCount)
				break;

			Column column = columns[i];
			SearchableType searchableType = column.getSearchableType();
			String myOperator = null;
			Object myKeyword = null;

			if (SearchableType.NO.equals(searchableType))
				;
			else if (SearchableType.ONLY_LIKE.equals(searchableType) || SearchableType.ALL.equals(searchableType))
			{
				myOperator = likeOpt;
				myKeyword = wrapLikeKeyword(query.getKeyword());
			}
			else
			{
				Number number = JdbcUtil.parseToNumber(query.getKeyword(), column.getType());

				if (number != null)
				{
					myOperator = equalOpt;
					myKeyword = number;
				}
			}

			if (myOperator != null && myKeyword != null)
			{
				if (!sql.isEmpty())
					sql.sql(joinOpt);

				sql.sql(quote(column.getName()) + myOperator + "?", myKeyword, column.getType());
			}
		}

		return sql;
	}

	@Override
	public Sql toOrderSql(Sql query, Order[] orders)
	{
		Sql orderSql = toOrderSql(orders);

		if (Sql.isEmpty(orderSql))
			return query;

		return Sql.valueOf().sql(query).sql(" ORDER BY ").sql(orderSql);
	}

	/**
	 * 转换为排序SQL。
	 * 
	 * @param orders
	 * @return 返回{@code null}表示无排序SQL
	 */
	protected Sql toOrderSql(Order... orders)
	{
		if (orders == null || orders.length == 0)
			return null;

		Sql orderSql = Sql.valueOf().delimit(", ");

		for (int i = 0; i < orders.length; i++)
		{
			Order order = orders[i];

			orderSql.sqld(quote(order.getName()) + " " + (order.isAsc() ? Order.ASC : Order.DESC));
		}

		return orderSql;
	}

	/**
	 * SQL是否为空。
	 * 
	 * @param sql
	 * @return
	 */
	protected boolean isEmptySql(Sql sql)
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
