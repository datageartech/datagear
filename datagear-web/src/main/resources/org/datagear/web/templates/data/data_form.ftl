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
	<@spring.message code='module.data' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<#include "include/data_page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<form id="${pid}form" class="flex flex-column" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<div v-for="col in pm.dbTable.columns" class="field grid">
				<label :for="'${pid}' + col.name" class="field-label overflow-hidden col-12 mb-2 md:col-3 md:mb-0"
					:title="col.comment || col.name"
					:class="{'text-500': !col.supported}">
					{{col.name}}
				</label>
		        <div class="field-input col-12 md:col-9">
		        	
		        	<p-inputtext v-if="!col.supported"
		        		:id="'${pid}' + col.name" type="text" class="input w-full"
		        		:name="col.name" disabled>
		        	</p-inputtext>
		        	
		        	<div v-else-if="col.isImportKey" class="p-inputgroup">
						<div class="p-input-icon-right flex-grow-1">
							<i class="pi pi-times cursor-pointer opacity-60" @click="onDeleteColValue($event, col)" v-if="!pm.isReadonlyAction">
							</i>
							<p-inputtext :id="'${pid}' + col.name" v-model="fm[col.name]" type="text" class="input w-full h-full border-noround-right"
								name="col.name">
							</p-inputtext>
						</div>
						<p-button label="<@spring.message code='select' />"
							@click="onSelectImportKeyColValue($event, col)" v-if="!pm.isReadonlyAction">
						</p-button>
					</div>
		        	
		        	<div v-else-if="col.isBinary" class="p-inputgroup">
						<div class="p-input-icon-right flex-grow-1">
							<i class="pi pi-times cursor-pointer opacity-60" @click="onDeleteColValue($event, col)" v-if="!pm.isReadonlyAction">
							</i>
							<p-inputtext :id="'${pid}' + col.name" v-model="fm[col.name]" type="text" class="input w-full h-full border-noround-right"
								name="col.name">
							</p-inputtext>
						</div>
						<p-splitbutton v-if="!pm.isReadonlyAction"
							label="<@spring.message code='upload' />" :model="pm.uploadBinaryColValueBtnItems"
							@click="onUploadBinaryColValue($event, col)">
						</p-splitbutton>
						<p-button v-else
							label="<@spring.message code='download' />" @click="onDownloadBinaryColValue($event, col)">
						</p-button>
					</div>
		        	
		        	<p-textarea v-else-if="col.renderAsTextarea"
		        		:id="'${pid}' + col.name" v-model="fm[col.name]" rows="6" class="input w-full"
		        		:name="col.name">
		        	</p-textarea>
		        	
		        	<p-inputtext v-else
		        		:id="'${pid}' + col.name" v-model="fm[col.name]" class="input w-full"
		        		:name="col.name">
		        	</p-inputtext>
		        	
		        	<div class="desc text-color-secondary text-sm mt-1" v-if="col.isDate || col.isTime || col.isTimestamp">
		        		<span v-if="col.isDate">{{pm.sqlDateFormat}}</span>
		        		<span v-else-if="col.isTime">{{pm.sqlTimeFormat}}</span>
		        		<span v-else-if="col.isTimestamp">{{pm.sqlTimestampFormat}}</span>
		        		<span v-else></span>
		        	</div>
		        </div>
			</div>
		</div>
		<div class="page-form-foot flex-grow-0 pt-3 text-center">
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
		</div>
	</form>
</div>
<#include "../include/page_form.ftl">
<script>
(function(po)
{
	po.submitUrl = po.dataUrl(po.submitAction);
	
	po.onDbTable(function(dbTable)
	{
		po.downloadBinaryColValue = function(column)
		{
			
		};
		
		var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel bigNumberToString=true />);
		po.originalFormModel = $.extend(true, {}, formModel);
		
		po.beforeSubmitForm = function(action)
		{
			if(po.isEditAction)
			{
				var data = action.options.data;
				action.options.data =
				{
					data: data,
					originalData: po.originalFormModel
				};
			}
		};
		
		po.setupForm(formModel);
		
		po.vuePageModel(
		{
			dbTable: dbTable,
			
			uploadBinaryColValueBtnItems:
			[
				{
					label: "<@spring.message code='download' />",
					command: function(e, column)
					{
						po.downloadBinaryColValue(column);
					}
				}
			],
			
			sqlDateFormat: "${sqlDateFormat}",
			sqlTimeFormat: "${sqlTimeFormat}",
			sqlTimestampFormat: "${sqlTimestampFormat}"
		});
		
		po.vueMethod(
		{
			onDeleteColValue: function(e, column)
			{
				var fm = po.vueFormModel();
				fm[column.name] = null;
			},
			
			onSelectImportKeyColValue: function(e, column)
			{
				var importKey = $.tableMeta.columnImportKey(dbTable, column);
				if(!importKey)
					return;
				
				var url = "/data/"+encodeURIComponent(po.schemaId)+"/"+encodeURIComponent(importKey.primaryTableName)+"/select";
				
				po.handleOpenSelectAction(url, function(entity)
				{
					var colValueObj = $.tableMeta.fromImportKeyPrimary(importKey, entity);
					
					var fm = po.vueFormModel();
					
					$.each(colValueObj, function(name, value)
					{
						fm[name] = value;
					});
				});
			},
			
			onUploadBinaryColValue: function(e, column)
			{
				
			},
			
			onDownloadBinaryColValue: function(e, column)
			{
				po.downloadBinaryColValue(column);
			}
		});
		
		po.vueMount();
	});
})
(${pid});
</script>
</body>
</html>