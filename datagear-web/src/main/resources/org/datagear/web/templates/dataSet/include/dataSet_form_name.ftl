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
<#--
数据集基本输入项

依赖：
page_boolean_options.ftl

-->
<div class="field grid">
	<label for="${pid}name" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='name' />
	</label>
	<div class="field-input col-12 md:col-9">
		<p-inputtext id="${pid}name" v-model="fm.name" type="text" class="input w-full"
			name="name" required maxlength="100" autofocus>
		</p-inputtext>
	</div>
</div>
<div class="field grid">
	<label for="${pid}mutableModel" class="field-label col-12 mb-2 md:col-3 md:mb-0"
		title="<@spring.message code='dataSet.mutableModel.desc' />">
		<@spring.message code='isMutableModel' />
	</label>
	<div class="field-input col-12 md:col-9">
		<p-selectbutton v-model="fm.mutableModel" :options="pm.booleanOptions"
			option-label="name" option-value="value" class="input w-full">
		</p-selectbutton>
	</div>
</div>
<div class="field grid">
	<label for="${pid}ownerProject" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='ownerProject' />
	</label>
	<div class="field-input col-12 md:col-9">
		<div class="p-inputgroup">
			<div class="p-input-icon-right flex-grow-1">
				<i class="pi pi-times cursor-pointer opacity-60" @click="onDeleteAnalysisProject" v-if="!pm.isReadonlyAction">
				</i>
				<p-inputtext id="${pid}ownerProject" v-model="fm.analysisProject.name" type="text" class="input w-full h-full border-noround-right"
					readonly="readonly" name="analysisProject.name" maxlength="200">
				</p-inputtext>
			</div>
			<p-button type="button" label="<@spring.message code='select' />"
				@click="onSelectAnalysisProject" class="p-button-secondary"
				v-if="!pm.isReadonlyAction">
			</p-button>
		</div>
	</div>
</div>
<script>
(function(po)
{
	po.vueMethod(
	{
		onDeleteAnalysisProject: function()
		{
			var fm = po.vueFormModel();
			fm.analysisProject = {};
		},
		
		onSelectAnalysisProject: function()
		{
			po.handleOpenSelectAction("/analysisProject/select", function(analysisProject)
			{
				var fm = po.vueFormModel();
				fm.analysisProject = analysisProject;
			});
		}
	});
})
(${pid});
</script>
