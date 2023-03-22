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
	<@spring.message code='module.importData' />
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-import-data">
	<div class="page-header grid grid-nogutter align-items-center pb-2">
		<div class="col-12 flex align-items-center mb-1">
			<i class="pi pi-database text-color-secondary text-sm"></i>
			<div class="text-color-secondary text-sm ml-1">${schema.title}</div>
			<i class="pi pi-angle-right text-color-secondary text-sm mx-1"></i>
			<div class="text-color-secondary text-sm"><@spring.message code='module.importData' /></div>
		</div>
		<div class="col-12">
			<label class="text-lg font-bold">
				<@spring.message code='dataImport.selectDataType' />
			</label>
		</div>
	</div>
	<div class="page-content">
		<form id="${pid}form" action="#">
			<div class="pl-5 pt-4 pb-2">
				<div class="field-radiobutton">
					<p-radiobutton id="${pid}vsc" name="dataType" value="csv" v-model="fm.dataType"></p-radiobutton>
					<label for="${pid}vsc" style="min-width:8rem;"><@spring.message code='dataImport.dataType.csv' /></label>
					<label for="${pid}vsc" class="ml-3 text-sm text-color-secondary"><@spring.message code='dataImport.dataType.csv.desc' /></label>
				</div>
				<div class="field-radiobutton pt-2">
					<p-radiobutton id="${pid}excel" name="dataType" value="excel" v-model="fm.dataType"></p-radiobutton>
					<label for="${pid}excel" style="min-width:8rem;"><@spring.message code='dataImport.dataType.excel' /></label>
					<label for="${pid}excel" class="ml-3 text-sm text-color-secondary"><@spring.message code='dataImport.dataType.excel.desc' /></label>
				</div>
				<div class="field-radiobutton pt-2">
					<p-radiobutton id="${pid}sql" name="dataType" value="sql" v-model="fm.dataType"></p-radiobutton>
					<label for="${pid}sql" style="min-width:8rem;"><@spring.message code='dataImport.dataType.sql' /></label>
					<label for="${pid}sql" class="ml-3 text-sm text-color-secondary"><@spring.message code='dataImport.dataType.sql.desc' /></label>
				</div>
				<div class="field-radiobutton pt-2">
					<p-radiobutton id="${pid}json" name="dataType" value="json" v-model="fm.dataType"></p-radiobutton>
					<label for="${pid}json" style="min-width:8rem;"><@spring.message code='dataImport.dataType.json' /></label>
					<label for="${pid}json" class="ml-3 text-sm text-color-secondary"><@spring.message code='dataImport.dataType.json.desc' /></label>
				</div>
			</div>
			<div class="pt-3 text-center">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
	<#include "../include/page_foot.ftl">
</div>
<#include "../include/page_form.ftl">
<#include "../include/page_simple_form.ftl">
<script>
(function(po)
{
	po.schemaId = "${schema.id}";
	po.submitUrl = function()
	{
		var fm = po.vueFormModel();
		return "/dataexchange/"+encodeURIComponent(po.schemaId)+"/import/" + fm.dataType;
	}
	
	if(po.isAjaxRequest)
	{
		po.setupForm({ dataType: "csv" },
		{
			closeAfterSubmit: false,
			success: function(response)
			{
				po.element().parent().html(response);
			}
		});
	}
	else
	{
		po.setupForm({ dataType: "csv" },
		{
			closeAfterSubmit: false
		});
		
		po.submitForm = function(url, options)
		{
			options.target = "_self";
			po.open(url, options);
		};
	}
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>