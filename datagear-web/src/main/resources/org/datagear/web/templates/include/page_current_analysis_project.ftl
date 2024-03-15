<#--
 *
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
	<p-button type="button" :label="pm.searchAnalysisProject.name" icon="pi pi-th-large"
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
	
	po.getCurrentAnalysisProject = function()
	{
		var pm = po.vuePageModel();
		return pm.searchAnalysisProject;
	};
	
	po.getCurrentAnalysisProjectId = function()
	{
		var pm = po.vuePageModel();
		return (pm.searchAnalysisProject ? (pm.searchAnalysisProject.id || "") : "");
	};
	
	po.addCurrentAnalysisProjectIdParam = function(url)
	{
		var paramName = po.currentAnalysisProjectCookieName;
		var paramValue = po.getCurrentAnalysisProjectId();
		
		if(paramValue)
			return $.addParam(url, paramName, paramValue);
		else
			return url;
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
