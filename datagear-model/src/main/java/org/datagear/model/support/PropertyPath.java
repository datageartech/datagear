/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.model.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.datagear.model.Model;
import org.datagear.model.Property;

/**
 * 属性路径。
 * <p>
 * <i>属性路径</i>用于唯一定位{@linkplain Model}的{@linkplain Property} 及其数据对象属性值，格式规范如下：
 * </p>
 * <ul>
 * <li>JavaBean属性：“.<i>property-name</i><i>&lt;属性具体模型索引&gt;</i>”（开头时不包含“.”）</li>
 * <li>集合/数组元素：“[<i>element-index</i>]”</li>
 * </ul>
 * <p>
 * “<i>&lt;属性具体模型索引&gt;</i>”是{@linkplain Property#getModels()}的特定
 * {@linkplain Model}元素的索引数值，对于{@linkplain MU#isConcreteProperty(Property)
 * 具体属性}，它并不是必须的。
 * </p>
 * <p>
 * “<i>element-index</i>”是元素索引数值。
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
 * “product&lt;0&gt;.customers&lt;1&gt;.name”
 * </p>
 * <p>
 * 注意：{@linkplain Model}、{@linkplain Property}概念中没有Map，因此这里没有定义映射表定位规范。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class PropertyPath implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final char PROPERTY = '.';

	public static final String PROPERTY_STRING = ".";

	public static final char CONCRETE_L = '<';

	public static final String CONCRETE_L_STRING = "<";

	public static final char CONCRETE_R = '>';

	public static final String CONCRETE_R_STRING = ">";

	public static final char ELEMENT_L = '[';

	public static final String ELEMENT_L_STRING = "[";

	public static final char ELEMENT_R = ']';

	public static final String ELEMENT_R_STRING = "]";

	private final Segment[] segments;

	private PropertyPath(Segment[] segments)
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
	 * 构建第0个位置的子{@linkplain PropertyPath}。
	 * 
	 * @return
	 */
	public PropertyPath first()
	{
		return sub(0);
	}

	/**
	 * 构建最后位置的子{@linkplain PropertyPath}。
	 * 
	 * @return
	 */
	public PropertyPath last()
	{
		return sub(this.length() - 1);
	}

	/**
	 * 构建指定位置的子{@linkplain PropertyPath}。
	 * 
	 * @param index
	 * @return
	 */
	public PropertyPath sub(int index)
	{
		return sub(index, 1);
	}

	/**
	 * 构建子{@linkplain PropertyPath}。
	 * 
	 * @param from
	 * @param length
	 * @return
	 */
	public PropertyPath sub(int from, int length)
	{
		Segment[] segments = Arrays.copyOfRange(this.segments, from, from + length);

		return new PropertyPath(segments);
	}

	/**
	 * 判断开头是否是JavaBean属性路径。
	 * 
	 * @return
	 */
	public boolean isPropertyHead()
	{
		return isProperty(0);
	}

	/**
	 * 判断末尾是否是JavaBean属性路径。
	 * 
	 * @return
	 */
	public boolean isPropertyTail()
	{
		return isProperty(this.segments.length - 1);
	}

	/**
	 * 判断给定位置是否是JavaBean属性路径。
	 * 
	 * @param index
	 * @return
	 */
	public boolean isProperty(int index)
	{
		Segment segment = this.segments[index];

		return segment.isProperty();
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
		Segment segment = this.segments[index];

		if (!segment.isProperty())
			throw new IllegalArgumentException("The [" + index + "] segment is not JavaBean property name");

		return segment.getPropertyName();
	}

	/**
	 * 设置开头位置的JavaBean属性名。
	 * 
	 * @param propertyName
	 */
	public void setPropertyNameHead(String propertyName)
	{
		setPropertyName(0, propertyName);
	}

	/**
	 * 设置末尾位置的JavaBean属性名。
	 * 
	 * @param propertyName
	 */
	public void setPropertyNameTail(String propertyName)
	{
		setPropertyName(this.segments.length - 1, propertyName);
	}

	/**
	 * 设置JavaBean属性名。
	 * 
	 * @param index
	 * @param propertyName
	 */
	public void setPropertyName(int index, String propertyName)
	{
		Segment segment = this.segments[index];

		segment.setPropertyName(propertyName);
	}

	/**
	 * 是否有开头位置的JavaBean属性模型索引。
	 * 
	 * @return
	 */
	public boolean hasPropertyModelIndexHead()
	{
		return hasPropertyModelIndex(0);
	}

	/**
	 * 是否有末尾位置的JavaBean属性具体模型索引。
	 * 
	 * @return
	 */
	public boolean hasPropertyModelIndexTail()
	{
		return hasPropertyModelIndex(this.segments.length - 1);
	}

	/**
	 * 是否有属性具体模型索引。
	 * 
	 * @param index
	 * @return
	 */
	public boolean hasPropertyModelIndex(int index)
	{
		Segment segment = this.segments[index];

		if (!segment.isProperty())
			throw new IllegalArgumentException("The [" + index + "] segment is not JavaBean property name");

		return segment.hasPropertyModelIndex();
	}

	/**
	 * 获取开头位置的JavaBean属性具体模型索引。
	 * 
	 * @return
	 */
	public int getPropertyModelIndexHead()
	{
		return getPropertyModelIndex(0);
	}

	/**
	 * 获取末尾位置的JavaBean属性具体模型索引。
	 * 
	 * @return
	 */
	public int getPropertyModelIndexTail()
	{
		return getPropertyModelIndex(this.segments.length - 1);
	}

	/**
	 * 获取JavaBean属性具体模型索引。
	 * 
	 * @param index
	 * @return
	 */
	public int getPropertyModelIndex(int index)
	{
		Segment segment = this.segments[index];

		if (!segment.isProperty())
			throw new IllegalArgumentException("The [" + index + "] segment is not JavaBean property name");

		return segment.getPropertyModelIndex();
	}

	/**
	 * 设置开头位置的JavaBean属性具体模型索引。
	 * 
	 * @param propertyModelIndex
	 */
	public void setPropertyModelIndexHead(int propertyModelIndex)
	{
		setPropertyModelIndex(0, propertyModelIndex);
	}

	/**
	 * 设置末尾位置的JavaBean属性具体模型索引。
	 * 
	 * @param propertyModelIndex
	 */
	public void setPropertyModelIndexTail(int propertyModelIndex)
	{
		setPropertyModelIndex(this.segments.length - 1, propertyModelIndex);
	}

	/**
	 * 设置指定位置的JavaBean属性具体模型索引。
	 * 
	 * @param index
	 * @param propertyModelIndex
	 */
	public void setPropertyModelIndex(int index, int propertyModelIndex)
	{
		Segment segment = this.segments[index];

		segment.setPropertyModelIndex(propertyModelIndex);
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
	 * 获取开头位置的数组/集合元素索引。
	 * 
	 * @return
	 */
	public int getElementIndexHead()
	{
		return getElementIndex(0);
	}

	/**
	 * 获取末尾位置的数组/集合元素索引。
	 * 
	 * @return
	 */
	public int getElementIndexTail()
	{
		return getElementIndex(this.segments.length - 1);
	}

	/**
	 * 获取数组/集合元素的索引。
	 * 
	 * @param index
	 * @return
	 */
	public int getElementIndex(int index)
	{
		Segment segment = this.segments[index];

		if (!segment.isElement())
			throw new IllegalArgumentException("The [" + index + "] segment is not collection/array element index");

		return segment.getElementIndex();
	}

	/**
	 * 设置开头位置的数组/集合元素索引。
	 * 
	 * @param elementIndex
	 */
	public void setElementIndexHead(int elementIndex)
	{
		setElementIndex(0, elementIndex);
	}

	/**
	 * 设置末尾位置的数组/集合元素索引。
	 * 
	 * @param elementIndex
	 */
	public void setElementIndexTail(int elementIndex)
	{
		setElementIndex(this.segments.length - 1, elementIndex);
	}

	/**
	 * 设置指定位置的数组/集合元素索引。
	 * 
	 * @param index
	 * @param elementIndex
	 */
	public void setElementIndex(int index, int elementIndex)
	{
		Segment segment = this.segments[index];

		segment.setElementIndex(elementIndex);
	}

	/**
	 * 将此{@linkplain PropertyPath}连接至{@code parent}末尾。
	 * 
	 * @param parent
	 * @return
	 */
	public String concatTo(String parent)
	{
		if (parent == null || parent.isEmpty())
			return this.toString();
		else if (isPropertyHead())
			return parent + PROPERTY + this.toString();
		else if (isElementHead())
			return parent + this.toString();
		else
			throw new UnsupportedOperationException();
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < this.segments.length; i++)
		{
			Segment segment = this.segments[i];

			if (segment.isProperty() && i != 0)
				sb.append(PROPERTY);

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
		PropertyPath other = (PropertyPath) obj;
		if (!Arrays.equals(segments, other.segments))
			return false;
		return true;
	}

	/**
	 * 由字符串构建<i>属性值定位符</i>。
	 * 
	 * @param pvl
	 * @return
	 */
	public static PropertyPath valueOf(String pvl)
	{
		Segment[] segments = parse(pvl);
		return new PropertyPath(segments);
	}

	/**
	 * 解析字符串。
	 * 
	 * @param pp
	 * @return
	 */
	protected static Segment[] parse(String propertyPathStr)
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

			if (c == PROPERTY)
			{
				parseSegmentAndClear(propertyPathStr, segmentStrCache, i, segmentList);
			}
			// '['
			else if (c == ELEMENT_L)
			{
				parseSegmentAndClear(propertyPathStr, segmentStrCache, i, segmentList);

				segmentStrCache.append(c);

				for (i = i + 1; i < length; i++)
				{
					char cj = cs[i];

					if (isBlankChar(cj))
						continue;

					segmentStrCache.append(cj);

					if ((c == ELEMENT_L && cj == ELEMENT_R))
					{
						break;
					}
				}

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
	 * @param segmentList
	 * @throws IllegalPropertyPathException
	 */
	protected static void parseSegmentAndClear(String propertyPathStr, StringBuilder segmentStrCache,
			int segmentStrIndex, List<Segment> segmentList) throws IllegalPropertyPathException
	{
		int cacheLen = segmentStrCache.length();
		if (cacheLen > 0)
		{
			Segment segment = parseSegment(propertyPathStr, segmentStrCache.toString(), segmentStrIndex);

			// 不能有连续的元素属性路径
			if (segment.isElement())
			{
				Segment pre = (segmentList.isEmpty() ? null : segmentList.get(segmentList.size() - 1));

				if (pre != null && pre.isElement())
					throw new IllegalPropertyPathException(
							"[" + propertyPathStr + "] is illegal at [" + segmentStrIndex + "] index");
			}

			segmentList.add(segment);
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
	 */
	protected static Segment parseSegment(String propertyPathStr, String segmentStr, int segmentStrIndex)
	{
		int len = segmentStr.length();

		char c0 = segmentStr.charAt(0);
		char cl = segmentStr.charAt(len - 1);

		String value = segmentStr;

		if (c0 == ELEMENT_L && cl == ELEMENT_R)
		{
			if (len < 3)
				throw new IllegalPropertyPathException(
						"[" + propertyPathStr + "] is illegal at [" + segmentStrIndex + "] index");

			value = value.substring(1, len - 1);

			if (isIntegerString(value))
				return new Segment(Integer.parseInt(value));
			else
				throw new IllegalPropertyPathException(
						"[" + propertyPathStr + "] is illegal at [" + segmentStrIndex + "] index");
		}
		else
		{
			if (cl == CONCRETE_R)
			{
				int crtl = value.lastIndexOf(CONCRETE_L);

				if (crtl < 1 || crtl > len - 3)
					throw new IllegalPropertyPathException(
							"[" + propertyPathStr + "] is illegal at [" + segmentStrIndex + "] index");

				String strIndex = value.substring(crtl + 1, len - 1);

				if (isIntegerString(strIndex))
					return new Segment(value.substring(0, crtl), Integer.parseInt(strIndex));
				else
					throw new IllegalPropertyPathException(
							"[" + propertyPathStr + "] is illegal at [" + segmentStrIndex + "] index");
			}
			else
				return new Segment(value);
		}
	}

	/**
	 * 是否是整数字符串。
	 * 
	 * @param s
	 * @return
	 */
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
		/** 属性名 */
		private String propertyName = null;

		/** 属性具体模型索引 */
		private Integer propertyModelIndex = null;

		/** 数值索引 */
		private Integer elementIndex = null;

		public Segment(String propertyName)
		{
			super();
			this.propertyName = propertyName;
		}

		public Segment(String propertyName, int propertyModelIndex)
		{
			super();
			this.propertyName = propertyName;
			this.propertyModelIndex = propertyModelIndex;
		}

		public Segment(int elementIndex)
		{
			super();
			this.elementIndex = elementIndex;
		}

		public boolean isProperty()
		{
			return (this.propertyName != null);
		}

		public String getPropertyName()
		{
			return propertyName;
		}

		public void setPropertyName(String propertyName)
		{
			if (propertyName == null)
				throw new IllegalArgumentException("[propertyName] must not be null");

			this.propertyName = propertyName;
			this.elementIndex = null;
		}

		public boolean hasPropertyModelIndex()
		{
			return this.propertyModelIndex != null;
		}

		public int getPropertyModelIndex()
		{
			return this.propertyModelIndex;
		}

		public void setPropertyModelIndex(int propertyModelIndex)
		{
			if (!isProperty())
				throw new IllegalStateException("Not property segment");

			this.propertyModelIndex = propertyModelIndex;
		}

		public boolean isElement()
		{
			return (this.elementIndex != null);
		}

		public int getElementIndex()
		{
			return this.elementIndex;
		}

		public void setElementIndex(int elementIndex)
		{
			this.elementIndex = elementIndex;
			this.propertyName = null;
			this.propertyModelIndex = null;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((elementIndex == null) ? 0 : elementIndex.hashCode());
			result = prime * result + ((propertyModelIndex == null) ? 0 : propertyModelIndex.hashCode());
			result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
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
			if (elementIndex == null)
			{
				if (other.elementIndex != null)
					return false;
			}
			else if (!elementIndex.equals(other.elementIndex))
				return false;
			if (propertyModelIndex == null)
			{
				if (other.propertyModelIndex != null)
					return false;
			}
			else if (!propertyModelIndex.equals(other.propertyModelIndex))
				return false;
			if (propertyName == null)
			{
				if (other.propertyName != null)
					return false;
			}
			else if (!propertyName.equals(other.propertyName))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			if (isElement())
				return String.valueOf(ELEMENT_L) + this.elementIndex.toString() + String.valueOf(ELEMENT_R);
			else
			{
				if (hasPropertyModelIndex())
					return this.propertyName + String.valueOf(CONCRETE_L) + this.propertyModelIndex
							+ String.valueOf(CONCRETE_R);
				else
					return this.propertyName;
			}
		}
	}
}
