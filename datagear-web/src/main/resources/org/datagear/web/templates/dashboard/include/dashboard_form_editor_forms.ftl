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
-->
<form id="${pid}visualEditorLoadForm" action="#" method="POST" style="display:none;">
	<input type="hidden" name="DG_EDIT_TEMPLATE" value="true" />
	<textarea name="DG_TEMPLATE_CONTENT"></textarea>
</form>
<p-dialog header="<@spring.message code='gridLayout' />" append-to="body"
	position="right" :dismissable-mask="true"
	v-model:visible="pm.vepss.gridLayoutShown" @show="onVeGridLayoutPanelShow">
	<form id="${pid}veGridLayoutForm" action="#">
		<div class="p-2">
			<div class="field grid">
				<label for="${pid}veGridLayoutRows" class="field-label col-12 mb-2">
					<@spring.message code='dashboard.veditor.gridLayout.rows' />
				</label>
				<div class="field-input col-12">
					<p-inputtext id="${pid}veGridLayoutRows" v-model="pm.vepms.gridLayout.rows" type="text"
						class="input w-full" name="rows" maxlength="10" autofocus>
					</p-inputtext>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}veGridLayoutColumns" class="field-label col-12 mb-2">
					<@spring.message code='dashboard.veditor.gridLayout.rows' />
				</label>
				<div class="field-input col-12">
					<p-inputtext id="${pid}veGridLayoutColumns" v-model="pm.vepms.gridLayout.columns" type="text"
						class="input w-full" name="columns" maxlength="10">
					</p-inputtext>
				</div>
			</div>
		</div>
		<div class="pt-3 text-center">
			<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
		</div>
	</form>
</p-dialog>
<p-dialog header="<@spring.message code='textElement' />" append-to="body"
	position="right" :dismissable-mask="true"
	v-model:visible="pm.vepss.textElementShown" @show="onVeTextElementPanelShow">
	<form id="${pid}veTextElementForm" action="#">
		<div class="p-2">
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
		<div class="pt-3 text-center">
			<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
		</div>
	</form>
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
			gridLayout: {},
			textElement: {}
		}
	});
	
	po.showVeGridLayoutPanel = function()
	{
		var pm = po.vuePageModel();
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
			
			po.setupSimpleForm(form, pm.vepms.gridLayout, function()
			{
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