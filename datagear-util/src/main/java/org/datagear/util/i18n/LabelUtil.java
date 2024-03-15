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

package org.datagear.util.i18n;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 标签工具集。
 * 
 * @author datagear@163.com
 *
 */
public class LabelUtil
{
	/**
	 * 获取指定{@linkplain Locale}的匹配优先级字符串列表。
	 * <p>
	 * 比如，对于{@code "zh_CN"}的{@linkplain Locale}，返回列表为：
	 * </p>
	 * <code>
	 * <p>
	 * "zh_CN"<br>
	 * "zh"
	 * </p>
	 * </code>
	 * 
	 * @param locale
	 * @return
	 */
	public static List<String> getPriorityStringList(Locale locale)
	{
		List<String> re = null;

		WeakReference<List<String>> wr = LOCALE_STRING_PRIORITY_LIST.get(locale);
		re = (wr == null ? null : wr.get());

		if (re == null)
		{
			re = new ArrayList<String>(4);

			String l0 = locale.toString();
			String l1 = new Locale(locale.getLanguage(), locale.getCountry(), locale.getVariant()).toString();
			String l2 = new Locale(locale.getLanguage(), locale.getCountry()).toString();
			String l3 = new Locale(locale.getLanguage()).toString();

			re.add(l0);
			if (!l1.equals(l0))
				re.add(l1);
			if (!l2.equals(l1))
				re.add(l2);
			if (!l3.equals(l2))
				re.add(l3);

			LOCALE_STRING_PRIORITY_LIST.put(locale, new WeakReference<List<String>>(re));
		}

		return re;
	}

	private static final ConcurrentMap<Locale, WeakReference<List<String>>> LOCALE_STRING_PRIORITY_LIST = new ConcurrentHashMap<Locale, WeakReference<List<String>>>();

	/**
	 * 将{@linkplain Label}转换为指定{@linkplain Locale}的{@linkplain Label}。
	 * <p>
	 * 返回的{@linkplain Label#getValue()}是最匹配给定{@linkplain Locale}的值、{@linkplain Label#getLocaleValues()}则为{@code null}。
	 * </p>
	 * <p>
	 * 如果{@code label}参数为{@code null}，将直接返回{@code null}。
	 * </p>
	 * 
	 * @param label
	 *            允许为{@code null}
	 * @param locale
	 *            允许为{@code null}
	 * @return
	 */
	public static Label concrete(Label label, Locale locale)
	{
		if (label == null)
			return null;

		String value = label.getValue(locale);
		return new Label(value);
	}

	/**
	 * 将{@code from}的{@linkplain Labeled#getNameLabel()}、{@linkplain Labeled#getDescLabel()}转换为指定{@linkplain Locale}的{@linkplain Label}，
	 * 然后写入{@code to}中。
	 * 
	 * @param from
	 * @param to
	 * @param locale
	 *            允许为{@code null}
	 */
	public static void concrete(Labeled from, Labeled to, Locale locale)
	{
		to.setNameLabel(concrete(from.getNameLabel(), locale));
		to.setDescLabel(concrete(from.getDescLabel(), locale));
	}
}
