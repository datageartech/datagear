/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import org.datagear.dataexchange.AbstractDevotedDBMetaDataExchangeService;
import org.datagear.dataexchange.DataExchangeContext;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.DataFormatContext;
import org.datagear.dataexchange.IndexFormatDataExchangeContext;
import org.datagear.dataexchange.RowDataIndex;
import org.datagear.dataexchange.TextDataExportListener;
import org.datagear.meta.Column;
import org.datagear.meta.resolver.DBMetaResolver;
import org.datagear.util.JdbcUtil;

/**
 * JSON导出服务。
 * 
 * @author datagear@163.com
 *
 */
public class JsonDataExportService extends AbstractDevotedDBMetaDataExchangeService<JsonDataExport>
{
	protected static final JsonGeneratorFactory FACTORY = Json.createGeneratorFactory(new HashMap<String, Object>());

	protected static JsonGeneratorFactory FACTORY_PRETTY_PRINT = null;
	static
	{
		Map<String, Object> config = new HashMap<>();
		config.put(JsonGenerator.PRETTY_PRINTING, true);

		FACTORY_PRETTY_PRINT = Json.createGeneratorFactory(config);
	}

	public JsonDataExportService()
	{
		super();
	}

	public JsonDataExportService(DBMetaResolver dbMetaResolver)
	{
		super(dbMetaResolver);
	}

	@Override
	protected DataExchangeContext createDataExchangeContext(JsonDataExport dataExchange)
	{
		return IndexFormatDataExchangeContext.valueOf(dataExchange);
	}

	@Override
	protected void exchange(JsonDataExport dataExchange, DataExchangeContext context) throws Throwable
	{
		IndexFormatDataExchangeContext exportContext = IndexFormatDataExchangeContext.cast(context);

		Writer jsonWriter = getResource(dataExchange.getWriterFactory(), exportContext);

		Connection cn = context.getConnection();
		JdbcUtil.setReadonlyIfSupports(cn, true);

		ResultSet rs = dataExchange.getQuery().execute(cn);
		List<Column> columns = getColumns(cn, rs);

		writeRecords(dataExchange, cn, columns, rs, jsonWriter, exportContext);
	}

	/**
	 * 写记录。
	 * 
	 * @param dataExchange
	 * @param cn
	 * @param columns
	 * @param rs
	 * @param out
	 * @param exportContext
	 */
	@SuppressWarnings("resource")
	protected void writeRecords(JsonDataExport dataExchange, Connection cn, List<Column> columns, ResultSet rs,
			Writer out, IndexFormatDataExchangeContext exportContext) throws Throwable
	{
		TextDataExportListener listener = dataExchange.getListener();
		JsonDataExportOption exportOption = dataExchange.getExportOption();
		JsonDataFormat jsonDataFormat = exportOption.getJsonDataFormat();

		int columnCount = columns.size();

		JsonGenerator generator = (exportOption.isPrettyPrint() ? FACTORY_PRETTY_PRINT.createGenerator(out)
				: FACTORY.createGenerator(out));

		if (JsonDataFormat.TABLE_OBJECT.equals(jsonDataFormat))
		{
			if (!dataExchange.hasTableName())
				throw new DataExchangeException("[JsonDataExport.tableName] must be set");

			generator.writeStartObject();
			generator.writeStartArray(dataExchange.getTableName());
		}
		else
		{
			generator.writeStartArray();
		}

		long row = 0;

		while (rs.next())
		{
			exportContext.setDataIndex(RowDataIndex.valueOf(row));

			generator.writeStartObject();

			for (int i = 0; i < columnCount; i++)
			{
				Column column = columns.get(i);

				writeJsonValue(dataExchange, cn, columns, rs, exportContext, generator, i + 1, column, exportOption,
						jsonDataFormat, listener);
			}

			generator.writeEnd();

			if (listener != null)
				listener.onSuccess(exportContext.getDataIndex());

			row++;
		}

		if (JsonDataFormat.TABLE_OBJECT.equals(jsonDataFormat))
		{
			generator.writeEnd();
			generator.writeEnd();
		}
		else
		{
			generator.writeEnd();
		}

		generator.flush();
	}

	/**
	 * 写JSON值。
	 * 
	 * @param dataExchange
	 * @param cn
	 * @param columns
	 * @param rs
	 * @param exportContext
	 * @param generator
	 * @param columnIndex
	 *            行号，以{@code 1}开始
	 * @param column
	 * @param exportOption
	 * @param jsonDataFormat
	 * @param listener
	 * @throws Throwable
	 */
	protected void writeJsonValue(JsonDataExport dataExchange, Connection cn, List<Column> columns, ResultSet rs,
			IndexFormatDataExchangeContext exportContext, JsonGenerator generator, int columnIndex, Column column,
			JsonDataExportOption exportOption, JsonDataFormat jsonDataFormat, TextDataExportListener listener)
			throws Throwable
	{
		String name = column.getName();
		Object value = null;

		try
		{
			value = getValue(cn, rs, columnIndex, column.getType());

			if (value == null)
			{
				generator.writeNull(name);
			}
			else if (value instanceof Number)
			{
				Number number = (Number) value;

				if (number instanceof Integer)
					generator.write(name, number.intValue());
				else if (number instanceof Long)
					generator.write(name, number.longValue());
				else if (number instanceof Float || number instanceof Double)
					generator.write(name, number.doubleValue());
				else if (number instanceof BigDecimal)
					generator.write(name, (BigDecimal) number);
				else if (number instanceof BigInteger)
					generator.write(name, (BigInteger) number);
				else
					generator.write(name, number.intValue());
			}
			else if (value instanceof Date)
			{
				DataFormatContext dataFormatContext = exportContext.getDataFormatContext();
				String valueStr = null;

				if (value instanceof java.sql.Date)
					valueStr = dataFormatContext.formatDate((java.sql.Date) value);
				else if (value instanceof java.sql.Time)
					valueStr = dataFormatContext.formatTime((Time) value);
				else if (value instanceof java.sql.Timestamp)
					valueStr = dataFormatContext.formatTimestamp((Timestamp) value);
				else
					valueStr = dataFormatContext.formatDate((java.sql.Date) value);

				if (valueStr == null)
					generator.writeNull(name);
				else
					generator.write(name, valueStr);
			}
			else if (value instanceof String)
			{
				generator.write(name, (String) value);
			}
			else if (value instanceof Boolean)
			{
				generator.write(name, (Boolean) value);
			}
			else if (value instanceof byte[])
			{
				String valueStr = exportContext.getDataFormatContext().formatBytes((byte[]) value);

				if (valueStr == null)
					generator.writeNull(name);
				else
					generator.write(name, valueStr);
			}
			else
			{
				generator.write(name, value.toString());
			}

		}
		catch (Throwable t)
		{
			if (exportOption.isNullForIllegalColumnValue())
			{
				generator.writeNull(name);

				if (listener != null)
					listener.onSetNullTextValue(exportContext.getDataIndex(), column.getName(),
							wrapToDataExchangeException(t));
			}
			else
				throw t;
		}
	}
}
