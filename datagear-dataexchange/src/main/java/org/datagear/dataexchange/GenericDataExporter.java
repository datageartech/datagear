/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.GenericTypeResolver;

/**
 * 通用{@linkplain DataExporter}。
 * <p>
 * 它使用{@linkplain #getDevotedDataExporters()}列表中的元素来支持导出，靠后的可用元素将优先被使用。
 * </p>
 * <p>
 * 如果没有可用的元素，{@linkplain #expt(Export)}将抛出{@linkplain UnsupportedExportException}异常。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class GenericDataExporter implements DataExporter<Export>
{
	private List<DevotedDataExporter<?>> devotedDataExporters;

	private transient List<Class<?>> _devotedDataExporterTypeParameters;

	public GenericDataExporter()
	{
		super();
	}

	public GenericDataExporter(List<DevotedDataExporter<?>> devotedDataExporters)
	{
		super();
		this.devotedDataExporters = devotedDataExporters;
		this._devotedDataExporterTypeParameters = resolveDevotedDataExporterTypeParameters(devotedDataExporters);
	}

	public List<DevotedDataExporter<?>> getDevotedDataExporters()
	{
		return devotedDataExporters;
	}

	public void setDevotedDataExporters(List<DevotedDataExporter<?>> devotedDataExporters)
	{
		this.devotedDataExporters = devotedDataExporters;
		this._devotedDataExporterTypeParameters = resolveDevotedDataExporterTypeParameters(devotedDataExporters);
	}

	@Override
	public ExportResult expt(Export expt) throws DataExportException
	{
		DevotedDataExporter<Export> devotedDataExporter = findDevotedDataExporter(expt);

		if (devotedDataExporter == null)
			throw new UnsupportedExportException();

		return devotedDataExporter.expt(expt);
	}

	/**
	 * 查找能支持指定{@linkplain Export}的{@linkplain DevotedDataExporter}。
	 * <p>
	 * 如果没有找到，将返回{@code null}。
	 * </p>
	 * 
	 * @param expt
	 * @return
	 */
	public DevotedDataExporter<Export> findDevotedDataExporter(Export expt)
	{
		if (this.devotedDataExporters == null)
			return null;

		if (expt == null)
			return null;

		Class<?> exptType = expt.getClass();

		for (int i = this.devotedDataExporters.size() - 1; i >= 0; i--)
		{
			Class<?> myImptType = this._devotedDataExporterTypeParameters.get(i);

			if (!myImptType.isAssignableFrom(exptType))
				continue;

			@SuppressWarnings("unchecked")
			DevotedDataExporter<Export> devotedDataExporter = (DevotedDataExporter<Export>) this.devotedDataExporters
					.get(i);

			if (devotedDataExporter.supports(expt))
				return devotedDataExporter;
		}

		return null;
	}

	/**
	 * 解析{@linkplain DevotedDataExporter}子类的类型参数。
	 * 
	 * @param devotedDataExporters
	 * @return
	 */
	public static List<Class<?>> resolveDevotedDataExporterTypeParameters(
			List<DevotedDataExporter<?>> devotedDataExporters)
	{
		if (devotedDataExporters == null)
			return null;

		List<Class<?>> tps = new ArrayList<Class<?>>(devotedDataExporters.size());

		for (int i = 0, len = devotedDataExporters.size(); i < len; i++)
		{
			Class<?> tp = resolveDevotedDataExporterTypeParameter(devotedDataExporters.get(i).getClass());

			tps.add(tp);
		}

		return tps;
	}

	/**
	 * 解析{@linkplain DevotedDataExporter}子类的类型参数。
	 * 
	 * @param subClass
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Class<?> resolveDevotedDataExporterTypeParameter(Class<? extends DevotedDataExporter> subClass)
	{
		Class<?> tp = GenericTypeResolver.resolveTypeArgument(subClass, DevotedDataExporter.class);

		return tp;
	}
}
