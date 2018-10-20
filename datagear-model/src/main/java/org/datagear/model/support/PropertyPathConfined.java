/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.model.Model;
import org.datagear.model.Property;

/**
 * 属性路径。
 * <p>
 * <i>属性路径</i>用于唯一定位{@linkplain Model}的{@linkplain Property} 及其数据对象属性值，格式规范如下：
 * </p>
 * <ul>
 * <li>JavaBean属性：“.<i>property-name</i><i>&lt;属性具体模型名称&gt;</i>”（开头时不包含“.”）</li>
 * <li>集合/数组元素：“.<i>property-name</i><i>&lt;属性具体模型名称&gt;</i>[
 * <i>element-index</i>]”（开头时不包含“.”）</li>
 * </ul>
 * <p>
 * “<i>&lt;属性具体模型名称&gt;</i>”是{@linkplain Property#getModels()}的特定
 * {@linkplain Model}元素的{@linkplain Model#getName() 名称}，对于
 * {@linkplain MU#isConcreteProperty(Property) 具体属性}，它并不是必须的。
 * </p>
 * <p>
 * “<i>element-index</i>”是元素索引，可以是基本的元素位置数值，也可以是能够唯一标识元素的以“,”隔开的属性名/值对。
 * </p>
 * <p>
 * 示例：
 * </p>
 * <p>
 * “product.customers[0].address.phone”
 * </p>
 * <p>
 * “[0].name”
 * </p>
 * <p>
 * “product&lt;concrete-model-0&gt;.customers&lt;concrete-model-1&gt;[p0=a,p1=b,
 * p2&lt;long&gt;=3]”
 * </p>
 * <p>
 * 注意：{@linkplain Model}、{@linkplain Property}概念中没有嵌套数组，因此连续一个以上的“[
 * <i>element-index</i>]”将是非法的。
 * </p>
 * 
 * @author datagear@163.com
 * @deprecated 此设计不允许出现单独的“[<i>element-index</i>]”，因而无法适应描述数组/集合对象的属性路径。
 *
 */
@Deprecated
public class PropertyPathConfined implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final char ACCESS = '.';

	public static final char CONCRETE_L = '<';

	public static final char CONCRETE_R = '>';

	public static final char ELEMENT_L = '[';

	public static final char ELEMENT_R = ']';

	public static final String ATTR_SP = ",";

	public static final String ATTR_AS = "=";

	private final Segment[] segments;

	private PropertyPathConfined(Segment[] segments)
	{
		this.segments = segments;
	}

	/**
	 * 获取长度。
	 * 
	 * @return
	 */
	public int length()
	{
		return this.segments.length;
	}

	/**
	 * 构建第0个位置的子{@linkplain PropertyPathConfined}。
	 * 
	 * @return
	 */
	public PropertyPathConfined first()
	{
		return sub(0);
	}

	/**
	 * 构建最后位置的子{@linkplain PropertyPathConfined}。
	 * 
	 * @return
	 */
	public PropertyPathConfined last()
	{
		return sub(this.length() - 1);
	}

	/**
	 * 构建指定位置的子{@linkplain PropertyPathConfined}。
	 * 
	 * @param index
	 * @return
	 */
	public PropertyPathConfined sub(int index)
	{
		return sub(index, 1);
	}

	/**
	 * 构建子{@linkplain PropertyPathConfined}。
	 * 
	 * @param from
	 * @param length
	 * @return
	 */
	public PropertyPathConfined sub(int from, int length)
	{
		Segment[] segments = Arrays.copyOfRange(this.segments, from, from + length);

		return new PropertyPathConfined(segments);
	}

	/**
	 * 获取开头位置的JavaBean属性名。
	 * 
	 * @return
	 */
	public String getPropertyNameHead()
	{
		return getPropertyName(0);
	}

	/**
	 * 获取末尾位置的JavaBean属性名。
	 * 
	 * @return
	 */
	public String getPropertyNameTail()
	{
		return getPropertyName(this.segments.length - 1);
	}

	/**
	 * 获取JavaBean属性名。
	 * 
	 * @param index
	 * @return
	 */
	public String getPropertyName(int index)
	{
		return this.segments[index].getPropertyName();
	}

	/**
	 * 是否有开头位置的JavaBean属性具体模型名。
	 * 
	 * @return
	 */
	public boolean hasPropertyConcreteModelNameHead()
	{
		return hasPropertyConcreteModelName(0);
	}

	/**
	 * 是否有末尾位置的JavaBean属性具体模型名。
	 * 
	 * @return
	 */
	public boolean hasPropertyConcreteModelNameTail()
	{
		return hasPropertyConcreteModelName(this.segments.length - 1);
	}

	/**
	 * 是否有属性具体模型名。
	 * 
	 * @param index
	 * @return
	 */
	public boolean hasPropertyConcreteModelName(int index)
	{
		return this.segments[index].hasConcreteModelName();
	}

	/**
	 * 获取开头位置的JavaBean属性具体模型名。
	 * 
	 * @return
	 */
	public String getPropertyConcreteModelNameHead()
	{
		return getPropertyConcreteModelName(0);
	}

	/**
	 * 获取末尾位置的JavaBean属性具体模型名。
	 * 
	 * @return
	 */
	public String getPropertyConcreteModelNameTail()
	{
		return getPropertyConcreteModelName(this.segments.length - 1);
	}

	/**
	 * 获取JavaBean属性具体模型名。
	 * 
	 * @param index
	 * @return
	 */
	public String getPropertyConcreteModelName(int index)
	{
		return this.segments[index].getConcreteModelName();
	}

	/**
	 * 判断开头位置是否是数组/集合元素属性路径。
	 * 
	 * @return
	 */
	public boolean isElementHead()
	{
		return isElement(0);
	}

	/**
	 * 判断末尾位置是否是数组/集合元素属性路径。
	 * 
	 * @return
	 */
	public boolean isElementTail()
	{
		return isElement(this.segments.length - 1);
	}

	/**
	 * 判断给定位置是否是数组/集合元素属性路径。
	 * 
	 * @param index
	 * @return
	 */
	public boolean isElement(int index)
	{
		Segment segment = this.segments[index];

		return segment.isElement();
	}

	/**
	 * 判断开头位置是否是数组/集合元素值索引。
	 * 
	 * @return
	 */
	public boolean isElementValueIndexHead()
	{
		return isElementValueIndex(0);
	}

	/**
	 * 判断末尾位置是否是数组/集合元素值索引。
	 * 
	 * @return
	 */
	public boolean isElementValueIndexTail()
	{
		return isElementValueIndex(this.segments.length - 1);
	}

	/**
	 * 判断给定位置是否是数组/集合元素值索引。
	 * 
	 * @param index
	 * @return
	 */
	public boolean isElementValueIndex(int index)
	{
		Segment segment = this.segments[index];

		return segment.isElement() && segment.isElementValueIndex();
	}

	/**
	 * 获取开头位置是否是数组/集合元素属性路径。
	 * 
	 * @return
	 */
	public int getElementValueIndexHead()
	{
		return getElementValueIndex(0);
	}

	/**
	 * 获取末尾位置是否是数组/集合元素属性路径。
	 * 
	 * @return
	 */
	public int getElementValueIndexTail()
	{
		return getElementValueIndex(this.segments.length - 1);
	}

	/**
	 * 获取数组/集合元素值索引。
	 * 
	 * @param index
	 * @return
	 */
	public int getElementValueIndex(int index)
	{
		Segment segment = this.segments[index];

		if (!segment.isElement())
			throw new IllegalArgumentException("The [" + index + "] segment is not collection/array element index");

		if (!segment.isElementValueIndex())
			throw new IllegalArgumentException("The [" + index + "] segment is not collection/array value index");

		return segment.getElementValueIndex();
	}

	/**
	 * 判断开头位置是否是数组/集合元素映射表索引。
	 * 
	 * @return
	 */
	public boolean isElementMapIndexHead()
	{
		return isElementMapIndex(0);
	}

	/**
	 * 判断末尾位置是否是数组/集合元素映射表索引。
	 * 
	 * @return
	 */
	public boolean isElementMapIndexTail()
	{
		return isElementMapIndex(this.segments.length - 1);
	}

	/**
	 * 判断给定位置是否是数组/集合元素映射表索引。
	 * 
	 * @param index
	 * @return
	 */
	public boolean isElementMapIndex(int index)
	{
		Segment segment = this.segments[index];

		return segment.isElement() && segment.isElementMapIndex();
	}

	/**
	 * 获取开头位置是否是数组/集合元素映射表索引。
	 * 
	 * @return
	 */
	public Map<String, String> getElementMapIndexHead()
	{
		return getElementMapIndex(0);
	}

	/**
	 * 获取末尾位置是否是数组/集合元素映射表索引。
	 * 
	 * @return
	 */
	public Map<String, String> getElementMapIndexTail()
	{
		return getElementMapIndex(this.segments.length - 1);
	}

	/**
	 * 获取数组/集合元素映射表索引。
	 * 
	 * @param index
	 * @return
	 */
	public Map<String, String> getElementMapIndex(int index)
	{
		Segment segment = this.segments[index];

		if (!segment.isElement())
			throw new IllegalArgumentException("The [" + index + "] segment is not collection/array element index");

		if (!segment.isElementMapIndex())
			throw new IllegalArgumentException("The [" + index + "] segment is not collection/array map index");

		return segment.getElementMapIndex();
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < this.segments.length; i++)
		{
			Segment segment = this.segments[i];

			if (i != 0)
				sb.append(ACCESS);

			sb.append(segment.toString());
		}

		return sb.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(segments);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyPathConfined other = (PropertyPathConfined) obj;
		if (!Arrays.equals(segments, other.segments))
			return false;
		return true;
	}

	/**
	 * 由属性路径字符串构建{@linkplain PropertyPathConfined}。
	 * 
	 * @param propertyPath
	 * @return
	 * @throws IllegalPropertyPathException
	 */
	public static PropertyPathConfined valueOf(String propertyPath) throws IllegalPropertyPathException
	{
		Segment[] segments = parse(propertyPath);
		return new PropertyPathConfined(segments);
	}

	/**
	 * 解析字符串。
	 * 
	 * @param propertyPathStr
	 * @return
	 * @throws IllegalPropertyPathException
	 */
	protected static Segment[] parse(String propertyPathStr) throws IllegalPropertyPathException
	{
		List<Segment> segmentList = new ArrayList<Segment>();

		char[] cs = propertyPathStr.toCharArray();

		StringBuilder segmentStrCache = new StringBuilder();

		for (int i = 0, length = cs.length;; i++)
		{
			if (i >= length)
			{
				parseSegmentAndClear(propertyPathStr, segmentStrCache, i, segmentList);
				break;
			}

			char c = cs[i];

			if (isBlankChar(c))
				continue;

			if (c == ACCESS)
			{
				parseSegmentAndClear(propertyPathStr, segmentStrCache, i, segmentList);
			}
			else
			{
				segmentStrCache.append(c);
			}
		}

		Segment[] segments = new Segment[segmentList.size()];
		segmentList.toArray(segments);

		return segments;
	}

	/**
	 * 解析。
	 * 
	 * @param propertyPathStr
	 * @param segmentStrCache
	 * @param segList
	 * @throws IllegalPropertyPathException
	 */
	protected static void parseSegmentAndClear(String propertyPathStr, StringBuilder segmentStrCache,
			int segmentStrIndex, List<Segment> segList) throws IllegalPropertyPathException
	{
		int cacheLen = segmentStrCache.length();
		if (cacheLen > 0)
		{
			Segment segment = parseSegment(propertyPathStr, segmentStrCache.toString(), segmentStrIndex);

			// 不能有连续的元素属性路径
			if (segment.isElement())
			{
				Segment pre = (segList.isEmpty() ? null : segList.get(segList.size() - 1));

				if (pre != null && pre.isElement())
					throw new IllegalPropertyPathException(
							"[" + propertyPathStr + "] is illegal at [" + segmentStrIndex + "] index");
			}

			segList.add(segment);
			segmentStrCache.delete(0, cacheLen);
		}
	}

	/**
	 * 解析{@linkplain Segment}。
	 * 
	 * @param propertyPathStr
	 * @param segmentStr
	 * @param segmentStrIndex
	 * @return
	 * @throws IllegalPropertyPathException
	 */
	protected static Segment parseSegment(String propertyPathStr, String segmentStr, int segmentStrIndex)
			throws IllegalPropertyPathException
	{
		int concreteStart = segmentStr.indexOf(CONCRETE_L);
		int elementStart = segmentStr.indexOf(ELEMENT_L, (concreteStart < 0 ? 0 : concreteStart));
		int propertyNameEnd = -1;

		if (concreteStart > 0)
			propertyNameEnd = concreteStart;
		else if (elementStart > 0)
			propertyNameEnd = elementStart;
		else
			propertyNameEnd = segmentStr.length();

		String propertyName;
		String concreteModelName = null;
		Integer elementValueIndex = null;
		Map<String, String> elementMapIndex = null;

		if (propertyNameEnd <= 0)
			throw new IllegalPropertyPathException(
					"[" + propertyPathStr + "] is illegal at [" + segmentStrIndex + "] index");

		propertyName = segmentStr.substring(0, propertyNameEnd);

		if (concreteStart > 0)
		{
			int concreteEnd = segmentStr.indexOf(CONCRETE_R, concreteStart + 1);

			if (concreteEnd <= concreteStart + 1)
				throw new IllegalPropertyPathException(
						"[" + propertyPathStr + "] is illegal at [" + (segmentStrIndex + concreteStart) + "] index");

			concreteModelName = segmentStr.substring(concreteStart + 1, concreteEnd);
		}

		if (elementStart > 0)
		{
			int elementEnd = segmentStr.indexOf(ELEMENT_R, elementStart + 1);

			if (elementEnd != segmentStr.length() - 1)
				throw new IllegalPropertyPathException(
						"[" + propertyPathStr + "] is illegal at [" + (segmentStrIndex + elementStart) + "] index");

			String elementStr = segmentStr.substring(elementStart + 1, elementEnd);

			if (isIntegerString(elementStr))
				elementValueIndex = Integer.parseInt(elementStr);
			else
			{
				elementMapIndex = resolveMapIndex(propertyPathStr, segmentStr, segmentStrIndex, elementStr,
						elementStart);
			}
		}

		return new Segment(segmentStr, propertyName, concreteModelName, elementValueIndex, elementMapIndex);
	}

	protected static boolean isIntegerString(String s)
	{
		if (s == null || s.isEmpty())
			return false;

		char[] cs = s.toCharArray();

		for (char c : cs)
		{
			if (c < '0' || c > '9')
				return false;
		}

		return true;
	}

	protected static Map<String, String> resolveMapIndex(String propertyPathStr, String segmentStr, int segmentStrIndex,
			String mapIndexString, int mapIndexStringIndex)
	{
		Map<String, String> mapIndex = new HashMap<String, String>();

		String[] mapary = mapIndexString.split(ATTR_SP);

		for (String ent : mapary)
		{
			String[] kv = ent.split(ATTR_AS);

			if (kv.length == 1)
				mapIndex.put(kv[0].trim(), "");
			else if (kv.length == 2)
				mapIndex.put(kv[0].trim(), kv[1].trim());
			else
				throw new IllegalPropertyPathException("[" + propertyPathStr + "] is illegal at ["
						+ (segmentStrIndex + mapIndexStringIndex) + "] index");
		}

		return mapIndex;
	}

	/**
	 * 判断给定字符是否是空格字符。
	 * 
	 * @param c
	 * @return
	 */
	protected static boolean isBlankChar(char c)
	{
		return c == ' ' || c == '\t' || c == '\r' || c == '\n';
	}

	/**
	 * 路径片段。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class Segment
	{
		/** 原始内容 */
		private String content;

		/** 属性名 */
		private String propertyName;

		/** 属性具体模型名 */
		private String concreteModelName;

		/** 数值索引 */
		private Integer elementValueIndex = null;

		/** 映射表索引 */
		private Map<String, String> elementMapIndex = null;

		public Segment(String content, String propertyName)
		{
			super();
			this.content = content;
			this.propertyName = propertyName;
		}

		public Segment(String content, String propertyName, String concreteModelName)
		{
			super();
			this.content = content;
			this.propertyName = propertyName;
			this.concreteModelName = concreteModelName;
		}

		public Segment(String content, String propertyName, Integer elementValueIndex)
		{
			super();
			this.content = content;
			this.propertyName = propertyName;
			this.elementValueIndex = elementValueIndex;
		}

		public Segment(String content, String propertyName, String concreteModelName, Integer elementValueIndex)
		{
			super();
			this.content = content;
			this.propertyName = propertyName;
			this.concreteModelName = concreteModelName;
			this.elementValueIndex = elementValueIndex;
		}

		public Segment(String content, String propertyName, Map<String, String> elementMapIndex)
		{
			super();
			this.content = content;
			this.propertyName = propertyName;
			this.elementMapIndex = elementMapIndex;
		}

		public Segment(String content, String propertyName, String concreteModelName,
				Map<String, String> elementMapIndex)
		{
			super();
			this.content = content;
			this.propertyName = propertyName;
			this.concreteModelName = concreteModelName;
			this.elementMapIndex = elementMapIndex;
		}

		public Segment(String content, String propertyName, String concreteModelName, Integer elementValueIndex,
				Map<String, String> elementMapIndex)
		{
			super();
			this.content = content;
			this.propertyName = propertyName;
			this.concreteModelName = concreteModelName;
			this.elementValueIndex = elementValueIndex;
			this.elementMapIndex = elementMapIndex;
		}

		public String getPropertyName()
		{
			return propertyName;
		}

		public boolean hasConcreteModelName()
		{
			return this.concreteModelName != null && !this.concreteModelName.isEmpty();
		}

		public String getConcreteModelName()
		{
			return concreteModelName;
		}

		public boolean isElement()
		{
			return (this.elementValueIndex != null || this.elementMapIndex != null);
		}

		public boolean isElementValueIndex()
		{
			return this.elementValueIndex != null;
		}

		public Integer getElementValueIndex()
		{
			return elementValueIndex;
		}

		public boolean isElementMapIndex()
		{
			return this.elementMapIndex != null;
		}

		public Map<String, String> getElementMapIndex()
		{
			return elementMapIndex;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((content == null) ? 0 : content.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Segment other = (Segment) obj;
			if (content == null)
			{
				if (other.content != null)
					return false;
			}
			else if (!content.equals(other.content))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return this.content;
		}
	}
}
