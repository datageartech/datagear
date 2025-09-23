<#--
 *
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
 *
-->
<#assign DashboardApiVersion=statics['org.datagear.web.analysis.DashboardApiVersion']>
<#include "../../include/page_search_form_dropdown.ftl">
<script>
(function(po)
{
	po.searchFilterMenuItems =
	[
		{
			label: "${DashboardApiVersion.V2}",
			value: "${DashboardApiVersion.V2}"
		},
		{
			label: "${DashboardApiVersion.V1}",
			value: "${DashboardApiVersion.V1}"
		}
	];
	
	po.initDropdownFilterSearchForm(po.searchFilterMenuItems, { dropdownBtnTitle: "<@spring.message code='apiVersion' />" });
})
(${pid});
</script>
