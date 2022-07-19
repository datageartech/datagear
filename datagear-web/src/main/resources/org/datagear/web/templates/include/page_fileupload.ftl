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
	<small class="file-name">{{fileuploadInfo.name}}</small>
	<small class="upload-progress ml-2">{{fileuploadInfo.progress}}</small>
</div>
<script>
(function(po)
{
	po.vueReactive("fileuploadInfo", { name: "", progress: "" });
	
	po.uploadFileOnSelect = function(e)
	{
		var ufi = po.vueReactive("fileuploadInfo");
		ufi.name = (e.files && e.files[0] ? e.files[0].name : "");
	};
	
	po.uploadFileOnProgress = function(e)
	{
		var ufi = po.vueReactive("fileuploadInfo");
		ufi.progress = (e.progress >= 100 ? 99 : e.progress) +"%";
	};
	
	po.uploadFileOnUploaded = function(e)
	{
		var ufi = po.vueReactive("fileuploadInfo");
		ufi.progress = "100%";
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
