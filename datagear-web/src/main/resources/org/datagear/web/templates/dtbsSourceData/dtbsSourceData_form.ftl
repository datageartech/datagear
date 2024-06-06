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
<#assign ConversionSqlParamValueMapper=statics['org.datagear.persistence.support.ConversionSqlParamValueMapper']>
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='module.data' />
	<#include "../include/html_request_action_suffix.ftl">
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<#include "include/dtbsSourceData_page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<form id="${pid}form" class="flex flex-column" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<div v-for="col in pm.dbTable.columns" class="field grid">
				<label :for="'${pid}' + col.name" class="field-label overflow-hidden col-12 mb-2 md:col-3 md:mb-0"
					:title="col.comment || col.name"
					:class="{'text-500': !col.isSupported}">
					{{col.name}}
				</label>
		        <div class="field-input col-12 md:col-9">
		        	
		        	<p-inputtext v-if="!col.isSupported"
		        		:id="'${pid}' + col.name" type="text" class="input w-full"
		        		:name="col.name" disabled>
		        	</p-inputtext>
		        	
		        	<div v-else-if="col.isImportKey" class="p-inputgroup">
						<div class="p-input-icon-right flex-grow-1">
							<i class="pi pi-times cursor-pointer opacity-60" @click="onDeleteColValue($event, col)" v-if="!pm.isReadonlyAction">
							</i>
							<p-inputtext :id="'${pid}' + col.name" v-model="fm[col.name]" type="text" class="input w-full h-full border-noround-right"
								:name="col.name" :required="col.isRequired">
							</p-inputtext>
						</div>
						<p-button label="<@spring.message code='select' />"
							@click="onSelectImportKeyColValue($event, col)" v-if="!pm.isReadonlyAction">
						</p-button>
					</div>
		        	
		        	<div v-else-if="col.isBinary">
		        		<div class="p-inputgroup">
							<div class="p-input-icon-right flex-grow-1">
								<i class="pi pi-times cursor-pointer opacity-60" @click="onDeleteColValue($event, col)" v-if="!pm.isReadonlyAction">
								</i>
								<p-inputtext :id="'${pid}' + col.name" v-model="fm[col.name]" type="text" class="input w-full h-full border-noround-right"
									:name="col.name" :required="col.isRequired">
								</p-inputtext>
							</div>
							<p-button label="<@spring.message code='download' />"
								@click="onDownloadBinaryColValue($event, col)">
							</p-button>
						</div>
						<div class="fileupload-wrapper flex align-items-center mt-1" v-if="!pm.isReadonlyAction">
				        	<p-fileupload mode="basic" name="file" :url="pm.uploadBinaryColumnFileUrl"
				        		@upload="onBinaryColumnFileUploaded($event, col)" @select="onSelectBinaryColumnFile($event, col)" @progress="onProgressBinaryColumnFile($event, col)" @error="onErrorBinaryColumnFile($event, col)"
				        		:auto="true" choose-label="<@spring.message code='upload' />" class="mr-2">
				        	</p-fileupload>
				        	<div class="fileupload-info text-color-secondary">
								<small class="file-name">{{pm.binaryColumnUploadInfos[col.name].fileLabel}}</small>
								<small class="upload-progress ml-2">{{pm.binaryColumnUploadInfos[col.name].progress}}</small>
							</div>
			        	</div>
					</div>
		        	
		        	<p-textarea v-else-if="col.isRenderAsTextarea"
		        		:id="'${pid}' + col.name" v-model="fm[col.name]" rows="6" class="input w-full"
		        		:name="col.name" :required="col.isRequired">
		        	</p-textarea>
		        	
		        	<p-inputtext v-else
		        		:id="'${pid}' + col.name" v-model="fm[col.name]" class="input w-full"
		        		:name="col.name" :required="col.isRequired">
		        	</p-inputtext>
		        	
		        	<div class="desc text-color-secondary mt-1" v-if="col.isDate || col.isTime || col.isTimestamp">
		        		<small v-if="col.isDate">{{formatAndExampleWithParam(pm.sqlDateFormat, pm.sqlDateFormatExample)}}</small>
		        		<small v-else-if="col.isTime">{{formatAndExampleWithParam(pm.sqlTimeFormat, pm.sqlTimeFormatExample)}}</small>
		        		<small v-else-if="col.isTimestamp">{{formatAndExampleWithParam(pm.sqlTimestampFormat, pm.sqlTimestampFormatExample)}}</small>
		        		<small v-else></small>
		        	</div>
		        </div>
			</div>
		</div>
		<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
		</div>
	</form>
</div>
<#include "../include/page_form.ftl">
<script>
(function(po)
{
	po.submitUrl = po.dataUrl(po.submitAction);
	po.binaryColumnValueFilePrefix = "${ConversionSqlParamValueMapper.PREFIX_FILE_PATH}";
	
	po.mergeBinaryColumnValue = function(data)
	{
		var pm = po.vuePageModel();
		var binaryColumnUploadInfos = pm.binaryColumnUploadInfos;
		
		$.each(binaryColumnUploadInfos, function(name, info)
		{
			if(data[name] && info.fileLabel && data[name] == info.fileLabel)
				data[name] = po.binaryColumnValueFilePrefix + info.fileName;
		});
	};
	
	po.onDbTable(function(dbTable)
	{
		var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel bigNumberToString=true />);
		po.originalFormModel = $.extend(true, {}, formModel);
		
		po.beforeSubmitForm = function(action)
		{
			var data = action.options.data;
			
			po.mergeBinaryColumnValue(data);
			
			if(po.isEditAction)
			{
				action.options.data =
				{
					data: data,
					originalData: po.originalFormModel
				};
			}
		};
		
		po.setupForm(formModel);
		
		var binaryColumnUploadInfos = {};
		$.each(dbTable.columns, function(i, column)
		{
			if(column.isBinary)
			{
				binaryColumnUploadInfos[column.name] =
				{
					fileName: "",
					fileLabel: "",
					progress: ""
				};
			}
		});
		
		po.downloadBinaryColValue = function(column)
		{
			var value = po.originalFormModel[column.name];
			
			if(!value)
			{
				$.tipInfo("<@spring.message code='noDataForDownload' />");
				return;
			}
			
			var url = po.dataUrl("downloadColumnValue");
			var data = $.tableMeta.uniqueRecordData(dbTable, po.originalFormModel);
			data = $.toJsonString(data);
			
			url = $.addParam(url, "data", data);
			url = $.addParam(url, "columnName", column.name);
			
			po.open(url, {target: "_blank"});
		};
		
		po.vuePageModel(
		{
			dbTable: dbTable,
			binaryColumnUploadInfos: binaryColumnUploadInfos,
			uploadBinaryColumnFileUrl: po.concatContextPath("/dtbsSourceData/uploadFile"),
			sqlDateFormat: "${sqlDateFormat}",
			sqlTimeFormat: "${sqlTimeFormat}",
			sqlTimestampFormat: "${sqlTimestampFormat}",
			sqlDateFormatExample: "${sqlDateFormatExample}",
			sqlTimeFormatExample: "${sqlTimeFormatExample}",
			sqlTimestampFormatExample: "${sqlTimestampFormatExample}"
		});
		
		po.vueMethod(
		{
			formatAndExampleWithParam: function(format, example)
			{
				return $.validator.format("<@spring.message code='formatAndExampleWithParam' />", format, example);
			},
			
			onDeleteColValue: function(e, column)
			{
				var fm = po.vueFormModel();
				fm[column.name] = null;
				
				if(column.isBinary)
				{
					var pm = po.vuePageModel();
					var binaryColumnUploadInfos = pm.binaryColumnUploadInfos;
					binaryColumnUploadInfos[column.name] = {};
				}
			},
			
			onSelectImportKeyColValue: function(e, column)
			{
				var importKey = $.tableMeta.columnImportKey(dbTable, column);
				if(!importKey)
					return;
				
				var url = "/dtbsSourceData/"+encodeURIComponent(po.dtbsSourceId)+"/"+encodeURIComponent(importKey.primaryTableName)+"/select";
				
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
			
			onSelectBinaryColumnFile: function(e, column)
			{
				var pm = po.vuePageModel();
				var binaryColumnUploadInfos = pm.binaryColumnUploadInfos;
				var myUploadInfo = binaryColumnUploadInfos[column.name];
				
				myUploadInfo.fileLabel = (e.files && e.files[0] ? e.files[0].name : "");
			},
			
			onProgressBinaryColumnFile: function(e, column)
			{
				var pm = po.vuePageModel();
				var binaryColumnUploadInfos = pm.binaryColumnUploadInfos;
				var myUploadInfo = binaryColumnUploadInfos[column.name];
				
				myUploadInfo.progress = (e.progress >= 100 ? 99 : e.progress) +"%";
			},

			onErrorBinaryColumnFile: function(e, column)
			{
				var om = $.getResponseJson(e.xhr);
				var message = "Error";
				
				if(om && om.message)
					message = om.message;
				
				$.tipError(message);
			},
			
			onBinaryColumnFileUploaded: function(e, column)
			{
				var pm = po.vuePageModel();
				var binaryColumnUploadInfos = pm.binaryColumnUploadInfos;
				var myUploadInfo = binaryColumnUploadInfos[column.name];
				var response = $.getResponseJson(e.xhr);
				
				myUploadInfo.fileName = response.name;
				myUploadInfo.progress = "100%";
				
				var fm = po.vueFormModel();
				fm[column.name] = myUploadInfo.fileLabel;
			},
			
			onDownloadBinaryColValue: function(e, column)
			{
				po.downloadBinaryColValue(column);
			}
		});
		
		po.vueMounted(function()
		{
			//聚焦第一个输入框
			po.element(":input:not([readonly],[disabled]):first", po.form()).focus();
		});
		
		po.vueMount();
	});
})
(${pid});
</script>
</body>
</html>