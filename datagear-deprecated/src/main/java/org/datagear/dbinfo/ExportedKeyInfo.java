/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

import java.sql.DatabaseMetaData;

/**
 * 表的导出键列信息。
 * <p>
 * 类结构参考{@linkplain DatabaseMetaData#getExportedKeys(String, String, String)}
 * 返回结果。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ExportedKeyInfo extends ResultSetSpecBean
{
	private static final long serialVersionUID = 1L;

	private String pkColumnName;

	private String fkTableName;

	private String fkColumnName;

	private int keySeq;

	private ImportedKeyRule updateRule;

	private ImportedKeyRule deleteRule;

	private String fkName;

	private String pkName;

	public ExportedKeyInfo()
	{
		super();
	}

	public String getPkColumnName()
	{
		return pkColumnName;
	}

	public void setPkColumnName(String pkColumnName)
	{
		this.pkColumnName = pkColumnName;
	}

	public String getFkTableName()
	{
		return fkTableName;
	}

	public void setFkTableName(String fkTableName)
	{
		this.fkTableName = fkTableName;
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
		return getClass().getSimpleName() + " [pkColumnName=" + pkColumnName + ", fkTableName=" + fkTableName
				+ ", fkColumnName=" + fkColumnName + ", keySeq=" + keySeq + ", updateRule=" + updateRule
				+ ", deleteRule=" + deleteRule + ", fkName=" + fkName + ", pkName=" + pkName + "]";
	}

}
