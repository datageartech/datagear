/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dbmodel;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.support.MU;
import org.datagear.persistence.columnconverter.LOBConversionContext;
import org.datagear.persistence.columnconverter.LOBConversionContext.LOBConversionSetting;
import org.datagear.persistence.support.AbstractModelDataAccessObject;
import org.datagear.util.IDUtil;
import org.datagear.util.JdbcUtil;
import org.datagear.util.JdbcUtil.QueryResultSet;

/**
 * 基于查询SQL的模型数据查询服务类。
 * 
 * @author datagear@163.com
 *
 */
public class ModelSqlSelectService extends AbstractModelDataAccessObject
{
	public static final byte[] DEFAULT_BLOB_TO_BYTES_PLACEHOLDER = new byte[] { 0x00 };

	public static final int DEFAULT_CLOB_LEFT_LEFTH = 200;

	/** BLOB文件存储目录 */
	private File blobFileManagerDirectory;

	/** BLOB转文件的占位符名称 */
	private String blobToFilePlaceholderName;

	/** BLOB转字节数组的占位符 */
	private byte[] blobToBytesPlaceholder = DEFAULT_BLOB_TO_BYTES_PLACEHOLDER;

	/** CLOB的取左最大长度 */
	private int clobLeftLength = DEFAULT_CLOB_LEFT_LEFTH;

	/** 完全加载大对象数据的最大行号，从1开始，小于1表示全都占位符加载 */
	private int fullLoadingLobMaxRow = 0;

	public ModelSqlSelectService()
	{
		super();
	}

	public ModelSqlSelectService(File blobFileManagerDirectory, String blobToFilePlaceholderName)
	{
		super();
		this.blobFileManagerDirectory = blobFileManagerDirectory;
		this.blobToFilePlaceholderName = blobToFilePlaceholderName;
	}

	public File getBlobFileManagerDirectory()
	{
		return blobFileManagerDirectory;
	}

	public void setBlobFileManagerDirectory(File blobFileManagerDirectory)
	{
		this.blobFileManagerDirectory = blobFileManagerDirectory;
	}

	public String getBlobToFilePlaceholderName()
	{
		return blobToFilePlaceholderName;
	}

	public void setBlobToFilePlaceholderName(String blobToFilePlaceholderName)
	{
		this.blobToFilePlaceholderName = blobToFilePlaceholderName;
	}

	public byte[] getBlobToBytesPlaceholder()
	{
		return blobToBytesPlaceholder;
	}

	public void setBlobToBytesPlaceholder(byte[] blobToBytesPlaceholder)
	{
		this.blobToBytesPlaceholder = blobToBytesPlaceholder;
	}

	public int getClobLeftLength()
	{
		return clobLeftLength;
	}

	public void setClobLeftLength(int clobLeftLength)
	{
		this.clobLeftLength = clobLeftLength;
	}

	public int getFullLoadingLobMaxRow()
	{
		return fullLoadingLobMaxRow;
	}

	public void setFullLoadingLobMaxRow(int fullLoadingLobMaxRow)
	{
		this.fullLoadingLobMaxRow = fullLoadingLobMaxRow;
	}

	/**
	 * 读取SQL的查询结果集。
	 * 
	 * @param cn
	 * @param sql
	 * @param startRow
	 * @param fetchSize
	 * @param databaseModelResolver
	 * @return
	 * @throws SQLException
	 */
	public ModelSqlResult select(Connection cn, String sql, int startRow, int fetchSize,
			DatabaseModelResolver databaseModelResolver) throws SQLException
	{
		Statement st = null;
		ResultSet rs = null;

		try
		{
			QueryResultSet queryResultSet = JdbcUtil.executeQuery(cn, sql, fetchSize);
			st = queryResultSet.getStatement();
			rs = queryResultSet.getResultSet();

			Model model = databaseModelResolver.resolve(cn, rs, IDUtil.uuid());

			return select(cn, sql, rs, model, startRow, fetchSize);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
			JdbcUtil.closeStatement(st);
		}
	}

	/**
	 * 读取SQL的查询结果集。
	 * 
	 * @param cn
	 * @param sql
	 * @param model
	 * @param startRow
	 * @param fetchSize
	 * @return
	 * @throws SQLException
	 */
	public ModelSqlResult select(Connection cn, String sql, Model model, int startRow, int fetchSize)
			throws SQLException
	{
		Statement st = null;
		ResultSet rs = null;

		try
		{
			QueryResultSet queryResultSet = JdbcUtil.executeQuery(cn, sql, fetchSize);
			st = queryResultSet.getStatement();
			rs = queryResultSet.getResultSet();

			return select(cn, sql, rs, model, startRow, fetchSize);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
			JdbcUtil.closeStatement(st);
		}
	}

	/**
	 * 读取SQL的查询结果集。
	 * 
	 * @param cn
	 * @param sql
	 * @param rs
	 * @param model
	 * @param startRow
	 * @param fetchSize
	 * @return
	 * @throws SQLException
	 */
	public ModelSqlResult select(Connection cn, String sql, ResultSet rs, Model model, int startRow, int fetchSize)
			throws SQLException
	{
		ModelSqlResult modelSqlResult = new ModelSqlResult();
		modelSqlResult.setSql(sql);
		modelSqlResult.setModel(model);
		modelSqlResult.setStartRow(startRow);
		modelSqlResult.setFetchSize(fetchSize);

		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		Property[] properties = model.getProperties();

		if (properties == null || properties.length != columnCount)
			throw new IllegalArgumentException("ResultSet not match Model");

		List<Object> datas = new ArrayList<Object>();

		JdbcUtil.moveToBeforeRow(rs, startRow);

		LOBConversionSetting contextLOBConversionSetting = LOBConversionContext.get();
		LOBConversionSetting fullLoadingLOBConversionSetting = getFullLoadingLOBConversionSetting();
		LOBConversionSetting placeholderLoadingLOBConversionSetting = getPlaceholderLoadingLOBConversionSetting();
		LOBConversionSetting prevLOBConversionSetting = null;

		for (int row = startRow; row < startRow + fetchSize; row++)
		{
			if (!rs.next())
				break;

			LOBConversionSetting myLOBConversionSetting = (row <= this.fullLoadingLobMaxRow
					? fullLoadingLOBConversionSetting
					: placeholderLoadingLOBConversionSetting);
			if (prevLOBConversionSetting != myLOBConversionSetting)
			{
				LOBConversionContext.set(myLOBConversionSetting);
				prevLOBConversionSetting = myLOBConversionSetting;
			}

			Object data = model.newInstance();

			for (int j = 1; j <= columnCount; j++)
			{
				Property property = properties[j - 1];

				if (!MU.isPrimitiveProperty(property))
					throw new IllegalArgumentException("The " + (j - 1) + "-th property must be primitive");

				Object columnValue = toPropertyValue(cn, rs, row, j, model, property);

				property.set(data, columnValue);
			}

			datas.add(data);
		}

		LOBConversionContext.set(contextLOBConversionSetting);

		modelSqlResult.setDatas(datas);

		return modelSqlResult;
	}

	/**
	 * 获取完全加载LOB的{@linkplain LOBConversionSetting}。
	 * 
	 * @return
	 */
	protected LOBConversionSetting getFullLoadingLOBConversionSetting()
	{
		return new LOBConversionSetting();
	}

	/**
	 * 获取占位符加载LOB的{@linkplain LOBConversionSetting}。
	 * 
	 * @return
	 */
	protected LOBConversionSetting getPlaceholderLoadingLOBConversionSetting()
	{
		File blobToFilePlaceholder = new File(this.blobFileManagerDirectory, this.blobToFilePlaceholderName);

		return new LOBConversionSetting(blobToFilePlaceholder, this.blobToBytesPlaceholder, this.clobLeftLength);
	}

	/**
	 * 模型查询结果。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class ModelSqlResult
	{
		private String sql;

		private Model model;

		private int startRow;

		private int fetchSize;

		private List<Object> datas;

		public ModelSqlResult()
		{
			super();
		}

		public ModelSqlResult(String sql, Model model, int startRow, int fetchSize, List<Object> datas)
		{
			super();
			this.sql = sql;
			this.model = model;
			this.startRow = startRow;
			this.fetchSize = fetchSize;
			this.datas = datas;
		}

		public String getSql()
		{
			return sql;
		}

		public void setSql(String sql)
		{
			this.sql = sql;
		}

		public Model getModel()
		{
			return model;
		}

		public void setModel(Model model)
		{
			this.model = model;
		}

		public int getStartRow()
		{
			return startRow;
		}

		public void setStartRow(int startRow)
		{
			this.startRow = startRow;
		}

		public int getFetchSize()
		{
			return fetchSize;
		}

		public void setFetchSize(int fetchSize)
		{
			this.fetchSize = fetchSize;
		}

		public boolean hasData()
		{
			return (this.datas != null && !this.datas.isEmpty());
		}

		public List<Object> getDatas()
		{
			return datas;
		}

		public void setDatas(List<Object> datas)
		{
			this.datas = datas;
		}

		public boolean hasMoreData()
		{
			return this.datas != null && this.datas.size() >= this.fetchSize;
		}

		public int getNextStartRow()
		{
			return this.startRow + this.fetchSize;
		}
	}
}
