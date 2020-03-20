/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.sqlpad;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.cometd.bayeux.server.ServerMessage.Mutable;
import org.cometd.common.JSONContext;
import org.cometd.common.JSONContext.Generator;
import org.cometd.common.JSONContext.Parser;
import org.cometd.common.JacksonJSONContext;
import org.cometd.server.ServerMessageImpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 基于Fastjson的{@linkplain JSONContext.Server}。
 * <p>
 * 此实现类参考了{@linkplain JacksonJSONContext}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class FastjsonJSONContextServer implements JSONContext.Server
{
	public static final String DEFAULT_ENCODING = "UTF-8";

	private SerializeConfig serializeConfig;

	private SerializerFeature[] serializerFeatures = new SerializerFeature[0];

	private String encoding = DEFAULT_ENCODING;

	public FastjsonJSONContextServer()
	{
		super();
	}

	public FastjsonJSONContextServer(SerializeConfig serializeConfig, SerializerFeature[] serializerFeatures)
	{
		super();
		this.serializeConfig = serializeConfig;
		this.serializerFeatures = inflateSerializerFeatureForCometd(serializerFeatures);
	}

	public SerializeConfig getSerializeConfig()
	{
		return serializeConfig;
	}

	public void setSerializeConfig(SerializeConfig serializeConfig)
	{
		this.serializeConfig = serializeConfig;
	}

	public SerializerFeature[] getSerializerFeatures()
	{
		return serializerFeatures;
	}

	public void setSerializerFeatures(SerializerFeature[] serializerFeatures)
	{
		this.serializerFeatures = inflateSerializerFeatureForCometd(serializerFeatures);
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
			String json = readToString(stream, this.encoding);

			List<ServerMessageImpl> list = JSON.parseArray(json, ServerMessageImpl.class);

			Mutable[] mutables = list.toArray(new Mutable[list.size()]);

			return mutables;
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
			String json = readToString(reader);

			List<ServerMessageImpl> list = JSON.parseArray(json, ServerMessageImpl.class);

			Mutable[] mutables = list.toArray(new Mutable[list.size()]);

			return mutables;
		}
		catch (IOException e)
		{
			throw (ParseException) new ParseException("", -1).initCause(e);
		}
	}

	@Override
	public Mutable[] parse(String json) throws ParseException
	{
		List<ServerMessageImpl> list = JSON.parseArray(json, ServerMessageImpl.class);

		Mutable[] mutables = list.toArray(new Mutable[list.size()]);

		return mutables;
	}

	@Override
	public String generate(Mutable message)
	{
		return JSON.toJSONString(message, this.serializeConfig, (SerializeFilter) null, this.serializerFeatures);
	}

	@Override
	public String generate(Mutable[] messages)
	{
		return JSON.toJSONString(messages, this.serializeConfig, (SerializeFilter) null, this.serializerFeatures);
	}

	@Override
	public Parser getParser()
	{
		return new FastjsonParser();
	}

	@Override
	public Generator getGenerator()
	{
		return new FastjsonGenerator();
	}

	@SuppressWarnings("unchecked")
	protected <T> T parseObject(Reader in, Type type, Feature... features) throws IOException
	{
		String json = readToString(in);

		Object obj = JSON.parseObject(json, type);

		return (T) obj;
	}

	protected String readToString(InputStream in, String encoding) throws IOException
	{
		Reader reader = new InputStreamReader(in, encoding);

		return readToString(reader);
	}

	protected String readToString(Reader in) throws IOException
	{
		StringBuilder sb = new StringBuilder();

		char[] buf = new char[512];
		int readCount = -1;

		while ((readCount = in.read(buf)) != -1)
		{
			for (int i = 0; i < readCount; i++)
				sb.append(buf[i]);
		}

		return sb.toString();
	}

	protected SerializerFeature[] inflateSerializerFeatureForCometd(SerializerFeature[] serializerFeatures)
	{
		List<SerializerFeature> list = new ArrayList<>();

		if (serializerFeatures != null)
		{
			for (SerializerFeature serializerFeature : serializerFeatures)
				list.add(serializerFeature);
		}

		// 开启循环引用的话，查询结果消息中可能会带有“$...”循环引用路径标识，但是在cometd的JSON处理后，它并不是正确的引用路径标识，这会
		// 导致浏览器端解析出问题，从而导致cometd通讯失败。
		list.add(SerializerFeature.DisableCircularReferenceDetect);

		return list.toArray(new SerializerFeature[list.size()]);
	}

	protected class FastjsonParser implements JSONContext.Parser
	{
		@Override
		public <T> T parse(Reader reader, Class<T> type) throws ParseException
		{
			try
			{
				return parseObject(reader, type);
			}
			catch (IOException x)
			{
				throw (ParseException) new ParseException("", -1).initCause(x);
			}
		}
	}

	protected class FastjsonGenerator implements JSONContext.Generator
	{
		@Override
		public String generate(Object object)
		{
			return JSON.toJSONString(object, FastjsonJSONContextServer.this.serializeConfig, (SerializeFilter) null,
					FastjsonJSONContextServer.this.serializerFeatures);
		}
	}
}
