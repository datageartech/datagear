<#--
数据集表单页：名称输入框片段
-->
<input type="hidden" name="id" value="${(dataSet.id)!''?html}" />
<#include "../../include/analysisProjectAware_form_select.ftl" >
<div class="form-item form-item-name">
	<div class="form-item-label">
		<label><@spring.message code='dataSet.name' /></label>
	</div>
	<div class="form-item-value form-item-value-name">
		<input type="text" name="name" value="${(dataSet.name)!''?html}" class="ui-widget ui-widget-content" />
	</div>
</div>