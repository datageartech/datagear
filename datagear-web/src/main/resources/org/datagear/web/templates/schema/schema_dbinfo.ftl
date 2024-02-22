<#--
 *
 * Copyright 2018-2023 datagear.tech
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
	<@spring.message code='module.schema' />
	- <@spring.message code='schema.dbinfo' />
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
		        		name="name">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label class="field-label col-12 mb-2">
					<@spring.message code='tableType' />
				</label>
				<div class="field-input col-12">
					<div class="p-component p-inputtext">
						<div id="${pid}tableTypes" class="columns-wrapper input w-full overflow-auto">
							<p-datatable :value="fm.tableTypes" :scrollable="true"
								:resizable-columns="true" column-resize-mode="expand"
								data-key="name" striped-rows class="table-sm">
								<p-column field="name" header="<@spring.message code='name' />">
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
	formModel.tableTypes = (formModel.tableTypes ? formModel.tableTypes : []);
	
	for(var i=0; i<formModel.tableTypes.length; i++)
	{
		formModel.tableTypes[i] = { name: formModel.tableTypes[i] };
	}
	
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