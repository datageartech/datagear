<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
上传文件片段。
-->
<div class="fileupload-info text-color-secondary">
	<small class="file-name">{{pm.fileuploadInfo.name}}</small>
	<small class="upload-progress ml-2">{{pm.fileuploadInfo.progress}}</small>
</div>
<script>
(function(po)
{
	po.vuePageModel(
	{
		fileuploadInfo: { name: "", progress: "" }
	});
	
	po.uploadFileOnSelect = function(e)
	{
		var pm = po.vuePageModel();
		pm.fileuploadInfo.name = (e.files && e.files[0] ? e.files[0].name : "");
	};
	
	po.uploadFileOnProgress = function(e)
	{
		var pm = po.vuePageModel();
		pm.fileuploadInfo.progress = (e.progress >= 100 ? 99 : e.progress) +"%";
	};
	
	po.uploadFileOnUploaded = function(e)
	{
		var pm = po.vuePageModel();
		pm.fileuploadInfo.progress = "100%";
	};
	
	po.vueMethod(
	{
		uploadFileOnSelect: function(e)
		{
			po.uploadFileOnSelect(e);
		},
		
		uploadFileOnProgress: function(e)
		{
			po.uploadFileOnProgress(e);
		}
	});
})
(${pid});
</script>
