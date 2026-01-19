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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 可本地化对象。
 * 
 * @author datagear@163.com
 *
 */
public interface Localizable
{
	/**
	 * 本地化。
	 * 
	 * @param <T>
	 * @param locale
	 * @return
	 */
	Localizable toLocale(Locale locale);

	/**
	 * 转换为指定{@linkplain Locale}下的{@linkplain Localizable}列表。
	 * 
	 * @param localizeables
	 * @param locale
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static <T extends Localizable> List<T> toLocale(Collection<T> localizeables, Locale locale)
	{
		if (localizeables == null)
			return null;

		if (localizeables.isEmpty())
			return Collections.emptyList();

		List<T> re = new ArrayList<>(localizeables.size());

		for (T localizeable : localizeables)
			re.add(localizeable == null ? null : (T) localizeable.toLocale(locale));

		return re;
	}
}
