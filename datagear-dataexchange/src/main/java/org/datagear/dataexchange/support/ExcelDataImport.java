/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.InputStream;

import org.datagear.dataexchange.ConnectionFactory;
import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.ResourceFactory;
import org.datagear.dataexchange.TextValueDataImport;
import org.datagear.dataexchange.ValueDataImportOption;

/**
 * Excel导入。
 * 
 * @author datagear@163.com
 *
 */
public class ExcelDataImport extends TextValueDataImport
{
	private ResourceFactory<InputStream> inputFactory;

	private boolean xlsx = true;

	private String unifiedTable;

	public ExcelDataImport()
	{
		super();
	}

	public ExcelDataImport(ConnectionFactory connectionFactory, DataFormat dataFormat,
			ValueDataImportOption importOption, ResourceFactory<InputStream> inputFactory)
	{
		super(connectionFactory, dataFormat, importOption);
		this.inputFactory = inputFactory;
	}

	public ResourceFactory<InputStream> getInputFactory()
	{
		return inputFactory;
	}

	public void setInputFactory(ResourceFactory<InputStream> inputFactory)
	{
		this.inputFactory = inputFactory;
	}

	/**
	 * 是否是{@code .xlsx}文件。
	 * 
	 * @return {@code true}，是{@code .xlsx}文件；{@code false} 是{@code .xls}文件。
	 */
	public boolean isXlsx()
	{
		return xlsx;
	}

	public void setXlsx(boolean xlsx)
	{
		this.xlsx = xlsx;
	}

	/**
	 * 是否设置了统一表名称。
	 * <p>
	 * 返回{@code true}，表示所有工作表导入同一表中；返回{@code false}，表示各自工作表导入其名称对应的表中。
	 * </p>
	 * 
	 * @return
	 */
	public boolean hasUnifiedTable()
	{
		return (this.unifiedTable != null && !this.unifiedTable.isEmpty());
	}

	public String getUnifiedTable()
	{
		return unifiedTable;
	}

	public void setUnifiedTable(String unifiedTable)
	{
		this.unifiedTable = unifiedTable;
	}
}
