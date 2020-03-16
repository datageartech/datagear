/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model.support;

import java.util.List;

import org.datagear.model.Model;
import org.datagear.model.Property;

/**
 * 属性路径信息。
 * 
 * @author datagear@163.com
 *
 */
public class PropertyPathInfo
{
	/** 对应模型 */
	private Model model;

	/** 对应的属性路径对象 */
	private PropertyPath propertyPath;

	/** 对应数据对象 */
	private Object obj;

	private Segment[] segments;

	private PropertyPathInfo()
	{
		super();
	}

	private PropertyPathInfo(Model model, PropertyPath propertyPath, Object obj, Segment[] segments)
	{
		super();
		this.model = model;
		this.propertyPath = propertyPath;
		this.obj = obj;
		this.segments = segments;
	}

	/**
	 * 获取对应的{@linkplain Model}。
	 * 
	 * @return
	 */
	public Model getModel()
	{
		return model;
	}

	/**
	 * 获取对应的{@linkplain PropertyPath}。
	 * 
	 * @return
	 */
	public PropertyPath getPropertyPath()
	{
		return propertyPath;
	}

	public boolean hasObj()
	{
		return (this.obj != null);
	}

	/**
	 * 获取对应数据对象。
	 * 
	 * @return
	 */
	public Object getObj()
	{
		return obj;
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
		return this.segments[index].isProperty();
	}

	/**
	 * 获取末尾位置的{@linkplain Property}。
	 * 
	 * @return
	 */
	public Property getPropertyTail()
	{
		return getProperty(this.segments.length - 1);
	}

	/**
	 * 判断开头位置是否是集合/数组属性路径。
	 * 
	 * @return
	 */
	public boolean isElementHead()
	{
		return isElement(0);
	}

	/**
	 * 判断末尾位置是否是集合/数组属性路径。
	 * 
	 * @return
	 */
	public boolean isElementTail()
	{
		return isElement(this.segments.length - 1);
	}

	/**
	 * 判断给定位置是否是集合/数组属性路径。
	 * 
	 * @param index
	 * @return
	 */
	public boolean isElement(int index)
	{
		return this.segments[index].isElement();
	}

	/**
	 * 获取开头位置的{@linkplain Property}。
	 * 
	 * @return
	 */
	public Property getPropertyHead()
	{
		return getProperty(0);
	}

	/**
	 * 获取指定位置的{@linkplain Property}。
	 * <p>
	 * 如果{@code index=0}且{@linkplain #isElement(int)}为{@code true}，则返回{@code null}；<br>
	 * 如果{@linkplain #isProperty(int)}为{@code true}，则返回此{@code index}位置的{@linkplain Property}；<br>
	 * 如果{@linkplain #isElement(int)}为{@code true}，则返回{@code index-1}位置的{@linkplain #getProperty(int)}。
	 * </p>
	 * 
	 * @param index
	 * @return
	 */
	public Property getProperty(int index)
	{
		Segment segment = this.segments[index];

		if (segment.isProperty())
			return segment.getProperty();
		else
		{
			if (index == 0)
				return null;

			return getProperty(index - 1);
		}
	}

	/**
	 * 获取末尾位置的所属{@linkplain Model}。
	 * 
	 * @return
	 */
	public Model getOwnerModelTail()
	{
		return getOwnerModel(this.segments.length - 1);
	}

	/**
	 * 获取给定位置的所属{@linkplain Model}。
	 * <p>
	 * 如果{@code index=0}且{@linkplain #isElement(int)}为{@code true}，则返回{@code null}；<br>
	 * 如果{@linkplain #isProperty(int)}为{@code true}，则返回此{@code index}位置属性所属的{@linkplain Model}；<br>
	 * 如果{@linkplain #isElement(int)}为{@code true}，则返回{@code index-1}位置的{@linkplain #getOwnerModel(int)}。
	 * </p>
	 * 
	 * @param index
	 * @return
	 */
	public Model getOwnerModel(int index)
	{
		Segment segment = this.segments[index];

		if (segment.isProperty())
		{
			if (index == 0)
				return this.model;
			else
				return this.segments[index - 1].getModel();
		}
		else
		{
			if (index == 0)
				return null;

			return getOwnerModel(index - 1);
		}
	}

	/**
	 * 判断末尾位置的{@linkplain #hasOwnerObj(int)}。
	 * 
	 * @return
	 */
	public boolean hasOwnerObjTail()
	{
		return hasOwnerObj(this.segments.length - 1);
	}

	/**
	 * 判断给定位置的{@linkplain #getOwnerObj(int)}是否为{@code null}。
	 * 
	 * @param index
	 * @return
	 */
	public boolean hasOwnerObj(int index)
	{
		return (this.getOwnerObj(index) != null);
	}

	/**
	 * 获取末尾位置的所属对象。
	 * 
	 * @return
	 */
	public Object getOwnerObjTail()
	{
		return getOwnerObj(this.segments.length - 1);
	}

	/**
	 * 获取给定位置的所属对象。
	 * <p>
	 * 如果{@code index=0}且{@linkplain #isElement(int)}为{@code true}，则返回{@code null}；<br>
	 * 如果{@linkplain #isProperty(int)}为{@code true}，则返回此{@code index}位置属性所属的JavaBean对象；<br>
	 * 如果{@linkplain #isElement(int)}为{@code true}，则返回{@code index-1}位置的{@linkplain #getOwnerObj(int)}。
	 * </p>
	 * 
	 * @param index
	 * @return
	 */
	public Object getOwnerObj(int index)
	{
		Segment segment = this.segments[index];

		if (segment.isProperty())
		{
			if (index == 0)
				return this.obj;
			else
				return this.segments[index - 1].getValue();
		}
		else
		{
			if (index == 0)
				return null;

			return getOwnerObj(index - 1);
		}
	}

	/**
	 * 判断开头位置的{@linkplain #hasModel(int)}。
	 * 
	 * @return
	 */
	public boolean hasModelhead()
	{
		return hasModel(0);
	}

	/**
	 * 判断末尾位置的{@linkplain #hasModel(int)}。
	 * 
	 * @return
	 */
	public boolean hasModelTail()
	{
		return hasModel(this.segments.length - 1);
	}

	/**
	 * 判断给定位置的{@linkplain #getModel(int)}是否为{@code null}。
	 * 
	 * @param index
	 * @return
	 */
	public boolean hasModel(int index)
	{
		return (this.segments[index].getModel() != null);
	}

	/**
	 * 获取开头位置的{@linkplain Model}。
	 * 
	 * @return
	 */
	public Model getModelHead()
	{
		return getModel(0);
	}

	/**
	 * 获取末尾位置的{@linkplain Model}。
	 * 
	 * @return
	 */
	public Model getModelTail()
	{
		return getModel(this.segments.length - 1);
	}

	/**
	 * 获取给定位置的{@linkplain Model}。
	 * 
	 * @param index
	 * @return
	 */
	public Model getModel(int index)
	{
		return this.segments[index].getModel();
	}

	/**
	 * 判断开头位置是否有值。
	 * 
	 * @return
	 */
	public boolean hasValueHead()
	{
		return hasValue(0);
	}

	/**
	 * 判断末尾位置是否有值。
	 * 
	 * @return
	 */
	public boolean hasValueTail()
	{
		return hasValue(this.segments.length - 1);
	}

	/**
	 * 判断给定位置是否有值。
	 * 
	 * @param index
	 * @return
	 */
	public boolean hasValue(int index)
	{
		return (this.segments[index].getValue() != null);
	}

	/**
	 * 获取开头位置的值。
	 * 
	 * @return
	 */
	public Object getValueHead()
	{
		return getValue(0);
	}

	/**
	 * 获取末尾位置的值。
	 * 
	 * @return
	 */
	public Object getValueTail()
	{
		return getValue(this.segments.length - 1);
	}

	/**
	 * 获取给定位置的值。
	 * 
	 * @param index
	 * @return
	 */
	public Object getValue(int index)
	{
		return this.segments[index].getValue();
	}

	/**
	 * 设置开头位置的值。
	 * 
	 * @param value
	 * @throws NullPointerException
	 *             参考{@linkplain #setValue(int, Object)}
	 */
	public void setValueHead(Object value) throws NullPointerException
	{
		setValue(0, value);
	}

	/**
	 * 设置末尾位置的值。
	 * 
	 * @param value
	 * @throws NullPointerException
	 *             参考{@linkplain #setValue(int, Object)}
	 */
	public void setValueTail(Object value) throws NullPointerException
	{
		setValue(this.segments.length - 1, value);
	}

	/**
	 * 设置给定位置的值。
	 * 
	 * @param index
	 * @param value
	 * @throws NullPointerException
	 *             当{@code index-1}位置的{@linkplain #getValue(int)}为{@code null}时{@linkplain #getObj()}为{@code null}时。
	 */
	@SuppressWarnings("unchecked")
	public void setValue(int index, Object value) throws NullPointerException
	{
		Segment segment = this.segments[index];

		Object ownerObj = null;

		if (index == 0)
			ownerObj = this.obj;
		else
			ownerObj = this.segments[index - 1].value;

		if (ownerObj == null)
			throw new NullPointerException(
					"The " + (index > 0 ? (index - 1) + "-th value" : "#getObj()") + " must not be null");

		segment.setValue(value);

		if (segment.isProperty())
		{
			segment.getProperty().set(ownerObj, value);
		}
		else
		{
			int elementIndex = this.propertyPath.getElementIndex(index);

			if (ownerObj instanceof Object[])
				((Object[]) ownerObj)[elementIndex] = value;
			else if (ownerObj instanceof List<?>)
				((List<Object>) ownerObj).set(elementIndex, value);
			else
				throw new UnsupportedOperationException(
						"Set element value of [" + ownerObj.getClass().getSimpleName() + "] type is not supported");
		}
	}

	/**
	 * 构建{@linkplain PropertyPathInfo}。
	 * 
	 * @param model
	 * @param propertyPath
	 * @return
	 */
	public static PropertyPathInfo valueOf(Model model, String propertyPath)
	{
		return valueOf(model, PropertyPath.valueOf(propertyPath), null);
	}

	/**
	 * 构建{@linkplain PropertyPathInfo}。
	 * 
	 * @param model
	 * @param propertyPath
	 * @return
	 */
	public static PropertyPathInfo valueOf(Model model, PropertyPath propertyPath)
	{
		return valueOf(model, propertyPath, null);
	}

	/**
	 * 构建{@linkplain PropertyPathInfo}。
	 * 
	 * @param model
	 * @param propertyPath
	 * @param obj
	 * @return
	 */
	public static PropertyPathInfo valueOf(Model model, String propertyPath, Object obj)
	{
		return valueOf(model, PropertyPath.valueOf(propertyPath), obj);
	}

	/**
	 * 构建{@linkplain PropertyPathInfo}。
	 * 
	 * @param model
	 * @param propertyPath
	 * @param obj
	 *            允许为{@code null}
	 * @return
	 */
	public static PropertyPathInfo valueOf(Model model, PropertyPath propertyPath, Object obj)
	{
		Segment[] segments = new Segment[propertyPath.length()];

		Model parentModel = model;
		Object parentObj = obj;

		for (int i = 0, len = propertyPath.length(); i < len; i++)
		{
			if (parentModel == null)
				throw new IllegalArgumentException(
						"The parent Model for the " + i + "-th property of [" + propertyPath + "] must not be null");

			Segment segment = null;

			Property segmentProperty = null;
			Model segmentModel = null;
			Object segmentValue = null;

			if (propertyPath.isProperty(i))
			{
				String pname = propertyPath.getPropertyName(i);

				segmentProperty = parentModel.getProperty(pname);

				if (segmentProperty == null)
					throw new IllegalArgumentException("The " + i + "-th property [" + pname + "] of [" + propertyPath
							+ "] is not found in Model [" + parentModel + "]");

				segmentModel = segmentProperty.getModel();
				segmentValue = (parentObj == null ? null : segmentProperty.get(parentObj));

				segment = Segment.propertySegment(segmentProperty, segmentModel, segmentValue);
			}
			else if (propertyPath.isElement(i))
			{
				if (i > 0 && segments[i - 1].isElement())
					throw new IllegalArgumentException("The " + i + "-th property of [" + propertyPath
							+ "] must not be element property path, sequential element property path is not allowed");

				if (parentObj != null)
				{
					if (parentObj instanceof Object[])
					{
						Object[] parentObjArray = (Object[]) parentObj;

						int valueIndex = propertyPath.getElementIndex(i);

						if (valueIndex < 0 || valueIndex >= parentObjArray.length)
							segmentValue = null;
						else
							segmentValue = parentObjArray[valueIndex];
					}
					else if (parentObj instanceof List<?>)
					{
						List<?> parentObjList = (List<?>) parentObj;

						int valueIndex = propertyPath.getElementIndex(i);

						if (valueIndex < 0 || valueIndex >= parentObjList.size())
							segmentValue = null;
						else
							segmentValue = ((List<?>) parentObjList).get(valueIndex);
					}
					else
						throw new IllegalArgumentException("The parent object for the " + i + "-th property of ["
								+ propertyPath + "] must be array or List");
				}

				segmentModel = parentModel;

				segment = Segment.elementSegment(segmentModel, segmentValue);
			}
			else
				throw new UnsupportedOperationException();

			segments[i] = segment;

			parentModel = segmentModel;
			parentObj = segmentValue;
		}

		return new PropertyPathInfo(model, propertyPath, obj, segments);
	}

	protected static class Segment
	{
		/** 属性 */
		private Property property;

		/** 模型 */
		private Model model;

		/** 值 */
		private Object value;

		private Segment()
		{
			super();
		}

		public boolean isProperty()
		{
			return (this.property != null);
		}

		public boolean isElement()
		{
			return (this.property == null);
		}

		public Property getProperty()
		{
			return property;
		}

		public void setProperty(Property property)
		{
			this.property = property;
		}

		public Model getModel()
		{
			return model;
		}

		public void setModel(Model model)
		{
			this.model = model;
		}

		public Object getValue()
		{
			return value;
		}

		public void setValue(Object value)
		{
			this.value = value;
		}

		/**
		 * 创建JavaBean属性{@linkplain Segment}。
		 * 
		 * @param property
		 * @param model
		 * @param value
		 * @return
		 */
		public static Segment propertySegment(Property property, Model model, Object value)
		{
			if (property == null)
				throw new IllegalArgumentException("[property] must not be null");

			Segment segment = new Segment();
			segment.setProperty(property);
			segment.setModel(model);
			segment.setValue(value);

			return segment;
		}

		/**
		 * 创建元素{@linkplain Segment}。
		 * 
		 * @param model
		 * @param value
		 * @return
		 */
		public static Segment elementSegment(Model model, Object value)
		{
			Segment segment = new Segment();
			segment.setModel(model);
			segment.setValue(value);

			return segment;
		}
	}
}
