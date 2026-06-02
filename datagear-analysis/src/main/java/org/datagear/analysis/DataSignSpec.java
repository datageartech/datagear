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

package org.datagear.analysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.datagear.util.i18n.Localizable;

/**
 * 数据标记规范，定义{@linkplain ChartPlugin}的{@linkplain DataSign}规范。
 * 
 * @author datagear@163.com
 *
 */
public class DataSignSpec implements AdditionsAware, Localizable, Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_DATA_SIGNS = "dataSigns";
	public static final String PROPERTY_ADDITIONS = AdditionsAware.PROPERTY_ADDITIONS;

	/** 数据标记 */
	private List<DataSign> dataSigns = Collections.emptyList();

	/** 附加属性 */
	private Map<String, ?> additions = null;

	public DataSignSpec()
	{
		super();
	}

	public DataSignSpec(List<DataSign> dataSigns)
	{
		super();
		this.dataSigns = dataSigns;
	}

	public List<DataSign> getDataSigns()
	{
		return dataSigns;
	}

	public void setDataSigns(List<DataSign> dataSigns)
	{
		this.dataSigns = dataSigns;
	}

	@Override
	public Map<String, ?> getAdditions()
	{
		return additions;
	}

	public void setAdditions(Map<String, ?> additions)
	{
		this.additions = additions;
	}

	@Override
	public DataSignSpec toLocale(Locale locale)
	{
		DataSignSpec target = createEmpty();

		if (this.dataSigns != null && !this.dataSigns.isEmpty())
		{
			List<DataSign> tss = new ArrayList<>();

			for (DataSign dataSign : this.dataSigns)
				tss.add(dataSign.toLocale(locale));

			target.setDataSigns(tss);
		}

		target.setAdditions(this.additions);

		return target;
	}

	protected DataSignSpec createEmpty()
	{
		return new DataSignSpec();
	}
}
