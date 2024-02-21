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

package org.datagear.meta.resolver;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.datagear.meta.SimpleTable;
import org.datagear.meta.TableType;
import org.datagear.util.AsteriskPatternMatcher;
import org.datagear.util.JDBCCompatiblity;
import org.datagear.util.JdbcUtil;
import org.datagear.util.StringUtil;
import org.datagear.util.sqlvalidator.DatabaseProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认{@linkplain TableTypeResolver}。
 * <p>
 * 此类优先使用配置，没有时才使用默认逻辑。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DefaultTableTypeResolver implements TableTypeResolver
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTableTypeResolver.class);

	/**
	 * 空表类型的匹配文本。
	 */
	public static final String BLANK_TABLE_TYPE_MATCH_TEXT = "NULL";

	private AsteriskPatternMatcher asteriskPatternMatcher = new AsteriskPatternMatcher(true);

	private List<DbTableTypeSpec> dbTableTypeSpecs = Collections.emptyList();

	private DbMetaSupport dbMetaSupport = new DbMetaSupport();

	public DefaultTableTypeResolver()
	{
		super();
	}

	public AsteriskPatternMatcher getAsteriskPatternMatcher()
	{
		return asteriskPatternMatcher;
	}

	public void setAsteriskPatternMatcher(AsteriskPatternMatcher asteriskPatternMatcher)
	{
		this.asteriskPatternMatcher = asteriskPatternMatcher;
	}

	public List<DbTableTypeSpec> getDbTableTypeSpecs()
	{
		return dbTableTypeSpecs;
	}

	public void setDbTableTypeSpecs(List<DbTableTypeSpec> dbTableTypeSpecs)
	{
		this.dbTableTypeSpecs = dbTableTypeSpecs;
	}

	public DbMetaSupport getDbMetaSupport()
	{
		return dbMetaSupport;
	}

	public void setDbMetaSupport(DbMetaSupport dbMetaSupport)
	{
		this.dbMetaSupport = dbMetaSupport;
	}

	@Override
	public String[] getTableTypes(Connection cn) throws DBMetaResolverException
	{
		DbTableTypeSpec dts = getMatchedDbTableTypeSpec(cn);
		List<String> types = (dts == null ? null : dts.getTables());

		if (StringUtil.isEmpty(types))
			return getTableTypesDft(cn);
		else
			return types.toArray(new String[types.size()]);
	}

	@Override
	public boolean isDataTable(Connection cn, SimpleTable table) throws DBMetaResolverException
	{
		DbTableTypeSpec dts = getMatchedDbTableTypeSpec(cn);
		return isDataTable(cn, table, dts);
	}

	@Override
	public boolean[] isDataTables(Connection cn, SimpleTable[] tables) throws DBMetaResolverException
	{
		boolean[] re = new boolean[tables.length];

		DbTableTypeSpec dts = getMatchedDbTableTypeSpec(cn);
		for (int i = 0; i < tables.length; i++)
		{
			re[i] = isDataTable(cn, tables[i], dts);
		}

		return re;
	}

	@Override
	public List<Boolean> isDataTables(Connection cn, List<? extends SimpleTable> tables) throws DBMetaResolverException
	{
		List<Boolean> re = new ArrayList<>(tables.size());

		DbTableTypeSpec dts = getMatchedDbTableTypeSpec(cn);
		for (SimpleTable table : tables)
		{
			re.add(isDataTable(cn, table, dts));
		}

		return re;
	}

	@Override
	public boolean isEntityTable(Connection cn, SimpleTable table) throws DBMetaResolverException
	{
		DbTableTypeSpec dts = getMatchedDbTableTypeSpec(cn);
		return isEntityTable(cn, table, dts);
	}

	@Override
	public boolean[] isEntityTables(Connection cn, SimpleTable[] tables) throws DBMetaResolverException
	{
		boolean[] re = new boolean[tables.length];

		DbTableTypeSpec dts = getMatchedDbTableTypeSpec(cn);
		for (int i = 0; i < tables.length; i++)
		{
			re[i] = isEntityTable(cn, tables[i], dts);
		}

		return re;
	}

	@Override
	public List<Boolean> isEntityTables(Connection cn, List<? extends SimpleTable> tables)
			throws DBMetaResolverException
	{
		List<Boolean> re = new ArrayList<>(tables.size());

		DbTableTypeSpec dts = getMatchedDbTableTypeSpec(cn);
		for (SimpleTable table : tables)
		{
			re.add(isEntityTable(cn, table, dts));
		}

		return re;
	}

	protected String[] getTableTypesDft(Connection cn) throws DBMetaResolverException
	{
		DatabaseMetaData metaData = this.dbMetaSupport.getDatabaseMetaData(cn);

		String[] types = null;

		ResultSet rs = null;
		try
		{
			List<String> typeList = new ArrayList<>();
			rs = metaData.getTableTypes();

			while (rs.next())
				typeList.add(rs.getString(1));

			types = typeList.toArray(new String[typeList.size()]);
		}
		catch (SQLException e)
		{
			if (LOGGER.isErrorEnabled())
				LOGGER.error("Get table types error :", e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
		}

		// 按照
		// DatabaseMetaData.getTables(String, String, String, String[])
		// 规范，types为空数组时应返回null
		if (types != null && types.length == 0)
			types = null;

		return types;
	}

	protected boolean isDataTable(Connection cn, SimpleTable table, DbTableTypeSpec dts) throws DBMetaResolverException
	{
		String tableType = table.getType();
		List<String> patterns = (dts == null ? null : dts.getDatas());

		if (!StringUtil.isEmpty(patterns))
		{
			// 必须将空表类型转换为明确的字符串，以支持定义匹配模式
			if (StringUtil.isBlank(tableType))
				tableType = BLANK_TABLE_TYPE_MATCH_TEXT;

			return containsIgnoreCase(patterns, tableType);
		}
		else
		{
			return isDataTableDft(table);
		}
	}

	protected boolean isDataTableDft(SimpleTable table) throws DBMetaResolverException
	{
		String type = table.getType();

		if (type == null)
			return false;

		if (TableType.SYSTEM_TABLE.equalsIgnoreCase(type) || TableType.LOCAL_TEMPORARY.equalsIgnoreCase(type)
				|| TableType.GLOBAL_TEMPORARY.equalsIgnoreCase(type))
			return false;

		@JDBCCompatiblity("各驱动的命名各有不同，所以这里采用子串匹配方式")
		String typeUpper = type.toUpperCase();

		if (typeUpper.indexOf(TableType.TABLE) > -1 || typeUpper.indexOf(TableType.VIEW) > -1
				|| typeUpper.indexOf(TableType.ALIAS) > -1 || typeUpper.indexOf(TableType.SYNONYM) > -1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	protected boolean isEntityTable(Connection cn, SimpleTable table, DbTableTypeSpec dts)
			throws DBMetaResolverException
	{
		String tableType = table.getType();
		List<String> patterns = (dts == null ? null : dts.getEntities());

		if (!StringUtil.isEmpty(patterns))
		{
			// 必须将空表类型转换为明确的字符串，以支持定义匹配模式
			if (StringUtil.isBlank(tableType))
				tableType = BLANK_TABLE_TYPE_MATCH_TEXT;

			return containsIgnoreCase(patterns, tableType);
		}
		else
		{
			return isEntityTableDft(table);
		}
	}

	protected boolean isEntityTableDft(SimpleTable table)
	{
		if (!isDataTableDft(table))
			return false;

		String type = table.getType();

		if (type == null)
			return false;

		@JDBCCompatiblity("各驱动的命名各有不同，所以这里采用子串匹配方式")
		String typeUpper = type.toUpperCase();

		if (typeUpper.indexOf(TableType.VIEW) > -1 || typeUpper.indexOf(TableType.ALIAS) > -1
				|| typeUpper.indexOf(TableType.SYNONYM) > -1)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	protected boolean containsIgnoreCase(List<String> list, String text)
	{
		for (String ele : list)
		{
			if (ele.equalsIgnoreCase(text))
				return true;
		}

		return false;
	}

	/**
	 * 获取匹配指定连接的{@linkplain DbTableTypeSpec}。
	 * 
	 * @param cn
	 * @return {@code null}表示没有
	 */
	protected DbTableTypeSpec getMatchedDbTableTypeSpec(Connection cn)
	{
		DatabaseProfile dp = DatabaseProfile.valueOf(cn);

		for (DbTableTypeSpec dts : this.dbTableTypeSpecs)
		{
			String db = dts.getDbPattern();

			// 忽略没有定义数据库匹配模式的
			if (StringUtil.isEmpty(db))
				continue;
			
			if (this.asteriskPatternMatcher.matches(db, dp.getName())
					|| this.asteriskPatternMatcher.matches(db, dp.getUrl()))
			{
				return dts;
			}
		}

		return null;
	}
}
