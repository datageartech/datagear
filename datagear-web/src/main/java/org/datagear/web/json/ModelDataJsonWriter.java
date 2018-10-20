/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.json;

import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.web.json.jackson.DynamicBeanJsonSerializer;

/**
 * {@linkplain Model}数据的JSON输出流。
 * <p>
 * 此类并不是线程安全的。
 * </p>
 * 
 * @author datagear@163.com
 * @deprecated 已通过{@linkplain DynamicBeanJsonSerializer}集成jackson，不再需要此类。
 */
@Deprecated
public class ModelDataJsonWriter
{
	private static final JsonGeneratorFactory JSON_FACTORY = Json.createGeneratorFactory(new HashMap<String, Object>());

	public static final String KEY_BEAN_CLASS = "class";

	public static final String KEY_REFERENCE = "$ref";

	public static final String REFERENCE_ROOT = "$";

	private boolean indent = false;

	private boolean quote = true;

	private boolean writeNull = true;

	private boolean writeBeanClass = true;

	private String beanClassKey = KEY_BEAN_CLASS;

	private boolean handleReference = true;

	private String referenceKey = KEY_REFERENCE;

	private String referenceRoot = REFERENCE_ROOT;

	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private DateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private DateFormat sqlTimestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private DateFormat sqlTimeFormat = new SimpleDateFormat("HH:mm:ss");

	private boolean ignoreReadException = true;

	private boolean closeAfterWrite = false;

	public ModelDataJsonWriter()
	{
	}

	public boolean isIndent()
	{
		return indent;
	}

	public void setIndent(boolean indent)
	{
		this.indent = indent;
	}

	public boolean isQuote()
	{
		return quote;
	}

	public void setQuote(boolean quote)
	{
		this.quote = quote;
	}

	public boolean isWriteNull()
	{
		return writeNull;
	}

	public void setWriteNull(boolean writeNull)
	{
		this.writeNull = writeNull;
	}

	public boolean isWriteBeanClass()
	{
		return writeBeanClass;
	}

	public void setWriteBeanClass(boolean writeBeanClass)
	{
		this.writeBeanClass = writeBeanClass;
	}

	public String getBeanClassKey()
	{
		return beanClassKey;
	}

	protected void setBeanClassKey(String beanClassKey)
	{
		this.beanClassKey = beanClassKey;
	}

	public boolean isHandleReference()
	{
		return handleReference;
	}

	public void setHandleReference(boolean handleReference)
	{
		this.handleReference = handleReference;
	}

	public String getReferenceKey()
	{
		return referenceKey;
	}

	protected void setReferenceKey(String referenceKey)
	{
		this.referenceKey = referenceKey;
	}

	public String getReferenceRoot()
	{
		return referenceRoot;
	}

	protected void setReferenceRoot(String referenceRoot)
	{
		this.referenceRoot = referenceRoot;
	}

	public DateFormat getDateFormat()
	{
		return dateFormat;
	}

	public void setDateFormat(DateFormat dateFormat)
	{
		this.dateFormat = dateFormat;
	}

	public DateFormat getSqlDateFormat()
	{
		return sqlDateFormat;
	}

	public void setSqlDateFormat(DateFormat sqlDateFormat)
	{
		this.sqlDateFormat = sqlDateFormat;
	}

	public DateFormat getSqlTimestampFormat()
	{
		return sqlTimestampFormat;
	}

	public void setSqlTimestampFormat(DateFormat sqlTimestampFormat)
	{
		this.sqlTimestampFormat = sqlTimestampFormat;
	}

	public DateFormat getSqlTimeFormat()
	{
		return sqlTimeFormat;
	}

	public void setSqlTimeFormat(DateFormat sqlTimeFormat)
	{
		this.sqlTimeFormat = sqlTimeFormat;
	}

	public boolean isIgnoreReadException()
	{
		return ignoreReadException;
	}

	public void setIgnoreReadException(boolean ignoreReadException)
	{
		this.ignoreReadException = ignoreReadException;
	}

	public boolean isCloseAfterWrite()
	{
		return closeAfterWrite;
	}

	public void setCloseAfterWrite(boolean closeAfterWrite)
	{
		this.closeAfterWrite = closeAfterWrite;
	}

	/**
	 * 写指定{@linkplain Model}对象的JSON至输出流。
	 * 
	 * @param out
	 * @param model
	 * @param obj
	 */
	public void write(Writer out, Model model, Object obj)
	{
		JsonGenerator jsonGenerator = JSON_FACTORY.createGenerator(out);

		IdentityHashMap<Object, String> referenceMap = (this.handleReference ? new IdentityHashMap<Object, String>()
				: null);

		try
		{
			write(jsonGenerator, model, obj, null, this.referenceRoot, referenceMap);
		}
		finally
		{
			jsonGenerator.flush();

			if (this.closeAfterWrite)
				jsonGenerator.close();
		}
	}

	@SuppressWarnings("unchecked")
	protected void write(JsonGenerator jsonGenerator, Model model, Object obj, String name, String path,
			IdentityHashMap<Object, String> referenceMap)
	{
		if (obj == null)
		{
			if (this.writeNull)
				writeNull(jsonGenerator, name, path, referenceMap);
		}
		else
		{
			Class<?> objType = obj.getClass();

			if (objType.isArray())
			{
				writeArray(jsonGenerator, model, (Object[]) obj, name, path, referenceMap);
			}
			else if (Collection.class.isAssignableFrom(objType))
			{
				writeCollection(jsonGenerator, model, (Collection<?>) obj, name, path, referenceMap);
			}
			else if (Map.class.isAssignableFrom(objType))
			{
				writeMap(jsonGenerator, model, (Map<String, ?>) obj, name, path, referenceMap);
			}
			else
			{
				Class<?> modelType = model.getType();

				// JavaBean
				if (model.hasProperty())
				{
					// 写引用，而非对象本身
					String refPath = (referenceMap == null ? null : referenceMap.get(obj));

					if (refPath != null)
					{
						writeReference(jsonGenerator, refPath, name);
					}
					else
					{
						if (referenceMap != null)
							referenceMap.put(obj, path);

						writeBean(jsonGenerator, model, obj, name, path, referenceMap);
					}
				}
				else if (String.class.equals(modelType))
				{
					if (name == null)
						jsonGenerator.write((String) obj);
					else
						jsonGenerator.write(name, (String) obj);
				}
				else if (boolean.class.equals(modelType) || Boolean.class.equals(modelType))
				{
					if (name == null)
						jsonGenerator.write((Boolean) obj);
					else
						jsonGenerator.write(name, (Boolean) obj);
				}
				else if (int.class.equals(modelType) || Integer.class.equals(modelType))
				{
					if (name == null)
						jsonGenerator.write((Integer) obj);
					else
						jsonGenerator.write(name, (Integer) obj);
				}
				else if (long.class.equals(modelType) || Long.class.equals(modelType))
				{
					if (name == null)
						jsonGenerator.write((Long) obj);
					else
						jsonGenerator.write(name, (Long) obj);
				}
				else if (float.class.equals(modelType) || Float.class.equals(modelType))
				{
					if (name == null)
						jsonGenerator.write((Float) obj);
					else
						jsonGenerator.write(name, (Float) obj);
				}
				else if (double.class.equals(modelType) || Double.class.equals(modelType))
				{
					if (name == null)
						jsonGenerator.write((Double) obj);
					else
						jsonGenerator.write(name, (Double) obj);
				}
				else if (BigInteger.class.equals(modelType))
				{
					if (name == null)
						jsonGenerator.write((BigInteger) obj);
					else
						jsonGenerator.write(name, (BigInteger) obj);
				}
				else if (BigDecimal.class.equals(modelType))
				{
					if (name == null)
						jsonGenerator.write((BigDecimal) obj);
					else
						jsonGenerator.write(name, (BigDecimal) obj);
				}
				else if (java.sql.Date.class.equals(modelType))
				{
					String str = this.sqlDateFormat.format(((java.sql.Date) obj));

					if (name == null)
						jsonGenerator.write(str);
					else
						jsonGenerator.write(name, str);
				}
				else if (java.sql.Timestamp.class.equals(modelType))
				{
					String str = this.sqlTimestampFormat.format(((java.sql.Timestamp) obj));

					if (name == null)
						jsonGenerator.write(str);
					else
						jsonGenerator.write(name, str);
				}
				else if (java.sql.Time.class.equals(modelType))
				{
					String str = this.sqlTimeFormat.format(((java.sql.Time) obj));

					if (name == null)
						jsonGenerator.write(str);
					else
						jsonGenerator.write(name, str);
				}
				else if (java.util.Date.class.isAssignableFrom(modelType))
				{
					String str = this.dateFormat.format(((java.util.Date) obj));

					if (name == null)
						jsonGenerator.write(str);
					else
						jsonGenerator.write(name, str);
				}
				else if (Enum.class.isAssignableFrom(modelType))
				{
					if (name == null)
						jsonGenerator.write(obj.toString());
					else
						jsonGenerator.write(name, obj.toString());
				}
				else if (Class.class.equals(obj.getClass()))
				{
					if (name == null)
						jsonGenerator.write(((Class<?>) obj).getName());
					else
						jsonGenerator.write(name, ((Class<?>) obj).getName());
				}
				else
					throw new UnsupportedOperationException();
			}
		}
	}

	protected void writeNull(JsonGenerator jsonGenerator, String name, String path,
			IdentityHashMap<Object, String> referenceMap)
	{
		if (name == null)
			jsonGenerator.writeNull();
		else
			jsonGenerator.writeNull(name);
	}

	protected void writeBean(JsonGenerator jsonGenerator, Model model, Object bean, String name, String path,
			IdentityHashMap<Object, String> referenceMap)
	{
		if (name == null)
			jsonGenerator.writeStartObject();
		else
			jsonGenerator.writeStartObject(name);

		Property[] properties = model.getProperties();

		if (properties != null)
		{
			for (Property property : properties)
			{
				String pname = property.getName();
				Object pvalue = property.get(bean);

				write(jsonGenerator, property.getModel(), pvalue, pname, path + "." + pname, referenceMap);
			}
		}

		if (this.writeBeanClass)
			jsonGenerator.write(this.beanClassKey, bean.getClass().getName());

		jsonGenerator.writeEnd();
	}

	protected void writeArray(JsonGenerator jsonGenerator, Model model, Object[] array, String name, String path,
			IdentityHashMap<Object, String> referenceMap)
	{
		if (name == null)
			jsonGenerator.writeStartArray();
		else
			jsonGenerator.writeStartArray(name);

		for (int i = 0; i < array.length; i++)
		{
			String myPath = concatPath(path, "[" + i + "]");

			Object element = array[i];

			write(jsonGenerator, model, element, null, myPath, referenceMap);
		}

		jsonGenerator.writeEnd();
	}

	protected void writeCollection(JsonGenerator jsonGenerator, Model model, Collection<?> collection, String name,
			String path, IdentityHashMap<Object, String> referenceMap)
	{
		if (name == null)
			jsonGenerator.writeStartArray();
		else
			jsonGenerator.writeStartArray(name);

		int idx = 0;

		for (Object element : collection)
		{
			String myPath = concatPath(path, "[" + idx + "]");

			write(jsonGenerator, model, element, null, myPath, referenceMap);

			idx++;
		}

		jsonGenerator.writeEnd();
	}

	protected void writeMap(JsonGenerator jsonGenerator, Model model, Map<String, ?> map, String name, String path,
			IdentityHashMap<Object, String> referenceMap)
	{
		if (name == null)
			jsonGenerator.writeStartObject();
		else
			jsonGenerator.writeStartObject(name);

		Set<String> keys = map.keySet();
		for (String key : keys)
		{
			Object value = map.get(key);

			String myPath = concatPath(path, "." + key);

			write(jsonGenerator, model, value, key, myPath, referenceMap);
		}

		jsonGenerator.writeEnd();
	}

	protected void writeReference(JsonGenerator jsonGenerator, String refPath, String name)
	{
		// 写引用，而非对象本身
		if (name == null)
			jsonGenerator.writeStartObject();
		else
			jsonGenerator.writeStartObject(name);

		jsonGenerator.write(this.referenceKey, refPath);

		jsonGenerator.writeEnd();
	}

	protected String concatPath(String path, String concat)
	{
		return path + concat;
	}
}
