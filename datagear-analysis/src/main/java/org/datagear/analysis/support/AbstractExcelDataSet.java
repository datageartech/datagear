/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.ResolvableDataSet;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.analysis.support.RangeExpResolver.IndexRange;
import org.datagear.analysis.support.RangeExpResolver.Range;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象Excel数据集。
 * <p>
 * 此类仅支持从Excel的单个sheet读取数据，具体参考{@linkplain #setSheetIndex(int)}。
 * </p>
 * <p>
 * 通过{@linkplain #setDataRowExp(String)}、{@linkplain #setDataColumnExp(String)}来设置读取行、列范围。
 * </p>
 * <p>
 * 通过{@linkplain #setNameRow(int)}可设置名称行。
 * </p>
 * <p>
 * 注意：此类不支持<code>Freemarker</code>模板语言。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractExcelDataSet extends AbstractResolvableDataSet implements ResolvableDataSet
{
	private static final long serialVersionUID = 1L;

	protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractExcelDataSet.class);

	public static final String EXTENSION_XLSX = "xlsx";

	public static final String EXTENSION_XLS = "xls";

	protected static final RangeExpResolver RANGE_EXP_RESOLVER = RangeExpResolver
			.valueOf(RangeExpResolver.RANGE_SPLITTER_CHAR, RangeExpResolver.RANGE_GROUP_SPLITTER_CHAR);

	/** 此数据集所处的sheet索引号（以1计数） */
	private int sheetIndex = 1;

	/** 作为名称行的行号 */
	private int nameRow = -1;

	/** 数据行范围表达式 */
	private String dataRowExp = "";

	/** 数据列范围表达式 */
	private String dataColumnExp = "";

	/** 是否强制作为xls文件处理 */
	private boolean forceXls = false;

	private transient List<IndexRange> _dataRowRanges = null;
	private transient List<IndexRange> _dataColumnRanges = null;

	public AbstractExcelDataSet()
	{
		super();
	}

	public AbstractExcelDataSet(String id, String name)
	{
		super(id, name);
	}

	public AbstractExcelDataSet(String id, String name, List<DataSetProperty> properties)
	{
		super(id, name, properties);
	}

	public int getSheetIndex()
	{
		return sheetIndex;
	}

	/**
	 * 设置此数据集所处的sheet号。
	 * 
	 * @param sheetIndex
	 *            sheet号（以{@code 1}计数）
	 */
	public void setSheetIndex(int sheetIndex)
	{
		this.sheetIndex = sheetIndex;
	}

	/**
	 * 是否有名称行。
	 * 
	 * @return
	 */
	public boolean hasNameRow()
	{
		return (this.nameRow > 0);
	}

	/**
	 * 获取作为名称行的行号。
	 * 
	 * @return
	 */
	public int getNameRow()
	{
		return nameRow;
	}

	/**
	 * 设置作为名称行的行号。
	 * 
	 * @param nameRow
	 *            行号，小于{@code 1}则表示无名称行。
	 */
	public void setNameRow(int nameRow)
	{
		this.nameRow = nameRow;
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
	 * <p>
	 * 标题行（{@linkplain #getNameRow()}）将自动被排除。
	 * </p>
	 * <p>
	 * 注意：行号以{@code 1}开始计数。
	 * </p>
	 * 
	 * @param dataRowExp
	 *            表达式，为{@code null}、{@code ""}则不限定
	 */
	public void setDataRowExp(String dataRowExp)
	{
		this.dataRowExp = dataRowExp;
		this._dataRowRanges = getRangeExpResolver().resolveIndex(this.dataRowExp);
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
	 * @param dataColumnExp
	 *            表达式，为{@code null}、{@code ""}则不限定
	 */
	public void setDataColumnExp(String dataColumnExp)
	{
		this.dataColumnExp = dataColumnExp;
		this._dataColumnRanges = resolveDataColumnRanges(dataColumnExp);
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

	@Override
	protected ResolvedDataSetResult resolveResult(DataSetQuery query, List<DataSetProperty> properties,
			boolean resolveProperties) throws DataSetException
	{
		File file = null;

		try
		{
			file = getExcelFile(query);
		}
		catch (DataSetException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetSourceParseException(t);
		}

		ResolvedDataSetResult result = null;

		if (isXls(file))
			result = resolveResultForXls(query, file, properties, resolveProperties);
		else
			result = resolveResultForXlsx(query, file, properties, resolveProperties);

		return result;
	}

	/**
	 * 解析{@code xls}结果。
	 * 
	 * @param query
	 * @param file
	 * @param properties
	 *            允许为{@code null}
	 * @param resolveProperties
	 * @throws DataSetException
	 */
	protected ResolvedDataSetResult resolveResultForXls(DataSetQuery query, File file,
			List<DataSetProperty> properties, boolean resolveProperties) throws DataSetException
	{
		POIFSFileSystem poifs = null;
		HSSFWorkbook wb = null;

		try
		{
			poifs = new POIFSFileSystem(file, true);
			wb = new HSSFWorkbook(poifs.getRoot(), true);

			Sheet sheet = wb.getSheetAt(getSheetIndex() - 1);

			return resolveResultForSheet(query, sheet, properties, resolveProperties);
		}
		catch (DataSetException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetSourceParseException(t);
		}
		finally
		{
			IOUtil.close(wb);
			IOUtil.close(poifs);
		}
	}

	/**
	 * 解析{@code xlsx}结果。
	 * 
	 * @param query
	 * @param file
	 * @param properties
	 *            允许为{@code null}
	 * @param resolveProperties
	 * @return
	 * @throws DataSetException
	 */
	protected ResolvedDataSetResult resolveResultForXlsx(DataSetQuery query, File file,
			List<DataSetProperty> properties, boolean resolveProperties) throws DataSetException
	{
		OPCPackage pkg = null;
		XSSFWorkbook wb = null;

		try
		{
			pkg = OPCPackage.open(file, PackageAccess.READ);
			wb = new XSSFWorkbook(pkg);

			Sheet sheet = wb.getSheetAt(getSheetIndex() - 1);

			return resolveResultForSheet(query, sheet, properties, resolveProperties);
		}
		catch (DataSetException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetSourceParseException(t);
		}
		finally
		{
			IOUtil.close(wb);
			IOUtil.close(pkg);
		}
	}

	/**
	 * 解析sheet结果。
	 * 
	 * @param query
	 * @param sheet
	 * @param properties
	 *            允许为{@code null}
	 * @param resolveProperties
	 * @return
	 * @throws Throwable
	 */
	protected ResolvedDataSetResult resolveResultForSheet(DataSetQuery query, Sheet sheet,
			List<DataSetProperty> properties, boolean resolveProperties) throws Throwable
	{
		List<Row> excelRows = new ArrayList<Row>();

		for (Row row : sheet)
			excelRows.add(row);

		List<ExcelPropertyInfo> rawDataPropertyInfos = resolvePropertyInfos(excelRows);
		List<Map<String, Object>> rawData = resolveRawData(query, rawDataPropertyInfos, excelRows);

		if (resolveProperties)
		{
			List<String> rawDataPropertyNames = toPropertyNames(rawDataPropertyInfos);
			List<DataSetProperty> resolvedProperties = resolveProperties(rawDataPropertyNames, rawData);
			mergeDataSetProperties(resolvedProperties, properties);
			properties = resolvedProperties;
		}

		return resolveResult(rawData, properties, query.getResultDataFormat());
	}

	/**
	 * 解析数据属性信息列表。
	 * 
	 * @param excelRows
	 * @return
	 * @throws Throwable
	 */
	protected List<ExcelPropertyInfo> resolvePropertyInfos(List<Row> excelRows) throws Throwable
	{
		List<ExcelPropertyInfo> propertyInfos = null;

		for (int i = 0, len = excelRows.size(); i < len; i++)
		{
			Row row = excelRows.get(i);

			if (isNameRow(i))
			{
				propertyInfos = new ArrayList<ExcelPropertyInfo>();

				short minColIdx = row.getFirstCellNum(), maxColIdx = row.getLastCellNum();
				for (short colIdx = minColIdx; colIdx < maxColIdx; colIdx++)
				{
					if (isDataColumn(colIdx))
					{
						String name = null;

						Cell cell = row.getCell(colIdx);

						if (cell != null)
						{
							try
							{
								name = cell.getStringCellValue();
							}
							catch(Throwable t)
							{
							}
						}

						if (StringUtil.isEmpty(name))
							name = CellReference.convertNumToColString(colIdx);

						propertyInfos.add(new ExcelPropertyInfo(name, colIdx));
					}
				}

				break;
			}
			else if (isDataRow(i))
			{
				if (propertyInfos == null)
				{
					propertyInfos = new ArrayList<ExcelPropertyInfo>();

					short minColIdx = row.getFirstCellNum(), maxColIdx = row.getLastCellNum();
					for (short colIdx = minColIdx; colIdx < maxColIdx; colIdx++)
					{
						if (isDataColumn(colIdx))
						{
							String name = CellReference.convertNumToColString(colIdx);
							propertyInfos.add(new ExcelPropertyInfo(name, colIdx));
						}
					}
				}

				if (isAfterNameRow(i))
					break;
			}
		}

		if (propertyInfos == null)
			propertyInfos = Collections.emptyList();

		return propertyInfos;
	}

	/**
	 * 解析{@linkplain DataSetProperty}。
	 * 
	 * @param rawDataPropertyNames
	 * @param rawData              允许为{@code null}
	 * @return
	 * @throws Throwable
	 */
	protected List<DataSetProperty> resolveProperties(List<String> rawDataPropertyNames,
			List<Map<String, Object>> rawData) throws Throwable
	{
		int propertyLen = rawDataPropertyNames.size();
		List<DataSetProperty> properties = new ArrayList<>(propertyLen);

		for (String name : rawDataPropertyNames)
			properties.add(new DataSetProperty(name, DataSetProperty.DataType.UNKNOWN));

		if (rawData != null && rawData.size() > 0)
		{
			for (Map<String, Object> row : rawData)
			{
				int resolvedPropertyTypeCount = 0;

				for (int i = 0; i < propertyLen; i++)
				{
					DataSetProperty property = properties.get(i);

					if (!DataSetProperty.DataType.UNKNOWN.equals(property.getType()))
					{
						resolvedPropertyTypeCount++;
						continue;
					}

					Object value = row.get(rawDataPropertyNames.get(i));

					if (value != null)
						property.setType(resolvePropertyDataType(value));
				}

				if (resolvedPropertyTypeCount == propertyLen)
					break;
			}
		}

		return properties;
	}

	/**
	 * 解析原始数据。
	 * 
	 * @param query
	 * @param propertyInfos
	 * @param excelRows
	 * @return
	 * @throws Throwable
	 */
	protected List<Map<String, Object>> resolveRawData(DataSetQuery query,
			List<ExcelPropertyInfo> propertyInfos, List<Row> excelRows) throws Throwable
	{
		List<Map<String, Object>> data = new ArrayList<>();

		Map<Short, String> cellNumPropertyNames = toCellNumPropertyNames(propertyInfos);

		for (int i = 0, len = excelRows.size(); i < len; i++)
		{
			if (isNameRow(i) || !isDataRow(i))
				continue;

			if (isReachResultFetchSize(query, data.size()))
				break;

			Map<String, Object> row = new HashMap<>();

			Row excelRow = excelRows.get(i);

			short minColIdx = excelRow.getFirstCellNum(), maxColIdx = excelRow.getLastCellNum();
			for (short colIdx = minColIdx; colIdx < maxColIdx; colIdx++)
			{
				if (isDataColumn(colIdx))
				{
					Cell cell = excelRow.getCell(colIdx);
					String name = cellNumPropertyNames.get(colIdx);
					Object value = resolveCellValue(cell);
					row.put(name, value);
				}
			}

			data.add(row);
		}

		return data;
	}

	protected Map<Short, String> toCellNumPropertyNames(List<ExcelPropertyInfo> propertyInfos)
	{
		Map<Short, String> re = new HashMap<Short, String>();

		for (ExcelPropertyInfo epi : propertyInfos)
			re.put(epi.getCellIdx(), epi.getName());

		return re;
	}

	protected List<String> toPropertyNames(List<ExcelPropertyInfo> propertyInfos)
	{
		List<String> re = new ArrayList<String>(propertyInfos.size());

		for (ExcelPropertyInfo epi : propertyInfos)
			re.add(epi.getName());

		return re;
	}

	/**
	 * 解析单元格属性值。
	 * 
	 * @param cell 允许为{@code null}
	 * @return
	 * @throws DataSetSourceParseException
	 * @throws DataSetException
	 */
	protected Object resolveCellValue(Cell cell) throws DataSetSourceParseException, DataSetException
	{
		if (cell == null)
			return null;

		CellType cellType = cell.getCellType();

		Object cellValue = null;

		try
		{
			if (CellType.BLANK.equals(cellType))
			{
				cellValue = null;
			}
			else if (CellType.BOOLEAN.equals(cellType))
			{
				cellValue = cell.getBooleanCellValue();
			}
			else if (CellType.ERROR.equals(cellType))
			{
				cellValue = cell.getErrorCellValue();
			}
			else if (CellType.FORMULA.equals(cellType))
			{
				cellValue = cell.getCellFormula();
			}
			else if (CellType.NUMERIC.equals(cellType))
			{
				if (DateUtil.isCellDateFormatted(cell))
					cellValue = cell.getDateCellValue();
				else
					cellValue = cell.getNumericCellValue();
			}
			else if (CellType.STRING.equals(cellType))
			{
				cellValue = cell.getStringCellValue();
			}
		}
		catch(DataSetException e)
		{
			throw e;
		}
		catch(Throwable t)
		{
			throw new DataSetSourceParseException(t);
		}

		return cellValue;
	}

	/**
	 * 是否名称行
	 * 
	 * @param rowIndex
	 *            行索引（以{@code 0}计数）
	 * @return
	 */
	protected boolean isNameRow(int rowIndex)
	{
		return ((rowIndex + 1) == this.nameRow);
	}

	/**
	 * 是否在名称行之后。
	 * <p>
	 * 如果没有名称行，应返回{@code true}。
	 * </p>
	 * 
	 * @param rowIndex
	 *            行索引（以{@code 0}计数）
	 * @return
	 */
	protected boolean isAfterNameRow(int rowIndex)
	{
		return ((rowIndex + 1) > this.nameRow);
	}

	/**
	 * 是否数据行。
	 * 
	 * @param rowIndex
	 *            行索引（以{@code 0}计数）
	 * @return
	 */
	protected boolean isDataRow(int rowIndex)
	{
		if (isNameRow(rowIndex))
			return false;

		if (this._dataRowRanges == null || this._dataRowRanges.isEmpty())
			return true;

		return IndexRange.includes(this._dataRowRanges, rowIndex + 1);
	}

	/**
	 * 是否数据列。
	 * 
	 * @param columnIndex
	 *            列索引（以{@code 0}计数）
	 * @return
	 */
	protected boolean isDataColumn(int columnIndex)
	{
		if (this._dataColumnRanges == null || this._dataColumnRanges.isEmpty())
			return true;

		return IndexRange.includes(this._dataColumnRanges, columnIndex);
	}

	@SuppressWarnings("unchecked")
	protected List<IndexRange> resolveDataColumnRanges(String dataColumnExp) throws DataSetException
	{
		List<Range> ranges = getRangeExpResolver().resolve(dataColumnExp);

		if (ranges == null || ranges.isEmpty())
			return Collections.EMPTY_LIST;

		List<IndexRange> indexRanges = new ArrayList<>(ranges.size());

		for (Range range : ranges)
		{
			int from = 0;
			int to = -1;

			String fromStr = range.trimFrom();
			String toStr = range.trimTo();

			if (!StringUtil.isEmpty(fromStr))
				from = CellReference.convertColStringToIndex(fromStr);

			if (!StringUtil.isEmpty(toStr))
				to = CellReference.convertColStringToIndex(toStr);

			indexRanges.add(new IndexRange(from, to));
		}

		return indexRanges;
	}

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

	protected RangeExpResolver getRangeExpResolver()
	{
		return RANGE_EXP_RESOLVER;
	}

	/**
	 * 获取Excel文件。
	 * <p>
	 * 实现方法应该返回实例级不变的文件。
	 * </p>
	 * 
	 * @param query
	 * @return
	 * @throws Throwable
	 */
	protected abstract File getExcelFile(DataSetQuery query) throws Throwable;

	protected static class ExcelPropertyInfo implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 属性名 */
		private final String name;

		/**
		 * 单元格序号，介于{@linkplain Row#getFirstCellNum()}和{@linkplain Row#getLastCellNum()}之间
		 */
		private final short cellIdx;

		public ExcelPropertyInfo(String name, short cellIdx)
		{
			super();
			this.name = name;
			this.cellIdx = cellIdx;
		}

		public String getName()
		{
			return name;
		}

		public short getCellIdx()
		{
			return cellIdx;
		}
	}
}
