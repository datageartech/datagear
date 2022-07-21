<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
是否选项JS片段。
-->
<script>
(function(po)
{
	po.formatBooleanValue = function(val)
	{
		return (val == true || val == "true" ? "<@spring.message code='true' />" : "<@spring.message code='false' />");
	};
	
	po.vueRef("booleanOptions",
	[
		{name: "<@spring.message code='true' />", value: true},
		{name: "<@spring.message code='false' />", value: false}
	]);
})
(${pid});
</script>
