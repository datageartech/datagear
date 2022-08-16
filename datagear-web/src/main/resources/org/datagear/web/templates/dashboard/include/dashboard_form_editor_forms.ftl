<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
看板编辑器表单面板HTML
注意：
这些HTML不能直接写在dashboard_form_editor.ftl内，
因为会出现嵌套form，这里面板里的form元素会被vue解析剔除

依赖：
page_boolean_options.ftl

-->
<form id="${pid}visualEditorLoadForm" action="#" method="POST" style="display:none;">
	<input type="hidden" name="DG_EDIT_TEMPLATE" value="true" />
	<textarea name="DG_TEMPLATE_CONTENT"></textarea>
</form>

<p-dialog :header="pm.vepts.gridLayout" append-to="body"
	position="center" :dismissable-mask="true"
	v-model:visible="pm.vepss.gridLayoutShown" @show="onVeGridLayoutPanelShow">
	<div class="page page-form">
		<form id="${pid}veGridLayoutForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veGridLayoutRows" class="field-label col-12 mb-2">
						<@spring.message code='rowCount' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veGridLayoutRows" v-model="pm.vepms.gridLayout.rows" type="text"
							class="help-target input w-full" name="rows" maxlength="10" autofocus>
						</p-inputtext>
						<div class="p-buttonset mt-1 text-sm">
							<p-button type="button" class="help-src p-button-secondary" help-value="1">
								<@spring.message code='dashboard.veditor.gridLayout.rows.1r' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="2">
								<@spring.message code='dashboard.veditor.gridLayout.rows.2r' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="3">
								<@spring.message code='dashboard.veditor.gridLayout.rows.3r' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="4">
								<@spring.message code='dashboard.veditor.gridLayout.rows.4r' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="5">
								<@spring.message code='dashboard.veditor.gridLayout.rows.5r' />
							</p-button>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veGridLayoutColumns" class="field-label col-12 mb-2">
						<@spring.message code='columnCount' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veGridLayoutColumns" v-model="pm.vepms.gridLayout.columns" type="text"
							class="help-target input w-full" name="columns" maxlength="10">
						</p-inputtext>
						<div class="p-buttonset mt-1 text-sm">
							<p-button type="button" class="help-src p-button-secondary" help-value="1">
								<@spring.message code='dashboard.veditor.gridLayout.columns.1c' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="2">
								<@spring.message code='dashboard.veditor.gridLayout.columns.2c' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="3">
								<@spring.message code='dashboard.veditor.gridLayout.columns.3c' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="4">
								<@spring.message code='dashboard.veditor.gridLayout.columns.4c' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="5">
								<@spring.message code='dashboard.veditor.gridLayout.columns.5c' />
							</p-button>
						</div>
					</div>
				</div>
				<div class="field grid" v-if="pm.veGridLayoutPanelShowFillParent">
					<label for="${pid}veGridLayoutFillParent" class="field-label col-12 mb-2"
						title="<@spring.message code='dashboard.veditor.gridLayout.fillParent.desc' />">
						<@spring.message code='dashboard.veditor.gridLayout.fillParent' />
					</label>
					<div class="field-input col-12">
						<p-selectbutton id="${pid}veGridLayoutFillParent" v-model="pm.vepms.gridLayout.fillParent" :options="pm.booleanOptions"
							option-label="name" option-value="value" class="input w-full">
						</p-selectbutton>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.textElement" append-to="body"
	position="center" :dismissable-mask="true"
	v-model:visible="pm.vepss.textElementShown" @show="onVeTextElementPanelShow">
	<div class="page page-form">
		<form id="${pid}veTextElementForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veTextElementContent" class="field-label col-12 mb-2">
						<@spring.message code='textContent' />
					</label>
					<div class="field-input col-12">
						<p-textarea id="${pid}veTextElementContent" v-model="pm.vepms.textElement.content"
							class="input w-full" name="content" autofocus>
						</p-textarea>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.image" append-to="body"
	position="center" :dismissable-mask="true"
	v-model:visible="pm.vepss.imageShown" @show="onVeImagePanelShow">
	<div class="page page-form">
		<form id="${pid}veImageForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veImageSrc" class="field-label col-12 mb-2"
						 title="<@spring.message code='dashboard.veditor.image.src.desc' />">
						<@spring.message code='url' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veImageSrc" v-model="pm.vepms.image.src" type="text"
							class="input w-full" name="src" autofocus>
						</p-inputtext>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veImageWdith" class="field-label col-12 mb-2"
						 title="<@spring.message code='dashboard.veditor.image.width.desc' />">
						<@spring.message code='width' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veImageWdith" v-model="pm.vepms.image.width" type="text"
							class="input w-full" name="width">
						</p-inputtext>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veImageHeight" class="field-label col-12 mb-2"
						 title="<@spring.message code='dashboard.veditor.image.height.desc' />">
						<@spring.message code='height' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veImageHeight" v-model="pm.vepms.image.height" type="text"
							class="input w-full" name="height">
						</p-inputtext>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.hyperlink" append-to="body"
	position="center" :dismissable-mask="true"
	v-model:visible="pm.vepss.hyperlinkShown" @show="onVeHyperlinkPanelShow">
	<div class="page page-form">
		<form id="${pid}veHyperlinkForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veHyperlinkHref" class="field-label col-12 mb-2"
						 title="<@spring.message code='dashboard.veditor.hyperlink.href.desc' />">
						<@spring.message code='url' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veHyperlinkHref" v-model="pm.vepms.hyperlink.href" type="text"
							class="input w-full" name="href" autofocus>
						</p-inputtext>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veHyperlinkContent" class="field-label col-12 mb-2">
						<@spring.message code='textContent' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veHyperlinkContent" v-model="pm.vepms.hyperlink.content" type="text"
							class="input w-full" name="content">
						</p-inputtext>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veHyperlinkTarget" class="field-label col-12 mb-2">
						<@spring.message code='target' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veHyperlinkTarget" v-model="pm.vepms.hyperlink.target" type="text"
							class="help-target input w-full" name="target">
						</p-inputtext>
						<div class="p-buttonset mt-1 text-sm">
							<p-button type="button" class="help-src p-button-secondary" help-value="_blank">
								<@spring.message code='dashboard.veditor.hyperlink.target._blank' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="_self">
								<@spring.message code='dashboard.veditor.hyperlink.target._self' />
							</p-button>
						</div>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.video" append-to="body"
	position="center" :dismissable-mask="true"
	v-model:visible="pm.vepss.videoShown" @show="onVeVideoPanelShow">
	<div class="page page-form">
		<form id="${pid}veVideoForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veVideoSrc" class="field-label col-12 mb-2"
						 title="<@spring.message code='dashboard.veditor.video.src.desc' />">
						<@spring.message code='url' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veVideoSrc" v-model="pm.vepms.video.src" type="text"
							class="input w-full" name="src" autofocus>
						</p-inputtext>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veVideoWdith" class="field-label col-12 mb-2"
						 title="<@spring.message code='dashboard.veditor.video.width.desc' />">
						<@spring.message code='width' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veVideoWdith" v-model="pm.vepms.video.width" type="text"
							class="input w-full" name="width">
						</p-inputtext>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veVideoHeight" class="field-label col-12 mb-2"
						 title="<@spring.message code='dashboard.veditor.video.height.desc' />">
						<@spring.message code='height' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veVideoHeight" v-model="pm.vepms.video.height" type="text"
							class="input w-full" name="height">
						</p-inputtext>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.dashboardSize" append-to="body"
	position="center" :dismissable-mask="true"
	v-model:visible="pm.vepss.dashboardSizeShown" @show="onVeDashboardSizePanelShow">
	<div class="page page-form">
		<form id="${pid}veDashboardSizeForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veDashboardSizeWdith" class="field-label col-12 mb-2">
						<@spring.message code='width' />
					</label>
					<div class="field-input col-12">
						<div class="p-inputgroup">
							<p-inputtext id="${pid}veDashboardSizeWdith" v-model="pm.vepms.dashboardSize.width" type="text"
								class="input w-full" name="width">
							</p-inputtext>
							<span class="p-inputgroup-addon">px</span>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veDashboardSizeHeight" class="field-label col-12 mb-2">
						<@spring.message code='height' />
					</label>
					<div class="field-input col-12">
						<div class="p-inputgroup">
							<p-inputtext id="${pid}veDashboardSizeHeight" v-model="pm.vepms.dashboardSize.height" type="text"
								class="input w-full" name="height">
							</p-inputtext>
							<span class="p-inputgroup-addon">px</span>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veDashboardSizeScale" class="field-label col-12 mb-2">
						<@spring.message code='scale' />
					</label>
					<div class="field-input col-12">
						<p-selectbutton id="${pid}veDashboardSizeScale" v-model="pm.vepms.dashboardSize.scale" :options="pm.dashboardSizeScaleOptions"
							option-label="name" option-value="value" class="input w-full">
						</p-selectbutton>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
				<p-button type="button" label="<@spring.message code='resetToDefault' />" class="p-button-secondary" @click="onVeDashboardSizeResetToDft"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.chartOptions" append-to="body"
	position="center" :dismissable-mask="true"
	v-model:visible="pm.vepss.chartOptionsShown" @show="onVeChartOptionsPanelShow">
	<div class="page page-form">
		<form id="${pid}veChartOptionsForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto" style="min-width:40vw;">
				<div class="field grid">
					<label for="${pid}veChartOptionsContent" class="field-label col-12 mb-2">
						<@spring.message code='chartOptions' />
					</label>
					<div class="field-input col-12">
						<div id="${pid}veChartOptionsContent" class="code-editor-wrapper input p-component p-inputtext w-full" style="height:30vh;">
							<div id="${pid}veChartOptionsCodeEditor" class="code-editor"></div>
						</div>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<script>
(function(po)
{
	po.vuePageModel(
	{
		//可视编辑操作对话框是否显示
		vepss:
		{
			gridLayoutShown: false,
			textElementShown: false,
			imageShown: false,
			hyperlinkShown: false,
			videoShown: false,
			dashboardSizeShown: false,
			chartOptionsShown: false
		},
		//可视编辑操作对话框标题
		vepts:
		{
			gridLayout: "<@spring.message code='gridLayout' />",
			textElement: "<@spring.message code='textElement' />",
			image: "<@spring.message code='image' />",
			hyperlink: "<@spring.message code='hyperlink' />",
			video: "<@spring.message code='video' />",
			dashboardSize: "<@spring.message code='dashboardSize' />",
			chartOptions: "<@spring.message code='chartOptions' />"
		},
		//可视编辑操作对话框表单模型
		vepms:
		{
			gridLayout: { fillParent: false },
			textElement: { content: "" },
			image: {},
			hyperlink: {},
			video: {},
			dashboardSize: { scale: "auto" },
			chartOptions: { value: "" },
		},
		//可视编辑操作对话框提交处理函数
		veshs:
		{
			textElement: function(model){},
			image: function(model){},
			hyperlink: function(model){},
			video: function(model){},
			chartOptions: function(model){}
		},
		veGridLayoutPanelShowFillParent: false,
		dashboardSizeScaleOptions:
		[
			{ name: "<@spring.message code='auto' />", value: "auto" },
			{ name: "100%", value: 100 },
			{ name: "75%", value: 75 },
			{ name: "50%", value: 50 },
			{ name: "25%", value: 25 }
		]
	});
	
	po.initVePanelHelperSrc = function(form, formModel)
	{
		po.element(".help-src", form).click(function()
		{
			var $this = $(this);
			var helpValue = ($this.attr("help-value") || "");
			var helpTarget = po.element(".help-target", $this.closest(".field-input"));
			var targetName = helpTarget.attr("name");
			
			if(targetName)
				formModel[targetName] = helpValue;
			
			helpTarget.focus();
		});
	};

	po.prettyChartOptionsStr = function(chartOptionsStr)
	{
		if(chartOptionsStr && /^\s*[\{\[]/.test(chartOptionsStr))
		{
			var obj = chartFactory.evalSilently(chartOptionsStr, chartOptionsStr);
			
			if(!chartFactory.isString(obj))
				chartOptionsStr = JSON.stringify(obj, null, '\t');
		}
		
		return (chartOptionsStr || "");
	};
	
	po.showVeGridLayoutPanel = function(showFillParent)
	{
		showFillParent = (showFillParent == null ? false : showFillParent);
		
		var pm = po.vuePageModel();
		
		pm.veGridLayoutPanelShowFillParent = showFillParent;
		pm.vepms.gridLayout.fillParent = showFillParent;
		pm.vepss.gridLayoutShown = true;
	};

	po.showVeTextElementPanel = function(submitHandler, model)
	{
		var pm = po.vuePageModel();
		pm.veshs.textElement = submitHandler;
		pm.vepms.textElement = $.extend(true, {}, model);
		pm.vepss.textElementShown = true;
	};
	
	po.showVeImagePanel = function(submitHandler, model)
	{
		var pm = po.vuePageModel();
		pm.veshs.image = submitHandler;
		pm.vepms.image = $.extend(true, {}, model);
		pm.vepss.imageShown = true;
	};
	
	po.showVeHyperlinkPanel = function(submitHandler, model)
	{
		var pm = po.vuePageModel();
		pm.veshs.hyperlink = submitHandler;
		pm.vepms.hyperlink = $.extend(true, {}, model);
		pm.vepss.hyperlinkShown = true;
	};
	
	po.showVeVideoPanel = function(submitHandler, model)
	{
		var pm = po.vuePageModel();
		pm.veshs.video = submitHandler;
		pm.vepms.video = $.extend(true, {}, model);
		pm.vepss.videoShown = true;
	};
	
	po.showVeDashboardSizePanel = function(model)
	{
		var pm = po.vuePageModel();
		pm.vepms.dashboardSize = $.extend(true, {}, model);
		pm.vepss.dashboardSizeShown = true;
	};

	po.showVeChartOptionsPanel = function(submitHandler, model)
	{
		var pm = po.vuePageModel();
		pm.veshs.chartOptions = submitHandler;
		pm.vepms.chartOptions = $.extend(true, {}, model);
		pm.vepss.chartOptionsShown = true;
	};
	
	po.vueMethod(
	{
		onVeGridLayoutPanelShow: function()
		{
			var pm = po.vuePageModel();
			var form = po.elementOfId("${pid}veGridLayoutForm", document.body);
			
			po.initVePanelHelperSrc(form, pm.vepms.gridLayout);
			
			po.setupSimpleForm(form, pm.vepms.gridLayout, function()
			{
				if(po.insertVeGridLayout(pm.vepms.gridLayout) !== false)
				{
					pm.vepms.gridLayout = { fillParent: false };
					pm.vepss.gridLayoutShown = false;
				}
			});
		},
		
		onVeTextElementPanelShow: function()
		{
			var pm = po.vuePageModel();
			var form = po.elementOfId("${pid}veTextElementForm", document.body);
			
			po.setupSimpleForm(form, pm.vepms.textElement, function()
			{
				if(pm.veshs.textElement(pm.vepms.textElement) !== false)
				{
					pm.vepms.textElement = {};
					pm.vepss.textElementShown = false;
				}
			});
		},
		
		onVeImagePanelShow: function()
		{
			var pm = po.vuePageModel();
			var form = po.elementOfId("${pid}veImageForm", document.body);
			
			po.setupSimpleForm(form, pm.vepms.image, function()
			{
				if(pm.veshs.image(pm.vepms.image) !== false)
				{
					pm.vepms.image = {};
					pm.vepss.imageShown = false;
				}
			});
		},
		
		onVeHyperlinkPanelShow: function()
		{
			var pm = po.vuePageModel();
			var form = po.elementOfId("${pid}veHyperlinkForm", document.body);
			
			po.initVePanelHelperSrc(form, pm.vepms.hyperlink);
			
			po.setupSimpleForm(form, pm.vepms.hyperlink, function()
			{
				if(pm.veshs.hyperlink(pm.vepms.hyperlink) !== false)
				{
					pm.vepms.hyperlink = {};
					pm.vepss.hyperlinkShown = false;
				}
			});
		},
		
		onVeVideoPanelShow: function()
		{
			var pm = po.vuePageModel();
			var form = po.elementOfId("${pid}veVideoForm", document.body);
			
			po.setupSimpleForm(form, pm.vepms.video, function()
			{
				if(pm.veshs.video(pm.vepms.video) !== false)
				{
					pm.vepms.video = {};
					pm.vepss.videoShown = false;
				}
			});
		},
		
		onVeDashboardSizePanelShow: function()
		{
			var pm = po.vuePageModel();
			var form = po.elementOfId("${pid}veDashboardSizeForm", document.body);
			
			po.setupSimpleForm(form, pm.vepms.dashboardSize, function()
			{
				if(po.setVeDashboardSize(null, pm.vepms.dashboardSize) !== false)
				{
					pm.vepss.dashboardSizeShown = false;
				}
			});
		},
		
		onVeDashboardSizeResetToDft: function()
		{
			var pm = po.vuePageModel();
			
			if(po.setVeDashboardSize(null, {}) !== false)
			{
				pm.vepss.dashboardSizeShown = false;
			}
		},
		
		onVeChartOptionsPanelShow: function()
		{
			var pm = po.vuePageModel();
			var form = po.elementOfId("${pid}veChartOptionsForm", document.body);
			var codeEditorEle = po.elementOfId("${pid}veChartOptionsCodeEditor", form);
			
			var editorOptions =
			{
				value: "",
				matchBrackets: true,
				autoCloseBrackets: true,
				mode: {name: "javascript", json: true}
			};
			
			codeEditorEle.empty();
			var codeEditor = po.createCodeEditor(codeEditorEle, editorOptions);
			po.setCodeTextTimeout(codeEditor, po.prettyChartOptionsStr(pm.vepms.chartOptions.value), true);
			
			po.setupSimpleForm(form, pm.vepms.chartOptions, function()
			{
				pm.vepms.chartOptions.value = po.getCodeText(codeEditor);
				if(pm.veshs.chartOptions(pm.vepms.chartOptions) !== false)
				{
					pm.vepms.chartOptions = {};
					pm.vepss.chartOptionsShown = false;
				}
			});
		}
	});
})
(${pid});
</script>