/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.GenericTypeResolver;

/**
 * 通用{@linkplain DataExchangeService}。
 * <p>
 * 它使用{@linkplain #getDevotedDataExchangeServices()}列表中的元素来支持数据交换，靠后的可用元素将优先被使用。
 * </p>
 * <p>
 * 如果没有可用的元素，{@linkplain #exchange(DataExchange)}将抛出{@linkplain UnsupportedExchangeException}异常。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class GenericDataExchangeService implements DataExchangeService<DataExchange>
{
	private List<DevotedDataExchangeService<?>> devotedDataExchangeServices;

	private transient List<Class<?>> _devotedDataExchangeServiceTypeParameters;

	public GenericDataExchangeService()
	{
		super();
	}

	public GenericDataExchangeService(List<DevotedDataExchangeService<?>> devotedDataExchangeServices)
	{
		super();
		this.devotedDataExchangeServices = devotedDataExchangeServices;
		this._devotedDataExchangeServiceTypeParameters = resolveDevotedDataExchangeServiceTypeParameters(
				devotedDataExchangeServices);
	}

	public List<DevotedDataExchangeService<?>> getDevotedDataExchangeServices()
	{
		return devotedDataExchangeServices;
	}

	public void setDevotedDataExchangeServices(List<DevotedDataExchangeService<?>> devotedDataExchangeServices)
	{
		this.devotedDataExchangeServices = devotedDataExchangeServices;
		this._devotedDataExchangeServiceTypeParameters = resolveDevotedDataExchangeServiceTypeParameters(
				devotedDataExchangeServices);
	}

	@Override
	public void exchange(DataExchange dataExchange) throws DataExchangeException
	{
		DevotedDataExchangeService<DataExchange> devotedDataExchangeService = findDevotedDataExchangeService(
				dataExchange);

		if (devotedDataExchangeService == null)
			throw new UnsupportedExchangeException();

		devotedDataExchangeService.exchange(dataExchange);
	}

	/**
	 * 查找能支持指定{@linkplain DataExport}的{@linkplain DevotedDataExporter}。
	 * <p>
	 * 如果没有找到，将返回{@code null}。
	 * </p>
	 * 
	 * @param dataExchange
	 * @return
	 */
	public DevotedDataExchangeService<DataExchange> findDevotedDataExchangeService(DataExchange dataExchange)
	{
		if (this.devotedDataExchangeServices == null)
			return null;

		if (dataExchange == null)
			return null;

		Class<?> exptType = dataExchange.getClass();

		for (int i = this.devotedDataExchangeServices.size() - 1; i >= 0; i--)
		{
			Class<?> myImptType = this._devotedDataExchangeServiceTypeParameters.get(i);

			if (!myImptType.isAssignableFrom(exptType))
				continue;

			@SuppressWarnings("unchecked")
			DevotedDataExchangeService<DataExchange> devotedDataExporter = (DevotedDataExchangeService<DataExchange>) this.devotedDataExchangeServices
					.get(i);

			if (devotedDataExporter.supports(dataExchange))
				return devotedDataExporter;
		}

		return null;
	}

	/**
	 * 解析{@linkplain DevotedDataExchangeService}子类的类型参数。
	 * 
	 * @param devotedDataExchangeServices
	 * @return
	 */
	public static List<Class<?>> resolveDevotedDataExchangeServiceTypeParameters(
			List<DevotedDataExchangeService<?>> devotedDataExchangeServices)
	{
		if (devotedDataExchangeServices == null)
			return null;

		List<Class<?>> tps = new ArrayList<Class<?>>(devotedDataExchangeServices.size());

		for (int i = 0, len = devotedDataExchangeServices.size(); i < len; i++)
		{
			Class<?> tp = resolveDevotedDataExchangeServiceTypeParameter(devotedDataExchangeServices.get(i).getClass());

			tps.add(tp);
		}

		return tps;
	}

	/**
	 * 解析{@linkplain DevotedDataExchangeService}子类的类型参数。
	 * 
	 * @param subClass
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Class<?> resolveDevotedDataExchangeServiceTypeParameter(
			Class<? extends DevotedDataExchangeService> subClass)
	{
		Class<?> tp = GenericTypeResolver.resolveTypeArgument(subClass, DevotedDataExchangeService.class);

		return tp;
	}
}
