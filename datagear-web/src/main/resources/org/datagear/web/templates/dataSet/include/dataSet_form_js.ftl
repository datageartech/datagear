<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
数据集表单页：JS片段

依赖：
dataSet_form_preview.ftl

变量：

-->
<script type="text/javascript">
(function(po)
{
	po.inflateDataSetModel = function(dataSet)
	{
		dataSet.analysisProject = (dataSet.analysisProject == null ? {} : dataSet.analysisProject);
		dataSet.properties = (dataSet.properties == null ? [] : dataSet.properties);
		dataSet.params = (dataSet.params == null ? [] : dataSet.params);
	};
	
	po.createWorkspaceEditor = function(dom, options)
	{
		options = (options || {});
		
		if(options.readOnly == null)
			options.readOnly = po.isReadonlyAction;
		
		if(!options.extraKeys)
			options.extraKeys = {};
		
		options.extraKeys["Ctrl-Enter"] = function(editor)
		{
			po.triggerPreview();
		};
		
		return po.createCodeEditor(dom, options);
	};
})
(${pid});
</script>