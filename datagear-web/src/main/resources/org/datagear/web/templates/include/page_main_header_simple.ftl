<#--
 *
 * Copyright 2018-2024 datagear.tech
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
简单页头。
-->
<div id="${pid}mainHeader" class="page-main-header flex-grow-0 p-card no-border text-primary py-1 border-noround-top border-noround-bottom">
	<div class="grid grid-nogutter align-items-center">
		<div id="sysLogoWrapper" class="logo-wrapper header-left col-fixed flex align-items-center pl-1">
			<#include "html_logo.ftl">
		</div>
		<div class="col text-right pr-2">
			<div class="header-right flex justify-content-end align-items-center">
				<#-- 这里保留占位元素但设为透明，为了与page_main_header.ftl高度保持一致 -->
				<div class="mr-1 opacity-0">
					<p-button type="button" icon="pi pi-cog" class="p-button-sm p-button-text p-button-rounded text-primary cursor-auto">
					</p-button>
				</div>
				<div>
					<a href="${contextPath}/" class="link text-primary px-1"><@spring.message code='module.main' /></a>
				</div>
			</div>
		</div>
	</div>
</div>