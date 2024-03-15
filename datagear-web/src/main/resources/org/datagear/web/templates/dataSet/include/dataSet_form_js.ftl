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
		dataSet.dataFormat = (dataSet.dataFormat == null ? {} : dataSet.dataFormat);
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