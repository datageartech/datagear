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
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='module.dtbsSource' />
	- <@spring.message code='dtbsSource.tableMeta' />
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<form id="${pid}form" class="flex flex-column">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<div class="field grid">
				<label for="${pid}name" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='name' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}name" v-model="fm.name" type="text" class="input w-full"
		        		name="title">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}type" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='type' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}type" v-model="fm.type" type="text" class="input w-full"
		        		name="type">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}comment" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='desc' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}comment" v-model="fm.comment" type="text" class="input w-full"
		        		name="type">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}columns" class="field-label col-12 mb-2">
					<@spring.message code='columnInfo' />
				</label>
				<div class="field-input col-12">
					<div class="p-component p-inputtext">
						<div id="${pid}columns" class="columns-wrapper input w-full overflow-auto">
							<p-datatable :value="fm.columns" :scrollable="true"
								:resizable-columns="true" column-resize-mode="expand"
								data-key="name" striped-rows class="columns-table table-sm">
								<p-column field="name" header="<@spring.message code='name' />">
								</p-column>
								<p-column field="typeName" header="<@spring.message code='type' />">
								</p-column>
								<p-column field="nullable" header="<@spring.message code='allowNull' />">
									<template #body="{data}">
										{{formatNullable(data)}}
									</template>
								</p-column>
								<p-column field="comment" header="<@spring.message code='desc' />">
								</p-column>
							</p-datatable>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
		</div>
	</form>
</div>
<#include "../include/page_form.ftl">
<#include "../include/page_boolean_options.ftl">
<script>
(function(po)
{
	po.submitUrl = "#";
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	po.setupForm(formModel);
	
	po.vueMethod(
	{
		formatNullable: function(data)
		{
			return po.formatBooleanValue(data.nullable);
		}
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>