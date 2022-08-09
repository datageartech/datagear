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
