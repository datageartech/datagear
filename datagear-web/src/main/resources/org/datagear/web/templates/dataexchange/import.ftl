<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<#include "../include/html_app_name_prefix.ftl">
	<@spring.message code='module.importData' />
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-import-data">
	<div class="page-header grid align-items-center">
		<div class="col-12 flex align-items-center py-1">
			<i class="pi pi-database text-color-secondary text-sm"></i>
			<div class="text-color-secondary text-sm ml-1">${schema.title}</div>
		</div>
		<div class="col-12 pt-1">
			<label class="text-lg font-bold">
				<@spring.message code='dataImport.selectDataType' />
			</label>
		</div>
	</div>
	<div class="page-content">
		<form id="${pid}form">
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
	
	po.setupForm({ dataType: "csv" });
	
	po.vuePageModel(
	{
		
	});
	
	po.vueMethod(
	{
		
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>