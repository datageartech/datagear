/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvableDataSet;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.util.FileUtil;

/**
 * 抽象Excel数据集。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractExcelDataSet extends AbstractFmkTemplateDataSet implements ResolvableDataSet
{
	public static final String EXTENSION_XLSX = "xlsx";

	public static final String EXTENSION_XLS = "xls";

	/** 是否强制作为xls文件处理 */
	private boolean forceXls = false;

	/** 作为标题行的行号 */
	private int titleRow = -1;

	/** 数据行范围表达式 */
	private String dataRowExp = null;

	/** 数据列范围表达式 */
	private String dataColumnExp = null;

	public AbstractExcelDataSet()
	{
		super();
	}

	@SuppressWarnings("unchecked")
	public AbstractExcelDataSet(String id, String name)
	{
		super(id, name, Collections.EMPTY_LIST);
	}

	public AbstractExcelDataSet(String id, String name, List<DataSetProperty> properties)
	{
		super(id, name, properties);
	}

	/**
	 * 是否强制作为xls文件处理。
	 * 
	 * @return
	 */
	public boolean isForceXls()
	{
		return forceXls;
	}

	/**
	 * 设置是否强制作为xls文件处理，如果为{@code false}，则根据文件扩展名判断。
	 * 
	 * @param forceXls
	 */
	public void setForceXls(boolean forceXls)
	{
		this.forceXls = forceXls;
	}

	/**
	 * 是否有标题行。
	 * 
	 * @return
	 */
	public boolean hasTitleRow()
	{
		return (this.titleRow > 0);
	}

	/**
	 * 获取作为标题行的行号。
	 * 
	 * @return
	 */
	public int getTitleRow()
	{
		return titleRow;
	}

	/**
	 * 设置作为标题行的行号。
	 * 
	 * @param titleRow 行号，小于{@code 1}则表示无标题行。
	 */
	public void setTitleRow(int titleRow)
	{
		this.titleRow = titleRow;
	}

	public String getDataRowExp()
	{
		return dataRowExp;
	}

	/**
	 * 设置数据行范围表达式。
	 * <p>
	 * 表达式格式示例为：
	 * </p>
	 * <p>
	 * {@code "6"} ：第6行 <br>
	 * {@code "3-15"} ：第3至15行 <br>
	 * {@code "1,4,8-15"}：第1、4、8至15行
	 * </p>
	 * 
	 * @param dataRowExp 表达式，为{@code null}、{@code ""}则不限定
	 */
	public void setDataRowExp(String dataRowExp)
	{
		this.dataRowExp = dataRowExp;
	}

	public String getDataColumnExp()
	{
		return dataColumnExp;
	}

	/**
	 * 设置数据列范围表达式。
	 * <p>
	 * 表达式格式为：
	 * </p>
	 * <p>
	 * {@code "A"}：第A列 <br>
	 * {@code "C-E"}：第C至E列 <br>
	 * {@code "A,C,E-H"}：第A、C、E至H列
	 * </p>
	 * 
	 * @param dataColumnExp 表达式，为{@code null}、{@code ""}则不限定
	 */
	public void setDataColumnExp(String dataColumnExp)
	{
		this.dataColumnExp = dataColumnExp;
	}

	@Override
	public DataSetResult getResult(Map<String, ?> paramValues) throws DataSetException
	{
		List<DataSetProperty> properties = getProperties();

		if (properties == null || properties.isEmpty())
			throw new DataSetException("[getProperties()] must not be empty");

		ResolvedDataSetResult result = resolve(paramValues, properties);
		return result.getResult();
	}

	@Override
	public ResolvedDataSetResult resolve(Map<String, ?> paramValues) throws DataSetException
	{
		return resolve(paramValues, null);
	}

	/**
	 * 解析Excel结果。
	 * 
	 * @param paramValues
	 * @param properties  允许为{@code null}，此时会自动解析
	 * @return
	 * @throws DataSetException
	 */
	protected ResolvedDataSetResult resolve(Map<String, ?> paramValues, List<DataSetProperty> properties)
			throws DataSetException
	{
		File file = getExcelFile(paramValues);

		ResolvedDataSetResult result = null;

		if (isXls(file))
			result = resolveResultForXls(paramValues, file, properties);
		else
			result = resolveResultForXlsx(paramValues, file, properties);

		return result;
	}

	/**
	 * 解析{@code xls}结果。
	 * 
	 * @param paramValues
	 * @param file
	 * @param properties  允许为{@code null}，此时会自动解析
	 * @return
	 * @throws DataSetException
	 */
	protected ResolvedDataSetResult resolveResultForXls(Map<String, ?> paramValues, File file,
			List<DataSetProperty> properties) throws DataSetException
	{
		return null;
	}

	/**
	 * 解析{@code xlsx}结果。
	 * 
	 * @param paramValues
	 * @param file
	 * @param properties  允许为{@code null}，此时会自动解析
	 * @return
	 * @throws DataSetException
	 */
	protected ResolvedDataSetResult resolveResultForXlsx(Map<String, ?> paramValues, File file,
			List<DataSetProperty> properties) throws DataSetException
	{
		return null;
	}

	/**
	 * 获取Excel文件。
	 * <p>
	 * 实现方法应该返回实例级不变的文件。
	 * </p>
	 * 
	 * @param paramValues
	 * @return
	 * @throws DataSetException
	 */
	protected abstract File getExcelFile(Map<String, ?> paramValues) throws DataSetException;

	/**
	 * 给定Excel文件是否是老版本的{@code .xls}文件。
	 * 
	 * @param file
	 * @return
	 */
	protected boolean isXls(File file)
	{
		if (this.forceXls)
			return true;

		return FileUtil.isExtension(file, EXTENSION_XLS);
	}
}
