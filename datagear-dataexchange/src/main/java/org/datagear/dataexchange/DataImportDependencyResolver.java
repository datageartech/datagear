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

package org.datagear.dataexchange;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.datagear.meta.resolver.DBMetaResolver;

/**
 * 数据导入依赖处理器。
 * 
 * @author datagear@163.com
 *
 */
public class DataImportDependencyResolver
{
	private DBMetaResolver dbMetaResolver;

	public DataImportDependencyResolver()
	{
		super();
	}

	public DataImportDependencyResolver(DBMetaResolver dbMetaResolver)
	{
		super();
		this.dbMetaResolver = dbMetaResolver;
	}

	public DBMetaResolver getDbMetaResolver()
	{
		return dbMetaResolver;
	}

	public void setDbMetaResolver(DBMetaResolver dbMetaResolver)
	{
		this.dbMetaResolver = dbMetaResolver;
	}

	/**
	 * 处理导入依赖关系，设置{@linkplain SubDataExchange#setDependencies(Set)}后返回。
	 * 
	 * @param dataImportDependencies
	 * @param cn
	 */
	public List<SubDataExchange> resolve(List<? extends DataImportDependency> dataImportDependencies)
	{
		List<String[]> dependIndexess = getDependIndexes(dataImportDependencies);
		resolveDependencies(dataImportDependencies, dependIndexess);

		return extractSubDataExchanges(dataImportDependencies);
	}

	/**
	 * 处理导入依赖关系，设置{@linkplain SubDataExchange#setDependencies(Set)}后返回。
	 * 
	 * @param dataImportDependencies
	 * @param cn
	 */
	public List<SubDataExchange> resolveAuto(List<? extends AutoDataImportDependency> dataImportDependencies,
			Connection cn)
	{
		List<String[]> dependIndexess = resolveDependIndexes(dataImportDependencies, cn);
		resolveDependencies(dataImportDependencies, dependIndexess);

		return extractSubDataExchanges(dataImportDependencies);
	}

	protected List<SubDataExchange> extractSubDataExchanges(List<? extends DataImportDependency> dataImportDependencies)
	{
		List<SubDataExchange> subDataExchanges = new ArrayList<SubDataExchange>(dataImportDependencies.size());

		for (DataImportDependency dd : dataImportDependencies)
		{
			subDataExchanges.add(dd.getSubDataExchange());
		}

		return subDataExchanges;
	}

	protected void resolveDependencies(List<? extends DataImportDependency> dataImportDependencies,
			List<String[]> dependIndexess)
	{
		for (int i = 0, len = dataImportDependencies.size(); i < len; i++)
		{
			DataImportDependency dd = dataImportDependencies.get(i);
			String[] dependIndexes = dependIndexess.get(i);
			Set<SubDataExchange> myDependencies = new HashSet<>();

			if (dependIndexes != null && dependIndexes.length > 0)
			{
				for (int j = 0; j < dependIndexes.length; j++)
				{
					for (DataImportDependency ddd : dataImportDependencies)
					{
						if (ddd.getIndex().equals(dependIndexes[j]))
						{
							myDependencies.add(ddd.getSubDataExchange());
							break;
						}
					}
				}
			}

			dd.getSubDataExchange().setDependencies(myDependencies);
		}
	}

	protected List<String[]> resolveDependIndexes(List<? extends AutoDataImportDependency> dataImportDependencies,
			Connection cn)
	{
		int dataImportLen = dataImportDependencies.size();
		List<String[]> dependIndexess = new ArrayList<String[]>(dataImportDependencies.size());

		String[] tableNames = getTableNames(dataImportDependencies);
		List<String[]> importTabless = this.dbMetaResolver.getImportTables(cn, tableNames);

		for (int i = 0; i < dataImportLen; i++)
		{
			AutoDataImportDependency dd = dataImportDependencies.get(i);

			if (dd.isDependTypeIndexes())
			{
				dependIndexess.add((dd.getDependIndexes() == null ? DataImportDependency.DEPEND_INDEXES_EMPTY
						: dd.getDependIndexes()));
			}
			else
			{
				String[] myImportTables = importTabless.get(i);

				if (myImportTables == null || myImportTables.length == 0)
				{
					dependIndexess.add(DataImportDependency.DEPEND_INDEXES_EMPTY);
				}
				else
				{
					List<String> myDependIndexes = new ArrayList<String>();

					for (String myImportTable : myImportTables)
					{
						for (AutoDataImportDependency ddd : dataImportDependencies)
						{
							if (myImportTable.equalsIgnoreCase(ddd.getTableName()))
							{
								myDependIndexes.add(ddd.getIndex());
							}
						}
					}

					dependIndexess.add(myDependIndexes.toArray(new String[myDependIndexes.size()]));
				}
			}
		}

		return dependIndexess;
	}

	protected String[] getTableNames(List<? extends AutoDataImportDependency> dataImportDependencies)
	{
		String[] tableNames = new String[dataImportDependencies.size()];

		for (int i = 0, len = dataImportDependencies.size(); i < len; i++)
		{
			AutoDataImportDependency dd = dataImportDependencies.get(i);
			tableNames[i] = dd.getTableName();
		}

		return tableNames;
	}

	protected List<String[]> getDependIndexes(List<? extends DataImportDependency> dataImportDependencies)
	{
		List<String[]> dependIndexess = new ArrayList<String[]>(dataImportDependencies.size());

		for (DataImportDependency dd : dataImportDependencies)
		{
			dependIndexess.add(dd.getDependIndexes());
		}

		return dependIndexess;
	}

	/**
	 * 数据导入依赖。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class DataImportDependency
	{
		public static final String[] DEPEND_INDEXES_EMPTY = new String[0];

		private SubDataExchange subDataExchange;

		/** 索引号 */
		private String index;

		/** 依赖索引 */
		private String[] dependIndexes = DEPEND_INDEXES_EMPTY;

		public DataImportDependency(SubDataExchange subDataExchange, String index)
		{
			super();
			this.subDataExchange = subDataExchange;
			this.index = index;
			this.dependIndexes = DEPEND_INDEXES_EMPTY;
		}

		public DataImportDependency(SubDataExchange subDataExchange, String index,
				String[] dependIndexes)
		{
			super();
			this.subDataExchange = subDataExchange;
			this.index = index;
			this.dependIndexes = dependIndexes;
		}

		public SubDataExchange getSubDataExchange()
		{
			return subDataExchange;
		}

		public void setSubDataExchange(SubDataExchange subDataExchange)
		{
			this.subDataExchange = subDataExchange;
		}

		public String getIndex()
		{
			return index;
		}

		public void setIndex(String index)
		{
			this.index = index;
		}

		public boolean hasDependIndexes()
		{
			return (this.dependIndexes != null && this.dependIndexes.length > 0);
		}

		public String[] getDependIndexes()
		{
			return dependIndexes;
		}

		public void setDependIndexes(String[] dependIndexes)
		{
			this.dependIndexes = dependIndexes;
		}
	}

	/**
	 * 自动数据导入依赖。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class AutoDataImportDependency extends DataImportDependency
	{
		/**
		 * 依赖类型：指定索引号
		 */
		public static final int DEPEND_TYPE_INDEXES = 0;

		/**
		 * 依赖类型：自动，根据数据库表的依赖关系自动设置
		 */
		public static final int DEPEND_TYPE_AUTO = 1;

		/** 依赖类型 */
		private int dependType = DEPEND_TYPE_INDEXES;

		/** 当dependType为DEPEND_TYPE_AUTO时的表名 */
		private String tableName = null;

		public AutoDataImportDependency(SubDataExchange subDataExchange, String index, String tableName)
		{
			super(subDataExchange, index);
			this.dependType = DEPEND_TYPE_AUTO;
			this.tableName = tableName;
		}

		public AutoDataImportDependency(SubDataExchange subDataExchange, String index, String[] dependIndexes)
		{
			super(subDataExchange, index, dependIndexes);
			this.dependType = DEPEND_TYPE_INDEXES;
			this.tableName = null;
		}

		public int getDependType()
		{
			return dependType;
		}

		public void setDependType(int dependType)
		{
			this.dependType = dependType;
		}

		public String getTableName()
		{
			return tableName;
		}

		public void setTableName(String tableName)
		{
			this.tableName = tableName;
		}

		public boolean isDependTypeIndexes()
		{
			return (this.dependType == DEPEND_TYPE_INDEXES);
		}

		public boolean isDependTypeAuto()
		{
			return (this.dependType == DEPEND_TYPE_AUTO);
		}
	}
}
