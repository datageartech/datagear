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

package org.datagear.web.analysis;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.datagear.analysis.DataSetQuery;
import org.junit.Test;

/**
 * {@linkplain DataSetQueryParams}单元测试。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetQueryParamsTest
{
	private DataSetQueryParams dataSetQueryParams = new DataSetQueryParams();

	@Test
	public void setParamValuesTest()
	{
		List<AnalysisRole> roles = new ArrayList<>();
		roles.add(new AnalysisRole("role-0", "role0", true));
		roles.add(new AnalysisRole("role-1", "role0", true));
		
		AnalysisUser user = new AnalysisUser("id", "name", "realName", false, false, roles);
		List<String> roleNames = user.getEnabledRoleNames();
		DataSetQuery query = DataSetQuery.valueOf();
		dataSetQueryParams.setParamValues(query, user, roleNames, LocalDateTime.of(2026, 3, 6, 13, 4, 16));

		assertEquals(user, query.getParamValue("DG_USER"));
		assertEquals(roleNames, query.getParamValue("DG_ROLE_NAMES"));
		assertEquals("2026-03-06 13:04:16", query.getParamValue("DG_DATETIME"));
		assertEquals("2026-03-06", query.getParamValue("DG_DATE"));
	}
}
