/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.web.freemarker;

import java.io.IOException;
import java.util.Map;

import org.datagear.analysis.support.JsonSupport;
import org.datagear.web.json.jackson.ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.core.Environment;
import freemarker.ext.util.WrapperTemplateModel;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

/**
 * 将参数输出为JSON的{@linkplain WriteJsonTemplateDirectiveModel}。
 * 
 * @author datagear@163.com
 *
 */
public class WriteJsonTemplateDirectiveModel implements TemplateDirectiveModel
{
	public static final String KEY_VAR = "var";
	public static final String KEY_BIG_NUMBER_TO_STRING = "bigNumberToString";
	public static final String KEY_ESCAPE_HTML = "escapeHtml";

	private ObjectMapperBuilder objectMapperBuilder;

	private ObjectMapper _objectMapper;
	private ObjectMapper _objectMapperForEscapeHtml;
	private ObjectMapper _objectMapperForBigNumberToString;
	private ObjectMapper _objectMapperForEscapeHtmlAndBigNumberToString;

	public WriteJsonTemplateDirectiveModel()
	{
		super();
	}

	public WriteJsonTemplateDirectiveModel(ObjectMapperBuilder objectMapperBuilder)
	{
		super();
		setObjectMapperBuilder(objectMapperBuilder);
	}

	public ObjectMapperBuilder getObjectMapperBuilder()
	{
		return objectMapperBuilder;
	}

	public void setObjectMapperBuilder(ObjectMapperBuilder objectMapperBuilder)
	{
		this.objectMapperBuilder = objectMapperBuilder;
		this._objectMapper = this.objectMapperBuilder.std().build();
		this._objectMapperForEscapeHtml = this.objectMapperBuilder.std().escapeHtml().build();
		this._objectMapperForBigNumberToString = this.objectMapperBuilder.std().bigNumberToString()
				.build();
		this._objectMapperForEscapeHtmlAndBigNumberToString = this.objectMapperBuilder.std().escapeHtml()
				.bigNumberToString().build();
		JsonSupport.disableAutoCloseTargetFeature(this._objectMapper);
		JsonSupport.disableAutoCloseTargetFeature(this._objectMapperForEscapeHtml);
		JsonSupport.disableAutoCloseTargetFeature(this._objectMapperForBigNumberToString);
		JsonSupport.disableAutoCloseTargetFeature(this._objectMapperForEscapeHtmlAndBigNumberToString);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
			throws TemplateException, IOException
	{
		TemplateModel var = (TemplateModel)params.get(KEY_VAR);
		TemplateBooleanModel escapeHtmlObj = (TemplateBooleanModel) params.get(KEY_ESCAPE_HTML);
		TemplateBooleanModel bigNumberToStringObj = (TemplateBooleanModel) params.get(KEY_BIG_NUMBER_TO_STRING);
		boolean escapeHtml = (escapeHtmlObj == null ? true : escapeHtmlObj.getAsBoolean());
		boolean bigNumberToString = (bigNumberToStringObj == null ? false : bigNumberToStringObj.getAsBoolean());

		Object obj = unwrap(var);

		try
		{
			if(escapeHtml && bigNumberToString)
			{
				this._objectMapperForEscapeHtmlAndBigNumberToString.writeValue(env.getOut(), obj);
			}
			else if(escapeHtml)
			{
				this._objectMapperForEscapeHtml.writeValue(env.getOut(), obj);
			}
			else if (bigNumberToString)
			{
				this._objectMapperForBigNumberToString.writeValue(env.getOut(), obj);
			}
			else
			{
				this._objectMapper.writeValue(env.getOut(), obj);
			}
		}
		catch (IOException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new TemplateException(t, env);
		}
	}

	/**
	 * 获取{@linkplain TemplateModel}的原始对象。
	 * <p>
	 * 由于freemarker的{@linkplain DeepUnwrap}处理存在循环引用的对象时会出现死循环，因而不能采用，
	 * 这里折中一下，要求此指令的参数对象必须是{@linkplain WrapperTemplateModel}。
	 * </p>
	 * 
	 * @param model
	 * @return
	 * @throws TemplateModelException
	 */
	protected Object unwrap(TemplateModel model) throws TemplateModelException
	{
		if (model == null)
			return null;

		if (model instanceof WrapperTemplateModel)
		{
			return ((WrapperTemplateModel) model).getWrappedObject();
		}

		throw new TemplateModelException("Cannot unwrap model of type " + model.getClass().getName());
	}

	/**
	 * 将对象包装为{@linkplain WrapperTemplateModel}，这样就可以使用此类对应的指令，并且能够处理循环引用对象。
	 * 
	 * @param obj
	 *            允许为{@code null}
	 * @return
	 */
	public static TemplateModel toWriteJsonTemplateModel(Object obj)
	{
		return new SimpleWrapperTemplateModel(obj);
	}

	protected static class SimpleWrapperTemplateModel implements WrapperTemplateModel
	{
		private Object object;

		public SimpleWrapperTemplateModel()
		{
			super();
		}

		public SimpleWrapperTemplateModel(Object object)
		{
			super();
			this.object = object;
		}

		public Object getObject()
		{
			return object;
		}

		public void setObject(Object object)
		{
			this.object = object;
		}

		@Override
		public Object getWrappedObject()
		{
			return object;
		}
	}
}
