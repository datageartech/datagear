/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.dbinfo;

import java.sql.DatabaseMetaData;

/**
 * 表的导入键列信息。
 * <p>
 * 类结构参考{@linkplain DatabaseMetaData#getImportedKeys(String, String, String)}
 * 返回结果。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ImportedKeyInfo extends ResultSetSpecBean
{
	private static final long serialVersionUID = 1L;

	private String pkTableName;

	private String pkColumnName;

	private String fkColumnName;

	private int keySeq;

	private ImportedKeyRule updateRule;

	private ImportedKeyRule deleteRule;

	private String fkName;

	private String pkName;

	public ImportedKeyInfo()
	{
		super();
	}

	public String getPkTableName()
	{
		return pkTableName;
	}

	public void setPkTableName(String pkTableName)
	{
		this.pkTableName = pkTableName;
	}

	public String getPkColumnName()
	{
		return pkColumnName;
	}

	public void setPkColumnName(String pkColumnName)
	{
		this.pkColumnName = pkColumnName;
	}

	public String getFkColumnName()
	{
		return fkColumnName;
	}

	public void setFkColumnName(String fkColumnName)
	{
		this.fkColumnName = fkColumnName;
	}

	public int getKeySeq()
	{
		return keySeq;
	}

	public void setKeySeq(int keySeq)
	{
		this.keySeq = keySeq;
	}

	public ImportedKeyRule getUpdateRule()
	{
		return updateRule;
	}

	public void setUpdateRule(ImportedKeyRule updateRule)
	{
		this.updateRule = updateRule;
	}

	public ImportedKeyRule getDeleteRule()
	{
		return deleteRule;
	}

	public void setDeleteRule(ImportedKeyRule deleteRule)
	{
		this.deleteRule = deleteRule;
	}

	public String getFkName()
	{
		return fkName;
	}

	public void setFkName(String fkName)
	{
		this.fkName = fkName;
	}

	public String getPkName()
	{
		return pkName;
	}

	public void setPkName(String pkName)
	{
		this.pkName = pkName;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [pkTableName=" + pkTableName + ", pkColumnName=" + pkColumnName
				+ ", fkColumnName=" + fkColumnName + ", keySeq=" + keySeq + ", updateRule=" + updateRule
				+ ", deleteRule=" + deleteRule + ", fkName=" + fkName + ", pkName=" + pkName + "]";
	}

}
