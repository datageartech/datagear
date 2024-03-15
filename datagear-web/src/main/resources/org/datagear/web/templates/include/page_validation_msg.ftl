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
表单校验提示消息。

变量：

-->
<script>
(function(po)
{
	po.initValidationMessagesIfNon = function()
	{
		if(!po._initGlobalValidationMessages)
		{
			$.extend($.validator.messages, po.validationMessages);
			po._initGlobalValidationMessages = true;
		}
	};
	
	po.validationMessages =
	{
		required: "<@spring.message code='validation.required' />",
		number: "<@spring.message code='validation.number' />",
		digits: "<@spring.message code='validation.digits' />",
		equalTo: "<@spring.message code='validation.equalTo' />",
		min: $.validator.format("<@spring.message code='validation.min' />"),
		max: $.validator.format("<@spring.message code='validation.max' />"),
		maxlength: $.validator.format("<@spring.message code='validation.maxlength' />"),
		minlength: $.validator.format("<@spring.message code='validation.minlength' />"),
		integer: "<@spring.message code='validation.integer' />"
	};
})
(${pid});
</script>
