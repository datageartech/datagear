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
图表属性值集表单

依赖：
page_boolean_options.ftl
page_palette.ftl
-->
<#assign FormPropertyType=statics['org.datagear.analysis.form.PropertyType']>
<#assign FormPropertyInputType=statics['org.datagear.analysis.form.PropertyInputType']>
<#assign JsonChartPluginPropertiesResolver=statics['org.datagear.analysis.support.JsonChartPluginPropertiesResolver']>
<form id="${pid}chartAttrValuesForm" class="chart-attr-values-form flex flex-column" :class="{readonly: pm.avoModel.readonly}">
	<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-scroll">
		<dg-obj-prop-field :obj-prop="pm.avoModel.pluginAttrForm" :prop-name-path=""
			:form-data="pm.avoModel.formData" :root-form-data="pm.avoModel.formData" :prop-type-def="pm.avoModel.FormPropertyType"
			:prop-input-type-def="pm.avoModel.FormPropertyInputType" :i18n="pm.avoModel.i18n"
			:ctrl-prop-name="pm.avoModel.ctrlPropName" :enable-options="pm.avoModel.enableOptions"
			:readonly="pm.avoModel.readonly">
		</dg-obj-prop-field>
	</div>
	<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
		<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
		<p-button type="button" label="<@spring.message code='clearEmpty' />" severity="danger" @click="onClearChartAttrValuesForm"
			v-if="pm.avoModel.showClearBtn">
		</p-button>
		
		<p-button v-for="(btn, btnIdx) in pm.avoModel.buttons" :key="btnIdx"
			type="button" class="p-button-secondary" :label="btn.name" @click="btn.clickHandler">
		</p-button>
	</div>
</form>
<script>
(function(po)
{
	var avo = (po.avo || (po.avo = {}));
	
	avo.FormPropertyType =
	{
		STRING: "${FormPropertyType.STRING}",
		BOOLEAN: "${FormPropertyType.BOOLEAN}",
		INTEGER: "${FormPropertyType.INTEGER}",
		NUMBER: "${FormPropertyType.NUMBER}",
		OBJECT: "${FormPropertyType.OBJECT}"
	};
	
	avo.FormPropertyInputType =
	{
		TEXT: "${FormPropertyInputType.TEXT}",
		SELECT: "${FormPropertyInputType.SELECT}",
		RADIO: "${FormPropertyInputType.RADIO}",
		CHECKBOX: "${FormPropertyInputType.CHECKBOX}",
		TEXTAREA: "${FormPropertyInputType.TEXTAREA}",
		COLOR: "${FormPropertyInputType.COLOR}"
	};
	
	avo.FormPropertyInputPayload =
	{
		//多选
		MULTIPLE: "multiple",
		//地图
		DG_MAP: "DG_MAP",
		
		//5.5.0旧版的下拉框inputPayload.multiple="repeat"值，表示可重复选取
		MultipleRepeat: "repeat"
	};
	
	avo.INPUT_PROPERTY_ADDITION_OLD_GROUP = "${JsonChartPluginPropertiesResolver.INPUT_PROPERTY_ADDITION_OLD_GROUP}";
	avo.chartAttrValuesFormEleId = "${pid}chartAttrValuesForm";
	
	po.i18n.confirmDeleteThisDataAsk = "<@spring.message code='confirmDeleteThisDataAsk' />";
	po.i18n.ungrouped = "<@spring.message code='ungrouped' />";
	po.i18n.unnamed = "<@spring.message code='unnamed' />";
	po.i18n.confirmClearAllChartAttr = "<@spring.message code='confirmClearAllChartAttr' />";
	po.i18n.none = "<@spring.message code='none' />";
	po.i18n.moveUp = "<@spring.message code='moveUp' />";
	po.i18n.moveDown = "<@spring.message code='moveDown' />";
	po.i18n.insert = "<@spring.message code='insert' />";
	po.i18n.del = "<@spring.message code='delete' />";
	po.i18n.enable = "<@spring.message code='enable' />";
	po.i18n.disable = "<@spring.message code='disable' />";
	po.i18n.actived = "<@spring.message code='actived' />";
	po.i18n.cleared = "<@spring.message code='cleared' />";
	po.i18n.activeOrClear = "<@spring.message code='activeOrClear' />";
	
	//page.js
	$.inflateChartAttrValuesForm(po);
})
(${pid});
</script>