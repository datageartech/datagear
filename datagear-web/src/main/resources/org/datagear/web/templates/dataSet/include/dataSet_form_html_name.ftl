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
	<div class="form-item-value form-item-value-name">
		<input type="text" name="name" value="${(dataSet.name)!''}" class="ui-widget ui-widget-content ui-corner-all" />
	</div>
	<#include "../../include/analysisProjectAware_form_select.ftl" >
</div>