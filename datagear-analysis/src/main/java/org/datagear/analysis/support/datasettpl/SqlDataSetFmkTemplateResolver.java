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

package org.datagear.analysis.support.datasettpl;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetParam;

import freemarker.core.OutputFormat;
import freemarker.ext.util.WrapperTemplateModel;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.Configuration;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;

/**
 * SQL {@linkplain DataSetFmkTemplateResolver}。
 * <p>
 * 此类为底层Freemarker自定义了{@code pcp}函数，用于支持预编译的SQL。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSetFmkTemplateResolver extends DataSetFmkTemplateResolver
{
	/**
	 * 预编译SQL参数的方法名。
	 * <p>
	 * 注意：不要修改此值，因为会导致已定义的SQL模板无法使用。
	 * </p>
	 */
	public static final String PRECOMPILE_METHOD_NAME = "pc";

	public SqlDataSetFmkTemplateResolver()
	{
		super(SqlOutputFormat.INSTANCE);
	}

	public SqlDataSetFmkTemplateResolver(int cacheCapacity)
	{
		super(SqlOutputFormat.INSTANCE, cacheCapacity);
	}

	public SqlDataSetFmkTemplateResolver(OutputFormat outputFormat, int cacheCapacity)
	{
		super(outputFormat, cacheCapacity);
	}

	@Override
	protected void setConfiguration(Configuration configuration)
	{
		super.setConfiguration(configuration);
		configuration = super.getConfiguration();
		configuration.setSharedVariable(PRECOMPILE_METHOD_NAME, new SqlPreCompileTemplateMethodModel());
	}

	@Override
	public SqlTemplateResult resolve(String template, Map<String, ?> paramValues) throws TemplateResolverException
	{
		return (SqlTemplateResult) super.resolve(template, paramValues);
	}

	@Override
	public SqlTemplateResult resolve(String template, TemplateContext templateContext) throws TemplateResolverException
	{
		return (SqlTemplateResult) super.resolve(template, templateContext);
	}

	@Override
	protected SqlTemplateResult buildTemplateResult(String template, String result, TemplateHolder templateHolder)
	{
		SqlTemplateHolder sh = (SqlTemplateHolder) templateHolder;
		return new SqlTemplateResult(result, sh.isPrecompiled(), sh.getParamValues());
	}

	@Override
	protected TemplateHolder buildTemplateHolder(String template, TemplateContext templateContext)
	{
		return new SqlTemplateHolder(template);
	}

	protected static class SqlTemplateHolder extends TemplateHolder
	{
		/**
		 * 是否预编译语法。
		 * <p>
		 * 此字段不应计入{@linkplain #hashCode()}、{@linkplain #equals(Object)}
		 * </p>
		 */
		private transient boolean precompiled = false;
		
		/**
		 * 预编译参数。
		 * <p>
		 * 此字段不应计入{@linkplain #hashCode()}、{@linkplain #equals(Object)}
		 * </p>
		 */
		private transient List<Object> paramValues = new ArrayList<>();

		public SqlTemplateHolder(String template)
		{
			super(template);
		}

		public boolean isPrecompiled()
		{
			return precompiled;
		}

		public void setPrecompiled(boolean precompiled)
		{
			this.precompiled = precompiled;
		}

		public List<Object> getParamValues()
		{
			return paramValues;
		}

		public void setParamValues(List<Object> paramValues)
		{
			this.paramValues = paramValues;
		}

		public void addParamValue(Object pv)
		{
			this.paramValues.add(pv);
		}

		@Override
		public int hashCode()
		{
			return super.hashCode();
		}

		@Override
		public boolean equals(Object obj)
		{
			return super.equals(obj);
		}
	}

	protected static class SqlPreCompileTemplateMethodModel implements TemplateMethodModelEx
	{
		/**
		 * 预编译执行结果，应始终是{@linkplain PreparedStatement}规范中的预编译占位符{@code ?}。
		 */
		public static final String PRECOMILE_RESULT = "?";

		public SqlPreCompileTemplateMethodModel()
		{
			super();
		}

		@Override
		@SuppressWarnings("rawtypes")
		public Object exec(List arguments) throws TemplateModelException
		{
			if (arguments == null || arguments.size() != 1)
				throw new TemplateModelException(
						"Unsupported arguments length : " + (arguments == null ? 0 : arguments.size()));

			try
			{
				SqlTemplateHolder sth = (SqlTemplateHolder) ThreadLocaleTemplateLoader.getTemplate();

				if (!sth.isPrecompiled())
					sth.setPrecompiled(true);

				Object pv = extractRaw(arguments.get(0));
				sth.addParamValue(pv);

				return PRECOMILE_RESULT;
			}
			catch (TemplateModelException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				throw new TemplateModelException(e);
			}
		}

		/**
		 * 解析Freemarker语境的{@linkplain TemplateModel}的原始数据对象。
		 * <p>
		 * 此方法需支持{@linkplain DataSetParam.DataType}中定义的相关类型。
		 * </p>
		 * 
		 * @param model
		 * @return
		 * @throws TemplateModelException
		 */
		protected Object extractRaw(Object model) throws TemplateModelException
		{
			if (model == null)
				return model;

			Object raw;

			// 字符串
			if (model instanceof SimpleScalar)
			{
				raw = ((SimpleScalar) model).getAsString();
			}
			// 数值
			else if (model instanceof TemplateNumberModel)
			{
				raw = ((TemplateNumberModel) model).getAsNumber();
			}
			// 布尔值
			else if (model instanceof TemplateBooleanModel)
			{
				raw = ((TemplateBooleanModel) model).getAsBoolean();
			}
			// 数组、集合、Bean、Map
			else if (model instanceof WrapperTemplateModel)
			{
				raw = ((WrapperTemplateModel) model).getWrappedObject();
			}
			// 数组、集合、Bean、Map
			else if (model instanceof AdapterTemplateModel)
			{
				raw = ((AdapterTemplateModel) model).getAdaptedObject(Object.class);
			}
			// 其他标量
			else if (model instanceof TemplateScalarModel)
			{
				raw = ((TemplateScalarModel) model).getAsString();
			}
			else
			{
				raw = model;
			}

			return raw;
		}
	}
}
