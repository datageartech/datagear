<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
数据集表单页：名称输入框片段
-->
<input type="hidden" name="id" value="${(dataSet.id)!''}" />
<div class="form-item form-item-name form-item-analysisProjectAware">
	<div class="form-item-label">
		<label><@spring.message code='dataSet.name' /></label>
	</div>
	<div class="form-item-value">
		<input type="text" name="name" value="${(dataSet.name)!''}" class="ui-widget ui-widget-content ui-corner-all" />
	</div>
	<#include "../../include/analysisProjectAware_form_select.ftl" >
</div>
<div class="form-item form-item-mutableModel">
	<div class="form-item-label">
		<label title="<@spring.message code='dataSet.mutableModel.desc' />">
			<@spring.message code='dataSet.mutableModel' />
		</label>
	</div>
	<div class="form-item-value form-item-value-mutableModel">
		<div class="mutableModelRadios">
			<label for="${pageId}-mutableModelYes"><@spring.message code='yes' /></label>
	  		<input type="radio" id="${pageId}-mutableModelYes" name="mutableModel" value="true" <#if (dataSet.mutableModel)!false>checked="checked"</#if> />
			<label for="${pageId}-mutableModelNo"><@spring.message code='no' /></label>
	  		<input type="radio" id="${pageId}-mutableModelNo" name="mutableModel" value="false" <#if !((dataSet.mutableModel)!false)>checked="checked"</#if> />
  		</div>
	</div>
</div>