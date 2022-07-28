<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
<p-overlaypanel ref="previewPanelEle" append-to="body"
	:show-close-icon="true" id="${pid}previewPanel" :class="{'opacity-0': !tm.previewPanelShow}">
	<div class="flex flex-column">
		<div class="flex-grow-0 mb-2">
			<label class="text-lg font-bold">
				<@spring.message code='preview' />
			</label>
		</div>
		<div class="flex-grow-1" style="width:50vw;height:50vh;">
			<p-datatable :value="tm.previewResultDatas" :scrollable="true" scroll-height="flex" striped-rows class="table-sm">
				<p-column v-for="col in tm.previewColumns"
					:field="col.name" :header="col.label" :sortable="false"
					:key="col.name">
				</p-column>
			</p-datatable>
		</div>
		<div class="flex-grow-0 mt-2">
			<div class="flex flex-row">
				<div class="flex-grow-1 flex justify-content-start">
					<div class="p-inputgroup" style="max-width:10rem">
						<p-inputtext v-model="tm.previewFetchSize" type="text" class="input p-inputtext-sm"
							maxlength="10" @keydown.enter="submitPreview" title="<@spring.message code='fetchSize' />">
						</p-inputtext>
						<p-button icon="pi pi-search" type="button" class="p-button-secondary p-button-sm"
							@click="submitPreview">
						</p-button>
					</div>
				</div>
				<div class="flex-grow-1 flex justify-content-end">
					<p-button icon="pi pi-comment" type="button"
						aria:haspopup="true" aria-controls="${pid}previewTplResultPanel"
						@click="togglePreviewTplResultPanel" class="p-button-secondary p-button-sm">
					</p-button>
					<p-overlaypanel ref="previewTplResultEle" append-to="body"
						:show-close-icon="true" id="${pid}previewTplResultPanel">
						<p-textarea v-model="tm.previewTplResult" class="overflow-auto"
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
<p-overlaypanel ref="previewParamPanelEle" append-to="body"
	:show-close-icon="true" @show="onPreviewParamPanelShow" id="${pid}previewParamPanel" class="dataSet-preview-panel">
	<div class="mb-2">
		<label class="text-lg font-bold">
			<@spring.message code='parameter' />
		</label>
	</div>
	<div class="preview-param-form-wrapper overflow-auto"></div>
</p-overlaypanel>
<script>
(function(po)
{
	//需实现
	po.previewUrl = "#";
	
	po.inPreviewAction = function(boolVal)
	{
		if(boolVal === undefined)
			return (po._inPreviewAction == true);
		
		po._inPreviewAction = boolVal;
	};
	
	po.inflateIfPreviewAction = function(action, data)
	{
		if(!po.inPreviewAction())
			return;
		
		action.url = po.previewUrl;
		action.options.defaultSuccessCallback = false;
		action.options.success = function(response)
		{
			po.handlePreviewResponse(response);
		};
		action.options.error = function()
		{
			po.vueUnref("previewPanelEle").hide();
		};
		
		var tm = po.vueTmpModel();
		var previewQuery = tm.previewQuery;
		po.trimPreviewQueryFetchSize(previewQuery);
		
		action.options.data = { dataSet: data, query: po.vueRaw(previewQuery) };
	};
	
	po.trimPreviewQueryFetchSize = function(dataSetQuery)
	{
		var resultFetchSize = $.parseIntWithDefault(dataSetQuery.resultFetchSize, 100);
		dataSetQuery.resultFetchSize = (resultFetchSize < 1 ? 100 : resultFetchSize);
	};
	
	po.handlePreviewResponse = function(response)
	{
		var pm = po.vuePageModel();
		var tm = po.vueTmpModel();
		
		tm.previewPanelShow = true;
		
		if(!pm.mutableModel && pm.properties.length == 0)
			pm.properties = response.properties;
		
		var previewColumns = [];
		$.each(pm.properties, function(i, p)
		{
			previewColumns.push({ name: p.name, label: p.name });	
		});
		
		tm.previewColumns = previewColumns;
		tm.previewResultDatas = (response.result && response.result.data ? response.result.data : []);
		tm.previewTplResult = response.templateResult;
	};
	
	po.handlePreviewInvalidForm = function()
	{
		if(po.inPreviewAction())
		{
			po.vueUnref("previewPanelEle").hide();
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
		var wrapper = $(".preview-param-form-wrapper", po.elementOfId("${pid}previewParamPanel", document.body));
		var tm = po.vueTmpModel();
		
		var formOptions = $.extend(
		{
			submitText: "<@spring.message code='preview' />",
			yesText: "<@spring.message code='yes' />",
			noText: "<@spring.message code='no' />",
			paramValues: po.vueRaw(tm.previewQuery.paramValues),
			render: function()
			{
				$("select, input[type='text'], textarea", this).addClass("p-inputtext p-component w-full");
				$("button", this).addClass("p-button p-component");
			},
			submit: function()
			{
				tm.previewQuery.paramValues = chartFactory.chartSetting.getDataSetParamValueObj(this);
				
				po.inParamFormSubmitAction(true);
				po.triggerPreview();
			}
		});
		
		chartFactory.chartSetting.removeDatetimePickerRoot();
		wrapper.empty();
		
		var pm = po.vuePageModel();
		var params = $.extend(true, [], po.vueRaw(pm).params);
		
		chartFactory.chartSetting.renderDataSetParamValueForm(wrapper, params, formOptions);
	};
	
	po.vueTmpModel(
	{
		//XXX 临时保存事件对象，在ajax响应后再打开面板会报错，所以采用先透明打开再显现的方案
		previewPanelShow: false,
		previewQuery: { resultFetchSize: 100, paramValues: {} },
		previewFetchSize: 100,
		previewColumns: [],
		previewResultDatas: [],
		previewTplResult: ""
	});
	
	po.vueRef("previewPanelEle", null);
	po.vueRef("previewTplResultEle", null);
	po.vueRef("previewParamPanelEle", null);

	po.vueMethod(
	{
		submitPreview: function()
		{
			po.submitPreview();
		},
		onPreview: function(e)
		{
			var pm = po.vuePageModel();
			
			if(pm.params.length == 0 || po.inParamFormSubmitAction())
			{
				var tm = po.vueTmpModel();
				tm.previewPanelShow = false;
				po.trimPreviewQueryFetchSize(tm.previewQuery);
				
				po.vueUnref("previewPanelEle").show(e);
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
			po.vueUnref("previewTplResultEle").toggle(e);
		},
		showPreviewParamPanel: function(e)
		{
			po.vueUnref("previewParamPanelEle").show(e);
		},
		onPreviewParamPanelShow: function(e)
		{
			po.inflatePreviewParamPanel();
		}
	});
})
(${pid});
</script>
