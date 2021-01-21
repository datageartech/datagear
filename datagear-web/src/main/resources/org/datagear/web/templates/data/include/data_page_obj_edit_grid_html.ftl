<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
编辑表格功能HTML片段。
-->
<#assign editGridFormPageId=(pageId +'_'+'egf')>
<div class="edit-grid">
	<div class="edit-grid-switch-wrapper">
		<label for="${pageId}-editGridSwitch"><@spring.message code='editGrid' /></label>
		<input id="${pageId}-editGridSwitch" type="checkbox" value="1" />
	</div>
	<div class="edit-grid-operation">
		<button type="button" class="edit-grid-button button-restore highlight" style="display: none;"><@spring.message code='restore' /></button>
		<button type="button" class="edit-grid-button button-restore-all highlight" style="display: none;"><@spring.message code='restoreAll' /></button>
		<button type="button" class="edit-grid-button button-save recommended" style="display: none;"><@spring.message code='save' /></button>
	</div>
</div>
<div id="${editGridFormPageId}" class="page-edit-grid-form">
	<div class="form-panel ui-widget ui-widget-content ui-corner-all ui-widget-shadow" tabindex="1">
		<div class="form-panel-title form-panel-dragger ui-corner-all ui-widget-header">
			<span class="close-icon ui-icon ui-icon-close"></span>
		</div>
		<form id="${editGridFormPageId}-form" method="POST" action="#">
		</form>
		<div class="form-panel-foot form-panel-dragger ui-widget-header">
		</div>
	</div>
</div>