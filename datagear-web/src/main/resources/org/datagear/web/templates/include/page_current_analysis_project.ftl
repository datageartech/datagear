<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
当前项目。

依赖：
page_search_form_filter.ftl
或
page_search_form.ftl
-->
<#assign AbstractController=statics['org.datagear.web.controller.AbstractController']>
<#assign APIDDataFilterPagingQuery=statics['org.datagear.web.vo.APIDDataFilterPagingQuery']>
<div class="current-analysis-project-wrapper flex align-items-center">
	<p-button type="button" icon="pi pi-times"
		class="p-button-text p-button-plain p-button-sm py-1 px-1 opacity-70"
		@click="onClearCurrentAnalysisProject" v-if="pm.searchAnalysisProject.id != ''">
	</p-button>
	<p-button type="button" :label="pm.searchAnalysisProject.name" icon="pi pi-folder"
		class="ap-name-btn p-button-text p-button-plain p-button-sm text-left py-1 pl-1 pr-3"
		@click="onSelectCurrentAnalysisProject">
	</p-button>
</div>
<script type="text/javascript">
(function(po)
{
	po.currentAnalysisProjectCookieName = "${AbstractController.KEY_ANALYSIS_PROJECT_ID}";
	po.searchFormAnalysisProjectIdName = "${APIDDataFilterPagingQuery.PROPERTY_APID}";
	var currentAnalysisProject = $.unescapeHtmlForJson(<@writeJson var=currentAnalysisProject />);
	
	po.searchAnalysisProjectDft = function()
	{
		var re ={ id: "", name: "<@spring.message code='allProject' />" };
		return re;
	};
	
	po.vuePageModel(
	{
		searchAnalysisProject: (currentAnalysisProject || po.searchAnalysisProjectDft())
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
						$.cookie(po.currentAnalysisProjectCookieName, analysisProject.id,
								{ expires : 365, path: po.concatContextPath("/") });
						
						pm.searchForm[po.searchFormAnalysisProjectIdName] = pm.searchAnalysisProject.id;
						po.submitSearchForm();
					}
				}
			});
		},
		
		onClearCurrentAnalysisProject: function()
		{
			var pm = po.vuePageModel();
			pm.searchAnalysisProject = po.searchAnalysisProjectDft();
			$.cookie(po.currentAnalysisProjectCookieName, "", { expires : 365, path: po.concatContextPath("/") });
			
			pm.searchForm[po.searchFormAnalysisProjectIdName] = "";
			po.submitSearchForm();
		}
	});
	
	po.vueMounted(function()
	{
		var pm = po.vuePageModel();
		pm.searchForm[po.searchFormAnalysisProjectIdName] = pm.searchAnalysisProject.id;
	});
})
(${pid});
</script>
