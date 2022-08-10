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
<p-dialog header="<@spring.message code='gridLayout' />" append-to="body"
	position="center" :dismissable-mask="true"
	v-model:visible="pm.vepss.gridLayoutShown" @show="onVeGridLayoutPanelShow">
	<div class="page page-form">
		<form id="${pid}veGridLayoutForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veGridLayoutRows" class="field-label col-12 mb-2">
						<@spring.message code='dashboard.veditor.gridLayout.rows' />
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
						<@spring.message code='dashboard.veditor.gridLayout.columns' />
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
<p-dialog header="<@spring.message code='textElement' />" append-to="body"
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
<script>
(function(po)
{
	po.vuePageModel(
	{
		//可视编辑操作对话框是否显示
		vepss:
		{
			gridLayoutShown: false,
			textElementShown: false
		},
		//可视编辑操作对话框表单模型
		vepms:
		{
			gridLayout: { fillParent: false },
			textElement: {}
		},
		veGridLayoutPanelShowFillParent: false
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
	
	po.showVeGridLayoutPanel = function(showFillParent)
	{
		showFillParent = (showFillParent == null ? false : showFillParent);
		
		var pm = po.vuePageModel();
		
		pm.veGridLayoutPanelShowFillParent = showFillParent;
		pm.vepms.gridLayout.fillParent = showFillParent;
		pm.vepss.gridLayoutShown = true;
	};

	po.showVeTextElementPanel = function()
	{
		var pm = po.vuePageModel();
		pm.vepss.textElementShown = true;
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
					pm.vepss.gridLayoutShown = false;
			});
		},
		
		onVeTextElementPanelShow: function()
		{
			var pm = po.vuePageModel();
			var form = po.elementOfId("${pid}veTextElementForm", document.body);
			
			po.setupSimpleForm(form, pm.vepms.textElement, function()
			{
			});
		}
	});
})
(${pid});
</script>