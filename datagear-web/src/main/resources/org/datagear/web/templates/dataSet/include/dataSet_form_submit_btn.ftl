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
数据集提交按钮页面片段

依赖：

-->
<div class="flex flex-grow-1">
	<div class="flex-shink-1"  style="flex-basis:50%;"></div>
	<div class="flex-grow-0 flex-shink-0 flex justify-content-center px-3 gap-2 white-space-nowrap">
		<#include "dataSet_form_preview.ftl">
		<p-button type="submit" label="<@spring.message code='save' />" class="hide-if-readonly"></p-button>
	</div>
	<div class="flex-shink-1 flex justify-content-start align-items-center" style="flex-basis:50%;">
		<#include "dataSet_form_setting.ftl">
	</div>
</div>