package org.datagear.web.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;

import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

/**
 * 字符串/数组至{@linkplain JsonStructure}转换器。
 * 
 * @author datagear@163.com
 *
 */
public class StringToJsonConverter implements GenericConverter
{
	private Set<ConvertiblePair> convertiblePairs = new HashSet<>();

	public StringToJsonConverter()
	{
		super();
		this.convertiblePairs.add(new ConvertiblePair(String.class, JsonStructure.class));
		this.convertiblePairs.add(new ConvertiblePair(String.class, JsonObject.class));
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
		return convertToJsonStructure(source);
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

	public static JsonStructure readJsonStructure(String json)
	{
		try (JsonReader jsonReader = Json.createReader(IOUtil.getReader(json)))
		{
			return jsonReader.read();
		}
	}

	/**
	 * 将{@linkplain JsonObject}转换为{@linkplain Map}。
	 * 
	 * @param json
	 * @return
	 * @see {@linkplain #toStringValueObject(JsonValue)}
	 */
	public static Map<String, Object> toStringValueMap(JsonObject json)
	{
		Map<String, Object> map = new HashMap<>();

		if (json == null)
			return map;

		for (Map.Entry<String, JsonValue> entry : json.entrySet())
		{
			Object toValue = toStringValueObject(entry.getValue());
			map.put(entry.getKey(), toValue);
		}

		return map;
	}

	/**
	 * 将{@linkplain JsonArray}转换为{@linkplain List}。
	 * 
	 * @param json
	 * @return
	 * @see {@linkplain #toStringValueObject(JsonValue)}
	 */
	public static List<Object> toStringValueList(JsonArray json)
	{
		List<Object> list = new ArrayList<>((json == null ? 0 : json.size()));

		if (json == null)
			return list;

		for (JsonValue value : json)
		{
			Object toValue = toStringValueObject(value);
			list.add(toValue);
		}

		return list;
	}

	/**
	 * 将{@linkplain JsonValue}转换为{@linkplain Object}：
	 * <p>
	 * {@linkplain JsonObject} --&gt; {@linkplain HashMap}
	 * </p>
	 * <p>
	 * {@linkplain JsonArray} --&gt; {@linkplain List}
	 * </p>
	 * <p>
	 * {@linkplain JsonString} --&gt; {@linkplain String}
	 * </p>
	 * <p>
	 * {@linkplain JsonNumber} --&gt; {@linkplain String}
	 * </p>
	 * 
	 * @param json
	 * @return
	 */
	public static Object toStringValueObject(JsonValue json)
	{
		if (json == null)
			return null;

		Object toValue = null;

		if (json instanceof JsonString)
			toValue = ((JsonString) json).getString();
		else if (json instanceof JsonNumber)
			toValue = ((JsonNumber) json).toString();
		else if (json instanceof JsonObject)
			toValue = toStringValueMap((JsonObject) json);
		else if (json instanceof JsonArray)
			toValue = toStringValueList((JsonArray) json);
		else
			throw new IllegalArgumentException("Unknown json value of type [" + json.getClass() + "]");

		return toValue;
	}
}