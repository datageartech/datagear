/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.datagear.util.KeywordMatcher;
import org.datagear.util.KeywordMatcher.MatchValue;

/**
 * 表常用工具类。
 * 
 * @author datagear@163.com
 *
 */
public class TableUtil
{
	/**
	 * 获取{@linkplain AbstractTable}集合的{@linkplain AbstractTable#getName()}列表。
	 * 
	 * @param tables
	 * @return
	 */
	public static List<String> namesOf(Collection<? extends AbstractTable> tables)
	{
		return namesOf(tables, false);
	}

	/**
	 * 获取{@linkplain AbstractTable}集合的{@linkplain AbstractTable#getName()}列表。
	 * 
	 * @param tables
	 * @param sort
	 *            是否按照名称升序排序
	 * @return
	 */
	public static List<String> namesOf(Collection<? extends AbstractTable> tables, boolean sort)
	{
		List<String> re = new ArrayList<String>(tables.size());

		for (AbstractTable st : tables)
			re.add(st.getName());

		if (sort)
			Collections.sort(re);

		return re;
	}

	/**
	 * 按照名称升序排列。
	 * 
	 * @param tables
	 */
	public static void sortAscByName(List<? extends AbstractTable> tables)
	{
		Collections.sort(tables, TABLE_SORT_BY_NAME_COMPARATOR);
	}

	/**
	 * 根据表名称关键字查询{@linkplain TableInfo}列表。
	 * 
	 * @param tables
	 * @param nameKeyword
	 * @return
	 */
	public static <T extends AbstractTable> List<T> findTable(List<T> tables, String nameKeyword)
	{
		KeywordMatcher km = new KeywordMatcher();

		return km.match(tables, nameKeyword, new MatchValue<T>()
		{
			@Override
			public String[] get(T t)
			{
				return new String[] { t.getName() };
			}
		});
	}

	/**
	 * 根据名称关键字查询{@linkplain Column}列表。
	 * 
	 * @param columns
	 * @param nameKeyword
	 * @return
	 */
	public static List<Column> findColumn(Column[] columns, String nameKeyword)
	{
		KeywordMatcher km = new KeywordMatcher();

		return km.match(columns, nameKeyword, new MatchValue<Column>()
		{
			@Override
			public String[] get(Column t)
			{
				return new String[] { t.getName() };
			}
		});
	}

	/**
	 * 根据名称关键字查询{@linkplain Column}列表。
	 * 
	 * @param columns
	 * @param nameKeyword
	 * @return
	 */
	public static List<Column> findColumn(List<Column> columns, String nameKeyword)
	{
		KeywordMatcher km = new KeywordMatcher();

		return km.match(columns, nameKeyword, new MatchValue<Column>()
		{
			@Override
			public String[] get(Column t)
			{
				return new String[] { t.getName() };
			}
		});
	}

	protected static final Comparator<AbstractTable> TABLE_SORT_BY_NAME_COMPARATOR = new Comparator<AbstractTable>()
	{
		@Override
		public int compare(AbstractTable o1, AbstractTable o2)
		{
			return o1.getName().compareTo(o2.getName());
		}
	};

	protected static final Comparator<Column> COLUMN_SORT_BY_NAME_COMPARATOR = new Comparator<Column>()
	{
		@Override
		public int compare(Column o1, Column o2)
		{
			return o1.getName().compareTo(o2.getName());
		}
	};
}
