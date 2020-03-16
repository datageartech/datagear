/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbmodel;

import java.sql.Connection;
import java.util.Collection;

import org.datagear.model.Model;
import org.datagear.model.ModelManager;
import org.datagear.model.Property;
import org.datagear.model.support.AbstractProperty;
import org.datagear.model.support.DefaultModelManager;
import org.datagear.model.support.MU;

/**
 * 抽象{@linkplain DbModelFactory}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDbModelFactory implements DbModelFactory
{
	private DatabaseModelResolver databaseModelResolver;

	private ModelNameResolver modelNameResolver = new IdentifierModelNameResolver();

	public AbstractDbModelFactory()
	{
		super();
	}

	public AbstractDbModelFactory(DatabaseModelResolver databaseModelResolver)
	{
		super();
		this.databaseModelResolver = databaseModelResolver;
	}

	public DatabaseModelResolver getDatabaseModelResolver()
	{
		return databaseModelResolver;
	}

	public void setDatabaseModelResolver(DatabaseModelResolver databaseModelResolver)
	{
		this.databaseModelResolver = databaseModelResolver;
	}

	public ModelNameResolver getModelNameResolver()
	{
		return modelNameResolver;
	}

	public void setModelNameResolver(ModelNameResolver modelNameResolver)
	{
		this.modelNameResolver = modelNameResolver;
	}

	/**
	 * 解析模型名称。
	 * 
	 * @param tableName
	 * @return
	 */
	protected String resolveModelName(String tableName)
	{
		return this.modelNameResolver.resolve(tableName);
	}

	/**
	 * 载入模型并加入全局{@linkplain ModelManager}。
	 * 
	 * @param cn
	 * @param schema
	 * @param tableName
	 * @param globalModelManager
	 * @return
	 * @throws DatabaseModelResolverException
	 */
	protected Model loadModelAndAdd(Connection cn, String schema, String tableName, ModelManager globalModelManager)
			throws DatabaseModelResolverException
	{
		ModelManager localModelManager = new DefaultModelManager();

		Model model = this.databaseModelResolver.resolve(cn, globalModelManager, localModelManager, tableName);

		// 将globalModelManager中引用的旧Model替换为此新Model
		replaceAndAddModel(globalModelManager, model);

		Collection<Model> localModels = localModelManager.toCollection();
		for (Model localModel : localModels)
		{
			// 不替换已经存在的Model
			if (!globalModelManager.contains(localModel.getName()))
				globalModelManager.put(localModel);
		}

		return model;

	}

	/**
	 * 替换新{@linkplain Model}。
	 * 
	 * @param modelManager
	 * @param model
	 */
	protected void replaceAndAddModel(ModelManager modelManager, Model model)
	{
		String modelName = model.getName();

		Collection<Model> zyModels = modelManager.toCollection();
		for (Model zyModel : zyModels)
		{
			if (MU.isPrimitiveModel(zyModel))
				continue;

			Property[] properties = zyModel.getProperties();
			for (Property property : properties)
			{
				Model propertyModel = MU.getModel(property);

				if (modelName.equals(propertyModel.getName()))
					propertyModel = model;

				if (property instanceof AbstractProperty)
					((AbstractProperty) property).setModel(propertyModel);
				else
					throw new UnsupportedOperationException("Property.setModels(Model[]) is required for updating");
			}
		}

		modelManager.put(model);
	}
}
