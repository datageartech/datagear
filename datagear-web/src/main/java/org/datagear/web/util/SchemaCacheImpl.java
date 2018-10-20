/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.util;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.datagear.management.domain.Schema;
import org.datagear.management.service.impl.SchemaCache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * {@linkplain SchemaCache}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class SchemaCacheImpl implements SchemaCache
{
	/** 缓存值的最大数 */
	private int maximumSize = 100;

	/** 缓存过期分钟数 */
	private int expireAfterAccessMinutes = 60 * 72;

	private Cache<String, Schema> cache = null;

	public SchemaCacheImpl()
	{
		super();

		this.cache = CacheBuilder.newBuilder().maximumSize(this.maximumSize)
				.expireAfterAccess(this.expireAfterAccessMinutes * 60, TimeUnit.SECONDS).build();
	}

	@Override
	public void putSchema(Schema schema)
	{
		this.cache.put(schema.getId(), schema);
	}

	@Override
	public Schema getSchema(String schemaId)
	{
		return this.cache.getIfPresent(schemaId);
	}

	@Override
	public void removeSchema(String schemaId)
	{
		this.cache.invalidate(schemaId);
	}

	@Override
	public Set<String> getAllSchemaIds()
	{
		Set<String> set = new HashSet<String>();

		ConcurrentMap<String, Schema> map = this.cache.asMap();

		set.addAll(map.keySet());

		return set;
	}
}
