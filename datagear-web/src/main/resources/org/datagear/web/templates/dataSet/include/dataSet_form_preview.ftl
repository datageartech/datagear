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
			<label class="field-label col-12 text-lg font-bold">
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
		
		action.options.data = { dataSet: data, query: {} };
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
		previewColumns: [],
		previewResultDatas: [],
	});
	
	po.vueRef("previewPanelEle", null);

	po.vueMethod(
	{
		onPreview: function(e)
		{
			var tm = po.vueTmpModel();
			tm.previewPanelShow = false;
			
			po.vueUnref("previewPanelEle").show(e);
			po.preview();
		}
	});
})
(${pid});
</script>
