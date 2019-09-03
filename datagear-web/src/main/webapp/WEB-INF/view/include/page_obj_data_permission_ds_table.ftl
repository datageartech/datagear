<#--
数据源表权限JS片段。

依赖：
page_js_obj.ftl
-->
<#assign __podpSchema=statics['org.datagear.management.domain.Schema']>
<script type="text/javascript">
(function(po)
{
	po.canReadTableData = function(schemaOrPermission)
	{
		if(schemaOrPermission == null)
			return false;
		
		if(schemaOrPermission.dataPermission != undefined)
			schemaOrPermission = schemaOrPermission.dataPermission;
		
		return ${__podpSchema.PERMISSION_TABLE_DATA_READ} <= schemaOrPermission;
	};
	
	po.canEditTableData = function(schemaOrPermission)
	{
		if(schemaOrPermission == null)
			return false;
		
		if(schemaOrPermission.dataPermission != undefined)
			schemaOrPermission = schemaOrPermission.dataPermission;
		
		return ${__podpSchema.PERMISSION_TABLE_DATA_EDIT} <= schemaOrPermission;
	};
	
	po.canDeleteTableData = function(schemaOrPermission)
	{
		if(schemaOrPermission == null)
			return false;
		
		if(schemaOrPermission.dataPermission != undefined)
			schemaOrPermission = schemaOrPermission.dataPermission;
		
		return ${__podpSchema.PERMISSION_TABLE_DATA_DELETE} <= schemaOrPermission;
	};
	
	po.toTableDataPermissionLabel = function(schemaOrPermission)
	{
		if(po.canDeleteTableData(schemaOrPermission))
			return "<@spring.message code='authorization.permission.DELETE' />";
		else if(po.canEditTableData(schemaOrPermission))
			return "<@spring.message code='authorization.permission.EDIT' />";
		else if(po.canReadTableData(schemaOrPermission))
			return "<@spring.message code='authorization.permission.READ' />";
		else
			return "<@spring.message code='authorization.permission.NONE' />";
	};
})
(${pageId});
</script>
