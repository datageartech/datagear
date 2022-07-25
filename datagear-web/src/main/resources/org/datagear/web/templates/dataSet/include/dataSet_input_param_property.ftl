<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
数据集参数、属性输入项
-->
<div class="field grid">
	<label for="${pid}parameters" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='parameter' />
	</label>
	<div class="field-input col-12 md:col-9">
		<div id="${pid}parameters" class="params-wrapper input p-component p-inputtext w-full overflow-auto">
			<p-datatable :value="pm.params" :scrollable="true" scroll-height="flex"
				v-model:selection="pm.selectedParams" :selection-mode="multiple" dataKey="name" striped-rows class="table-sm">
				<p-column :selection-mode="multiple" :frozen="true" class="col-check"></p-column>
				<p-column field="name" header="<@spring.message code='name' />" class="col-name"></p-column>
				<p-column field="type" header="<@spring.message code='type' />" class="col-name"></p-column>
				<p-column field="required" header="<@spring.message code='isRequired' />" class="col-name"></p-column>
				<p-column field="desc" header="<@spring.message code='desc' />" class="col-name"></p-column>
				<p-column field="inputType" header="<@spring.message code='inputType' />" class="col-name"></p-column>
				<p-column field="inputPayload" header="<@spring.message code='inputConfig' />" class="col-name"></p-column>
			</p-datatable>
		</div>
	</div>
</div>
<div class="field grid">
	<label for="${pid}properties" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='property' />
	</label>
	<div class="field-input col-12 md:col-9">
		<div id="${pid}properties" class="properties-wrapper input p-component p-inputtext w-full overflow-auto">
			<p-datatable :value="pm.properties" :scrollable="true" scroll-height="flex"
				v-model:selection="pm.selectedProperties" :selection-mode="multiple" dataKey="name" striped-rows class="table-sm">
				<p-column :selection-mode="multiple" :frozen="true" class="col-check"></p-column>
				<p-column field="name" header="<@spring.message code='name' />" class="col-name"></p-column>
				<p-column field="type" header="<@spring.message code='type' />" class="col-name"></p-column>
				<p-column field="displayName" header="<@spring.message code='displayName' />" class="col-name"></p-column>
				<p-column field="defaultValue" header="<@spring.message code='defaultValue' />" class="col-name"></p-column>
			</p-datatable>
		</div>
	</div>
</div>
<script>
(function(po)
{
	
})
(${pid});
</script>
