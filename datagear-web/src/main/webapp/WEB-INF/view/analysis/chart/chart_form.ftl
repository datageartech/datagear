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
					<input type="text" name="htmlChartPlugin.id" class="ui-widget ui-widget-content" value="${(chart.htmlChartPlugin.id)!''?html}" style="display:none" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='chart.chartDataSets' /></label>
				</div>
				<div class="form-item-value form-item-value-chartDataSet">
					<input type="text" name="dataSignValidation" style="display: none" />
					<div class="data-set-wrapper ui-widget ui-widget-content">
					</div>
					<#if !readonly>
					<button type="button" class="add-data-set-button"><@spring.message code='add' /></button>
					</#if>
					<div class='data-sign-select-panel ui-widget ui-widget-content ui-corner-all ui-front ui-widget-shadow'>
						<div class="select-panel-head ui-widget-header ui-corner-all"><@spring.message code='chart.selectDataSign' /></div>
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
			<button type="button" name="saveAndShow"><@spring.message code='chart.saveAndShow' /></button>
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
	var dataSetWrapperHeight = $(window).height()/5*2;
	po.element(".data-set-wrapper").height(dataSetWrapperHeight);
	po.element(".form-item-value-chartDataSet").height(dataSetWrapperHeight + 35);
	
	po.url = function(action)
	{
		return "${contextPath}/analysis/chart/" + action;
	};
	
	po.chartDataSets = <@writeJson var=chartDataSets />;
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
	
	po.dataSignLabel = function(dataSign)
	{
		return (dataSign.nameLabel && dataSign.nameLabel.value ? dataSign.nameLabel.value : dataSign.name);
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
			
			if(cp.iconUrl)
				$("<a class='plugin-icon'>&nbsp;</a>").css("background-image", "url(${contextPath}/"+cp.iconUrl+")").appendTo($li);
			else
				$("<a class='plugin-name'></a>").text(cp.nameLabel.value).appendTo($li);
		}
		
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
	};
	
	po.initChartDataSets = function()
	{
		if(!po.chartDataSets)
			return;
		
		var $parent = po.element(".data-set-wrapper");
		var chartPlugin = po.getCurrentChartPlugin();
		
		for(var i=0; i<po.chartDataSets.length; i++)
			po.renderChartDataSetItem($parent, chartPlugin, po.chartDataSets[i]);
	};
	
	po.nextChartDataSetSeq = function()
	{
		if(!po._nextChartDataSetSeq)
			po._nextChartDataSetSeq = 0;
		
		return (po._nextChartDataSetSeq++);
	};
	
	po.renderChartDataSetItem = function($parent, chartPlugin, chartDataSet)
	{
		var chartDataSetIndex = po.nextChartDataSetSeq();
		
		var dataSet = chartDataSet.dataSet;
		var propertySigns = (chartDataSet.propertySigns || {});
		var dataSetProperties = (dataSet.properties || []);
		
		var $item = $("<div class='data-set-item ui-widget ui-widget-content ui-corner-all' />").appendTo($parent);
		$("<input type='hidden' name='chartDataSetIndex' />").attr("value", chartDataSetIndex).appendTo($item);
		$("<input type='hidden' class='chartDataSetId' />").attr("name", "chartDataSet_"+chartDataSetIndex+"_dataSetId").attr("value", dataSet.id).appendTo($item);
		
		var $head = $("<div class='item-head ui-widget-header ui-corner-all' />").appendTo($item);
		$("<span class='data-set-name' />").text(dataSet.name).appendTo($head);
		<#if !readonly>
		$("<div class='delete-icon''><span class=' ui-icon ui-icon-close'>&nbsp;</span></div>").attr("title", "<@spring.message code='delete' />").appendTo($head);
		</#if>
		
		var $signs = $("<div class='item-signs' />").appendTo($item);
		for(var i=0; i<dataSetProperties.length; i++)
		{
			var dsp = dataSetProperties[i];
			
			var $signItem = $("<div class='item-signs-item' />").appendTo($signs);
			
			$("<input type='hidden' />")
				.attr("name", "chartDataSet_"+chartDataSetIndex+"_propertySignIndex").attr("value", i)
				.appendTo($item);
			
			var $name = $("<div class='sign-item-name' />")
				.text(dsp.name)
				.attr("title", (dsp.label || ""))
				.appendTo($signItem);
			
			$("<input type='hidden' class='chartDataSetPropertySignName' />")
				.attr("name", "chartDataSet_"+chartDataSetIndex+"_propertySign"+"_" +i+"_name")
				.val(dsp.name)
				.appendTo($name);
			
			var $valuesWrapper = $("<div class='sign-item-values-wrapper ui-widget ui-widget-content ui-corner-all' />")
				.appendTo($signItem);
			
			var $values = $("<div class='sign-item-values' />")
				.attr("chartDataSetIndex", chartDataSetIndex)
				.attr("propertySignIndex", i).appendTo($valuesWrapper);
			
			var mySigns = propertySigns[dsp.name];
			if(chartPlugin && chartPlugin.dataSigns && mySigns)
			{
				for(var j=0; j<chartPlugin.dataSigns.length; j++)
				{
					var dataSign = chartPlugin.dataSigns[j];
					
					for(var k=0; k<mySigns.length; k++)
					{
						if(mySigns[k] == dataSign.name)
							po.addPropertySignItem($values, dataSign.name, po.dataSignLabel(dataSign));
					}
				}
			}
			
			<#if !readonly>
			$("<button type='button' class='sign-add-button ui-button ui-corner-all ui-widget ui-button-icon-only'><span class='ui-icon ui-icon-plus'></span><span class='ui-button-icon-space'> </span></button>")
				.attr("title", "<@spring.message code='chart.addDataSign' />")
				.appendTo($valuesWrapper);
			</#if>
		}
	};
	
	po.addPropertySignItem = function($parent, value, label)
	{
		$(".chartDataSetPropertySignValue", $parent).each(function()
		{
			var $this = $(this);
			if($this.val() === value)
				$this.closest(".sign-value").remove();;
		});
		
		var chartDataSetIndex = $parent.attr("chartDataSetIndex");
		var propertySignIndexIndex = $parent.attr("propertySignIndex");
		
		var $value = $("<div class='sign-value ui-state-default ui-corner-all' />").appendTo($parent);
		
		$("<input type='hidden' class='chartDataSetPropertySignValue' />")
			.attr("name", "chartDataSet_"+chartDataSetIndex+"_propertySign"+"_" +propertySignIndexIndex+"_value")
			.attr("value", value)
			.appendTo($value);
		
		$("<span class='sign-value-label' />").text((label || value)).appendTo($value);
		
		<#if !readonly>
		$("<span class='sign-value-delete-icon ui-icon ui-icon-close'>&nbsp;</span>")
			.attr("title", "<@spring.message code='delete' />").appendTo($value);
		</#if>
	};
	
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
				
				po.renderChartDataSetItem($wrapper, po.getCurrentChartPlugin(), { "dataSet" : dataSet });
			}
		});
	}
	
	po.element(".data-sign-select-panel").draggable({ handle : ".select-panel-head" });
	
	po.element(".data-set-wrapper").on("click", ".delete-icon", function()
	{
		$(this).closest(".data-set-item").remove();
	})
	.on("click", ".sign-add-button", function()
	{
		var $this =$(this);
		
		var chartPlugin = po.getCurrentChartPlugin();
		var dataSigns = (chartPlugin ? (chartPlugin.dataSigns || []) : []);
		
		var $itemSigns = $this.closest(".item-signs");
		var $panel = po.element(".data-sign-select-panel");
		var $panelContent = po.element("> .select-panel-content", $panel);
		$panelContent.empty();
		
		for(var i=0; i<dataSigns.length; i++)
		{
			var dataSign = dataSigns[i];
			
			var $button = $("<button type='button' class='data-sign-item ui-button ui-corner-all' />")
				.attr("value", dataSign.name)
				.attr("occurMultiple", dataSign.occurMultiple)
				.text(po.dataSignLabel(dataSign)).appendTo($panelContent);
			
			po.updatePropertySignButtonEnable($button, $itemSigns);
		}
		
		$panel.show();
		$panel.position({ my : "left top", at : "right bottom+3", of : $this});
		
		po._currentPropertySignItemParent = $(".sign-item-values", $this.closest(".item-signs-item"));
	})
	.on("click", ".sign-value-delete-icon", function()
	{
		$(this).closest(".sign-value").remove();
	});
	
	po.element(".data-sign-select-panel").on("click", ".data-sign-item", function()
	{
		if(!po._currentPropertySignItemParent)
			return;
		
		var $this = $(this);
		po.addPropertySignItem(po._currentPropertySignItemParent, $this.val(), $this.text());
		po.updatePropertySignButtonEnable($this, po._currentPropertySignItemParent.closest(".item-signs"));
	});
	
	po.updatePropertySignButtonEnable = function($button, $itemSigns)
	{
		if($button.attr("occurMultiple") != "false")
			return;
		
		var setSigns = [];
		$(".chartDataSetPropertySignValue", $itemSigns).each(function()
		{
			setSigns.push($(this).val());
		});
		
		for(var i=0;i<setSigns.length; i++)
		{
			if(setSigns[i] == $button.val())
			{
				$button.addClass("ui-state-disabled");
				break;
			}
		}
	};
	
	po.element(".data-set-wrapper").sortable();

	po.previewAfterSave = false;
	
	po.element("button[name=saveAndShow]").click(function()
	{
		po.previewAfterSave = true;
		po.element("input[type='submit']").click();
	});
	
	$.validator.addMethod("dataSignValidationRequired", function(value, element)
	{
		var chartPlugin = po.getCurrentChartPlugin();
		var dataSigns = (chartPlugin ? (chartPlugin.dataSigns || []) : []);
		
		if(!dataSigns)
			return true;
		
		var $element = $(element);
		
		var requiredSigns = [];
		for(var i=0; i<dataSigns.length; i++)
		{
			if(dataSigns[i].occurRequired == true)
				requiredSigns.push(dataSigns[i]);
		}
		
		var $chartDataSetItems = po.element(".data-set-item");
		for(var i=0; i<$chartDataSetItems.length; i++)
		{
			var $chartDataSetItem = $($chartDataSetItems[i]);
			var $itemSigns = $(".item-signs" , $chartDataSetItem);
			
			var setSigns = [];
			$(".chartDataSetPropertySignValue", $itemSigns).each(function()
			{
				setSigns.push($(this).val());
			});
			
			for(var j=0; j<requiredSigns.length; j++)
			{
				var requiredSign = requiredSigns[j];
				
				var contains = false;
				for(var k=0; k<setSigns.length; k++)
				{
					if(requiredSign.name == setSigns[k])
					{
						contains = true;
						break;
					}
				}
				
				if(!contains)
				{
					var dataSetName = $(".data-set-name", $chartDataSetItem).text();
					$element.attr("needSignDataSetName", dataSetName)
						.attr("needDataSignLabel", po.dataSignLabel(requiredSign));
					return false;
				}
			}
		}
		
		return true;
	});
	
	po.form().validate(
	{
		ignore : "[hidden]",
		rules :
		{
			"name" : "required",
			"htmlChartPlugin.id": "required",
			"dataSignValidation" : "dataSignValidationRequired"
		},
		messages :
		{
			"name" : "<@spring.message code='validation.required' />",
			"htmlChartPlugin.id" : "<@spring.message code='validation.required' />",
			"dataSignValidation" :
			{
				"dataSignValidationRequired" : function()
				{
					var $input = po.element("input[name='dataSignValidation']");
					var needSignDataSetName = $input.attr("needSignDataSetName");
					var needDataSignLabel = $input.attr("needDataSignLabel");
					
					return "<@spring.message code='chart.validation.chartDataSetSign' />"
						.replace( /\{needSignDataSetName\}/g, needSignDataSetName)
						.replace( /\{needDataSignLabel\}/g, needDataSignLabel);
				}
			}
		},
		submitHandler : function(form)
		{
			$(form).ajaxSubmit(
			{
				success : function(response)
				{
					var chart = response.data;
					po.element("input[name='id']").val(chart.id);
					
					var close = (po.pageParamCall("afterSave")  == true);
					
					if(close)
						po.close();
					
					if(po.previewAfterSave)
						window.open(po.url("show/"+chart.id+"/index"), chart.id);
				},
				complete: function()
				{
					po.previewAfterSave = false;
				}
			});
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item-value"));
		}
	});
	
	$(document.body).on("click", function(event)
	{
		var $target = $(event.target);

		var $ssp = po.element(".data-sign-select-panel");
		if(!$ssp.is(":hidden"))
		{
			if($target.closest(".data-sign-select-panel, .sign-add-button").length == 0)
				$ssp.hide();
		}
	});
	</#if>
	
	po.initChartPluginList();
	po.initChartDataSets();
})
(${pageId});
</script>
</body>
</html>