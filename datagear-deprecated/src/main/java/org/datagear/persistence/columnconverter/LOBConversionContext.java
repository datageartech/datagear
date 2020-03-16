/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.columnconverter;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Arrays;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.persistence.features.ColumnConverter;

/**
 * 大对象转换线程变量设置上下文。
 * 
 * @author datagear@163.com
 *
 */
public class LOBConversionContext
{
	private static final ThreadLocal<LOBConversionSetting> THREAD_CONVERSION_SETTING = new ThreadLocal<LOBConversionSetting>()
	{
		@Override
		protected LOBConversionSetting initialValue()
		{
			return new LOBConversionSetting();
		}
	};

	public LOBConversionContext()
	{
		super();
	}

	/**
	 * 获取大对象转换设置。
	 * <p>
	 * 此方法不会返回{@code null}。
	 * </p>
	 * 
	 * @return
	 */
	public static LOBConversionSetting get()
	{
		return THREAD_CONVERSION_SETTING.get();
	}

	/**
	 * 设置大对象转换设置。
	 * 
	 * @param conversionSetting
	 */
	public static void set(LOBConversionSetting conversionSetting)
	{
		if (conversionSetting == null)
			throw new IllegalArgumentException("[conversionSetting] must not be null");

		THREAD_CONVERSION_SETTING.set(conversionSetting);
	}

	/**
	 * 移除大对象转换设置。
	 */
	public static void remove()
	{
		THREAD_CONVERSION_SETTING.remove();
	}

	/**
	 * 大对象转换设置。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class LOBConversionSetting
	{
		/** 读取BLOB列值至File时的占位符 */
		private File blobToFilePlaceholder = null;

		/** 读取BLOB列值至byte[]时的占位符 */
		private byte[] blobToBytesPlaceholder = null;

		/** 是否开启CLOB列值的取左功能，以及取左长度 */
		private int leftClobLengthOnReading = -1;

		/** 文件转换为CLOB时的文件编码 */
		private String fileToClobEncoding = null;

		/** CLOB转换为文件时的文件编码 */
		private String clobToFileEncoding = null;

		public LOBConversionSetting()
		{
			super();
		}

		public LOBConversionSetting(File blobToFilePlaceholder, byte[] blobToBytesPlaceholder)
		{
			super();
			this.blobToFilePlaceholder = blobToFilePlaceholder;
			this.blobToBytesPlaceholder = blobToBytesPlaceholder;
		}

		public LOBConversionSetting(File blobToFilePlaceholder, byte[] blobToBytesPlaceholder,
				int leftClobLengthOnReading)
		{
			super();
			this.blobToFilePlaceholder = blobToFilePlaceholder;
			this.blobToBytesPlaceholder = blobToBytesPlaceholder;
			this.leftClobLengthOnReading = leftClobLengthOnReading;
		}

		public LOBConversionSetting(File blobToFilePlaceholder, byte[] blobToBytesPlaceholder,
				int leftClobLengthOnReading, String fileToClobEncoding)
		{
			super();
			this.blobToFilePlaceholder = blobToFilePlaceholder;
			this.blobToBytesPlaceholder = blobToBytesPlaceholder;
			this.leftClobLengthOnReading = leftClobLengthOnReading;
			this.fileToClobEncoding = fileToClobEncoding;
		}

		public LOBConversionSetting(File blobToFilePlaceholder, byte[] blobToBytesPlaceholder,
				int leftClobLengthOnReading, String fileToClobEncoding, String clobToFileEncoding)
		{
			super();
			this.blobToFilePlaceholder = blobToFilePlaceholder;
			this.blobToBytesPlaceholder = blobToBytesPlaceholder;
			this.leftClobLengthOnReading = leftClobLengthOnReading;
			this.fileToClobEncoding = fileToClobEncoding;
			this.clobToFileEncoding = clobToFileEncoding;
		}

		/**
		 * 是否开启读取BLOB列值至File时的占位符功能。
		 * 
		 * @return
		 */
		public boolean isBlobToFilePlaceholder()
		{
			return (this.blobToFilePlaceholder != null);
		}

		/**
		 * 获取读取BLOB列值至File时的占位符。
		 * 
		 * @return
		 */
		public File getBlobToFilePlaceholder()
		{
			return blobToFilePlaceholder;
		}

		/**
		 * 设置读取BLOB列值至File时的占位符。
		 * <p>
		 * 当设置为非{@code null}时，BLOB列值转换器的{@linkplain ColumnConverter#from(Connection, ResultSet, int, int, Model, Property, int, Model)}方法将有如下返回值：
		 * </p>
		 * <ul>
		 * <li>如果BLOB是{@code null}，将返回{@code null}；</li>
		 * <li>否则，如果目标类型是{@linkplain File}，将返回此{@code blobToFilePlaceholder}；</li>
		 * </ul>
		 * <p>
		 * 当设置为{@code null}时，BLOB列值转换器的{@linkplain ColumnConverter#from(Connection, ResultSet, int, int, Model, Property, int, Model)}方法将返回真实对象。
		 * </p>
		 * 
		 * @param blobToFilePlaceholder
		 */
		public void setBlobToFilePlaceholder(File blobToFilePlaceholder)
		{
			this.blobToFilePlaceholder = blobToFilePlaceholder;
		}

		/**
		 * 是否开启读取BLOB列值至bytes[]时的占位符功能。
		 * 
		 * @return
		 */
		public boolean isBlobToBytesPlaceholder()
		{
			return (this.blobToBytesPlaceholder != null);
		}

		/**
		 * 获取读取BLOB列值至bytes[]时的占位符。
		 * 
		 * @return
		 */
		public byte[] getBlobToBytesPlaceholder()
		{
			return blobToBytesPlaceholder;
		}

		/**
		 * 设置读取BLOB列值至byte[]时的占位符。
		 * <p>
		 * 当设置为非{@code null}时，BLOB列值转换器的{@linkplain ColumnConverter#from(Connection, ResultSet, int, int, Model, Property, int, Model)}方法将有如下返回值：
		 * </p>
		 * <ul>
		 * <li>如果BLOB是{@code null}，将返回{@code null}；</li>
		 * <li>否则，如果目标类型是{@code byte[]}，将返回此{@code blobToBytesPlaceholder}；</li>
		 * </ul>
		 * <p>
		 * 当设置为{@code null}时，BLOB列值转换器的{@linkplain ColumnConverter#from(Connection, ResultSet, int, int, Model, Property, int, Model)}方法将返回真实对象。
		 * </p>
		 * 
		 * @param blobToBytesPlaceholder
		 */
		public void setBlobToBytesPlaceholder(byte[] blobToBytesPlaceholder)
		{
			this.blobToBytesPlaceholder = blobToBytesPlaceholder;
		}

		/**
		 * 是否已开启CLOB列值的取左功能。
		 * 
		 * @return
		 */
		public boolean isLeftClobOnReading()
		{
			return (this.leftClobLengthOnReading > -1);
		}

		/**
		 * 获取CLOB列值的取左功能的取左长度。
		 * 
		 * @return
		 */
		public int getLeftClobLengthOnReading()
		{
			return leftClobLengthOnReading;
		}

		/**
		 * 设置CLOB列值的取左功能的取左长度。
		 * <p>
		 * 开启后，CLOB列值转换器的{@linkplain ColumnConverter#from(Connection, ResultSet, int, int, Model, Property, int, Model)}方法，将有如下返回值：
		 * </p>
		 * <ul>
		 * <li>如果CLOB是{@code null}，将返回{@code null}；</li>
		 * <li>否则，仅返回包含{@code LEFT(clob, length)}的数据（字符串或者文件）；</li>
		 * </ul>
		 * 
		 * @param leftClobLengthOnReading
		 */
		public void setLeftClobLengthOnReading(int leftClobLengthOnReading)
		{
			this.leftClobLengthOnReading = leftClobLengthOnReading;
		}

		/**
		 * 禁用CLOB列值的取左功能。
		 * <p>
		 * 禁用后，CLOB列值转换器的{@linkplain ColumnConverter#from(Connection, ResultSet, int, int, Model, Property, int, Model)}方法将返回真实对象。
		 * </p>
		 */
		public void disableLeftClobOnReading()
		{
			this.leftClobLengthOnReading = -1;
		}

		/**
		 * 是否设置了文件转换为CLOB时的文件编码。
		 * 
		 * @return
		 */
		public boolean hasFileToClobEncoding()
		{
			return this.fileToClobEncoding != null && !this.fileToClobEncoding.isEmpty();
		}

		/**
		 * 获取文件转换为CLOB时的文件编码。
		 * <p>
		 * 如果未设置，将返回{@code null}。
		 * </p>
		 * 
		 * @return
		 */
		public String getFileToClobEncoding()
		{
			return fileToClobEncoding;
		}

		/**
		 * 设置文件转换为CLOB时的文件编码。
		 * 
		 * @param fileToClobEncoding
		 */
		public void setFileToClobEncoding(String fileToClobEncoding)
		{
			this.fileToClobEncoding = fileToClobEncoding;
		}

		/**
		 * 是否设置了CLOB至文件转换时的文件编码。
		 * 
		 * @return
		 */
		public boolean hasClobToFileEncoding()
		{
			return this.clobToFileEncoding != null && !this.clobToFileEncoding.isEmpty();
		}

		/**
		 * 获取CLOB至文件转换时的文件编码。
		 * <p>
		 * 如果未设置，将返回{@code null}。
		 * </p>
		 * 
		 * @return
		 */
		public String getClobToFileEncoding()
		{
			return clobToFileEncoding;
		}

		/**
		 * 设置CLOB至文件转换时的文件编码。
		 * 
		 * @param clobToFileEncoding
		 */
		public void setClobToFileEncoding(String clobToFileEncoding)
		{
			this.clobToFileEncoding = clobToFileEncoding;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [blobToFilePlaceholder=" + blobToFilePlaceholder
					+ ", blobToBytesPlaceholder=" + Arrays.toString(blobToBytesPlaceholder)
					+ ", leftClobLengthOnReading=" + leftClobLengthOnReading + ", fileToClobEncoding="
					+ fileToClobEncoding + ", clobToFileEncoding=" + clobToFileEncoding + "]";
		}
	}
}
