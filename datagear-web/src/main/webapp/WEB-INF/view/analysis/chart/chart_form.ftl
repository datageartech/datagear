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
					</ul>
					<input type="hidden" name="htmlChartPlugin.id" class="ui-widget ui-widget-content" value="${(chart.htmlChartPlugin.id)!''?html}" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='chart.chartDataSets' /></label>
				</div>
				<div class="form-item-value">
					<div class="data-set-wrapper ui-widget ui-widget-content ui-corner-all">
						<#if (chart.chartDataSets)??>
						<#list chart.chartDataSets as cds>
						<div class='data-set-item ui-widget ui-widget-content ui-corner-all ui-state-default'>
							<input type='hidden' name='dataSetId' value="${(cds.dataSet.id)!''?html}" />
							<span class='data-set-name'>${(cds.dataSet.name)!''?html}</span>
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
					<div class='data-sign-select-panel ui-widget ui-widget-content ui-corner-all ui-front ui-widget-shadow'>
						<div class="select-panel-head ui-widget-header ui-corner-all">选择数据标记</div>
						<div class="select-panel-content"></div>
					</div>
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
	
	po.chartPluginVOs = <@writeJson var=pluginVOs />;
	
	po.getChartPlugin = function(id)
	{
		if(!po.chartPluginVOs)
			return;
		
		for(var i=0; i<po.chartPluginVOs.length; i++)
		{
			var cp = po.chartPluginVOs[i];
			if(cp.id == id)
				return cp;
		}
		
		return null;
	};
	
	po.getCurrentChartPlugin = function()
	{
		var id = po.element("input[name='htmlChartPlugin.id']").val();
		return po.getChartPlugin(id);
	};
	
	po.initChartPluginList = function()
	{
		if(!po.chartPluginVOs)
			return;
		
		var $wapper = po.element(".chart-plugin-list");
		$wapper.empty();
		
		for(var i=0; i<po.chartPluginVOs.length; i++)
		{
			var cp = po.chartPluginVOs[i];
			
			var $li = $("<li class='ui-state-default ui-corner-all' />")
						.attr("chart-plugin-id", cp.id).attr("title", cp.nameLabel.value)
						.appendTo($wapper);
			
			if(cp.hasIcon)
				$("<a class='plugin-icon'>&nbsp;</a>").css("background-image", "url(${contextPath}/"+cp.iconUrl+")").appendTo($li);
			else
				$("<a class='plugin-name'></a>").text(cp.nameLabel.value).appendTo($li);
		}
	};

	po.initChartPluginList();
	
	po.renderChartDataSetItem = function($parent, chartPlugin, chartDataSet)
	{
		var dataSet = chartDataSet.dataSet;
		var propertySigns = (chartDataSet.propertySigns || {});
		var dataSetProperties = (dataSet.properties || []);
		
		var $item = $("<div class='data-set-item ui-widget ui-widget-content ui-corner-all ui-state-default' />").appendTo($parent);
		$("<input type='hidden' name='dataSetId' />").attr("value", dataSet.id).appendTo($item);
		
		var $head = $("<div class='item-head ui-widget-header' />").appendTo($item);
		$("<span class='data-set-name' />").text(dataSet.name).appendTo($head);
		<#if !readonly>
		$("<div class='delete-icon''><span class=' ui-icon ui-icon-close'>&nbsp;</span></div>").attr("title", "<@spring.message code='delete' />").appendTo($head);
		</#if>
		
		var $signs = $("<div class='item-signs' />").appendTo($item);
		for(var i=0; i<dataSetProperties.length; i++)
		{
			var dsp = dataSetProperties[i];
			
			var $signItem = $("<div class='item-signs-item' />").appendTo($signs);
			$("<div class='sign-item-name' />").text(dsp.name).attr("title", (dsp.label || "")).appendTo($signItem);
			$("<div class='sign-item-values ui-widget ui-widget-content ui-corner-all' />").appendTo($signItem);
			<#if !readonly>
			$("<button type='button' class='sign-add-button ui-button ui-corner-all ui-widget ui-button-icon-only add-schema-button'><span class='ui-icon ui-icon-plus'></span><span class='ui-button-icon-space'> </span></button>").attr("title", "<@spring.message code='add' />").appendTo($signItem);
			</#if>
		}
	};
	
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
		
		var data = $.getPropertyParamString(dataSets, "id");
		
		$.get("${contextPath}/analysis/dataSet/getByIds", data, function(dataSets)
		{
			var $wrapper = po.element(".data-set-wrapper");
			
			for(var i=0; i<dataSets.length; i++)
			{
				var dataSet = dataSets[i];
				
				po.renderChartDataSetItem($wrapper, null, { "dataSet" : dataSet });
			}
		});
	}
	
	po.element(".data-set-wrapper").on("click", ".delete-icon", function(){ $(this).parent().parent().remove(); });
	po.element(".data-set-wrapper").on("click", ".sign-add-button", function()
	{
		var chartPlugin = po.getCurrentChartPlugin();
		var dataSigns = (chartPlugin ? (chartPlugin.dataSigns || []) : []);
		
		var $panel = po.element(".data-sign-select-panel");
		var $panelContent = po.element("> .select-panel-content", $panel);
		$panelContent.empty();
		
		for(var i=0; i<dataSigns.length; i++)
		{
			var dataSign = dataSigns[i];
			
			$("<button type='button' class='data-sign-item ui-button ui-corner-all' />")
				.attr("value", dataSign.name)
				.text(dataSign.nameLabel.value || dataSign.name).appendTo($panelContent);
		}
		
		$panel.show();
		$panel.position({ my : "left top", at : "left bottom+3", of : this});
	});
	
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