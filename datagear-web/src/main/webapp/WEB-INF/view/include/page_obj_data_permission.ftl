<#--
数据权限JS片段。

依赖：
page_js_obj.jsp
-->
<#assign __podpAuthorization=statics['org.datagear.management.domain.Authorization']>
<script type="text/javascript">
(function(po)
{
	po.canRead = function(dataOrPermission)
	{
		if(dataOrPermission == null)
			return false;
		
		if(dataOrPermission.dataPermission != undefined)
			dataOrPermission = dataOrPermission.dataPermission;
		
		return ${__podpAuthorization.PERMISSION_READ_START} <= dataOrPermission;
	};
	
	po.canEdit = function(dataOrPermission)
	{
		if(dataOrPermission == null)
			return false;
		
		if(dataOrPermission.dataPermission != undefined)
			dataOrPermission = dataOrPermission.dataPermission;
		
		return ${__podpAuthorization.PERMISSION_EDIT_START} <= dataOrPermission;
	};
	
	po.canDelete = function(dataOrPermission)
	{
		if(dataOrPermission == null)
			return false;
		
		if(dataOrPermission.dataPermission != undefined)
			dataOrPermission = dataOrPermission.dataPermission;
		
		return ${__podpAuthorization.PERMISSION_DELETE_START} <= dataOrPermission;
	};
	
	po.toPermissionLabel = function(dataOrPermission)
	{
		if(po.canDelete(dataOrPermission))
			return "<@spring.message code='authorization.permission.DELETE' />";
		else if(po.canEdit(dataOrPermission))
			return "<@spring.message code='authorization.permission.EDIT' />";
		else if(po.canRead(dataOrPermission))
			return "<@spring.message code='authorization.permission.READ' />";
		else
			return "<@spring.message code='authorization.permission.NONE' />";
	};
})
(${pageId});
</script>
