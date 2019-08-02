/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

import java.sql.DatabaseMetaData;
import java.util.List;

/**
 * {@linkplain ImportedKeyInfo}结果集规范。
 * 
 * @author datagear@163.com
 *
 */
public class ImportedKeyInfoResultSetSpec extends ResultSetSpec<ImportedKeyInfo>
{
	public static final Converter<Integer, ImportedKeyRule> IMPORTED_KEY_RULE_CONVERTER = new Converter<Integer, ImportedKeyRule>()
	{
		@Override
		public ImportedKeyRule convert(Integer s) throws ResultSetIncompatibleException
		{
			if (s == null)
				return null;

			if (DatabaseMetaData.importedKeyNoAction == s)
				return ImportedKeyRule.NO_ACTION;
			else if (DatabaseMetaData.importedKeyRestrict == s)
				return ImportedKeyRule.RESTRICT;
			else if (DatabaseMetaData.importedKeyCascade == s)
				return ImportedKeyRule.CASCADE;
			else if (DatabaseMetaData.importedKeySetNull == s)
				return ImportedKeyRule.SET_NULL;
			else if (DatabaseMetaData.importedKeySetDefault == s)
				return ImportedKeyRule.SET_DEFAUL;
			else
				return null;
		}
	};

	public static final RsColumnSpec<?, ?>[] RS_COLUMN_SPECS = new RsColumnSpec[] {
			new RsColumnSpec<String, String>("PKTABLE_NAME", String.class, true, false, "pkTableName"),
			new RsColumnSpec<String, String>("PKCOLUMN_NAME", String.class, true, false, "pkColumnName"),
			new RsColumnSpec<String, String>("FKCOLUMN_NAME", String.class, true, false, "fkColumnName"),
			new RsColumnSpec<Integer, Integer>("KEY_SEQ", Integer.class, false, true, 0, "keySeq"),
			new RsColumnSpec<Integer, ImportedKeyRule>("UPDATE_RULE", Integer.class, false, true, "updateRule",
					IMPORTED_KEY_RULE_CONVERTER),
			new RsColumnSpec<Integer, ImportedKeyRule>("DELETE_RULE", Integer.class, false, true, "deleteRule",
					IMPORTED_KEY_RULE_CONVERTER),
			new RsColumnSpec<String, String>("FK_NAME", String.class, false, true, "", "fkName"),
			new RsColumnSpec<String, String>("PK_NAME", String.class, false, true, "", "pkName") };

	public ImportedKeyInfoResultSetSpec()
	{
		super();
	}

	@Override
	protected void addToList(List<ImportedKeyInfo> list, ImportedKeyInfo bean)
	{
		for (ImportedKeyInfo ele : list)
		{
			if (equalsWithNull(ele.getFkColumnName(), bean.getFkColumnName())
					&& equalsWithNull(ele.getPkColumnName(), bean.getPkColumnName())
					&& equalsWithNull(ele.getPkTableName(), bean.getPkTableName()))
				return;
		}

		list.add(bean);
	}

	@Override
	protected Class<ImportedKeyInfo> getRowType()
	{
		return ImportedKeyInfo.class;
	}

	@Override
	protected RsColumnSpec<?, ?>[] getRsColumnSpecs()
	{
		return RS_COLUMN_SPECS;
	}
}
