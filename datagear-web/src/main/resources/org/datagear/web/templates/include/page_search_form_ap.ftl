<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
搜索表单支持功能-当前项目。

依赖：
page_search_form_filter.ftl
或
page_search_form.ftl
-->
<div class="col-12 py-0">
	<div class="flex align-items-center pt-1">
		<p-button type="button" :label="pm.searchAnalysisProject.name" icon="pi pi-folder"
			class="p-button-text p-button-plain p-button-sm text-left py-1 px-1"
			@click="onSelectCurrentAnalysisProject">
		</p-button>
		<p-button type="button" icon="pi pi-times"
			class="p-button-text p-button-plain p-button-sm py-1 px-1 opacity-50"
			@click="onClearCurrentAnalysisProject" v-if="pm.searchAnalysisProject.id != ''">
		</p-button>
	</div>
</div>
<script type="text/javascript">
(function(po)
{
	po.searchAnalysisProjectDft = function()
	{
		var re = { id: "", name: "<@spring.message code='allProject' />" };
		return re;
	};
	
	po.vuePageModel(
	{
		searchAnalysisProject: po.searchAnalysisProjectDft()
	});
	
	po.vueMethod(
	{
		onSelectCurrentAnalysisProject: function()
		{
			po.openTableDialog("/analysisProject/select",
			{
				pageParam:
				{
					select: function(analysisProject)
					{
						var pm = po.vuePageModel();
						pm.searchAnalysisProject = analysisProject;
					}
				}
			});
		},
		
		onClearCurrentAnalysisProject: function()
		{
			var pm = po.vuePageModel();
			pm.searchAnalysisProject = po.searchAnalysisProjectDft();
		}
	});
})
(${pid});
</script>
