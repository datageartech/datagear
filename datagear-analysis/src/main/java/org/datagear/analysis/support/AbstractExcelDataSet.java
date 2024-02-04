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

package org.datagear.analysis.support;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.support.AbstractExcelDataSet.ExcelDataSetResource;
import org.datagear.analysis.support.RangeExpResolver.IndexRange;
import org.datagear.analysis.support.RangeExpResolver.Range;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象Excel数据集。
 * <p>
 * 此类仅支持从Excel的单个sheet读取数据，具体参考{@linkplain #setSheetName(String)}、{@linkplain #setSheetIndex(int)}。
 * </p>
 * <p>
 * 通过{@linkplain #setDataRowExp(String)}、{@linkplain #setDataColumnExp(String)}可设置读取行、列范围。
 * </p>
 * <p>
 * 通过{@linkplain #setNameRow(int)}可设置名称行。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractExcelDataSet<T extends ExcelDataSetResource> extends AbstractResolvableResourceDataSet<T>
{
	private static final long serialVersionUID = 1L;

	protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractExcelDataSet.class);

	public static final String EXTENSION_XLSX = "xlsx";

	public static final String EXTENSION_XLS = "xls";

	protected static final RangeExpResolver RANGE_EXP_RESOLVER = RangeExpResolver
			.valueOf(RangeExpResolver.RANGE_SPLITTER_CHAR, RangeExpResolver.RANGE_GROUP_SPLITTER_CHAR);

	/** 数据集数据所处的sheet名称 */
	private String sheetName = "";

	/** 数据集数据所处的sheet索引号（以1计数） */
	private int sheetIndex = -1;

	/** 作为名称行的行号 */
	private int nameRow = -1;

	/** 数据行范围表达式 */
	private String dataRowExp = "";

	/** 数据列范围表达式 */
	private String dataColumnExp = "";

	/** 是否强制作为xls文件处理 */
	private boolean forceXls = false;

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

	/**
	 * 获取此数据集所处的sheet名。
	 * <p>
	 * 如果返回{@code null}、{@code ""}，则表名应使用{@linkplain #getSheetIndex()}。
	 * </p>
	 * 
	 * @return
	 */
	public String getSheetName()
	{
		return sheetName;
	}

	public void setSheetName(String sheetName)
	{
		this.sheetName = sheetName;
	}

	/**
	 * 获取此数据集所处的sheet号，与{@linkplain #getSheetName()}功能相同，用于兼容旧版本（4.0及以前版本）。
	 * <p>
	 * 只有在{@linkplain #getSheetName()}为{@code null}、{@code ""}时，才应使用此值。
	 * </p>
	 * 
	 * @return 返回值{@code <1}表示未设置，应使用默认规则（比如第一个sheet）
	 */
	public int getSheetIndex()
	{
		return sheetIndex;
	}
	
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
	protected ResourceData resolveResourceData(T resource, boolean resolveProperties) throws Throwable
	{
		if (resource.isXls())
			return resolveExcelResourceDataForXls(resource, resolveProperties);
		else
			return resolveExcelResourceDataForXlsx(resource, resolveProperties);
	}

	/**
	 * 解析{@code ExcelResourceData}。
	 * 
	 * @param resource
	 * @return
	 * @throws DataSetException
	 */
	protected ResourceData resolveExcelResourceDataForXls(T resource, boolean resolveProperties)
			throws DataSetException
	{
		InputStream in = null;
		POIFSFileSystem poifs = null;
		HSSFWorkbook wb = null;

		try
		{
			in = resource.getInputStream();
			poifs = new POIFSFileSystem(in);
			wb = new HSSFWorkbook(poifs.getRoot(), true);
			Sheet sheet = resource.getDataSheet(wb);

			return resolveExcelResourceDataForSheet(resource, sheet, resolveProperties);
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
			IOUtil.close(in);
		}
	}

	/**
	 * 解析{@code ExcelResourceData}。
	 * 
	 * @param resource
	 * @return
	 * @throws Throwable
	 */
	protected ResourceData resolveExcelResourceDataForXlsx(T resource, boolean resolveProperties) throws Throwable
	{
		InputStream in = null;
		OPCPackage pkg = null;
		XSSFWorkbook wb = null;

		try
		{
			in = resource.getInputStream();
			pkg = OPCPackage.open(in);
			wb = new XSSFWorkbook(pkg);
			Sheet sheet = resource.getDataSheet(wb);

			return resolveExcelResourceDataForSheet(resource, sheet, resolveProperties);
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
			IOUtil.close(in);
		}
	}
	
	/**
	 * 解析sheet数据。
	 * 
	 * @param resource
	 * @param sheet
	 * @param resolveProperties
	 * @return
	 * @throws Throwable
	 */
	protected ResourceData resolveExcelResourceDataForSheet(T resource, Sheet sheet, boolean resolveProperties)
			throws Throwable
	{
		List<Row> excelRows = new ArrayList<Row>();

		for (Row row : sheet)
			excelRows.add(row);

		List<ExcelPropertyInfo> propertyInfos = resolvePropertyInfos(resource, excelRows);
		List<Map<String, Object>> data = resolveData(resource, propertyInfos, excelRows);

		List<DataSetProperty> properties = null;

		if (resolveProperties)
		{
			List<String> rawDataPropertyNames = toPropertyNames(propertyInfos);
			properties = resolveProperties(rawDataPropertyNames, data);
		}

		return new ResourceData(data, properties);
	}

	/**
	 * 解析属性信息。
	 * 
	 * @param resource
	 * @param excelRows
	 * @return
	 * @throws Throwable
	 */
	protected List<ExcelPropertyInfo> resolvePropertyInfos(T resource, List<Row> excelRows)
			throws Throwable
	{
		List<ExcelPropertyInfo> propertyInfos = null;

		for (int i = 0, len = excelRows.size(); i < len; i++)
		{
			Row row = excelRows.get(i);

			if (resource.isNameRow(i))
			{
				propertyInfos = new ArrayList<ExcelPropertyInfo>();

				short minColIdx = row.getFirstCellNum(), maxColIdx = row.getLastCellNum();
				for (short colIdx = minColIdx; colIdx < maxColIdx; colIdx++)
				{
					if (resource.isDataColumn(colIdx))
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
			else if (resource.isDataRow(i))
			{
				if (propertyInfos == null)
				{
					propertyInfos = new ArrayList<ExcelPropertyInfo>();

					short minColIdx = row.getFirstCellNum(), maxColIdx = row.getLastCellNum();
					for (short colIdx = minColIdx; colIdx < maxColIdx; colIdx++)
					{
						if (resource.isDataColumn(colIdx))
						{
							String name = CellReference.convertNumToColString(colIdx);
							propertyInfos.add(new ExcelPropertyInfo(name, colIdx));
						}
					}
				}

				if (resource.isAfterNameRow(i))
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
	 * 解析数据。
	 * 
	 * @param resource
	 * @param propertyInfos
	 * @param excelRows
	 * @return
	 * @throws Throwable
	 */
	protected List<Map<String, Object>> resolveData(T resource,
			List<ExcelPropertyInfo> propertyInfos, List<Row> excelRows) throws Throwable
	{
		List<Map<String, Object>> data = new ArrayList<>();

		Map<Short, String> cellNumPropertyNames = toCellNumPropertyNames(propertyInfos);

		for (int i = 0, len = excelRows.size(); i < len; i++)
		{
			if (resource.isNameRow(i) || !resource.isDataRow(i))
				continue;

			Map<String, Object> row = new HashMap<>();

			Row excelRow = excelRows.get(i);

			short minColIdx = excelRow.getFirstCellNum(), maxColIdx = excelRow.getLastCellNum();
			for (short colIdx = minColIdx; colIdx < maxColIdx; colIdx++)
			{
				if (resource.isDataColumn(colIdx))
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
	
	protected static class ExcelPropertyInfo
	{
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

	/**
	 * Excel数据集资源。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static abstract class ExcelDataSetResource extends DataSetResource
	{
		private static final long serialVersionUID = 1L;

		private String sheetName;
		
		private int sheetIndex;

		private int nameRow;

		private String dataRowExp;

		private String dataColumnExp;

		private boolean xls;

		private transient List<IndexRange> _dataRowRanges = null;
		private transient List<IndexRange> _dataColumnRanges = null;

		public ExcelDataSetResource()
		{
			super();
		}

		public ExcelDataSetResource(String resolvedTemplate, String sheetName, int sheetIndex, int nameRow, String dataRowExp,
				String dataColumnExp, boolean xls)
		{
			super(resolvedTemplate);
			this.sheetName = sheetName;
			this.sheetIndex = sheetIndex;
			this.nameRow = nameRow;
			this.dataRowExp = dataRowExp;
			this.dataColumnExp = dataColumnExp;
			this.xls = xls;

			this._dataRowRanges = getRangeExpResolver().resolveIndex(this.dataRowExp);
			this._dataColumnRanges = resolveDataColumnRanges(dataColumnExp);
		}

		public String getSheetName()
		{
			return sheetName;
		}

		public int getSheetIndex()
		{
			return sheetIndex;
		}

		public int getNameRow()
		{
			return nameRow;
		}

		public String getDataRowExp()
		{
			return dataRowExp;
		}

		public String getDataColumnExp()
		{
			return dataColumnExp;
		}

		public boolean isXls()
		{
			return xls;
		}
		
		/**
		 * 获取数据所在工作表。
		 * 
		 * @param wb
		 * @return
		 * @throws DataSetException
		 */
		public Sheet getDataSheet(Workbook wb) throws DataSetException
		{
			Sheet sheet = null;
			
			//sheet名应优先使用
			if(!StringUtil.isEmpty(this.sheetName))
				sheet = wb.getSheet(this.sheetName);
			else
			{
				int sheetIndex = (this.sheetIndex < 1 ? 0 : this.sheetIndex - 1);
				sheet = wb.getSheetAt(sheetIndex);
			}
			
			if(sheet == null)
				throw new DataSetSourceParseException("No sheet found");
			
			return sheet;
		}
		
		/**
		 * 获取Excel输入流。
		 * 
		 * @return
		 * @throws Throwable
		 */
		public abstract InputStream getInputStream() throws Throwable;

		/**
		 * 是否名称行。
		 * 
		 * @param rowIndex 行索引（以{@code 0}计数）
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
		 * @param rowIndex 行索引（以{@code 0}计数）
		 * @return
		 */
		protected boolean isAfterNameRow(int rowIndex)
		{
			return ((rowIndex + 1) > this.nameRow);
		}

		/**
		 * 是否数据行。
		 * 
		 * @param rowIndex 行索引（以{@code 0}计数）
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
		 * @param columnIndex 列索引（以{@code 0}计数）
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

		protected RangeExpResolver getRangeExpResolver()
		{
			return RANGE_EXP_RESOLVER;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((dataColumnExp == null) ? 0 : dataColumnExp.hashCode());
			result = prime * result + ((dataRowExp == null) ? 0 : dataRowExp.hashCode());
			result = prime * result + nameRow;
			result = prime * result + sheetIndex;
			result = prime * result + ((sheetName == null) ? 0 : sheetName.hashCode());
			result = prime * result + (xls ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			ExcelDataSetResource other = (ExcelDataSetResource) obj;
			if (dataColumnExp == null)
			{
				if (other.dataColumnExp != null)
					return false;
			}
			else if (!dataColumnExp.equals(other.dataColumnExp))
				return false;
			if (dataRowExp == null)
			{
				if (other.dataRowExp != null)
					return false;
			}
			else if (!dataRowExp.equals(other.dataRowExp))
				return false;
			if (nameRow != other.nameRow)
				return false;
			if (sheetIndex != other.sheetIndex)
				return false;
			if (sheetName == null)
			{
				if (other.sheetName != null)
					return false;
			}
			else if (!sheetName.equals(other.sheetName))
				return false;
			if (xls != other.xls)
				return false;
			return true;
		}
	}
}
