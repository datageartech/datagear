/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

/**
 * 完整表信息。
 * 
 * @author datagear@163.com
 *
 */
public class EntireTableInfo
{
	private TableInfo tableInfo;

	private ColumnInfo[] columnInfos;

	private String[] primaryKeyColumnNames;

	private String[][] uniqueKeyColumnNames;

	private ImportedKeyInfo[] importedKeyInfos;

	private ExportedKeyInfo[] exportedKeyInfos;

	public EntireTableInfo()
	{
		super();
	}

	public EntireTableInfo(TableInfo tableInfo)
	{
		super();
		this.tableInfo = tableInfo;
	}

	public EntireTableInfo(TableInfo tableInfo, ColumnInfo[] columnInfos, String[] primaryKeyColumnNames,
			String[][] uniqueKeyColumnNames, ImportedKeyInfo[] importedKeyInfos, ExportedKeyInfo[] exportedKeyInfos)
	{
		super();
		this.tableInfo = tableInfo;
		this.columnInfos = columnInfos;
		this.primaryKeyColumnNames = primaryKeyColumnNames;
		this.uniqueKeyColumnNames = uniqueKeyColumnNames;
		this.importedKeyInfos = importedKeyInfos;
		this.exportedKeyInfos = exportedKeyInfos;
	}

	public TableInfo getTableInfo()
	{
		return tableInfo;
	}

	public void setTableInfo(TableInfo tableInfo)
	{
		this.tableInfo = tableInfo;
	}

	public ColumnInfo[] getColumnInfos()
	{
		return columnInfos;
	}

	public void setColumnInfos(ColumnInfo[] columnInfos)
	{
		this.columnInfos = columnInfos;
	}

	public String[] getPrimaryKeyColumnNames()
	{
		return primaryKeyColumnNames;
	}

	public void setPrimaryKeyColumnNames(String[] primaryKeyColumnNames)
	{
		this.primaryKeyColumnNames = primaryKeyColumnNames;
	}

	public String[][] getUniqueKeyColumnNames()
	{
		return uniqueKeyColumnNames;
	}

	public void setUniqueKeyColumnNames(String[][] uniqueKeyColumnNames)
	{
		this.uniqueKeyColumnNames = uniqueKeyColumnNames;
	}

	public ImportedKeyInfo[] getImportedKeyInfos()
	{
		return importedKeyInfos;
	}

	public void setImportedKeyInfos(ImportedKeyInfo[] importedKeyInfos)
	{
		this.importedKeyInfos = importedKeyInfos;
	}

	public ExportedKeyInfo[] getExportedKeyInfos()
	{
		return exportedKeyInfos;
	}

	public void setExportedKeyInfos(ExportedKeyInfo[] exportedKeyInfos)
	{
		this.exportedKeyInfos = exportedKeyInfos;
	}

	/**
	 * 是否有列。
	 * 
	 * @return
	 */
	public boolean hasColumn()
	{
		return (this.columnInfos != null && this.columnInfos.length != 0);
	}

	/**
	 * 是否有主键。
	 * 
	 * @return
	 */
	public boolean hasPrimaryKey()
	{
		return (this.primaryKeyColumnNames != null && this.primaryKeyColumnNames.length != 0);
	}

	/**
	 * 是否有唯一键。
	 * 
	 * @return
	 */
	public boolean hasUniqueKey()
	{
		return (this.uniqueKeyColumnNames != null && this.uniqueKeyColumnNames.length != 0);
	}

	/**
	 * 是否有{@linkplain ImportedKeyInfo}。
	 * 
	 * @return
	 */
	public boolean hasImportedKey()
	{
		return (this.importedKeyInfos != null && this.importedKeyInfos.length != 0);
	}

	/**
	 * 是否有{@linkplain ExportedKeyInfo}。
	 * 
	 * @return
	 */
	public boolean hasExportedKey()
	{
		return (this.exportedKeyInfos != null && this.exportedKeyInfos.length != 0);
	}
}
