/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import org.datagear.dbinfo.SqlTypeInfo.SearchableType;
import org.datagear.model.PropertyFeature;
import org.datagear.model.ValueFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 可搜索类型。
 * 
 * @author datagear@163.com
 *
 */
public class Searchable extends ValueFeature<SearchableType> implements PropertyFeature, PersistenceFeature
{
	public Searchable()
	{
		super();
	}

	public Searchable(SearchableType value)
	{
		super(value);
	}
}
