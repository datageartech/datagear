/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.datagear.dataexchange.AbstractDevotedDbInfoAwareDataExchangeService;
import org.datagear.dataexchange.ColumnNotFoundException;
import org.datagear.dataexchange.DataExchangeContext;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.IndexFormatDataExchangeContext;
import org.datagear.dataexchange.RowColumnDataIndex;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;
import org.datagear.util.JdbcUtil;
import org.datagear.util.StringUtil;

/**
 * JSON导入服务。
 * 
 * @author datagear@163.com
 *
 */
public class JsonDataImportService extends AbstractDevotedDbInfoAwareDataExchangeService<JsonDataImport>
{
	public JsonDataImportService()
	{
		super();
	}

	public JsonDataImportService(DatabaseInfoResolver databaseInfoResolver)
	{
		super(databaseInfoResolver);
	}

	@Override
	protected DataExchangeContext createDataExchangeContext(JsonDataImport dataExchange)
	{
		return IndexFormatDataExchangeContext.valueOf(dataExchange);
	}

	@Override
	protected void exchange(JsonDataImport dataExchange, DataExchangeContext context) throws Throwable
	{
		JsonDataImportOption importOption = dataExchange.getImportOption();
		JsonDataFormat jsonDataFormat = importOption.getJsonDataFormat();

		if (JsonDataFormat.ROW_ARRAY.equals(jsonDataFormat))
			importForRowArrayData(dataExchange, context);
		else if (JsonDataFormat.TABLE_OBJECT.equals(jsonDataFormat))
			importForTableObjectData(dataExchange, context);
		else
			throw new UnsupportedOperationException();
	}

	/**
	 * 导入{@linkplain JsonDataFormat#TABLE_OBJECT}格式的数据。
	 * 
	 * @param dataExchange
	 * @param context
	 * @throws Throwable
	 */
	protected void importForTableObjectData(JsonDataImport dataExchange, DataExchangeContext context) throws Throwable
	{
		IndexFormatDataExchangeContext importContext = IndexFormatDataExchangeContext.cast(context);

		Reader jsonReader = getResource(dataExchange.getReaderFactory(), importContext);

		Connection cn = context.getConnection();
		JdbcUtil.setAutoCommitIfSupports(cn, false);

		JsonParser p = Json.createParser(jsonReader);

		if (p.hasNext())
		{
			Event event = p.next();

			if (!Event.START_OBJECT.equals(event))
				throw new IllegalJsonDataFormatException(p.getLocation(), true, Event.START_OBJECT);

			String table = null;

			while (p.hasNext())
			{
				event = p.next();

				if (Event.END_OBJECT.equals(event))
					break;

				if (Event.KEY_NAME.equals(event))
				{
					table = p.getString();
				}
				else if (Event.START_ARRAY.equals(event))
				{
					if (table == null)
						throw new IllegalJsonDataFormatException(p.getLocation(), true, Event.KEY_NAME);

					importJsonArray(dataExchange, importContext, cn, p, table);
					table = null;
				}
				else
					throw new IllegalJsonDataFormatException(p.getLocation(), false, event);
			}
		}

		commit(cn);
	}

	/**
	 * 导入{@linkplain JsonDataFormat#ROW_ARRAY}格式的数据。
	 * 
	 * @param dataExchange
	 * @param context
	 * @throws Throwable
	 */
	protected void importForRowArrayData(JsonDataImport dataExchange, DataExchangeContext context) throws Throwable
	{
		String table = dataExchange.getTable();

		if (StringUtil.isEmpty(table))
			throw new DataExchangeException("JsonDataImport.table must be set");

		IndexFormatDataExchangeContext importContext = IndexFormatDataExchangeContext.cast(context);

		Reader jsonReader = getResource(dataExchange.getReaderFactory(), importContext);

		Connection cn = context.getConnection();
		JdbcUtil.setAutoCommitIfSupports(cn, false);

		JsonParser p = Json.createParser(jsonReader);

		if (p.hasNext())
		{
			Event event = p.next();

			if (!Event.START_ARRAY.equals(event))
				throw new IllegalJsonDataFormatException(p.getLocation(), true, Event.START_ARRAY);

			importJsonArray(dataExchange, importContext, cn, p, table);
		}

		commit(cn);
	}

	/**
	 * 解析并导入{@code [}标记之后的一个数组。
	 * 
	 * @param dataExchange
	 * @param context
	 * @param cn
	 * @param p
	 * @param table
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	protected void importJsonArray(JsonDataImport dataExchange, IndexFormatDataExchangeContext context, Connection cn,
			JsonParser p, String table) throws Throwable
	{
		JsonDataImportOption importOption = dataExchange.getImportOption();

		List<ColumnInfo> totalColumnInfos = getColumnInfos(cn, table);

		PreparedStatement prevSt = null;
		List<ColumnInfo> prevColumnInfos = null;

		while (p.hasNext())
		{
			Event event = p.next();

			if (Event.END_ARRAY.equals(event))
				break;

			if (!Event.START_OBJECT.equals(event))
				throw new IllegalJsonDataFormatException(p.getLocation(), true, Event.START_OBJECT);

			JsonLocation jsonLocation = p.getLocation();
			context.setDataIndex(
					RowColumnDataIndex.valueOf(jsonLocation.getLineNumber(), jsonLocation.getColumnNumber()));

			Map<String, Object> row = parseNextObject(p);

			Object[] myColumnInfoValues = getColumnInfoValues(dataExchange, table, totalColumnInfos, row);
			List<ColumnInfo> myColumnInfos = (List<ColumnInfo>) myColumnInfoValues[0];
			List<Object> myColumnValues = (List<Object>) myColumnInfoValues[1];

			if (!myColumnInfos.isEmpty())
			{
				boolean newSql = false;

				if (prevSt == null || prevColumnInfos == null)
					newSql = true;
				else if (myColumnInfos.equals(prevColumnInfos))
					newSql = false;
				else
					newSql = true;

				if (newSql)
				{
					JdbcUtil.closeStatement(prevSt);

					String sql = buildInsertPreparedSql(cn, table, myColumnInfos);

					prevSt = cn.prepareStatement(sql);
					prevColumnInfos = myColumnInfos;
				}

				importValueData(cn, prevSt, prevColumnInfos, myColumnValues, context.getDataIndex(),
						importOption.isNullForIllegalColumnValue(), importOption.getExceptionResolve(),
						context.getDataFormatContext(), dataExchange.getListener());
			}
		}

		JdbcUtil.closeStatement(prevSt);
	}

	/**
	 * 获取列信息及列值列表数组。
	 * 
	 * @param dataExchange
	 * @param table
	 * @param columnInfos
	 * @param row
	 * @return
	 * @throws ColumnNotFoundException
	 */
	protected Object[] getColumnInfoValues(JsonDataImport dataExchange, String table, List<ColumnInfo> columnInfos,
			Map<String, Object> row) throws ColumnNotFoundException
	{
		List<ColumnInfo> myColumnInfos = new ArrayList<ColumnInfo>();
		List<Object> myColumnValues = new ArrayList<Object>();

		for (ColumnInfo columnInfo : columnInfos)
		{
			if (row.containsKey(columnInfo.getName()))
			{
				myColumnInfos.add(columnInfo);
				myColumnValues.add(row.get(columnInfo.getName()));
			}
		}

		// 有不存在的列且不被允许
		if (row.size() > myColumnInfos.size() && !dataExchange.getImportOption().isIgnoreInexistentColumn())
		{
			Set<String> myNames = row.keySet();

			for (String myName : myNames)
			{
				if (findColumnInfo(columnInfos, myName) == null)
					throw new ColumnNotFoundException(table, myName);
			}
		}

		return new Object[] { myColumnInfos, myColumnValues };
	}

	/**
	 * 解析<code>{</code>标记之后的一个对象。
	 * 
	 * @param p
	 * @return
	 * @throws Throwable
	 */
	protected Map<String, Object> parseNextObject(JsonParser p) throws Throwable
	{
		Map<String, Object> map = new HashMap<String, Object>();

		String name = null;

		while (p.hasNext())
		{
			Event e = p.next();

			if (Event.KEY_NAME.equals(e))
			{
				name = p.getString();
			}
			else if (Event.VALUE_FALSE.equals(e))
			{
				if (name == null)
					throw new IllegalJsonDataFormatException(p.getLocation(), true, Event.KEY_NAME);

				map.put(name, false);
				name = null;
			}
			else if (Event.VALUE_NULL.equals(e))
			{
				if (name == null)
					throw new IllegalJsonDataFormatException(p.getLocation(), true, Event.KEY_NAME);

				map.put(name, null);
				name = null;
			}
			else if (Event.VALUE_NUMBER.equals(e))
			{
				if (name == null)
					throw new IllegalJsonDataFormatException(p.getLocation(), true, Event.KEY_NAME);

				map.put(name, p.getBigDecimal());
				name = null;
			}
			else if (Event.VALUE_STRING.equals(e))
			{
				if (name == null)
					throw new IllegalJsonDataFormatException(p.getLocation(), true, Event.KEY_NAME);

				map.put(name, p.getString());
				name = null;
			}
			else if (Event.VALUE_TRUE.equals(e))
			{
				if (name == null)
					throw new IllegalJsonDataFormatException(p.getLocation(), true, Event.KEY_NAME);

				map.put(name, true);
				name = null;
			}
			else if (Event.START_OBJECT.equals(e))
			{
				if (name == null)
					throw new IllegalJsonDataFormatException(p.getLocation(), true, Event.KEY_NAME);

				Object subMap = parseNextObject(p);
				map.put(name, subMap);
				name = null;
			}
			else if (Event.END_OBJECT.equals(e))
			{
				break;
			}
			else if (Event.START_ARRAY.equals(e))
			{
				if (name == null)
					throw new IllegalJsonDataFormatException(p.getLocation(), true, Event.KEY_NAME);

				Object[] subArray = parseNextArray(p);
				map.put(name, subArray);
				name = null;
			}
			else if (Event.END_ARRAY.equals(e))
				throw new IllegalJsonDataFormatException(p.getLocation(), false, Event.END_ARRAY);
		}

		return map;
	}

	/**
	 * 解析{@code [}标记之后的一个数组。
	 * 
	 * @param p
	 * @return
	 * @throws Throwable
	 */
	protected Object[] parseNextArray(JsonParser p) throws Throwable
	{
		List<Object> list = new ArrayList<Object>();

		while (p.hasNext())
		{
			Event e = p.next();

			if (Event.KEY_NAME.equals(e))
			{
				throw new IllegalJsonDataFormatException(p.getLocation(), false, Event.KEY_NAME);
			}
			else if (Event.VALUE_FALSE.equals(e))
			{
				list.add(false);
			}
			else if (Event.VALUE_NULL.equals(e))
			{
				list.add(null);
			}
			else if (Event.VALUE_NUMBER.equals(e))
			{
				list.add(p.getBigDecimal());
			}
			else if (Event.VALUE_STRING.equals(e))
			{
				list.add(p.getString());
			}
			else if (Event.VALUE_TRUE.equals(e))
			{
				list.add(true);
			}
			else if (Event.START_OBJECT.equals(e))
			{
				Object subMap = parseNextObject(p);
				list.add(subMap);
			}
			else if (Event.END_OBJECT.equals(e))
			{
				throw new IllegalJsonDataFormatException(p.getLocation(), false, Event.END_OBJECT);
			}
			else if (Event.START_ARRAY.equals(e))
			{
				Object[] subArray = parseNextArray(p);
				list.add(subArray);
			}
			else if (Event.END_ARRAY.equals(e))
			{
				break;
			}
		}

		return list.toArray();
	}

	@Override
	protected void onException(JsonDataImport dataExchange, DataExchangeContext context, DataExchangeException e)
			throws DataExchangeException
	{
		processTransactionForDataExchangeException(context, e, dataExchange.getImportOption().getExceptionResolve());

		super.onException(dataExchange, context, e);
	}
}
