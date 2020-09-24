<#--
数据分析项目相关表格页：扩展数据分析项目查询参数

依赖：
page_obj_searchform_js.ftl 或者 page_obj_searchform_data_filter.ftl
-->
<script type="text/javascript">
(function(po)
{
	po.getSearchParamSuper = po.getSearchParam;
	
	po.getSearchParam = function()
	{
		var param = po.getSearchParamSuper();
		
		param["${statics['org.datagear.web.vo.APIDDataFilterPagingQuery'].PROPERTY_APID}"] = po.currentAnalysisProjectId();
		
		return param;
	};
	
	po.currentAnalysisProjectId = function()
	{
		var id = null;
		
		if($.analysisProjectContext)
		{
			var analysisProject = $.analysisProjectContext.value();
			id = (analysisProject == null ? null : analysisProject.id);
		}
		//当页面在新窗口打开时
		else
			id = $.cookie("${statics['org.datagear.web.controller.AbstractController'].KEY_ANALYSIS_PROJECT_ID}");
		
		return id;
	};
})
(${pageId});
</script>