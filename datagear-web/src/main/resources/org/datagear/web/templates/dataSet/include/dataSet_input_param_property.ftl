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
		<div class="p-component p-inputtext">
			<div class="flex flex-row pb-1">
				<div class="h-opts flex-grow-1">
					<p-button type="button" label="<@spring.message code='add' />" class="p-button-secondary p-button-sm"></p-button>
					<p-button type="button" label="<@spring.message code='moveUp' />" class="p-button-secondary p-button-sm"></p-button>
					<p-button type="button" label="<@spring.message code='moveDown' />" class="p-button-secondary p-button-sm"></p-button>
					<p-button type="button" label="<@spring.message code='delete' />" class="p-button-danger p-button-sm"></p-button>
				</div>
			</div>
			<div id="${pid}parameters" class="params-wrapper input w-full overflow-auto">
				<p-datatable :value="pm.params" :scrollable="true" scroll-height="flex"
					v-model:selection="pm.selectedParams"
					edit-mode="row" v-model:editing-rows="editingParamRows"  @row-edit-save="onParamRowEditSave"
					selection-mode="multiple" dataKey="name" striped-rows class="table-sm">
					<p-column selection-mode="multiple" :frozen="true" class="col-check"></p-column>
					<p-column :row-editor="true" :frozen="true" style="max-width:6rem;min-width:6rem" bodyStyle="text-align:center"></p-column>
					<p-column field="name" header="<@spring.message code='name' />" class="col-name">
						<template #editor="{ data, field }">
							<p-inputtext v-model="data[field]" autofocus></p-inputtext>
						</template>
					</p-column>
					<p-column field="type" header="<@spring.message code='type' />" class="col-name">
						<template #editor="{ data, field }">
							<p-inputtext v-model="data[field]"></p-inputtext>
						</template>
					</p-column>
					<p-column field="required" header="<@spring.message code='isRequired' />" class="col-name">
						<template #editor="{ data, field }">
							<p-inputtext v-model="data[field]"></p-inputtext>
						</template>
					</p-column>
					<p-column field="desc" header="<@spring.message code='desc' />" class="col-name">
						<template #editor="{ data, field }">
							<p-inputtext v-model="data[field]"></p-inputtext>
						</template>
					</p-column>
					<p-column field="inputType" header="<@spring.message code='inputType' />" class="col-name">
						<template #editor="{ data, field }">
							<p-inputtext v-model="data[field]"></p-inputtext>
						</template>
					</p-column>
					<p-column field="inputPayload" header="<@spring.message code='inputConfig' />" class="col-name">
						<template #editor="{ data, field }">
							<p-inputtext v-model="data[field]"></p-inputtext>
						</template>
					</p-column>
				</p-datatable>
			</div>
		</div>
	</div>
</div>
<div class="field grid">
	<label for="${pid}properties" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='property' />
	</label>
	<div class="field-input col-12 md:col-9">
		<div class="p-component p-inputtext">
			<div class="flex flex-row pb-1">
				<div class="h-opts flex-grow-1">
					<p-button type="button" label="<@spring.message code='add' />" class="p-button-secondary p-button-sm"></p-button>
					<p-button type="button" label="<@spring.message code='moveUp' />" class="p-button-secondary p-button-sm"></p-button>
					<p-button type="button" label="<@spring.message code='moveDown' />" class="p-button-secondary p-button-sm"></p-button>
					<p-button type="button" label="<@spring.message code='delete' />" class="p-button-danger p-button-sm"></p-button>
				</div>
				<div class="flex-grow-1 flex justify-content-end">
					<p-button type="button" label="<@spring.message code='set' />" class="p-button-secondary p-button-sm"></p-button>
				</div>
			</div>
			<div id="${pid}properties" class="properties-wrapper input w-full overflow-auto">
				<p-datatable :value="pm.properties" :scrollable="true" scroll-height="flex"
					v-model:selection="pm.selectedProperties"
					edit-mode="row" v-model:editing-rows="editingPropertyRows"  @row-edit-save="onPropertyRowEditSave"
					selection-mode="multiple" dataKey="name" striped-rows class="table-sm">
					<p-column selection-mode="multiple" :frozen="true" class="col-check"></p-column>
					<p-column :row-editor="true" :frozen="true" style="max-width:6rem;min-width:6rem" bodyStyle="text-align:center"></p-column>
					<p-column field="name" header="<@spring.message code='name' />" class="col-name">
						<template #editor="{ data, field }">
							<p-inputtext v-model="data[field]" autofocus></p-inputtext>
						</template>
					</p-column>
					<p-column field="type" header="<@spring.message code='type' />" class="col-name">
						<template #editor="{ data, field }">
							<p-inputtext v-model="data[field]"></p-inputtext>
						</template>
					</p-column>
					<p-column field="displayName" header="<@spring.message code='displayName' />" class="col-name">
						<template #editor="{ data, field }">
							<p-inputtext v-model="data[field]"></p-inputtext>
						</template>
					</p-column>
					<p-column field="defaultValue" header="<@spring.message code='defaultValue' />" class="col-name">
						<template #editor="{ data, field }">
							<p-inputtext v-model="data[field]"></p-inputtext>
						</template>
					</p-column>
				</p-datatable>
			</div>
		</div>
	</div>
</div>
<script>
(function(po)
{
	po.vueRef("editingPropertyRows", []);
	po.vueRef("editingParamRows", []);
	
	po.vueMethod(
	{
		onPropertyRowEditSave: function(e){},
		
		onParamRowEditSave: function(e){}
	});
})
(${pid});
</script>
