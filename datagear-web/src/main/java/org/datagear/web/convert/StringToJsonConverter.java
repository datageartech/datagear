package org.datagear.web.convert;

import java.util.HashSet;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonStructure;

import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import com.alibaba.fastjson.JSONObject;

/**
 * 字符串/数组至{@linkplain JsonStructure}转换器。
 * 
 * @author datagear@163.com
 *
 */
public class StringToJsonConverter implements GenericConverter
{
	private Set<ConvertiblePair> convertiblePairs = new HashSet<ConvertiblePair>();

	public StringToJsonConverter()
	{
		super();
		this.convertiblePairs.add(new ConvertiblePair(String.class, JsonStructure.class));
		this.convertiblePairs.add(new ConvertiblePair(String.class, JSONObject.class));
		this.convertiblePairs.add(new ConvertiblePair(String.class, JsonArray.class));
		this.convertiblePairs.add(new ConvertiblePair(String[].class, JsonStructure.class));
		this.convertiblePairs.add(new ConvertiblePair(String[].class, JsonArray.class));
	}

	@Override
	public Set<ConvertiblePair> getConvertibleTypes()
	{
		return convertiblePairs;
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType)
	{
		return convertToJsonStructure(sourceType);
	}

	/**
	 * 将字符串/字符串数组转换为{@linkplain JsonStructure}。
	 * 
	 * @param source
	 * @return
	 * @throws ConversionException
	 */
	public JsonStructure convertToJsonStructure(Object source) throws ConversionException
	{
		if (StringUtil.isEmpty(source))
			return null;

		Class<?> sourceClass = source.getClass();
		
		if (String.class.equals(sourceClass))
			return readJsonStructure((String) source);
		else if (String[].class.equals(sourceClass))
		{
			String[] strs = (String[]) source;
			StringBuilder json = new StringBuilder();

			json.append('[');
			for (int i = 0; i < strs.length; i++)
			{
				if (i > 0)
					json.append(',');

				json.append(strs[i]);
			}
			json.append(']');

			return readJsonStructure(json.toString());
		}
		else
			throw new ConversionFailedException(TypeDescriptor.valueOf(sourceClass),
					TypeDescriptor.valueOf(JsonStructure.class), source, new UnsupportedOperationException());
	}

	protected JsonStructure readJsonStructure(String json)
	{
		JsonReader jsonReader = Json.createReader(IOUtil.getReader(json));
		return jsonReader.read();
	}
}