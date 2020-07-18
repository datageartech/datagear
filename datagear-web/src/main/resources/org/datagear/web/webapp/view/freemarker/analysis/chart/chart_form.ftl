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
					<input type="text" name="htmlChartPlugin.id" class="ui-widget ui-widget-content" value="${(chart.htmlChartPlugin.id)!''?html}" style="display:none" />
					<div class="chart-plugin input ui-widget ui-widget-content"></div>
					<#if !readonly>
					<button class="selectChartPluginButton" type="button"><@spring.message code='select' /></button>
					</#if>
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
						<div class="select-panel-content">
							<div class="content-left"></div>
							<div class="content-right ui-widget ui-widget-content ui-corner-all ui-widget-shadow">
								<div class="data-sign-label"></div>
								<div class="data-sign-desc"></div>
							</div>
						</div>
					</div>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='chart.updateInterval' /></label>
				</div>
				<div class="form-item-value">
					<div class="updateInterval-radios">
						<label for="${pageId}-updateInterval_0" title="">
							<@spring.message code='chart.updateInterval.none' />
						</label>
			   			<input type="radio" id="${pageId}-updateInterval_0" name="updateIntervalRadio" value="0" />
						<label for="${pageId}-updateInterval_1" title="">
							<@spring.message code='chart.updateInterval.realtime' />
						</label>
			   			<input type="radio" id="${pageId}-updateInterval_1" name="updateIntervalRadio" value="1"  />
						<label for="${pageId}-updateInterval_2" title="">
							<@spring.message code='chart.updateInterval.interval' />
						</label>
			   			<input type="radio" id="${pageId}-updateInterval_2" name="updateIntervalRadio" value="2"  />
					</div>
					&nbsp;
					<span class="updateInterval-wrapper">
						<input type="text" name="updateInterval" value="${(chart.updateInterval)!'-1'?html}" class="ui-widget ui-widget-content" style="width:7em;" />
						<span><@spring.message code='chart.updateIntervalUnit' /></span>
					</span>
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
	<div class="data-set-param-value-panel ui-widget ui-widget-content ui-corner-all ui-widget-shadow ui-front">
		<div class="ui-widget-header ui-corner-all"><@spring.message code='chart.setDataSetParamValue' /></div>
		<div class="data-set-param-value-panel-content"></div>
	</div>
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
	
	po.element("input[name='updateIntervalRadio']").checkboxradio({icon:false});
	po.element(".updateInterval-radios").controlgroup();
	
	po.url = function(action)
	{
		return "${contextPath}/analysis/chart/" + action;
	};
	
	po.chartPluginVO = <@writeJson var=chartPluginVO />;
	po.chartDataSets = <@writeJson var=chartDataSets />;
	
	po.element(".selectChartPluginButton").click(function()
	{
		var options =
		{
			pageParam :
			{
				select : function(chartPlugin)
				{
					po.initChartPlugin(chartPlugin);
				}
			}
		};
		
		$.setGridPageHeightOption(options);
		
		po.open("${contextPath}/analysis/chartPlugin/select", options);
	});
	
	po.element("input[name='updateIntervalRadio']").on("change", function()
	{
		var radioVal = $(this).val();
		var $inputWrapper = po.element(".updateInterval-wrapper");
		var $input = po.element("input[name='updateInterval']");
		var inputVal = parseInt($input.val());
		
		if(!$input.attr("init-val"))
			$input.attr("init-val", inputVal);
		
		if(radioVal == "0")
		{
			$input.val("-1");
			if(inputVal > 0)
				$input.attr("init-val", inputVal);
			
			$inputWrapper.hide();
		}
		else if(radioVal == "1")
		{
			$input.val("0");
			if(inputVal > 0)
				$input.attr("init-val", inputVal);
			
			$inputWrapper.hide();
		}
		else
		{
			var initVal = parseInt($input.attr("init-val"));
			if(initVal > 0)
				$input.val(initVal);
			else
				$input.val("");
			
			$inputWrapper.show();
		}
	});
	
	po.updateIntervalValue = function(value)
	{
		if(value == undefined)
		{
			var radioVal = po.element("input[name='updateIntervalRadio']").val();
			
			if(radioVal == "2")
				return parseInt(po.element("input[name='updateInterval']").val());
			else if(radioVal == "1")
				return 0;
			else
				return -1;
		}
		else
		{
			var radioVal = "-1";
			
			if(value < 0)
				radioVal = "0";
			else if(value == 0)
				radioVal = "1";
			else
				radioVal = "2";
			
			po.element("input[name='updateIntervalRadio'][value='"+radioVal+"']").attr("checked", "checked").change();
		}
	};
	
	po.updateIntervalValue(${(chart.updateInterval)!"-1"});
	
	po.getCurrentChartPlugin = function()
	{
		return po.chartPluginVO;
	};
	
	po.dataSignLabel = function(dataSign)
	{
		return (dataSign.nameLabel && dataSign.nameLabel.value ? dataSign.nameLabel.value + " (" + dataSign.name +")" : dataSign.name);
	};

	po.dataSignDescLabel = function(dataSign)
	{
		return (dataSign.descLabel && dataSign.descLabel.value ? dataSign.descLabel.value : "");
	};
	
	po.initChartPlugin = function(chartPluginVO)
	{
		if(!chartPluginVO)
			return;
		
		po.chartPluginVO = chartPluginVO;
		po.element("input[name='htmlChartPlugin.id']").val(chartPluginVO.id);
		
		var $wapper = po.element(".chart-plugin");
		$wapper.empty();
		
		if(chartPluginVO.iconUrl)
		{
			$wapper.removeClass("no-icon");
			$("<div class='plugin-icon'></div>").css("background-image", "url(${contextPath}"+chartPluginVO.iconUrl+")").appendTo($wapper);
		}
		else
			$wapper.addClass("no-icon");
		
		$("<div class='plugin-name' />").text(chartPluginVO.nameLabel.value).appendTo($wapper);
		
		//更新数据标记
		var dataSigns = (chartPluginVO.dataSigns || []);
		po.element(".sign-value").each(function()
		{
			var $this = $(this);
			
			var mySignName = $("input[type='hidden']", $this).val();
			var myDataSign = null;
			
			for(var i=0; i<dataSigns.length; i++)
			{
				if(dataSigns[i].name == mySignName)
				{
					myDataSign = dataSigns[i];
					break;
				}
			}
			
			if(!myDataSign)
				$this.remove();
			else
				$(".sign-value-label", $this).text(po.dataSignLabel(myDataSign));
		});
	};
	
	po.getFormChartDataSets = function()
	{
		var re = [];
		
		po.element(".data-set-item").each(function()
		{
			var dataSetId = po.element(".chartDataSetId", this).val();
			var propertySigns = {};
			var alias = po.element(".chartDataSetAlias", this).val();
			var dataSetParams = (po.element(".dataSetParamValueButton", this).data("dataSetParams") || []);
			var paramValues = (po.element(".dataSetParamValueButton", this).data("paramValues") || {});
			
			po.element(".item-signs-item", this).each(function()
			{
				var signName = po.element(".chartDataSetPropertySignName", this).val();
				var signValues = [];
				
				po.element(".chartDataSetPropertySignValue", this).each(function()
				{
					signValues.push($(this).val());
				});
				
				propertySigns[signName] = signValues;
			});
			
			re.push({ "sqlDataSet": { "id": dataSetId, "params": dataSetParams }, "propertySigns": propertySigns, "alias": alias, "paramValues": paramValues });
		});
		
		return re;
	};

	po.element(".data-set-param-value-panel").draggable({ handle : ".ui-widget-header" });
	
	po.showDataSetParamValuePanel = function($paramValueButton, formOptions)
	{
		var $panel = po.element(".data-set-param-value-panel");
		
		formOptions = $.extend(
		{
			submitText: "<@spring.message code='confirm' />",
			yesText: "<@spring.message code='yes' />",
			noText: "<@spring.message code='no' />",
			paramValues: $paramValueButton.data("paramValues")
		},
		formOptions);
		
		chartFactory.chartForm.renderDataSetParamValueForm($(".data-set-param-value-panel-content", $panel),
				$paramValueButton.data("dataSetParams"), formOptions);
		
		$panel.show();
		$panel.position({ my : "left top", at : "right top", of : $paramValueButton});
	};
	
	$(po.element()).on("click", function(event)
	{
		var $target = $(event.target);
		
		var $pvp = po.element(".data-set-param-value-panel");
		if(!$pvp.is(":hidden"))
		{
			if($target.closest(".data-set-param-value-panel, .dataSetParamValueButton").length == 0)
				$pvp.hide();
		}
	});
	
	po.initChartDataSets = function()
	{
		if(!po.chartDataSets)
			return;
		
		var $parent = po.element(".data-set-wrapper");
		var chartPlugin = po.getCurrentChartPlugin();
		
		for(var i=0; i<po.chartDataSets.length; i++)
			po.renderChartDataSetItem($parent, chartPlugin, po.chartDataSets[i]);
	};
	
	po.renderChartDataSetItem = function($parent, chartPlugin, chartDataSet)
	{
		var dataSet = chartDataSet.dataSet;
		var propertySigns = (chartDataSet.propertySigns || {});
		var dataSetProperties = (dataSet.properties || []);
		
		var $item = $("<div class='data-set-item ui-widget ui-widget-content ui-corner-all' />").appendTo($parent);
		$("<input type='hidden' class='chartDataSetId' />").attr("value", dataSet.id).appendTo($item);
		
		var $head = $("<div class='item-head ui-widget-header ui-corner-all' />").appendTo($item);
		$("<span class='data-set-name' />").text(dataSet.name).appendTo($head);
		<#if !readonly>
		$("<div class='delete-icon''><span class=' ui-icon ui-icon-close'>&nbsp;</span></div>")
			.attr("title", "<@spring.message code='delete' />").appendTo($head);
		</#if>
		
		var $aliasDiv = $("<div class='item-alias ui-widget ui-widget-content' />").appendTo($item);
		$("<div class='alias-label' />").html("<@spring.message code='chart.chartDataSet.alias' />")
			.attr("title", "<@spring.message code='chart.chartDataSet.alias.desc' />").appendTo($aliasDiv);
		$("<input type='text' class='chartDataSetAlias ui-widget ui-widget-content' />")
			.attr("value", (chartDataSet.alias || "")).appendTo($aliasDiv);
		
		if(dataSet.params && dataSet.params.length > 0)
		{
			var $pvButton = $("<button type='button' class='dataSetParamValueButton ui-button ui-corner-all ui-widget'><@spring.message code='chart.chartDataSet.paramValue' /></button>")
								.appendTo($aliasDiv);
			$pvButton.data("dataSetParams", dataSet.params).data("paramValues", (chartDataSet.paramValues || {}));
			
			$pvButton.click(function(event)
			{
				var $this = $(this);
				po.showDataSetParamValuePanel($this,
				{
					submit: function()
					{
						$this.data("paramValues", chartFactory.chartForm.getDataSetParamValueObj(this));
						po.element(".data-set-param-value-panel").hide();
					},
					readonly: <#if readonly>true<#else>false</#if>,
					render: function()
					{
						$("select, input, textarea", this).addClass("ui-widget ui-widget-content");
						$("button", this).addClass("ui-button ui-corner-all ui-widget");
						
						<#if !readonly>
						var $foot = chartFactory.chartForm.getDataSetParamValueFormFoot(this);
						var $button = $(" <button type='button' class='ui-button ui-corner-all ui-widget' style='margin-left:1em;' />")
										.html("<@spring.message code='clear' />").attr("title", "<@spring.message code='chart.chartDataSet.clearParamValueTip' />").appendTo($foot);
						$button.click(function()
						{
							$this.data("paramValues", {});
							po.element(".data-set-param-value-panel").hide();
						});
						</#if>
					}
				});
			});
		}
		
		var $signs = $("<div class='item-signs' />").appendTo($item);
		for(var i=0; i<dataSetProperties.length; i++)
		{
			var dsp = dataSetProperties[i];
			
			var $signItem = $("<div class='item-signs-item' />").appendTo($signs);
			
			var $name = $("<div class='sign-item-name' />")
				.text(dsp.name)
				.attr("title", (dsp.label || ""))
				.appendTo($signItem);
			
			$("<input type='hidden' class='chartDataSetPropertySignName' />").val(dsp.name).appendTo($name);
			
			var $valuesWrapper = $("<div class='sign-item-values-wrapper ui-widget ui-widget-content ui-corner-all' />")
				.appendTo($signItem);
			
			var $values = $("<div class='sign-item-values' />").appendTo($valuesWrapper);
			
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
		
		var $value = $("<div class='sign-value ui-state-default ui-corner-all' />").appendTo($parent);
		
		$("<input type='hidden' class='chartDataSetPropertySignValue' />")
			.attr("value", value).appendTo($value);
		
		$("<span class='sign-value-label' />").text((label || value)).appendTo($value);
		
		<#if !readonly>
		$("<span class='sign-value-delete-icon ui-icon ui-icon-close'>&nbsp;</span>")
			.attr("title", "<@spring.message code='delete' />").appendTo($value);
		</#if>
	};
	
	po.element(".add-data-set-button").click(function()
	{
		var options =
		{
			pageParam :
			{
				select : function(dataSets)
				{
					if(!$.isArray(dataSets))
						dataSets = [dataSets];
					
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
		var $contentLeft = po.element(".content-left", $panel);
		var $contentRight = po.element(".content-right", $panel);
		$contentLeft.empty();
		$(".data-sign-label", $contentRight).empty();
		$(".data-sign-desc", $contentRight).empty();
		
		for(var i=0; i<dataSigns.length; i++)
		{
			var dataSign = dataSigns[i];
			
			var $item = $("<div class='data-sign-select-item' />").appendTo($contentLeft);
			
			var $button = $("<button type='button' class='data-sign-item ui-button ui-corner-all' />")
				.attr("value", dataSign.name)
				.attr("dataSignDesc", po.dataSignDescLabel(dataSign))
				.attr("dataSignMultiple", dataSign.multiple)
				.text(po.dataSignLabel(dataSign))
				.appendTo($item);
			
			$button.hover(function()
			{
				var $thisBtn = $(this);
				
				$(".data-sign-label", $contentRight).text($thisBtn.text());
				$(".data-sign-desc", $contentRight).text($thisBtn.attr("dataSignDesc"));
				$contentRight.show();
			},
			function()
			{
				$(".data-sign-label", $contentRight).empty();
				$(".data-sign-desc", $contentRight).empty();
				$contentRight.hide();
			});
			
			po.updatePropertySignButtonEnable($button, $itemSigns);
		}
		
		$panel.show();
		$panel.position({ my : "left center", at : "right+10 center", of : $this});
		
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
		
		if($this.hasClass("data-sign-disabled"))
			return;
		
		po.addPropertySignItem(po._currentPropertySignItemParent, $this.val(), $this.text());
		po.updatePropertySignButtonEnable($this, po._currentPropertySignItemParent.closest(".item-signs"));
		
		po.element(".data-sign-select-panel").hide();
	});
	
	po.updatePropertySignButtonEnable = function($button, $itemSigns)
	{
		if($button.attr("dataSignMultiple") != "false")
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
				$button.addClass("data-sign-disabled");
				break;
			}
		}
	};
	
	po.element(".data-set-wrapper").sortable({ handle: ".item-head" });
	
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
			if(dataSigns[i].required == true)
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
			"dataSignValidation" : "dataSignValidationRequired",
			"updateInterval" : {"required": true, "integer": true}
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
			},
			"updateInterval" : {"required": "<@spring.message code='validation.required' />", "integer": "<@spring.message code='validation.integer' />"}
		},
		submitHandler : function(form)
		{
			var data = $.formToJson(form, ["dataSignValidation", "updateIntervalRadio"]);
			data["chartDataSetVOs"] = po.getFormChartDataSets();
			
			$.ajaxJson($(form).attr("action"),
			{
				data: data,
				success : function(response)
				{
					var chart = response.data;
					po.element("input[name='id']").val(chart.id);
					
					po.pageParamCallAfterSave(true, response.data);
					
					if(po.previewAfterSave)
						window.open(po.url("show/"+chart.id+"/"), chart.id);
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
	
	po.element().on("click", function(event)
	{
		var $target = $(event.target);

		var $ssp = po.element(".data-sign-select-panel");
		if(!$ssp.is(":hidden"))
		{
			if($target.closest(".data-sign-select-panel, .sign-add-button").length == 0)
				$ssp.hide();
		}
	});
	
	po.initChartPlugin(po.chartPluginVO);
	po.initChartDataSets();
})
(${pageId});
</script>
</body>
</html>