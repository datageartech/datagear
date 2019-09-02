<#--
数据源表权限JS片段。

依赖：
page_js_obj.ftl
page_obj_data_permission.ftl
-->
<script type="text/javascript">
(function(po)
{
	po.canReadTableData = function(schemaOrPermission)
	{
		return po.canRead(schemaOrPermission);
	};
	
	po.canEditTableData = function(schemaOrPermission)
	{
		return po.canEdit(schemaOrPermission);
	};
	
	po.canDeleteTableData = function(schemaOrPermission)
	{
		return po.canDelete(schemaOrPermission);
	};
	
	po.canEditSchema = function(schema, user)
	{
		if(user.admin)
			return true;
		
		if(!po.canEdit(schema))
			return false;
		
		if(!schema.createUser)
			return false;
		
		return schema.createUser.id = user.id;
	};

	po.canDeleteSchema = function(schema, user)
	{
		if(user.admin)
			return true;
		
		if(!po.canEdit(schema))
			return false;
		
		if(!schema.createUser)
			return false;
		
		return schema.createUser.id = user.id;
	};
	
	po.canAuthorizeSchema = function(schema, user)
	{
		if(user.admin)
			return true;
		
		if(user.anonymous)
			return false;
		
		if(!po.canDelete(schema))
			return false;
		
		if(!schema.createUser)
			return false;
		
		return schema.createUser.id == user.id;
	};
})
(${pageId});
</script>
