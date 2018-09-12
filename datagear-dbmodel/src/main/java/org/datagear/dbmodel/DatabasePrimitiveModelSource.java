/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.dbmodel;

import java.util.Map;

import org.datagear.model.Model;
import org.datagear.model.support.DefaultPrimitiveModelSource;
import org.datagear.model.support.PrimitiveModelSource;

/**
 * 数据库{@linkplain PrimitiveModelSource}。
 * 
 * @author datagear@163.com
 *
 */
public class DatabasePrimitiveModelSource extends DefaultPrimitiveModelSource
{
	public DatabasePrimitiveModelSource()
	{
		super();
	}

	public DatabasePrimitiveModelSource(Map<Class<?>, Model> primitiveModels)
	{
		super(primitiveModels);
	}

	@Override
	protected void initDefaultPrimitiveModels()
	{
		// XXX 目前还没研究是否要添加java.sql.Array, java.sql.Ref, java.sql.RowId,
		// java.sql.Struct，暂是保留此类
		super.initDefaultPrimitiveModels();
	}
}
