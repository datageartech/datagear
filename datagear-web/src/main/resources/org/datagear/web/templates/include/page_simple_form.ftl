<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
简单表单JS片段。

依赖：
page_validation_msg.ftl

-->
<script>
(function(po)
{
	po.setupSimpleForm = function(form, reactiveData, validateOptions)
	{
		validateOptions = (validateOptions || {});
		if($.isFunction(validateOptions))
			validateOptions = { submitHandler: validateOptions };
		
		po.initValidationMessagesIfNon();
		
		$(form).validateForm(reactiveData, validateOptions);
	};
})
(${pid});
</script>
