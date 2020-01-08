<#include "../../include/import_global.ftl">
<#include "../../include/html_doctype.ftl">
<#--
titleMessageKey 标题标签I18N关键字，不允许null
formAction 表单提交action，允许为null
readonly 是否只读操作，允许为null
-->
<#assign formAction=(formAction!'#')>
<#assign readonly=(readonly!false)>
<#assign isAdd=(formAction == 'saveAdd')>
<html>
<head>
<#include "../../include/html_head.ftl">
<title><#include "../../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body>
<div id="${pageId}" class="page-form page-form-chart">
	<form id="${pageId}-form" action="${contextPath}/analysis/chart/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="${(chart.id)!''?html}" />
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='chart.name' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="name" value="${(chart.name)!''?html}" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='chart.htmlChartPlugin' /></label>
				</div>
				<div class="form-item-value">
					<ul class="chart-plugin-list ui-widget ui-helper-clearfix">
					<#list pluginInfos as pi>
						<li class="ui-state-default ui-corner-all" chart-plugin-id="${pi.id?html}" title="${pi.name?html}">
							<#if pi.hasIcon>
							<a class="plugin-icon" style="background-image: url(${contextPath}/${pi.iconUrl})">&nbsp;</a>
							<#else>
							<a class="plugin-name">${pi.name?html}</a>
							</#if>
						</li>
					</#list>
					</ul>
					<input type="hidden" name="htmlChartPlugin.id" class="ui-widget ui-widget-content" value="${(chart.htmlChartPlugin.id)!''?html}" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='chart.sqlDataSetFactoryEntities' /></label>
				</div>
				<div class="form-item-value">
					<div class="data-set-wrapper ui-widget ui-widget-content ui-corner-all">
						<#if (chart.sqlDataSetFactoryEntities)??>
						<#list chart.sqlDataSetFactoryEntities as ds>
						<div class='data-set-item ui-widget ui-widget-content ui-corner-all ui-state-default'>
							<input type='hidden' name='dataSetId' value="${ds.id?html}" />
							<span class='data-set-name'>${ds.name?html}</span>
							<#if !readonly>
							<div class='delete-icon'><span class=' ui-icon ui-icon-close'>&nbsp;</span></div>
							</#if>
						</div>
						</#list>
						</#if>
					</div>
					<#if !readonly>
					<button type="button" class="add-data-set-button"><@spring.message code='add' /></button>
					</#if>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='chart.updateInterval' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="updateInterval" value="${(chart.updateInterval)!'-1'?html}" class="ui-widget ui-widget-content" />
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
<#include "../../include/page_js_obj.ftl" >
<#include "../../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element());

	po.url = function(action)
	{
		return "${contextPath}/analysis/chart/" + action;
	};
	
	var currentPluginId = po.element("input[name='htmlChartPlugin.id']").val();
	
	po.element(".chart-plugin-list li")
		.hover(function(){ $(this).addClass("ui-state-hover"); },
			function(){ $(this).removeClass("ui-state-hover"); })
		.click(function()
		{
			<#if !readonly>
			var $this = $(this);
			
			po.element("input[name='htmlChartPlugin.id']").val($this.attr("chart-plugin-id"));

			po.element(".chart-plugin-list li").removeClass("ui-state-active");
			$this.addClass("ui-state-active");
			</#if>
		})
		.each(function()
		{
			var $this = $(this);
			
			var myPluginId = $this.attr("chart-plugin-id");
			if(myPluginId == currentPluginId)
				$this.addClass("ui-state-active");
		});
	
	
	<#if !readonly>
	po.element(".add-data-set-button").click(function()
	{
		var options =
		{
			pageParam :
			{
				submit : function(dataSets)
				{
					po.addDataSet(dataSets);
				}
			}
		};
		
		$.setGridPageHeightOption(options);
		
		po.open("${contextPath}/analysis/dataSet/select?multiple", options);
	});
	
	po.addDataSet = function(dataSets)
	{
		if(!dataSets)
			return;
		
		var $wrapper = po.element(".data-set-wrapper");
		
		for(var i=0; i<dataSets.length; i++)
		{
			var dataSet = dataSets[i];
			
			var $item = $("<div class='data-set-item ui-widget ui-widget-content ui-corner-all ui-state-default' />").appendTo($wrapper);
			$("<input type='hidden' name='dataSetId'>").attr("value", dataSet.id).appendTo($item);
			$("<span class='data-set-name' />").text(dataSet.name).appendTo($item);
			$("<div class='delete-icon'><span class=' ui-icon ui-icon-close'>&nbsp;</span></div>").appendTo($item);
		}
	}
	
	po.element(".data-set-wrapper").on("click", ".delete-icon", function(){ $(this).parent().remove(); });
	
	po.element(".data-set-wrapper").sortable();
	
	po.form().validate(
	{
		rules :
		{
			"name" : "required"
		},
		messages :
		{
			"name" : "<@spring.message code='validation.required' />"
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
})
(${pageId});
</script>
</body>
</html>