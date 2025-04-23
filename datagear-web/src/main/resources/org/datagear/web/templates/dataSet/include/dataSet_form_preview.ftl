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
数据集预览页面片段

依赖：

-->
<p-button id="${pid}previewPanelBtn" type="button" label="<@spring.message code='preview' />"
	aria:haspopup="true" aria-controls="${pid}previewPanel"
	@click="onPreview" class="p-button-secondary">
</p-button>
<p-overlaypanel ref="${pid}previewPanelEle" append-to="body"
	:show-close-icon="false" id="${pid}previewPanel" :class="{'opacity-0': !pm.previewPanelShow}">
	<div class="flex flex-column">
		<div class="flex-grow-0 pb-2">
			<label class="text-lg font-bold">
				<@spring.message code='preview' />
			</label>
		</div>
		<div class="flex-grow-1 p-2">
			<div style="width:50vw;height:50vh;">
				<p-datatable :value="pm.previewResultDatas" :scrollable="true" scroll-height="flex"
					:resizable-columns="true" column-resize-mode="expand"
					striped-rows class="table-sm" v-if="!pm.previewError">
					<p-column field="number" header="<@spring.message code='serialNumber' />" :frozen="true" class="col-row-number-sm">
						<template #body="slotProps">
							{{slotProps.index + 1}}
						</template>
					</p-column>
					<p-column v-for="col in pm.previewColumns"
						:field="col.name" :header="col.label" :sortable="false" :style="col.style" :key="col.name">
						<template #body="slotProps">{{formatPreviewColValue(slotProps.data, slotProps.field)}}</template>
					</p-column>
				</p-datatable>
				<p-textarea v-model="pm.previewTplResult" class="overflow-auto p-invalid w-full h-full" readonly
					v-if="pm.previewError">
				</p-textarea>
			</div>
			<div class="flex flex-row mt-2">
				<div class="flex-grow-1 flex justify-content-start">
					<div class="p-inputgroup" style="max-width:10rem">
						<p-inputtext v-model="pm.previewQuery.resultFetchSize" type="text" class="input p-inputtext-sm"
							maxlength="10" @keydown.enter="submitPreview" title="<@spring.message code='fetchSize' />">
						</p-inputtext>
						<p-button icon="pi pi-search" type="button" class="p-button-secondary p-button-sm"
							@click="submitPreview">
						</p-button>
					</div>
				</div>
				<div class="flex-grow-1 flex justify-content-end" v-if="!pm.previewError">
					<p-button icon="pi pi-info-circle" type="button"
						aria:haspopup="true" aria-controls="${pid}previewTplResultPanel"
						@click="togglePreviewTplResultPanel" class="p-button-secondary p-button-sm">
					</p-button>
					<p-overlaypanel ref="${pid}previewTplResultEle" append-to="body"
						:show-close-icon="false" id="${pid}previewTplResultPanel">
						<p-textarea v-model="pm.previewTplResult" class="overflow-auto"
							readonly style="width:30vw;height:30vh;">
						</p-textarea>
					</p-overlaypanel>
				</div>
			</div>
		</div>
	</div>
</p-overlaypanel>
<p-button id="${pid}paramPanelBtn" type="button" label="<@spring.message code='parameter' />"
	aria:haspopup="true" aria-controls="${pid}previewParamPanel"
	@click="showPreviewParamPanel" class="p-button-secondary opacity-0 overflow-hidden px-0 mx-0 white-space-nowrap"
	style="width:1px;z-index:-999">
</p-button>
<p-overlaypanel ref="${pid}previewParamPanelEle" append-to="body"
	:show-close-icon="false" @show="onPreviewParamPanelShow" @hide="onPreviewParamPanelHide" id="${pid}previewParamPanel" class="dataset-paramvalue-panel">
	<div class="pb-2">
		<label class="text-lg font-bold">
			<@spring.message code='parameter' />
		</label>
	</div>
	<div class="paramvalue-form-wrapper panel-content-size-sm overflow-auto p-2"></div>
</p-overlaypanel>
<script>
(function(po)
{
	//需实现，可以是字符串或函数
	po.previewUrl = "#";
	//需实现
	po.inflatePreviewFingerprint = function(fingerprint, dataSet){};
	
	po.previewColumnAllFieldName = $.uid();

	po.toPreviewFingerprint = function(dataSet)
	{
		var fingerprint = {};
		
		fingerprint.mutableModel = dataSet.mutableModel;
		fingerprint.fields = $.extend(true, [], dataSet.fields);
		fingerprint.params = $.extend(true, [], dataSet.params);
		fingerprint.dataFormat = $.extend(true, {}, dataSet.dataFormat);
		
		po.inflatePreviewFingerprint(fingerprint, dataSet);
		
		return fingerprint;
	};
	
	po.inPreviewAction = function(boolVal)
	{
		if(boolVal === undefined)
			return (po._inPreviewAction == true);
		
		po._inPreviewAction = boolVal;
	};
	
	po.isPreviewSuccess = function()
	{
		if(po.isAddAction || po.isCopyAction)
			return (po._isPreviewSuccess === true);
		else
			return (po._isPreviewSuccess !== false);
	};
	
	po.beforeSubmitFormWithPreview = function(action)
	{
		if(po.inPreviewAction())
		{
			action.url = ($.isFunction(po.previewUrl) ? po.previewUrl() : po.previewUrl);
			action.options.defaultSuccessCallback = false;
			action.options.success = function(response)
			{
				po.handlePreviewSuccess(response);
			};
			action.options.error = function(jqXHR)
			{
				po.handlePreviewError(jqXHR);
			};
			
			var pm = po.vuePageModel();
			var previewQuery = pm.previewQuery;
			po.trimPreviewQueryFetchSize(previewQuery);
			
			action.options.data = { dataSet: action.options.data, query: po.vueRaw(previewQuery), view: po.isViewAction };
			
			po._prevPreviewFingerprint = po.toPreviewFingerprint(action.options.data.dataSet);
		}
		else
		{
			var myPreviewFingerprint = po.toPreviewFingerprint(action.options.data);
			if(!$.equalsForSameType(myPreviewFingerprint, po._prevPreviewFingerprint)
					|| !po.isPreviewSuccess())
			{
				$.tipInfo("<@spring.message code='dataSet.previewRequired' />");
				return false;
			}
		}
		
		return true;
	};
	
	po.trimPreviewQueryFetchSize = function(dataSetQuery)
	{
		var resultFetchSize = $.parseIntWithDefault(dataSetQuery.resultFetchSize, 100);
		dataSetQuery.resultFetchSize = (resultFetchSize < 1 ? 100 : resultFetchSize);
	};
	
	po.handlePreviewSuccess = function(response)
	{
		po._isPreviewSuccess = true;
		
		var fm = po.vueFormModel();
		var pm = po.vuePageModel();
		
		pm.previewError = false;
		pm.previewPanelShow = true;
		
		if(!fm.mutableModel && !pm.isReadonlyAction && pm.autoGenerateField)
		{
			fm.fields = response.fields;
			pm.selectedFields = [];
			
			if(po._prevPreviewFingerprint)
				po._prevPreviewFingerprint.fields = $.extend(true, [], response.fields);
		}
		
		var previewColumns = [];
		$.each(fm.fields, function(i, p)
		{
			previewColumns.push({ name: p.name, label: p.name, style: "" });	
		});
		
		if(fm.mutableModel)
		{
			previewColumns.push({ name: po.previewColumnAllFieldName, label: "<@spring.message code='dataSet.mutableModelDataDetail' />", style: "min-width:25rem;" });
		}
		
		pm.previewColumns = previewColumns;
		pm.previewResultDatas = $.wrapAsArray(response.result && response.result.data ? response.result.data : []);
		pm.previewTplResult = response.templateResult;
		
		if(fm.mutableModel)
		{
			$.each(pm.previewResultDatas, function(idx, rd)
			{
				rd[po.previewColumnAllFieldName] = $.toJsonString(rd);
			});
		}
	};
	
	po.handlePreviewError = function(jqXHR)
	{
		po._isPreviewSuccess = false;
		
		var pm = po.vuePageModel();
		
		pm.previewError = true;
		pm.previewPanelShow = true;
		
		var er = $.getResponseJson(jqXHR);
		var msg = (er && er.data ? er.data : null);
		msg = (msg ? msg : (er && er.message ? er.message : ""));
		
		pm.previewTplResult = msg;
	};
	
	po.handlePreviewInvalidForm = function()
	{
		if(po.inPreviewAction())
		{
			po.vueUnref("${pid}previewPanelEle").hide();
		}
	};
	
	po.triggerPreview = function()
	{
		po.elementOfId("${pid}previewPanelBtn").click();
	};
	
	po.submitPreview = function()
	{
		po.inPreviewAction(true);
		
		try
		{
			po.form().submit();
		}
		finally
		{
			po.inPreviewAction(false);
		}
	};

	po.inParamFormSubmitAction = function(boolVal)
	{
		if(boolVal === undefined)
			return (po._inParamFormSubmitAction == true);
		
		po._inParamFormSubmitAction = boolVal;
	};
	
	po.inflatePreviewParamPanel = function()
	{
		var wrapper = $(".paramvalue-form-wrapper", po.elementOfId("${pid}previewParamPanel", document.body));
		var pm = po.vuePageModel();
		
		var formOptions = $.extend(
		{
			submitText: "<@spring.message code='preview' />",
			yesText: "<@spring.message code='yes' />",
			noText: "<@spring.message code='no' />",
			paramValues: po.vueRaw(pm.previewQuery.paramValues),
			readonly: false,
			render: function()
			{
				$("select, input[type='text'], textarea", this).addClass("p-inputtext p-component w-full");
				$("button", this).addClass("p-button p-component");
				$.focusOnFirstInput(this);
			},
			submit: function()
			{
				pm.previewQuery.paramValues = chartFactory.chartSetting.getDataSetParamValueObj(this);
				
				po.inParamFormSubmitAction(true);
				po.triggerPreview();
			}
		});
		
		chartFactory.chartSetting.removeDatetimePickerRoot();
		wrapper.empty();
		
		var fm = po.vueFormModel();
		var params = $.extend(true, [], po.vueRaw(fm.params));
		
		chartFactory.chartSetting.renderDataSetParamValueForm(wrapper, params, formOptions);
	};
	
	po.vuePageModel(
	{
		//XXX 临时保存事件对象，在ajax响应后再打开面板会报错，所以采用先透明打开再显现的方案
		previewPanelShow: false,
		previewQuery: { resultFetchSize: 100, paramValues: {} },
		previewColumns: [],
		previewResultDatas: [],
		previewTplResult: "",
		previewError: false
	});
	
	po.vueRef("${pid}previewPanelEle", null);
	po.vueRef("${pid}previewTplResultEle", null);
	po.vueRef("${pid}previewParamPanelEle", null);

	po.vueMethod(
	{
		submitPreview: function()
		{
			po.submitPreview();
		},
		onPreview: function(e)
		{
			var fm = po.vueFormModel();
			
			if(fm.params.length == 0 || po.inParamFormSubmitAction())
			{
				var pm = po.vuePageModel();
				pm.previewPanelShow = false;
				po.trimPreviewQueryFetchSize(pm.previewQuery);
				
				po.vueUnref("${pid}previewPanelEle").show(e);
				po.submitPreview();
				po.inParamFormSubmitAction(false);
			}
			else
			{
				//避免参数面板被隐藏
				e.stopPropagation();
				
				po.elementOfId("${pid}paramPanelBtn").click();
			}
		},
		togglePreviewTplResultPanel: function(e)
		{
			po.vueUnref("${pid}previewTplResultEle").toggle(e);
		},
		showPreviewParamPanel: function(e)
		{
			po.vueUnref("${pid}previewParamPanelEle").show(e);
		},
		onPreviewParamPanelShow: function(e)
		{
			po.inflatePreviewParamPanel();
		},
		onPreviewParamPanelHide: function(e)
		{
			var wrapper = $(".paramvalue-form-wrapper", po.elementOfId("${pid}previewParamPanel", document.body));
			chartFactory.chartSetting.destroyDataSetParamValueForm(wrapper);
		},
		formatPreviewColValue: function(data, name)
		{
			var value = (data ? data[name] : "");
			
			if(value && $.isTypeString(value) && value.length > 1003)
			{
				value = value.substr(0, 1000) + "...";
			}
			
			return value;
		}
	});

	po.vueMounted(function()
	{
		var fm = po.vueFormModel();
		po._prevPreviewFingerprint = po.toPreviewFingerprint(po.vueRaw(fm));
	});
})
(${pid});
</script>
