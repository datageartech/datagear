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
<p-button id="${pid}previewBtn" type="button" label="<@spring.message code='preview' />"
	aria:haspopup="true" aria-controls="${pid}previewPanel"
	@click="onPreview" class="p-button-secondary">
</p-button>
<p-overlaypanel ref="previewPanelEle"
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
							maxlength="10" @keydown.enter="doPreview" title="<@spring.message code='fetchSize' />">
						</p-inputtext>
						<p-button icon="pi pi-search" class="p-button-secondary p-button-sm"
							@click="doPreview">
						</p-button>
					</div>
				</div>
				<div class="flex-grow-1 flex justify-content-end">
					<p-button icon="pi pi-comment"
						aria:haspopup="true" aria-controls="${pid}previewTplResultPanel"
						@click="togglePreviewTplResultPanel" class="p-button-secondary p-button-sm">
					</p-button>
					<p-overlaypanel ref="previewTplResultEle"
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
		var resultFetchSize = $.parseIntWithDefault(tm.previewFetchSize, 100);
		resultFetchSize = (resultFetchSize < 1 ? 100 : resultFetchSize);
		
		action.options.data = { dataSet: data, query: { resultFetchSize: resultFetchSize } };
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
		po.elementOfId("${pid}previewBtn").click();
	};
	
	po.preview = function()
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
	
	po.vueTmpModel(
	{
		//XXX 临时保存事件对象，在ajax响应后再打开面板会报错，所以采用先透明打开再显现的方案
		previewPanelShow: false,
		previewFetchSize: 100,
		previewColumns: [],
		previewResultDatas: [],
		previewTplResult: ""
	});
	
	po.vueRef("previewPanelEle", null);
	po.vueRef("previewTplResultEle", null);

	po.vueMethod(
	{
		doPreview: function()
		{
			po.preview();
		},
		onPreview: function(e)
		{
			var tm = po.vueTmpModel();
			tm.previewPanelShow = false;
			tm.previewFetchSize = (tm.previewFetchSize > 0 ? tm.previewFetchSize : 100);
			
			po.vueUnref("previewPanelEle").show(e);
			po.preview();
		},
		togglePreviewTplResultPanel: function(e)
		{
			po.vueUnref("previewTplResultEle").toggle(e);
		}
	});
})
(${pid});
</script>
