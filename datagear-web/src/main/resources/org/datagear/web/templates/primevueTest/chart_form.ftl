<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "include/page_import.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<title><#include "include/html_app_name_prefix.ftl">图表 - 添加</title>
</head>
<body class="p-card no-border">
<#include "include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<form @submit.prevent="onSubmit" class="flex flex-column">
		<div class="page-form-content flex-grow-1 pr-2 py-1 overflow-y-auto">
			<div class="field grid">
				<label for="${pid}name" class="col-12 mb-2 md:col-3 md:mb-0">名称</label>
		        <div class="col-12 md:col-9">
		        	<p-inputtext id="${pid}name" v-model="pm.name" type="text" class="form-input w-full"></p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}type" class="col-12 mb-2 md:col-3 md:mb-0">类型</label>
		        <div class="col-12 md:col-9">
		        	<p-inputtext id="${pid}type" v-model="pm.type" type="text" class="form-input w-full"></p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}dataSet" class="col-12 mb-2 md:col-3 md:mb-0">数据集</label>
		        <div class="col-12 md:col-9">
		        	<p-inputtext id="${pid}dataSet" v-model="pm.dataSet" type="text" class="form-input w-full md:w-9"></p-inputtext>
					<p-button label="选择" @click="onSelectDataSet" class="ml-0 mt-1 md:ml-1 md:mt-0" />
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}interval" class="col-12 mb-2 md:col-3 md:mb-0">更新间隔</label>
		        <div class="col-12 md:col-9">
		        	<p-inputtext id="${pid}interval" v-model="pm.interval" type="text" class="form-input w-full"></p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}desc" class="col-12 mb-2 md:col-3 md:mb-0">描述</label>
		        <div class="col-12 md:col-9">
		        	<p-textarea id="${pid}desc" v-model="pm.desc" rows="30" class="form-input w-full"></p-textarea>
		        </div>
			</div>
		</div>
		<div class="page-form-foot flex-grow-0 pt-3 text-center">
			<p-button type="submit" label="保存" />
		</div>
	</form>
</div>
<#include "include/page_form.ftl">
<script>
(function(po)
{
	po.setupForm(
	{
		name: "name",
		type: "type",
		dataSet: "dataSet",
		interval: "interval",
		desc: "desc"
	},
	"/primevue/saveChart");
	
	po.vueMethod(
	{
		onSelectDataSet: function()
		{
			po.open("/primevue/chartList", { width: "80vw" });
		}
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>