/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.GenericTypeResolver;

/**
 * 通用{@linkplain DataImporter}。
 * <p>
 * 它使用{@linkplain #getDevotedDataImporters()}列表中的元素来支持导入，靠后的可用元素将优先被使用。
 * </p>
 * <p>
 * 如果没有可用的元素，{@linkplain #impt(Import)}将抛出{@linkplain UnsupportedImportException}异常。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class GenericDataImporter implements DataImporter<Import>
{
	private List<DevotedDataImporter<?>> devotedDataImporters;

	private transient List<Class<?>> _devotedDataImporterTypeParameters;

	public GenericDataImporter()
	{
		super();
	}

	public GenericDataImporter(List<DevotedDataImporter<?>> devotedDataImporters)
	{
		super();
		this.devotedDataImporters = devotedDataImporters;
		this._devotedDataImporterTypeParameters = resolveDevotedDataImporterTypeParameters(devotedDataImporters);
	}

	public List<DevotedDataImporter<?>> getDevotedDataImporters()
	{
		return devotedDataImporters;
	}

	public void setDevotedDataImporters(List<DevotedDataImporter<?>> devotedDataImporters)
	{
		this.devotedDataImporters = devotedDataImporters;
		this._devotedDataImporterTypeParameters = resolveDevotedDataImporterTypeParameters(devotedDataImporters);
	}

	@Override
	public ImportResult impt(Import impt) throws DataImportException
	{
		DevotedDataImporter<Import> devotedDataImporter = findDevotedDataImporter(impt);

		if (devotedDataImporter == null)
			throw new UnsupportedImportException();

		return devotedDataImporter.impt(impt);
	}

	/**
	 * 查找能支持指定{@linkplain Import}的{@linkplain DevotedDataImporter}。
	 * <p>
	 * 如果没有找到，将返回{@code null}。
	 * </p>
	 * 
	 * @param impt
	 * @return
	 */
	public DevotedDataImporter<Import> findDevotedDataImporter(Import impt)
	{
		if (this.devotedDataImporters == null)
			return null;

		if (impt == null)
			return null;

		Class<?> imptType = impt.getClass();

		for (int i = this.devotedDataImporters.size() - 1; i >= 0; i--)
		{
			Class<?> myImptType = this._devotedDataImporterTypeParameters.get(i);

			if (!myImptType.isAssignableFrom(imptType))
				continue;

			@SuppressWarnings("unchecked")
			DevotedDataImporter<Import> devotedDataImporter = (DevotedDataImporter<Import>) this.devotedDataImporters
					.get(i);

			if (devotedDataImporter.supports(impt))
				return devotedDataImporter;
		}

		return null;
	}

	/**
	 * 解析{@linkplain DevotedDataImporter}子类的类型参数。
	 * 
	 * @param devotedDataImporters
	 * @return
	 */
	public static List<Class<?>> resolveDevotedDataImporterTypeParameters(
			List<DevotedDataImporter<?>> devotedDataImporters)
	{
		if (devotedDataImporters == null)
			return null;

		List<Class<?>> tps = new ArrayList<Class<?>>(devotedDataImporters.size());

		for (int i = 0, len = devotedDataImporters.size(); i < len; i++)
		{
			Class<?> tp = resolveDevotedDataImporterTypeParameter(devotedDataImporters.get(i).getClass());

			tps.add(tp);
		}

		return tps;
	}

	/**
	 * 解析{@linkplain DevotedDataImporter}子类的类型参数。
	 * 
	 * @param subClass
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Class<?> resolveDevotedDataImporterTypeParameter(Class<? extends DevotedDataImporter> subClass)
	{
		Class<?> tp = GenericTypeResolver.resolveTypeArgument(subClass, DevotedDataImporter.class);

		return tp;
	}
}
