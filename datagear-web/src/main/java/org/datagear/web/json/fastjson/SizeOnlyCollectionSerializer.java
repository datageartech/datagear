/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.json.fastjson;

import java.io.IOException;
import java.lang.reflect.Type;

import org.datagear.persistence.collection.SizeOnlyCollection;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;

/**
 * {@linkplain SizeOnlyCollection}的{@linkplain ObjectSerializer}。
 * 
 * @author datagear@163.com
 *
 */
public class SizeOnlyCollectionSerializer implements ObjectSerializer
{
	public SizeOnlyCollectionSerializer()
	{
		super();
	}

	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
			throws IOException
	{
		if (object != null)
		{
			@SuppressWarnings("unchecked")
			SizeOnlyCollection<Object> sizeOnlyCollection = (SizeOnlyCollection<Object>) object;

			object = sizeOnlyCollection.toMap();
		}

		serializer.write(object);
	}
}
