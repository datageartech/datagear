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
	//需实现
	po.inflatePreviewFingerprint = function(fingerprint, dataSet){};
	
	po.toPreviewFingerprint = function(dataSet)
	{
		var fingerprint = {};
		fingerprint.properties = $.extend(true, [], dataSet.properties);
		fingerprint.params = $.extend(true, [], dataSet.params);
		fingerprint.dataFormat = $.extend(true, {}, dataSet.dataFormat);
		
		po.inflatePreviewFingerprint(fingerprint, dataSet);
		
		return fingerprint;
	};
	
	po.beforeSubmitForm = function(url, options)
	{
		if(po.inPreviewAction())
		{
			po._prevPreviewFingerprint = po.toPreviewFingerprint(options.data.dataSet);
		}
		else
		{
			var myPreviewFingerprint = po.toPreviewFingerprint(options.data);
			if(!$.equalsForSameType(myPreviewFingerprint, po._prevPreviewFingerprint)
					|| !po.isPreviewSuccess())
			{
				$.tipInfo("<@spring.message code='dataSet.previewRequired' />");
				return false;
			}
		}
	};
	
	po.inflateDataSetModel = function(dataSet)
	{
		dataSet.analysisProject = (dataSet.analysisProject == null ? {} : dataSet.analysisProject);
		dataSet.properties = (dataSet.properties == null ? [] : dataSet.properties);
		dataSet.params = (dataSet.params == null ? [] : dataSet.params);
	};
	
	po.createWorkspaceEditor = function(dom, options)
	{
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
	
	po.vueMounted(function()
	{
		var pm = po.vuePageModel();
		po._prevPreviewFingerprint = po.toPreviewFingerprint(po.vueRaw(pm));
	});
})
(${pid});
</script>