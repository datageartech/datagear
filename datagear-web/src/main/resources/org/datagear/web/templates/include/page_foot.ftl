<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
页脚
-->
<#assign Global=statics['org.datagear.util.Global']>
<div class="page-foot text-center text-xs opacity-40 text-color-secondary pt-1">
	<a id="${pid}footAboutLink" href="javascript:void(0);" class="link text-color-secondary mr-1">
		<@spring.message code='module.about' />
	</a>
	<a href="${Global.WEB_SITE}/documentation/" target="_blank" class="link text-color-secondary mr-1">
		<@spring.message code='module.documentation' />
	</a>
	<a id="${pid}footChangelogLink" href="javascript:void(0);" class="link text-color-secondary mr-1">
		<@spring.message code='module.changelog' />
	</a>
	<a href="${Global.WEB_SITE}" target="_blank" class="link text-color-secondary">
		<@spring.message code='module.downloadLatestVersion' />
	</a>
</div>
<script>
(function(po)
{
	po.vueMounted(function()
	{
		po.elementOfId("${pid}footAboutLink").click(function()
		{
			po.open("/about");
		});
		
		po.elementOfId("${pid}footChangelogLink").click(function()
		{
			po.open("/changelog");
		});
	});
})
(${pid});
</script>