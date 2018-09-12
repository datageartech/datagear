/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.mapper;

/**
 * 映射工具类。
 * 
 * @author datagear@163.com
 *
 */
public class MapperUtil
{
	private MapperUtil()
	{
	}

	/**
	 * 是否是{@linkplain ModelTableMapper}。
	 * 
	 * @param mapper
	 * @return
	 */
	public static boolean isModelTableMapper(Mapper mapper)
	{
		return (mapper instanceof ModelTableMapper);
	}

	/**
	 * 强转为{@linkplain ModelTableMapper}。
	 * 
	 * @param mapper
	 * @return
	 */
	public static ModelTableMapper castModelTableMapper(Mapper mapper)
	{
		return (ModelTableMapper) mapper;
	}

	/**
	 * 是否是{@linkplain PropertyTableMapper}。
	 * 
	 * @param mapper
	 * @return
	 */
	public static boolean isPropertyTableMapper(Mapper mapper)
	{
		return (mapper instanceof PropertyTableMapper);
	}

	/**
	 * 强转为{@linkplain PropertyTableMapper}。
	 * 
	 * @param mapper
	 * @return
	 */
	public static PropertyTableMapper castPropertyTableMapper(Mapper mapper)
	{
		return (PropertyTableMapper) mapper;
	}

	/**
	 * 是否是{@linkplain JoinTableMapper}。
	 * 
	 * @param mapper
	 * @return
	 */
	public static boolean isJoinTableMapper(Mapper mapper)
	{
		return (mapper instanceof JoinTableMapper);
	}

	/**
	 * 强转为{@linkplain JoinTableMapper}。
	 * 
	 * @param mapper
	 * @return
	 */
	public static JoinTableMapper castJoinTableMapper(Mapper mapper)
	{
		return (JoinTableMapper) mapper;
	}

	/**
	 * 获取实体{@linkplain Model 模型}的ID列名称数组。
	 * <p>
	 * 注意：返回数组索引不一定与{@linkplain Model#getIdProperties()}
	 * 数组索引一一对应，因为可能某个ID属性又是实体属性，而其又有多个ID属性。
	 * </p>
	 * 
	 * @param model
	 * @param relationMappers
	 * @return
	 */
	// public static String[] getIdColumnNames(Model model, RelationMapper[]
	// relationMappers)
	// {
	// if (!MU.isEntityModel(model))
	// throw new NotEntityModelException();
	//
	// List<String> idColumnNameList = new ArrayList<String>();
	//
	// Property[] idProperties = model.getIdProperties();
	// RelationMapper[] idRelationMappers = getIdRelationMappers(model,
	// relationMappers);
	//
	// for (int i = 0; i < idProperties.length; i++)
	// {
	// Property idProperty = idProperties[i];
	// RelationMapper idRelationMapper = idRelationMappers[i];
	//
	// if (idRelationMapper.isOneToMany() || idRelationMapper.isManyToMany())
	// throw new IllegalIdPropertyException("ID property [" +
	// idProperty.getName() + "] must not be ["
	// + OneToMany.class.getSimpleName() + "] nor be [" +
	// ManyToMany.class.getSimpleName() + "]");
	//
	// Mapper[] mappers = idRelationMapper.getMappers();
	// for (Mapper mapper : mappers)
	// {
	// if (!isModelTableMapper(mapper))
	// throw new IllegalIdPropertyException("ID property [" +
	// idProperty.getName()
	// + "] 's all mappers must be [" + ModelTableMapper.class.getSimpleName() +
	// "]");
	// }
	//
	// String[] consistentColumnNames = getConsistentColumnNames(model,
	// idProperty, idRelationMapper);
	//
	// if (consistentColumnNames == null || consistentColumnNames.length == 0)
	// throw new IllegalIdPropertyException(
	// "[" + idProperty.getName() + "] ID property 's all mappers must have
	// consistent column names");
	//
	// idColumnNameList.addAll(Arrays.asList(consistentColumnNames));
	// }
	//
	// return idColumnNameList.toArray(new String[idColumnNameList.size()]);
	// }

	/**
	 * 获取指定属性的一致列名称数组。
	 * <p>
	 * 一致列名称是指所有{@linkplain Property#getModels()}在模型表内都使用同一组字段。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @param relationMapper
	 * @return 如果属性没有任何字段在模型表内，返回空数组；如果不是一致的，将返回{@code null}；否则，返回一致列名称数组。
	 */
	// public static String[] getConsistentColumnNames(Model model, Property
	// property, RelationMapper relationMapper)
	// {
	// String[] consistentColumnNames = null;
	//
	// Mapper[] mappers = relationMapper.getMappers();
	//
	// for (int i = 0; i < mappers.length; i++)
	// {
	// if (!isModelTableMapper(mappers[i]))
	// continue;
	//
	// ModelTableMapper mapper = castModelTableMapper(mappers[i]);
	//
	// String[] myColumnNames = null;
	//
	// if (mapper.isPrimitiveValuePropertyMapper())
	// myColumnNames = new String[] { mapper.getPrimitiveValueColumnName() };
	// else if (mapper.isEntityPropertyMapper())
	// {
	// myColumnNames = mapper.getPropertyKeyColumnNames();
	// }
	// else
	// throw new UnsupportedOperationException();
	//
	// if (consistentColumnNames == null)
	// consistentColumnNames = myColumnNames;
	// else
	// {
	// if (!Arrays.equals(consistentColumnNames, myColumnNames))
	// return null;
	// }
	// }
	//
	// return (consistentColumnNames == null ? new String[0] :
	// consistentColumnNames);
	// }
}
