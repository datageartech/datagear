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
导出表格页头片段

依赖：
dataexchange_js.ftl
export_js.ftl
-->
<div class="flex align-items-center justify-content-between">
	<div class="fileupload-wrapper flex align-items-center">
		<p-splitbutton label="<@spring.message code='add' />" @click="onAdd" :model="pm.addBtnItems"
			:disabled="pm.dataExchangeStatus != pm.DataExchangeStatusEnum.edit">
		</p-splitbutton>
	</div>
	<div class="flex align-items-center w-3">
		<div class="w-full"
			v-if="pm.dataExchangeStatus != pm.DataExchangeStatusEnum.edit">
			<p-progressbar :value="pm.dataExchangeProgress.value">
				{{pm.dataExchangeProgress.label}}
			</p-progressbar>
		</div>
	</div>
	<div class="h-opts">
		<p-button type="button" label="<@spring.message code='delete' />"
			@click="onDeleteSelSubDataExchanges"
			class="p-button-danger"
			v-if="pm.dataExchangeStatus == pm.DataExchangeStatusEnum.edit">
		</p-button>
		<p-button type="button" label="<@spring.message code='fileEncoding' />"
			aria:haspopup="true" aria-controls="${pid}fileEncodingPanel"
			@click="onToggleFileEncodingPanel" class="p-button-secondary"
			v-if="pm.dataExchangeStatus == pm.DataExchangeStatusEnum.edit && pm.tableHeadOptions.fileEncodingEnable">
		</p-button>
		<p-button type="button" label="<@spring.message code='downloadAll' />"
			@click="onDownloadAll"
			v-if="pm.dataExchangeStatus == pm.DataExchangeStatusEnum.finish">
		</p-button>
		<p-button type="button" label="<@spring.message code='cancel' />"
			title="<@spring.message code='dataExport.cancel.desc' />"
			@click="onCancelSubDataExchanges"
			v-if="pm.dataExchangeStatus == pm.DataExchangeStatusEnum.exchange">
		</p-button>
	</div>
	<p-overlaypanel ref="${pid}fileEncodingPanelEle" append-to="body"
		:show-close-icon="false" id="${pid}fileEncodingPanel">
		<div class="pb-2">
			<label class="text-lg font-bold">
				<@spring.message code='fileEncoding' />
			</label>
		</div>
		<div class="p-2 panel-content-size-xxs overflow-auto">
			<div class="field grid">
				<label for="${pid}fileEncoding" class="field-label col-12 mb-2"
					title="<@spring.message code='dataExport.fileEncoding.desc' />">
					<@spring.message code='fileEncoding' />
				</label>
		        <div class="field-input col-12">
		        	<p-dropdown id="${pid}fileEncoding" v-model="fm.fileEncoding"
		        		:options="pm.availableCharsetNames" class="input w-full">
		        	</p-dropdown>
		        </div>
			</div>
		</div>
	</p-overlaypanel>
</div>
<script>
(function(po)
{
	po.availableCharsetNames = $.unescapeHtmlForJson(<@writeJson var=availableCharsetNames />);

	po.addAllTable = function()
	{
		if(po._addAllTableDoing)
			return;
		
		po._addAllTableDoing = true;
		
		po.ajax("/dataexchange/" + encodeURIComponent(po.schemaId) +"/getAllTableNames",
		{
			success : function(tableNames)
			{
				if(!tableNames)
					return;
				
				po.addSubDataExchangesForQueries(tableNames);
			},
			complete : function()
			{
				po._addAllTableDoing = false;
			}
		});
	};
	
	po.setupExportTableHead = function(options)
	{
		options = $.extend(
		{
			fileEncodingEnable: true,
			downloadAllFileName: "export.zip"
		},
		options);
		
		po.vuePageModel(
		{
			addBtnItems:
			[
				{
					label: "<@spring.message code='addAllTable' />",
					command: function()
					{
						po.addAllTable();
					}
				}
			],
			availableCharsetNames: po.availableCharsetNames,
			tableHeadOptions: options
		});
		
		po.vueMethod(
		{
			onAdd: function(e)
			{
				po.addSubDataExchangesForQueries([ "" ]);
			},
			
			onToggleFileEncodingPanel: function(e)
			{
				po.vueUnref("${pid}fileEncodingPanelEle").toggle(e);
			},
			
			onDownloadAll: function(e)
			{
				var url = "/dataexchange/" + encodeURIComponent(po.schemaId) +"/export/downloadAll";
				url = $.addParam(url, "dataExchangeId", encodeURIComponent(po.dataExchangeId));
				url = $.addParam(url, "fileName", encodeURIComponent(options.downloadAllFileName));
				
				po.open(url, { target : "_blank" });
			}
		});
		
		po.vueRef("${pid}fileEncodingPanelEle", null);
	};
})
(${pid});
</script>