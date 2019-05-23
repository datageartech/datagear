/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

/**
 * 数据格式上下文。
 * 
 * @author datagear@163.com
 *
 */
public class DataFormatContext
{
	private DataFormat dataFormat;

	private DateFormat dateFormatter;

	private DateFormat timeFormatter;

	private DateFormat timestampFormatter;

	private NumberFormat numberFormatter;

	public DataFormatContext()
	{
		super();
	}

	public DataFormatContext(DataFormat dataFormat)
	{
		super();
		setDataFormat(dataFormat);
	}

	public DataFormat getDataFormat()
	{
		return dataFormat;
	}

	public void setDataFormat(DataFormat dataFormat)
	{
		this.dataFormat = dataFormat;

		this.dateFormatter = new SimpleDateFormat(dataFormat.getDateFormat(), dataFormat.getLocale());
		this.timeFormatter = new SimpleDateFormat(dataFormat.getTimeFormat(), dataFormat.getLocale());
		this.timestampFormatter = new SimpleDateFormat(dataFormat.getTimestampFormat(), dataFormat.getLocale());
		this.numberFormatter = new DecimalFormat(dataFormat.getNumberFormat(),
				DecimalFormatSymbols.getInstance(dataFormat.getLocale()));
	}

	public DateFormat getDateFormatter()
	{
		return dateFormatter;
	}

	public void setDateFormatter(DateFormat dateFormatter)
	{
		this.dateFormatter = dateFormatter;
	}

	public DateFormat getTimeFormatter()
	{
		return timeFormatter;
	}

	public void setTimeFormatter(DateFormat timeFormatter)
	{
		this.timeFormatter = timeFormatter;
	}

	public DateFormat getTimestampFormatter()
	{
		return timestampFormatter;
	}

	public void setTimestampFormatter(DateFormat timestampFormatter)
	{
		this.timestampFormatter = timestampFormatter;
	}

	public NumberFormat getNumberFormatter()
	{
		return numberFormatter;
	}

	public void setNumberFormatter(NumberFormat numberFormatter)
	{
		this.numberFormatter = numberFormatter;
	}
}
