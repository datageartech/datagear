/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetOption;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetResult;
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
	protected ResolvedDataSetResult resolveResult(Map<String, ?> paramValues, List<DataSetProperty> properties,
			DataSetOption dataSetOption) throws DataSetException
	{
		File file = null;

		try
		{
			file = getExcelFile(paramValues);
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
			result = resolveResultForXls(paramValues, file, properties, dataSetOption);
		else
			result = resolveResultForXlsx(paramValues, file, properties, dataSetOption);

		return result;
	}

	/**
	 * 解析{@code xls}结果。
	 * 
	 * @param paramValues
	 * @param file
	 * @param properties
	 *            允许为{@code null}，此时会自动解析
	 * @param dataSetOption
	 *            允许为{@code null}
	 * @return
	 * @throws DataSetException
	 */
	protected ResolvedDataSetResult resolveResultForXls(Map<String, ?> paramValues, File file,
			List<DataSetProperty> properties, DataSetOption dataSetOption) throws DataSetException
	{
		POIFSFileSystem poifs = null;
		HSSFWorkbook wb = null;

		try
		{
			poifs = new POIFSFileSystem(file, true);
			wb = new HSSFWorkbook(poifs.getRoot(), true);

			Sheet sheet = wb.getSheetAt(getSheetIndex() - 1);

			return resolveResultForSheet(paramValues, sheet, properties, dataSetOption);
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
	 * @param paramValues
	 * @param file
	 * @param properties
	 *            允许为{@code null}，此时会自动解析
	 * @param dataSetOption
	 *            允许为{@code null}
	 * @return
	 * @throws DataSetException
	 */
	protected ResolvedDataSetResult resolveResultForXlsx(Map<String, ?> paramValues, File file,
			List<DataSetProperty> properties, DataSetOption dataSetOption) throws DataSetException
	{
		OPCPackage pkg = null;
		XSSFWorkbook wb = null;

		try
		{
			pkg = OPCPackage.open(file, PackageAccess.READ);
			wb = new XSSFWorkbook(pkg);

			Sheet sheet = wb.getSheetAt(getSheetIndex() - 1);

			return resolveResultForSheet(paramValues, sheet, properties, dataSetOption);
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
	 * @param paramValues
	 * @param sheet
	 * @param properties
	 *            允许为{@code null}，此时会自动解析
	 * @param dataSetOption
	 *            允许为{@code null}
	 * @return
	 * @throws DataSetException
	 */
	protected ResolvedDataSetResult resolveResultForSheet(Map<String, ?> paramValues, Sheet sheet,
			List<DataSetProperty> properties, DataSetOption dataSetOption) throws DataSetException
	{
		boolean resolveProperties = (properties == null || properties.isEmpty());

		if (resolveProperties)
			properties = new ArrayList<>();

		List<List<Object>> data = new ArrayList<>();

		List<String> propertyNames = null;

		DataSetPropertyValueConverter converter = createDataSetPropertyValueConverter();

		try
		{
			int rowIdx = 0;
			int dataRowIdx = 0;

			for (Row row : sheet)
			{
				if (isNameRow(rowIdx))
				{
					if (resolveProperties)
						propertyNames = resolveDataSetPropertyNames(row, false);
				}
				else if (isDataRow(rowIdx))
				{
					if (resolveProperties && dataRowIdx == 0 && propertyNames == null)
						propertyNames = resolveDataSetPropertyNames(row, true);

					// 名称行不一定在数据行之前，此时可能还无法确定属性名，所以暂时采用列表存储
					List<Object> rowObj = new ArrayList<>();

					int colIdx = 0;
					int dataColIdx = 0;

					for (Cell cell : row)
					{
						if (isDataColumn(colIdx))
						{
							DataSetProperty property = null;

							if (!resolveProperties)
							{
								if (dataColIdx >= properties.size())
									throw new DataSetSourceParseException(
											"No property defined for column index " + dataColIdx);

								property = properties.get(dataColIdx);
							}

							Object value = resolvePropertyValue(cell, property, converter);

							if (resolveProperties)
							{
								property = resolveDataSetProperty(row, rowIdx, dataRowIdx, cell, colIdx, dataColIdx,
										value, properties);
							}

							rowObj.add(value);

							dataColIdx++;
						}

						colIdx++;
					}

					boolean reachMaxCount = isReachResultDataMaxCount(dataSetOption, data.size());
					boolean breakLoop = (reachMaxCount && (!resolveProperties || isAfterNameRow(rowIdx)));

					if (!reachMaxCount)
						data.add(rowObj);

					dataRowIdx++;

					if (breakLoop)
						break;
				}

				rowIdx++;
			}
		}
		catch (DataSetException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetSourceParseException(t);
		}

		if (resolveProperties)
			inflateDataSetProperties(properties, propertyNames);

		DataSetResult result = new DataSetResult(listRowsToMapRows(data, properties));

		return new ResolvedDataSetResult(result, properties);
	}

	protected void inflateDataSetProperties(List<DataSetProperty> properties, List<String> propertyNames)
	{
		for (int i = 0; i < properties.size(); i++)
		{
			DataSetProperty property = properties.get(i);
			property.setName(propertyNames.get(i));

			if (StringUtil.isEmpty(property.getType()))
				property.setType(DataSetProperty.DataType.UNKNOWN);
		}
	}

	/**
	 * 解析{@linkplain DataSetProperty}并写入{@code properties}。
	 * 
	 * @param row
	 * @param rowIdx
	 * @param dataRowIdx
	 * @param cell
	 * @param colIdx
	 * @param dataColIdx
	 * @param cellValue
	 * @param properties
	 * @return
	 */
	protected DataSetProperty resolveDataSetProperty(Row row, int rowIdx, int dataRowIdx, Cell cell, int colIdx,
			int dataColIdx, Object cellValue, List<DataSetProperty> properties)
	{
		DataSetProperty property = null;

		if (dataRowIdx == 0)
		{
			// 空单元格先不处理数据类型，等待后续有非空单元格再判断
			String dataType = (cellValue == null ? "" : resolvePropertyDataType(cellValue));

			// 名称行不一定在数据行之前，所以此时可能无法确定属性名
			property = new DataSetProperty("should-be-set-later", dataType);
			properties.add(property);
		}
		else
		{
			property = properties.get(dataColIdx);

			if (StringUtil.isEmpty(property.getType()) && cellValue != null)
				property.setType(resolvePropertyDataType(cellValue));
		}

		return property;
	}

	/**
	 * 解析属性名。
	 * 
	 * @param nameRow
	 * @return
	 */
	protected List<String> resolveDataSetPropertyNames(Row nameRow, boolean forceColumnString)
	{
		List<String> propertyNames = new ArrayList<>();

		int colIdx = 0;
		for (Cell cell : nameRow)
		{
			if (isDataColumn(colIdx))
			{
				String name = null;

				if (forceColumnString)
					name = CellReference.convertNumToColString(colIdx);
				else
				{
					try
					{
						name = cell.getStringCellValue();
					}
					catch (Throwable t)
					{
					}

					if (StringUtil.isEmpty(name))
						name = CellReference.convertNumToColString(colIdx);
				}

				propertyNames.add(name);
			}

			colIdx++;
		}

		return propertyNames;
	}

	/**
	 * 解析单元格属性值。
	 * 
	 * @param cell
	 * @param property
	 *            允许为{@code null}
	 * @param converter
	 * @return
	 * @throws DataSetSourceParseException
	 * @throws DataSetException
	 */
	protected Object resolvePropertyValue(Cell cell, DataSetProperty property, DataSetPropertyValueConverter converter)
			throws DataSetSourceParseException, DataSetException
	{
		CellType cellType = cell.getCellTypeEnum();

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
		catch (DataSetException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new DataSetSourceParseException(t);
		}

		cellValue = convertToPropertyDataType(converter, cellValue, property);

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
	 * @param paramValues
	 * @return
	 * @throws Throwable
	 */
	protected abstract File getExcelFile(Map<String, ?> paramValues) throws Throwable;
}
