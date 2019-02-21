<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
titleMessageKey 标题标签I18N关键字，不允许null
formAction 表单提交action，允许为null
readonly 是否只读操作，允许为null
-->
<#assign formAction=(formAction!'#')>
<#assign readonly=(readonly!false)>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body>
<div id="${pageId}" class="schema-form">
	<form id="${pageId}-form" action="${contextPath}/schema/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="${(schema.id)!''?html}">
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='schema.title' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="title" value="${(schema.title)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='schema.url' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="url" value="${(schema.url)!''?html}" class="ui-widget ui-widget-content" />
					<#if !readonly>
					<span id="schemaBuildUrlHelp" class="ui-state-default ui-corner-all" style="cursor: pointer;" title="<@spring.message code='schema.urlHelp' />"><span class="ui-icon ui-icon-help"></span></span>&nbsp;
					</#if>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='schema.user' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="user" value="${(schema.user)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<#if !readonly>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='schema.password' /></label>
				</div>
				<div class="form-item-value">
					<input type="password" name="password" value="${(schema.password)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			</#if>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='schema.shared' /></label>
				</div>
				<div class="form-item-value">
					<div class="schema-shared-radios">
					<label for="${pageId}-schemaSharedYes"><@spring.message code='yes' /></label>
		   			<input type="radio" id="${pageId}-schemaSharedYes" name="shared" value="1" <#if (schema.shared)!false>checked="checked"</#if> />
					<label for="${pageId}-schemaSharedNo"><@spring.message code='no' /></label>
		   			<input type="radio" id="${pageId}-schemaSharedNo" name="shared" value="0" <#if !((schema.shared)!false)>checked="checked"</#if> />
		   			</div>
				</div>
			</div>
			<#if !readonly>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='advancedSetting' /></label>
				</div>
				<div class="form-item-value">
					<button id="schemaAdvancedSet" type="button" style="font-size: 0.8em;">&nbsp;</button>
				</div>
			</div>
			</#if>
			<div class="form-item" id="schemaDriverEntityFormItem">
				<div class="form-item-label">
					<label><@spring.message code='schema.driverEntity' /></label>
				</div>
				<div id="driverEntityFormItemValue" class="form-item-value">
					<input type="hidden" id="driverEntityId" name="driverEntity.id" value="${(schema.driverEntity.id)!''?html}" />
					<input type="text" id="driverEntityText" value="${(schema.driverEntity.displayText)!''?html}" size="20" readonly="readonly" class="ui-widget ui-widget-content" />
					<#if !readonly>
					<div id="driverEntityActionGroup">
						<button id="driverEntitySelectButton" type="button"><@spring.message code='select' /></button>
						<select id="driverEntityActionSelect">
							<option value='del'><@spring.message code='delete' /></option>
						</select>
					</div>
					</#if>
				</div>
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<#if !readonly>
			<input type="submit" value="<@spring.message code='save' />" class="recommended" />
			&nbsp;&nbsp;
			<input type="reset" value="<@spring.message code='reset' />" />
			</#if>
		</div>
	</form>
</div>
<#include "../include/page_js_obj.ftl" >
<#include "../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	po.driverEntityFormItemValue = function(){ return this.element("#driverEntityFormItemValue"); };
	po.schemaDriverEntityFormItem = function(){ return this.element("#schemaDriverEntityFormItem"); };
	po.isDriverEntityEmpty = (po.element("input[name='driverEntity.id']").val() == "");
	
	<#if !readonly>
	
	po.element("#schemaBuildUrlHelp").click(function()
	{
		po.open("${contextPath}/schemaUrlBuilder/buildUrl",
		{
			data : { url : po.element("input[name='url']").val() },
			width: "60%",
			pageParam :
			{
				"setSchemaUrl" : function(url)
				{
					po.element("input[name='url']").val(url);
				}
			}
		});
	});
	
	po.element("#driverEntitySelectButton").click(function()
	{
		var options =
		{
			pageParam :
			{
				submit : function(driverEntity)
				{
					po.element("input[name='driverEntity.id']").val(driverEntity.id);
					po.element("#driverEntityText").val(driverEntity.displayText);
				}
			}
		};
		$.setGridPageHeightOption(options);
		po.open("${contextPath}/driverEntity/select", options);
	});
	
	po.element("#driverEntityActionSelect").selectmenu(
	{
		appendTo: po.driverEntityFormItemValue(),
		classes:
		{
	          "ui-selectmenu-button": "ui-button-icon-only splitbutton-select"
	    },
	    select: function(event, ui)
    	{
    		var action = $(ui.item).attr("value");
    		
			if("del" == action)
    		{
				po.element("#driverEntityId").val("");
				po.element("#driverEntityText").val("");
    		}
    	}
	});
	
	po.element("#driverEntityActionGroup").controlgroup();
	
	po.form().validate(
	{
		rules :
		{
			title : "required",
			url : "required"
		},
		messages :
		{
			title : "<@spring.message code='validation.required' />",
			url : "<@spring.message code='validation.required' />"
		},
		submitHandler : function(form)
		{
			$(form).ajaxSubmit(
			{
				success : function()
				{
					var close = (po.pageParamCall("afterSave")  != false);
					
					if(close)
						po.close();
				}
			});
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item-value"));
		}
	});
	</#if>
	
	po.element("input[name='shared']").checkboxradio({icon:false});
	po.element(".schema-shared-radios").controlgroup();
	
	$.initButtons(po.element());
	
	if(po.isDriverEntityEmpty)
		po.schemaDriverEntityFormItem().hide();
	
	<#if !readonly>
	$("#schemaAdvancedSet", po.page).button(
	{
		icon: (po.schemaDriverEntityFormItem().is(":hidden") ? "ui-icon-triangle-1-s" : "ui-icon-triangle-1-n"),
		showLabel: false
	})
	.click(function()
	{
		var item = po.schemaDriverEntityFormItem();
		
		if(item.is(":hidden"))
		{
			item.show();
			$(this).button("option", "icon", "ui-icon-triangle-1-n");
		}
		else
		{
			item.hide();
			$(this).button("option", "icon", "ui-icon-triangle-1-s");
		}
	});
	</#if>
})
(${pageId});
</script>
</body>
</html>