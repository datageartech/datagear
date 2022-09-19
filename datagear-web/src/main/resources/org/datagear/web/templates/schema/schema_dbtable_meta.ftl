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
	<@spring.message code='module.schema' />
	- <@spring.message code='schema.tableMeta' />
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
								dataKey="name" striped-rows class="columns-table table-sm">
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
		<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
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
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>