/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.json.fastjson;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.datagear.model.Model;
import org.datagear.model.support.DynamicBean;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerialContext;
import com.alibaba.fastjson.serializer.SerializeFilterable;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * {@linkplain DynamicBean}对象序列化类。
 * 
 * @author datagear@163.com
 * @deprecated DynamicBean已改为继承自Map，不再需要单独编写序列化类，{@linkplain FastJsonConfigFactory#init()}方法中也已将此类的配置移除
 */
@Deprecated
public class DynamicBeanObjectSerializer extends SerializeFilterable implements ObjectSerializer
{
	public static final String MODEL_NAME = "___MODEL_NAME___";

	public DynamicBeanObjectSerializer()
	{
		super();
	}

	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
			throws IOException
	{
		if (object == null)
		{
			serializer.write(object);
		}
		else
		{
			if (serializer.containsReference(object))
			{
				serializer.writeReference(object);
				return;
			}

			SerialContext parent = serializer.getContext();
			serializer.setContext(parent, object, fieldName, 0);

			DynamicBean dynamicBean = (DynamicBean) object;
			Model model = dynamicBean.getModel();

			Map<String, Object> propValues = dynamicBean;

			if (model != null)
			{
				propValues = new HashMap<String, Object>(propValues);
				propValues.put(MODEL_NAME, model.getName());
			}

			boolean unwrapped = false;
			SerializeWriter out = serializer.out;

			try
			{
				if (!unwrapped)
				{
					out.write('{');
				}

				serializer.incrementIndent();

				Class<?> preClazz = null;
				ObjectSerializer preWriter = null;

				boolean first = true;

				for (Map.Entry<String, Object> entry : propValues.entrySet())
				{
					Object value = entry.getValue();
					String entryKey = entry.getKey();

					value = this.processValue(serializer, null, object, entryKey, value);

					if (value == null)
					{
						if (!out.isEnabled(SerializerFeature.WRITE_MAP_NULL_FEATURES))
						{
							continue;
						}
					}

					if (!first)
					{
						out.write(',');
					}

					if (out.isEnabled(SerializerFeature.PrettyFormat))
					{
						serializer.println();
					}
					out.writeFieldName(entryKey, true);

					first = false;

					if (value == null)
					{
						out.writeNull();
						continue;
					}

					Class<?> clazz = value.getClass();

					if (clazz == preClazz)
					{
						preWriter.write(serializer, value, entryKey, null, 0);
					}
					else
					{
						preClazz = clazz;
						preWriter = serializer.getObjectWriter(clazz);

						preWriter.write(serializer, value, entryKey, null, 0);
					}
				}
			}
			finally
			{
				serializer.setContext(parent);
			}

			serializer.decrementIdent();
			if (out.isEnabled(SerializerFeature.PrettyFormat) && propValues.size() > 0)
			{
				serializer.println();
			}

			if (!unwrapped)
			{
				out.write('}');
			}
		}
	}
}
