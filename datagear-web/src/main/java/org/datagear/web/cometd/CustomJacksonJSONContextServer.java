/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.cometd;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;
import java.util.List;

import org.cometd.bayeux.server.ServerMessage.Mutable;
import org.cometd.common.JSONContext;
import org.cometd.common.JSONContext.Generator;
import org.cometd.common.JSONContext.Parser;
import org.cometd.common.JacksonJSONContext;
import org.cometd.server.ServerMessageImpl;
import org.datagear.analysis.support.JsonSupport;
import org.datagear.util.IOUtil;
import org.datagear.web.json.jackson.ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 自定义的{@linkplain JSONContext.Server}。
 * <p>
 * 此实现类参考了{@linkplain JacksonJSONContext}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class CustomJacksonJSONContextServer implements JSONContext.Server
{
	public static final String DEFAULT_ENCODING = "UTF-8";

	private ObjectMapperBuilder objectMapperBuilder;

	private String encoding = DEFAULT_ENCODING;

	private ObjectMapper _objectMapper;

	public CustomJacksonJSONContextServer()
	{
		super();
	}

	public CustomJacksonJSONContextServer(ObjectMapperBuilder objectMapperBuilder)
	{
		super();
		setObjectMapperBuilder(objectMapperBuilder);
	}

	public ObjectMapperBuilder getObjectMapperBuilder()
	{
		return objectMapperBuilder;
	}

	public void setObjectMapperBuilder(ObjectMapperBuilder objectMapperBuilder)
	{
		this.objectMapperBuilder = objectMapperBuilder;
		this._objectMapper = this.objectMapperBuilder.build();
		JsonSupport.disableAutoCloseTargetFeature(this._objectMapper);
	}

	public String getEncoding()
	{
		return encoding;
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	@Override
	public Mutable[] parse(InputStream stream) throws ParseException
	{
		try
		{
			ServerMessageImpl[] array = this._objectMapper.readValue(IOUtil.getReader(stream, this.encoding),
					ServerMessageImpl[].class);
			return array;
		}
		catch (IOException e)
		{
			throw (ParseException) new ParseException("", -1).initCause(e);
		}
	}

	@Override
	public Mutable[] parse(Reader reader) throws ParseException
	{
		try
		{
			ServerMessageImpl[] array = this._objectMapper.readValue(reader, ServerMessageImpl[].class);
			return array;
		}
		catch (IOException e)
		{
			throw (ParseException) new ParseException("", -1).initCause(e);
		}
	}

	@Override
	public Mutable[] parse(String json) throws ParseException
	{
		try
		{
			ServerMessageImpl[] array = this._objectMapper.readValue(json, ServerMessageImpl[].class);
			return array;
		}
		catch (IOException e)
		{
			throw (ParseException) new ParseException("", -1).initCause(e);
		}
	}

	@Override
	public String generate(Mutable message)
	{
		return toJsonString(message);
	}

	@Override
	public String generate(List<Mutable> messages)
	{
		return toJsonString(messages);
	}

	@Override
	public Parser getParser()
	{
		return new JsonParser();
	}

	@Override
	public Generator getGenerator()
	{
		return new JsonGenerator();
	}

	protected String toJsonString(Object value)
	{
		try
		{
			return this._objectMapper.writeValueAsString(value);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	protected class JsonParser implements JSONContext.Parser
	{
		@Override
		public <T> T parse(Reader reader, Class<T> type) throws ParseException
		{
			try
			{
				return _objectMapper.readValue(reader, type);
			}
			catch (IOException x)
			{
				throw (ParseException) new ParseException("", -1).initCause(x);
			}
		}
	}

	protected class JsonGenerator implements JSONContext.Generator
	{
		@Override
		public String generate(Object object)
		{
			return toJsonString(object);
		}
	}
}
