/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.util.dialect;

import org.datagear.management.service.impl.AbstractMybatisService;

/**
 * Mybatis SQL方言。
 * <p>
 * 此类仅用于为{@linkplain AbstractMybatisService}及其实现类提供多数据库部署支持。
 * </p>
 * <p>
 * 基本思路是：根据当前部署数据库，生成底层Mybatis所需的数据库方言SQL片段，然后以参数的方式传入底层SQL
 * Mapper语境，组装成合规的SQL语句。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class MbSqlDialect
{
	public static final String FUNC_NAME_MAX = "MAX";

	public static final String FUNC_NAME_MODINT = "DATAGEAR_FUNC_MODINT";

	/** 标识符引用符 */
	private String identifierQuote;

	public MbSqlDialect()
	{
	}

	public MbSqlDialect(String identifierQuote)
	{
		super();
		this.identifierQuote = identifierQuote;
	}

	public String getIdentifierQuote()
	{
		return identifierQuote;
	}

	public void setIdentifierQuote(String identifierQuote)
	{
		this.identifierQuote = identifierQuote;
	}

	/**
	 * 是否支持分页查询。
	 * 
	 * @return
	 */
	public abstract boolean supportsPaging();

	/**
	 * 获取分页查询SQL首部片段。
	 * <p>
	 * 分页查询SQL语句的格式规定为：
	 * </p>
	 * <p>
	 * <code>
	 * 分页查询SQL首部片段<br>
	 * 业务查询语句<br>
	 * 分页查询SQL尾部片段
	 * </code>
	 * </p>
	 * <p>
	 * 例如：
	 * </p>
	 * <p>
	 * <code>
	 * <pre>
	 * SELECT PQ.* FROM (     --分页查询SQL首部片段 <br>
	 * SELECT * FROM TABLE_0  --业务查询语句 <br>
	 * ) PQ LIMIT 0, 10       --分页查询SQL尾部片段
	 * </pre>
	 * </code>
	 * </p>
	 * 
	 * @param index
	 *            页起始索引（以{@code 0}开头）
	 * @param fetchSize
	 *            页大小
	 * @return 返回{@code null}表示不支持分页
	 */
	public abstract String pagingSqlHead(int index, int fetchSize);

	/**
	 * 获取分页查询SQL尾部片段。
	 * 
	 * @param index
	 *            页起始索引（以{@code 0}开头）
	 * @param fetchSize
	 *            页大小
	 * @return 返回{@code null}表示不支持分页
	 */
	public abstract String pagingSqlFoot(int index, int fetchSize);

	/**
	 * 获取MAX函数名。
	 * 
	 * @return
	 */
	public String funcNameMax()
	{
		return FUNC_NAME_MAX;
	}

	/**
	 * 获取求余函数名。
	 * 
	 * @return
	 */
	public String funcNameModInt()
	{
		return FUNC_NAME_MODINT;
	}

	/**
	 * 将字符串转换为SQL字符串字面值。
	 * <p>
	 * 例如：{@code "abc'def"}应转换为{@code "'abc''def'"}
	 * </p>
	 * 
	 * @param value
	 * @return
	 */
	public String toStringLiteral(String value)
	{
		if (value == null)
			return "NULL";

		StringBuilder sb = new StringBuilder();

		sb.append('\'');

		for (int i = 0, len = value.length(); i < len; i++)
		{
			char c = value.charAt(i);

			if (c == '\'')
				sb.append("''");
			else
				sb.append(c);
		}

		sb.append('\'');

		return sb.toString();
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [identifierQuote=" + getIdentifierQuote() + ", supportsPaging="
				+ supportsPaging() + ", pagingSqlHead=" + pagingSqlHead(0, 10) + ", pagingSqlFoot="
				+ pagingSqlFoot(0, 10) + ", funcNameMax=" + funcNameMax()
				+ "]";
	}
}
