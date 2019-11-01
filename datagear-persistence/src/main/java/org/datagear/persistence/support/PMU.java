/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.support.MU;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.collection.SizeOnlyCollection;
import org.datagear.persistence.features.KeyRule;
import org.datagear.persistence.features.KeyRule.RuleType;
import org.datagear.persistence.features.MappedBy;
import org.datagear.persistence.mapper.Mapper;
import org.datagear.persistence.mapper.MapperUtil;
import org.datagear.persistence.mapper.ModelTableMapper;

/**
 * 持久化模型公用方法集。
 * 
 * @author datagear@163.com
 *
 */
public class PMU
{
	/**
	 * 判断给定{@linkplain Property}是否是私有的。
	 * <p>
	 * 如果属性值没有独立的生命周期，那么它就是私有的。
	 * </p>
	 * <p>
	 * 属性值只有私有的和共享的两种可能。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @return
	 */
	public static boolean isPrivate(Model model, Property property)
	{
		Mapper mapper = property.getFeature(Mapper.class);

		KeyRule keyRule = mapper.getPropertyKeyDeleteRule();

		if (keyRule != null && RuleType.CASCADE.equals(keyRule.getRuleType()))
			return true;

		if (MapperUtil.isModelTableMapper(mapper))
		{
			ModelTableMapper modelTableMapper = MapperUtil.castModelTableMapper(mapper);

			return modelTableMapper.isPrimitivePropertyMapper();
		}
		else if (MapperUtil.isPropertyTableMapper(mapper))
		{
			return true;
		}
		else if (MapperUtil.isJoinTableMapper(mapper))
		{
			return false;
		}
		else
			return false;
	}

	/**
	 * 判断给定{@linkplain Property}是否是共享的。
	 * <p>
	 * 如果属性值有独立的生命周期，那么它就是共享的。
	 * </p>
	 * <p>
	 * 属性值只有私有的和共享的两种可能。
	 * </p>
	 * 
	 * @param model
	 * @param property
	 * @return
	 */
	public static boolean isShared(Model model, Property property)
	{
		return !isPrivate(model, property);
	}

	/**
	 * 设置属性值。
	 * <p>
	 * 此方法会处理{@linkplain MappedBy}双向关联，反向赋值。
	 * </p>
	 * <p>
	 * 注意：此方法不会处理反向集合属性，因为此时obj仅是反向集合中的单个元素
	 * </p>
	 * 
	 * @param model
	 * @param obj
	 * @param property
	 * @param propertyValue
	 */
	public static void setPropertyValue(Model model, Object obj, Property property, Object propertyValue)
	{
		Model pmodel = MU.getModel(property);

		if (propertyValue == null)
		{
			if (pmodel.getType().isPrimitive())
				;
			else
				property.set(obj, null);
		}
		else if (MU.isPrimitiveModel(pmodel))
		{
			property.set(obj, propertyValue);
		}
		else
		{
			property.set(obj, propertyValue);

			setPropertyValueMappedByIf(model, obj, property, propertyValue);
		}
	}

	/**
	 * 设置属性值的{@linkplain MappedBy}反向属性值。
	 * 
	 * @param model
	 * @param obj
	 * @param property
	 * @param propertyValueOrElement
	 */
	public static boolean setPropertyValueMappedByIf(Model model, Object obj, Property property,
			Object propertyValueOrElement)
	{
		if (propertyValueOrElement == null)
			return false;

		Model pmodel = MU.getModel(property);

		if (MU.isPrimitiveModel(pmodel))
			return false;

		Mapper mapper = property.getFeature(Mapper.class);

		String mappedTarget = null;
		if (mapper.isMappedBySource())
			mappedTarget = mapper.getMappedByTarget();
		else if (mapper.isMappedByTarget())
			mappedTarget = mapper.getMappedBySource();

		// 反向设置属性值
		if (mappedTarget != null)
		{
			Property mappedProperty = MU.getProperty(pmodel, mappedTarget);

			// 不处理反向集合属性，因为此时obj仅是反向集合中的单个元素
			if (MU.isMultipleProperty(mappedProperty))
				return false;

			if (propertyValueOrElement instanceof Collection<?>)
			{
				@SuppressWarnings("unchecked")
				Collection<Object> collection = (Collection<Object>) propertyValueOrElement;

				for (Object ele : collection)
					mappedProperty.set(ele, obj);
			}
			else if (propertyValueOrElement instanceof Object[])
			{
				Object[] array = (Object[]) propertyValueOrElement;

				for (Object ele : array)
					mappedProperty.set(ele, obj);
			}
			else
				mappedProperty.set(propertyValueOrElement, obj);

			return true;
		}

		return false;
	}

	/**
	 * 将对象转换为数组。
	 * 
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Object[] toArray(Object obj)
	{
		if (obj == null)
			return null;

		if (obj instanceof Object[])
			return (Object[]) obj;

		Class<?> objType = obj.getClass();

		if (SizeOnlyCollection.class.isAssignableFrom(objType))
		{
			return new Object[0];
		}
		else if (List.class.isAssignableFrom(objType))
		{
			return ((List<Object>) obj).toArray();
		}
		else if (Collection.class.isAssignableFrom(objType))
		{
			Collection<Object> cobj = (Collection<Object>) obj;

			Object[] array = new Object[cobj.size()];

			int i = 0;
			for (Object eobj : cobj)
			{
				array[i] = eobj;
				i++;
			}

			return array;
		}
		else
			return new Object[] { obj };
	}

	/**
	 * 替换SQL查询条件（WHERE之后的SQL片段）中的标识符。
	 * 
	 * @param dialect
	 * @param condition
	 * @param source
	 * @param keepIdentifierQuote
	 * @return
	 */
	public static String replaceIdentifier(Dialect dialect, String condition, IdentifierReplaceSource source,
			boolean keepIdentifierQuote)
	{
		if (condition == null || condition.isEmpty())
			return condition;

		String iq = dialect.getIdentifierQuote();
		StringBuilder cb = new StringBuilder(condition.length());

		StringBuilder token = new StringBuilder();
		for (int i = 0, len = condition.length(); i < len; i++)
		{
			char c = condition.charAt(i);

			if (Character.isWhitespace(c) || c == '=' || c == '>' || c == '<' || c == '!' || c == '(' || c == ')'
					|| c == ',')
			{
				String replaceStr = null;
				boolean hasToken = (token.length() > 0);

				if (hasToken)
				{
					String tokenStr = token.toString();

					if (!isConditionKeyword(tokenStr))
						replaceStr = source.replace(dialect.unquote(tokenStr));

					if (replaceStr != null)
					{
						if (keepIdentifierQuote && dialect.isQuoted(tokenStr))
							replaceStr = dialect.quote(replaceStr);

						cb.append(replaceStr);
					}
					else
						cb.append(tokenStr);

					token.delete(0, token.length());

					cb.append(c);
				}
				else
					cb.append(c);
			}
			// SQL字符串
			else if (c == '\'')
			{
				cb.append(c);

				for (i = i + 1; i < len; i++)
				{
					c = condition.charAt(i);
					char cn = (i + 1 >= len ? 0 : condition.charAt(i + 1));

					cb.append(c);

					if (c == '\'')
					{
						if (cn == '\'')
						{
							cb.append(cn);
							i += 1;
						}
						else
							break;
					}
				}
			}
			// 标识符引用
			else if (condition.indexOf(iq, i) == i)
			{
				int endIdx = condition.indexOf(iq, i + 1);
				if (endIdx < i + 1)
					endIdx = len;
				else
					endIdx = endIdx + iq.length();

				token.append(condition.subSequence(i, endIdx));
				i = endIdx - 1;
			}
			else
				token.append(c);
		}

		if (token.length() > 0)
		{
			String tokenStr = token.toString();
			String replaceStr = null;

			if (!isConditionKeyword(tokenStr))
				replaceStr = source.replace(dialect.unquote(tokenStr));

			if (replaceStr != null)
			{
				if (keepIdentifierQuote && dialect.isQuoted(tokenStr))
					replaceStr = dialect.quote(replaceStr);

				cb.append(replaceStr);
			}
			else
				cb.append(tokenStr);
		}

		return cb.toString();
	}

	protected static boolean isConditionKeyword(String s)
	{
		if (s == null || s.isEmpty())
			return false;

		return CONDITION_KEYWORDS.contains(s.toUpperCase());
	}

	private static final Set<String> CONDITION_KEYWORDS = new HashSet<String>();
	static
	{
		CONDITION_KEYWORDS.add("AND");
		CONDITION_KEYWORDS.add("OR");
		CONDITION_KEYWORDS.add("LIKE");
		CONDITION_KEYWORDS.add("NOT");
		CONDITION_KEYWORDS.add("BETWEEN");
		CONDITION_KEYWORDS.add("IN");
		CONDITION_KEYWORDS.add("IS");
		CONDITION_KEYWORDS.add("NULL");
	}

	/**
	 * 标识符替换源。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static interface IdentifierReplaceSource
	{
		/**
		 * 替换标识符。
		 * <p>
		 * 如果没有可替换，返回{@code null}。
		 * </p>
		 * 
		 * @param identifier
		 * @return
		 */
		String replace(String identifier);
	}
}
