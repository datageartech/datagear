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
<#--
数据集设置页面片段

依赖：

-->
<p-button id="${pid}settingPanelBtn" icon="pi pi-cog" size="small" severity="secondary" text class="px-2 hide-if-readonly" 
	@click="toggleSettingPanel" aria:haspopup="true" aria-controls="${pid}settingPanel">
</p-button>
<p-overlaypanel ref="${pid}settingPanelEle" append-to="body" :show-close-icon="false" id="${pid}settingPanel">
	<div class="pb-2">
		<label class="text-lg font-bold">
			<@spring.message code='set' />
		</label>
	</div>
	<div class="panel-content-size-xxs overflow-auto p-2">
		<div class="field grid">
			<div class="field-input col-12 flex align-items-center">
				<p-checkbox input-id="${pid}autoGenerateField" v-model="pm.autoGenerateField" :binary="true"></p-checkbox>
				<label for="${pid}autoGenerateField" class="mx-2 align-tip" title="<@spring.message code='dataSet.autoGenerateField.desc' />">
					<@spring.message code='dataSet.autoGenerateField' />
				</label>
			</div>
		</div>
		<div class="field grid">
			<div class="field-input col-12 flex align-items-center">
				<p-checkbox input-id="${pid}saveMustPreview" v-model="pm.saveMustPreview" :binary="true"></p-checkbox>
				<label for="${pid}saveMustPreview" class="mx-2 align-tip">
					<@spring.message code='dataSet.saveMustPreview' />
				</label>
			</div>
		</div>
	</div>
</p-overlaypanel>
<script>
(function(po)
{
	po.vuePageModel(
	{
		autoGenerateField: true,
		saveMustPreview: true
	});
	
	po.vueRef("${pid}settingPanelEle", null);
	
	po.vueMethod(
	{
		toggleSettingPanel: function(e)
		{
			po.vueUnref("${pid}settingPanelEle").toggle(e);
		}
	});
})
(${pid});
</script>
