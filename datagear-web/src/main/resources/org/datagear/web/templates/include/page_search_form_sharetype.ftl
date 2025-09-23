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
<#--
"我的"、"分享的"、"全部"过滤搜索表单。
-->
<#assign DataPermissionSpec=statics['org.datagear.management.util.DataPermissionSpec']>
<#include "page_search_form_dropdown.ftl">
<script>
(function(po)
{
	po.searchFilterMenuItems =
	[
		{
			label: "<@spring.message code='searchFilter.all' />",
			value: "${DataPermissionSpec.DATA_FILTER_VALUE_ALL}"
		},
		{
			label: "<@spring.message code='searchFilter.mine' />",
			value: "${DataPermissionSpec.DATA_FILTER_VALUE_MINE}"
		},
		{
			label: "<@spring.message code='searchFilter.other' />",
			value: "${DataPermissionSpec.DATA_FILTER_VALUE_OTHER}"
		}
	];
	
	po.initDropdownFilterSearchForm(po.searchFilterMenuItems);
})
(${pid});
</script>
