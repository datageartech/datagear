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
	<form class="flex flex-column">
		<div class="page-form-content flex-grow-1 pr-2 py-1 overflow-y-auto">
			<div class="field grid">
				<label for="${pid}name" class="field-label col-12 mb-2 md:col-3 md:mb-0">名称</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}name" v-model="pm.name" type="text" class="input w-full"
		        		name="name" required maxlength="10">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}type" class="field-label col-12 mb-2 md:col-3 md:mb-0">类型</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}type" v-model="pm.type" type="text" class="input w-full"
		        		name="type" required maxlength="10">
		        	</p-inputtext>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}dataSet" class="field-label col-12 mb-2 md:col-3 md:mb-0">数据集</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-inputtext id="${pid}dataSet" v-model="pm.dataSet" type="text" class="input w-full md:w-9"
		        		name="dataSet" required maxlength="20">
		        	</p-inputtext>
					<p-button label="选择" @click="onSelectDataSet" class="ml-0 mt-1 md:ml-1 md:mt-0" />
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}checkbox" class="field-label col-12 mb-2 md:col-3 md:mb-0">多选按钮</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-selectbutton v-model="pm.checkbox" :options="selectButtonOptions" :multiple="true"
		        		option-label="name" option-value="value" data-key="value" class="input">
		        	</p-selectbutton>
		        	<div class="validate-msg">
		        		<input name="checkbox" required type="text" class="validate-proxy" />
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}password" class="field-label col-12 mb-2 md:col-3 md:mb-0">密码</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-password id="${pid}password" v-model="pm.password" toggle-mask :feedback="false"
		        		input-class="w-full" class="input w-full"
		        		name="password" required maxlength="10">
		        	</p-password>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}radio" class="field-label col-12 mb-2 md:col-3 md:mb-0">单选按钮</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-selectbutton v-model="pm.radio" :options="selectButtonOptions" option-label="name"
		        		option-value="value" data-key="value" class="input">
		        	</p-selectbutton>
		        	<div class="validate-msg">
		        		<input name="radio" required type="text" class="validate-proxy" />
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}dropdown" class="field-label col-12 mb-2 md:col-3 md:mb-0">下拉框</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-dropdown v-model="pm.dropdown" :options="selectButtonOptions"
		        		option-label="name" option-value="value" data-key="value" class="input"
		        		name="dropdown" required>
		        	</p-dropdown>
		        	<div class="validate-msg">
		        		<input name="dropdown" required type="text" class="validate-proxy" />
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}desc" class="field-label col-12 mb-2 md:col-3 md:mb-0">描述</label>
		        <div class="field-input col-12 md:col-9">
		        	<p-textarea id="${pid}desc" v-model="pm.desc" rows="20" class="input w-full"
		        		name="desc" required maxlength="20">
		        	</p-textarea>
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
		name: "",
		type: "",
		dataSet: "",
		desc: "",
		checkbox: [],
		password: "",
		radio: null,
		dropdown: null
	},
	"/primevue/saveChart");
	
	po.vueRef("selectButtonOptions",
	[
		{name: "Value-0", value: 0},
		{name: "Value-1", value: 1},
		{name: "Value-2", value: 2}
	]);
	
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