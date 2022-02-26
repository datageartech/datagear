<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
titleMessageKey 标题标签I18N关键字，不允许null
formAction 表单提交action，允许为null
readonly 是否只读操作，允许为null
-->
<#assign formAction=(formAction!'#')>
<#assign readonly=(readonly!false)>
<#assign isAdd=(formAction == 'saveAdd')>
<#assign ResultDataFormat=statics['org.datagear.analysis.ResultDataFormat']>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body>
<#include "../include/page_js_obj.ftl" >
<div id="${pageId}" class="page-form page-form-chart">
	<form id="${pageId}-form" action="${contextPath}/chart/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="${(chart.id)!''}" />
			<div class="form-item form-item-analysisProjectAware">
				<div class="form-item-label">
					<label><@spring.message code='chart.name' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="name" value="${(chart.name)!''}" class="ui-widget ui-widget-content ui-corner-all" />
				</div>
				<#include "../include/analysisProjectAware_form_select.ftl" >
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='chart.htmlChartPlugin' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="htmlChartPlugin.id" class="ui-widget ui-widget-content ui-corner-all" value="${(chart.htmlChartPlugin.id)!''}" style="display:none" />
					<div class="chart-plugin input ui-widget ui-widget-content"></div>
					<#if !readonly>
					<button class="selectChartPluginButton" type="button"><@spring.message code='select' /></button>
					</#if>
				</div>
			</div>
			<div class="form-item error-newline">
				<div class="form-item-label">
					<label title="<@spring.message code='chart.chartDataSets.desc' />">
						<@spring.message code='chart.chartDataSets' />
					</label>
				</div>
				<div class="form-item-value form-item-value-chartDataSet">
					<input type="text" name="dataSignValidation" style="display: none" />
					<div class="data-set-wrapper input ui-widget ui-widget-content ui-corner-all">
					</div>
					<div class="data-set-opt">
						<#if !readonly>
						<button type="button" class="add-data-set-button"><@spring.message code='add' /></button>
						</#if>
						<button type="button" auto-close-prevent="dataformat-panel" class="dataformat-button"><@spring.message code='chart.resultDataFormat' /></button>
					</div>
					<div class='data-sign-select-panel auto-close-panel minor-panel ui-widget ui-widget-content ui-corner-all ui-front ui-widget-shadow'>
						<div class="select-panel-head panel-head ui-widget-header ui-corner-all"><@spring.message code='chart.selectDataSign' /></div>
						<div class="select-panel-content panel-content">
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
					<label title="<@spring.message code='chart.updateInterval.desc' />">
						<@spring.message code='chart.updateInterval' />
					</label>
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
						<input type="text" name="updateInterval" value="${(chart.updateInterval)!'-1'}" class="ui-widget ui-widget-content ui-corner-all" style="width:7em;" />
						<span><@spring.message code='chart.updateIntervalUnit' /></span>
					</span>
				</div>
			</div>
		</div>
		<div class="form-foot">
			<#if !readonly>
			<button type="submit" class="recommended"><@spring.message code='save' /></button>
			<button id="saveAndShowChart" type="button"><@spring.message code='chart.saveAndShow' /></button>
			</#if>
		</div>
		<div id="${pageId}-dataFormatPanel" class='dataformat-panel auto-close-panel minor-panel ui-widget ui-widget-content ui-corner-all ui-front ui-widget-shadow'>
			<div class="panel-head ui-widget-header ui-corner-all">
				<label class="tip-label" title="<@spring.message code='chart.resultDataFormat.desc' />">
					<@spring.message code='chart.resultDataFormat' />
				</label>
			</div>
			<div class="panel-content">
				<div class="form display-37">
					<div class="form-content">
						<div class="form-item">
							<div class="form-item-label">
								<label title="<@spring.message code='chart.resultDataFormatEnable.desc' />">
									<@spring.message code='chart.resultDataFormatEnable' />
								</label>
							</div>
							<div class="form-item-value">
								<div class="resultDataFormatEnable-radios">
									<label for="${pageId}-resultDataFormatEnable-0" title="">
										<@spring.message code='enable' />
									</label>
						   			<input type="radio" id="${pageId}-resultDataFormatEnable-0" name="resultDataFormatEnable" value="true"
						   				<#if enableResultDataFormat>checked="checked"</#if> />
									<label for="${pageId}-resultDataFormatEnable-1" title="">
										<@spring.message code='disable' />
									</label>
						   			<input type="radio" id="${pageId}-resultDataFormatEnable-1" name="resultDataFormatEnable" value="false"
						   				<#if !enableResultDataFormat>checked="checked"</#if> />
								</div>
							</div>
						</div>
						<div class="form-item resultDataFormatEnableAware">
							<div class="form-item-label">
								<label><@spring.message code='chart.resultDataFormat.dateType' /></label>
							</div>
							<div class="form-item-value">
								<div class="resultDataFormat-dateType-radios">
									<label for="${pageId}-resultDataFormat-dateType-0" title="">
										<@spring.message code='chart.resultDataFormat.TYPE_STRING' />
									</label>
						   			<input type="radio" id="${pageId}-resultDataFormat-dateType-0"
						   				<#if ResultDataFormat.TYPE_STRING == initResultDataFormat.dateType>checked="checked"</#if>
						   				name="resultDataFormat.dateType" value="${ResultDataFormat.TYPE_STRING}" />
									<label for="${pageId}-resultDataFormat-dateType-1" title="">
										<@spring.message code='chart.resultDataFormat.TYPE_NUMBER' />
									</label>
						   			<input type="radio" id="${pageId}-resultDataFormat-dateType-1"
						   				<#if ResultDataFormat.TYPE_NUMBER == initResultDataFormat.dateType>checked="checked"</#if>
						   				name="resultDataFormat.dateType" value="${ResultDataFormat.TYPE_NUMBER}" />
								</div>
							</div>
						</div>
						<div class="form-item resultDataFormatEnableAware">
							<div class="form-item-label">
								<label title="<@spring.message code='chart.resultDataFormat.dateFormat.desc' />">
									<@spring.message code='chart.resultDataFormat.dateFormat' />
								</label>
							</div>
							<div class="form-item-value">
								<input name="resultDataFormat.dateFormat" type="text" value="${(initResultDataFormat.dateFormat)!}" class="ui-widget ui-widget-content ui-corner-all" />
							</div>
						</div>
						<div class="form-item resultDataFormatEnableAware">
							<div class="form-item-label">
								<label><@spring.message code='chart.resultDataFormat.timeType' /></label>
							</div>
							<div class="form-item-value">
								<div class="resultDataFormat-timeType-radios">
									<label for="${pageId}-resultDataFormat-timeType-0" title="">
										<@spring.message code='chart.resultDataFormat.TYPE_STRING' />
									</label>
						   			<input type="radio" id="${pageId}-resultDataFormat-timeType-0"
						   				<#if ResultDataFormat.TYPE_STRING == initResultDataFormat.timeType>checked="checked"</#if>
						   				name="resultDataFormat.timeType" value="${ResultDataFormat.TYPE_STRING}" />
									<label for="${pageId}-resultDataFormat-timeType-1" title="">
										<@spring.message code='chart.resultDataFormat.TYPE_NUMBER' />
									</label>
						   			<input type="radio" id="${pageId}-resultDataFormat-timeType-1"
						   				<#if ResultDataFormat.TYPE_NUMBER == initResultDataFormat.timeType>checked="checked"</#if>
						   				name="resultDataFormat.timeType" value="${ResultDataFormat.TYPE_NUMBER}" />
								</div>
							</div>
						</div>
						<div class="form-item resultDataFormatEnableAware">
							<div class="form-item-label">
								<label title="<@spring.message code='chart.resultDataFormat.timeFormat.desc' />">
									<@spring.message code='chart.resultDataFormat.timeFormat' />
								</label>
							</div>
							<div class="form-item-value">
								<input name="resultDataFormat.timeFormat" type="text" value="${(initResultDataFormat.timeFormat)!}" class="ui-widget ui-widget-content ui-corner-all" />
							</div>
						</div>
						<div class="form-item resultDataFormatEnableAware">
							<div class="form-item-label">
								<label><@spring.message code='chart.resultDataFormat.timestampType' /></label>
							</div>
							<div class="form-item-value">
								<div class="resultDataFormat-timestampType-radios">
									<label for="${pageId}-resultDataFormat-timestampType-0" title="">
										<@spring.message code='chart.resultDataFormat.TYPE_STRING' />
									</label>
						   			<input type="radio" id="${pageId}-resultDataFormat-timestampType-0"
						   				<#if ResultDataFormat.TYPE_STRING == initResultDataFormat.timestampType>checked="checked"</#if>
						   				name="resultDataFormat.timestampType" value="${ResultDataFormat.TYPE_STRING}" />
									<label for="${pageId}-resultDataFormat-timestampType-1" title="">
										<@spring.message code='chart.resultDataFormat.TYPE_NUMBER' />
									</label>
						   			<input type="radio" id="${pageId}-resultDataFormat-timestampType-1"
						   				<#if ResultDataFormat.TYPE_NUMBER == initResultDataFormat.timestampType>checked="checked"</#if>
						   				name="resultDataFormat.timestampType" value="${ResultDataFormat.TYPE_NUMBER}" />
								</div>
							</div>
						</div>
						<div class="form-item resultDataFormatEnableAware">
							<div class="form-item-label">
								<label title="<@spring.message code='chart.resultDataFormat.timestampFormat.desc' />">
									<@spring.message code='chart.resultDataFormat.timestampFormat' />
								</label>
							</div>
							<div class="form-item-value">
								<input name="resultDataFormat.timestampFormat" type="text" value="${(initResultDataFormat.timestampFormat)!}" class="ui-widget ui-widget-content ui-corner-all" />
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</form>
	<div class="data-set-param-value-panel auto-close-panel minor-panel ui-widget ui-widget-content ui-corner-all ui-widget-shadow ui-front">
		<div class="panel-head ui-widget-header ui-corner-all"><@spring.message code='chart.chartDataSet.paramValue' /></div>
		<div class="data-set-param-value-panel-content panel-content"></div>
	</div>
</div>
<#include "../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	po.chartPluginVO = <@writeJson var=chartPluginVO />;
	po.chartDataSets = <@writeJson var=chartDataSets />;
	
	$.initButtons(po.element());
	po.element().autoCloseSubPanel();
	po.initAnalysisProject("${((chart.analysisProject.id)!'')?js_string?no_esc}", "${((chart.analysisProject.name)!'')?js_string?no_esc}");
	po.element(".form-item-value-chartDataSet").height($(window).height()/5*2);
	
	po.element(".updateInterval-radios").checkboxradiogroup();
	
	po.element(".resultDataFormatEnable-radios").checkboxradiogroup();
	po.element(".resultDataFormat-dateType-radios").checkboxradiogroup();
	po.element(".resultDataFormat-timeType-radios").checkboxradiogroup();
	po.element(".resultDataFormat-timestampType-radios").checkboxradiogroup();
	
	po.url = function(action)
	{
		return "${contextPath}/chart/" + action;
	};
	
	po.updateResultDataFormatPanelEnable = function(enable)
	{
		if(enable == null)
			enable = ("true" == po.element("input[name='resultDataFormatEnable']:checked").val());
		
		var enableMethod = (enable ? "enable" : "disable");
		
		po.element(".resultDataFormat-dateType-radios").controlgroup(enableMethod);
		po.element(".resultDataFormat-timeType-radios").controlgroup(enableMethod);
		po.element(".resultDataFormat-timestampType-radios").controlgroup(enableMethod);
		
		po.element("input[name='resultDataFormat.dateFormat']").prop("disabled", !enable);
		po.element("input[name='resultDataFormat.timeFormat']").prop("disabled", !enable);
		po.element("input[name='resultDataFormat.timestampFormat']").prop("disabled", !enable);
		
		if(enable)
			po.element(".resultDataFormatEnableAware").removeClass("ui-state-disabled");
		else
			po.element(".resultDataFormatEnableAware").addClass("ui-state-disabled");
	};
	
	po.element("input[name='resultDataFormatEnable']").on("change", function()
	{
		po.updateResultDataFormatPanelEnable();
	});
	
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
		
		po.open("${contextPath}/chartPlugin/select", options);
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
		
		var $pluginName = $("<div class='plugin-name' />").text(chartPluginVO.nameLabel.value).appendTo($wapper);
		if(chartPluginVO.descLabel && chartPluginVO.descLabel.value)
		{
			$pluginName.addClass("tip-label");
			$pluginName.attr("title", chartPluginVO.descLabel.value);
		}
		
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
			var propertyAliases = {};
			var propertyOrders = {};
			var alias = po.element(".chartDataSetAlias", this).val();
			var attachment = po.element(".chartDataSetAttachment", this).prop("checked");
			var dataSetParams = (po.element(".dataSetParamValueButton", this).data("dataSetParams") || []);
			var paramValues = (po.element(".dataSetParamValueButton", this).data("paramValues") || {});
			
			po.element(".item-props-p", this).each(function(eleIdx)
			{
				var propertyName = po.element(".chartDataSetPropertyName", this).val();
				var signValues = [];
				
				po.element(".chartDataSetPropertySignValue", this).each(function()
				{
					signValues.push($(this).val());
				});
				
				//没有标记的不必保存，避免存储长度溢出
				if(signValues.length > 0)
					propertySigns[propertyName] = signValues;
				
				var propertyAlias = po.element(".chartDataSetPropertyAlias", this).val();
				if(propertyAlias)
					propertyAliases[propertyName] = propertyAlias;
				
				var propertyOrder = po.element(".chartDataSetPropertyOrder", this).val();
				propertyOrder = (propertyOrder ? parseInt(propertyOrder) : null);
				if(propertyOrder != null && !isNaN(propertyOrder))
					propertyOrders[propertyName] = propertyOrder;
			});
			
			re.push(
			{
				"summaryDataSetEntity": { "id": dataSetId, "params": dataSetParams },
				"propertySigns": propertySigns,
				"alias": alias,
				"propertyAliases": propertyAliases,
				"propertyOrders": propertyOrders,
				"attachment": attachment,
				"query": { "paramValues": paramValues }
			});
		});
		
		return re;
	};

	po.element(".data-set-param-value-panel").draggable({ handle : ".ui-widget-header" });
	
	po.showDataSetParamValuePanel = function($paramValueButton, formOptions)
	{
		var $panel = po.element(".data-set-param-value-panel");
		var $panelContent = $(".data-set-param-value-panel-content", $panel);
		
		formOptions = $.extend(
		{
			submitText: "<@spring.message code='confirm' />",
			yesText: "<@spring.message code='yes' />",
			noText: "<@spring.message code='no' />",
			paramValues: $paramValueButton.data("paramValues")
		},
		formOptions);
		
		chartFactory.chartSetting.removeDatetimePickerRoot();
		$panelContent.empty();
		
		chartFactory.chartSetting.renderDataSetParamValueForm($panelContent,
				$paramValueButton.data("dataSetParams"), formOptions);
		
		$panel.show();
		$panel.position({ my : "left center", at : "right top", of : $paramValueButton});
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
	
	po.chartDataSetAttachmentIdSeq = 0;
	
	po.renderChartDataSetItem = function($parent, chartPlugin, chartDataSet)
	{
		var dataSet = chartDataSet.dataSet;
		var propertySigns = (chartDataSet.propertySigns || {});
		var propertyAliases = (chartDataSet.propertyAliases || {});
		var propertyOrders = (chartDataSet.propertyOrders || {});
		var dataSetProperties = (dataSet.properties || []);
		
		var $item = $("<div class='data-set-item ui-widget ui-widget-content ui-corner-all' />").appendTo($parent);
		$("<input type='hidden' class='chartDataSetId' />").attr("value", dataSet.id).appendTo($item);
		
		var $head = $("<div class='item-head ui-widget-header ui-corner-all' />").appendTo($item);
		$("<span class='data-set-name' />").text(dataSet.name).appendTo($head);
		
		<#if !readonly>
		var $optIconWrapper = $("<div class='opt-icons' />").appendTo($head);
		
		$("<div class='sort-up-icon opt-icon ui-state-default ui-corner-all'><span class='ui-icon ui-icon-arrowthick-1-n'>&nbsp;</span></div>")
		.attr("title", "<@spring.message code='moveUp' />").appendTo($optIconWrapper);
		
		$("<div class='sort-down-icon opt-icon ui-state-default ui-corner-all'><span class='ui-icon ui-icon-arrowthick-1-s'>&nbsp;</span></div>")
		.attr("title", "<@spring.message code='moveDown' />").appendTo($optIconWrapper);
		
		$("<div class='delete-icon opt-icon ui-state-default ui-corner-all'><span class='ui-icon ui-icon-close'>&nbsp;</span></div>")
			.attr("title", "<@spring.message code='delete' />").appendTo($optIconWrapper);
		</#if>
		
		var $dsProps = $("<div class='item-props' />").appendTo($item);
		for(var i=0; i<dataSetProperties.length; i++)
		{
			var dsp = dataSetProperties[i];
			
			var $dsProp = $("<div class='item-props-p' />").appendTo($dsProps);
			
			var $name = $("<div class='prop-name' />");
			$name.append("<span class='ui-icon ui-icon-bullet'></span>");
			$("<span />").text(dsp.name + (dsp.label ? " (" +dsp.label+")" : "")).appendTo($name);
			$name.appendTo($dsProp);
			
			$("<input type='hidden' class='chartDataSetPropertyName' />").val(dsp.name).appendTo($name);
			
			var $propSigns = $("<div class='prop-signs item-lv' />").appendTo($dsProp);
			
			$("<div class='tip-label item-lv-l' />").attr("title", "<@spring.message code='chart.chartDataSet.dataSign.desc' />")
				.html("<@spring.message code='chart.chartDataSet.dataSign' />").appendTo($propSigns);
			
			var $signsWrapper = $("<div class='signs-wrapper item-lv-v ui-widget ui-widget-content ui-corner-all' />")
				.appendTo($propSigns);
			
			var $values = $("<div class='sign-values' />").appendTo($signsWrapper);
			
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
			$("<button type='button' auto-close-prevent='data-sign-select-panel' class='sign-add-button ui-button ui-corner-all ui-widget ui-button-icon-only'><span class='ui-icon ui-icon-plus'></span><span class='ui-button-icon-space'> </span></button>")
				.attr("title", "<@spring.message code='chart.addDataSign' />")
				.appendTo($signsWrapper);
			</#if>
			
			var $propertyAlias = $("<div class='prop-alias item-lv' />");
			$("<div class='tip-label item-lv-l' />").attr("title", "<@spring.message code='chart.chartDataSet.propertyAlias.desc' />")
				.html("<@spring.message code='alias' />").appendTo($propertyAlias);
			var $aliasInputWrapper = $("<div class='item-lv-v' />").appendTo($propertyAlias);
			$("<input type='text' class='chartDataSetPropertyAlias ui-widget ui-widget-content ui-corner-all' />")
				.attr("placeholder", (dsp.label ? dsp.label : dsp.name)).val(propertyAliases[dsp.name] || "")
				.appendTo($aliasInputWrapper);
			$propertyAlias.appendTo($dsProp);
			
			var $propertyOrder = $("<div class='prop-order item-lv' />");
			$("<div class='tip-label item-lv-l' />").attr("title", "<@spring.message code='chart.chartDataSet.propertyOrder.desc' />")
				.html("<@spring.message code='chart.chartDataSet.propertyOrder' />").appendTo($propertyOrder);
			var $orderInputWrapper = $("<div class='item-lv-v' />").appendTo($propertyOrder);
			$("<input type='text' class='chartDataSetPropertyOrder ui-widget ui-widget-content ui-corner-all' />")
				.attr("placeholder", i).val(propertyOrders[dsp.name] != null ? propertyOrders[dsp.name] : "")
				.appendTo($orderInputWrapper);
			$propertyOrder.appendTo($dsProp);
		}
		
		var $settingDiv = $("<div class='item-setting ui-widget ui-widget-content' />").appendTo($item);
		var $aliasSetting = $("<div class='item-setting-alias item-lv' />").appendTo($settingDiv);
		$("<div class='tip-label item-lv-l' />").html("<@spring.message code='alias' />")
			.attr("title", "<@spring.message code='chart.chartDataSet.alias.desc' />").appendTo($aliasSetting);
		var $asInputWrapper = $("<div class='item-lv-v' />").appendTo($aliasSetting);
		$("<input type='text' class='chartDataSetAlias ui-widget ui-widget-content ui-corner-all' />")
			.attr("placeholder", dataSet.name)
			.attr("value", (chartDataSet.alias || "")).appendTo($asInputWrapper);
		
		var $attachment = $("<div class='item-lv' />").appendTo($settingDiv);
		var atchChkId = "${pageId}-attachmentChk-" + (po.chartDataSetAttachmentIdSeq++);
		$("<div class='tip-label item-lv-l' />").html("<@spring.message code='chart.chartDataSet.attachment' />")
			.attr("title", "<@spring.message code='chart.chartDataSet.attachment.desc' />").appendTo($attachment);
		var $atchInputWrapper = $("<div class='item-lv-v' />").appendTo($attachment);
		var $chkWrapper = $("<div />").appendTo($atchInputWrapper);
		$("<label />").attr("for", atchChkId).html("").appendTo($chkWrapper);
		var $atchChk = $("<input type='checkbox' class='chartDataSetAttachment ui-widget ui-widget-content' />")
			.attr("id", atchChkId).attr("value", "true").prop("checked", chartDataSet.attachment).appendTo($chkWrapper);
		$chkWrapper.checkboxradiogroup({icon:true});
		
		if(dataSet.params && dataSet.params.length > 0)
		{
			var $pvBtnItem = $("<div class='item-lv' />").appendTo($settingDiv);
			$("<div class='tip-label item-lv-l' />").html("<@spring.message code='parameter' />")
				.attr("title", "<@spring.message code='chart.chartDataSet.paramValue.desc' />").appendTo($pvBtnItem);
			var $pvBtnWrapper = $("<div class='item-lv-v' />").appendTo($pvBtnItem);
			var $pvButton = $("<button type='button' auto-close-prevent='data-set-param-value-panel' class='dataSetParamValueButton ui-button ui-corner-all ui-widget'></button>")
					.html("<#if readonly><@spring.message code='view' /><#else><@spring.message code='edit' /></#if>")
					.appendTo($pvBtnWrapper);
			$pvButton.data("dataSetParams", dataSet.params).data("paramValues",
					(chartDataSet.query && chartDataSet.query.paramValues ? chartDataSet.query.paramValues : {}));
			
			$pvButton.click(function(event)
			{
				var $this = $(this);
				po.showDataSetParamValuePanel($this,
				{
					submit: function(formData)
					{
						$this.data("paramValues", formData);
						po.element(".data-set-param-value-panel").hide();
					},
					readonly: <#if readonly>true<#else>false</#if>,
					render: function()
					{
						$("select, input, textarea", this).addClass("ui-widget ui-widget-content ui-corner-all");
						$("button", this).addClass("ui-button ui-corner-all ui-widget");
						
						<#if !readonly>
						var $foot = chartFactory.chartSetting.getDataSetParamValueFormFoot(this);
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
	
	po.element(".dataformat-button").click(function()
	{
		var panel = po.element("#${pageId}-dataFormatPanel");
		
		if(panel.is(":hidden"))
		{
			panel.show();
			panel.position({ my : "right bottom+50", at : "left bottom", of : this});
		}
		else
			panel.hide();
	});
	
	po.element("#${pageId}-dataFormatPanel").draggable({ handle : ".ui-widget-header" });
	
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
		
		po.open("${contextPath}/dataSet/select?multiple", options);
	});
	
	po.addDataSet = function(dataSets)
	{
		if(!dataSets)
			return;
		
		var data = $.getPropertyParamString(dataSets, "id");
		
		$.get("${contextPath}/dataSet/getProfileDataSetByIds", data, function(dataSets)
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
	
	po.element(".data-set-wrapper")
	.on("click", ".delete-icon", function()
	{
		$(this).closest(".data-set-item").remove();
	})
	.on("click", ".sort-up-icon", function()
	{
		var $myItem = $(this).closest(".data-set-item");
		var $prevItem = $myItem.prev();
		
		if($prevItem.length > 0)
			$myItem.insertBefore($prevItem);
	})
	.on("click", ".sort-down-icon", function()
	{
		var $myItem = $(this).closest(".data-set-item");
		var $nextItem = $myItem.next();
		
		if($nextItem.length > 0)
			$myItem.insertAfter($nextItem);
	})
	.on("click", ".sign-add-button", function()
	{
		var $this =$(this);
		
		var chartPlugin = po.getCurrentChartPlugin();
		var dataSigns = (chartPlugin ? (chartPlugin.dataSigns || []) : []);
		
		var $itemSigns = $this.closest(".item-props");
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
		
		po._currentPropertySignItemParent = $(".sign-values", $this.closest(".item-props-p"));
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
		po.updatePropertySignButtonEnable($this, po._currentPropertySignItemParent.closest(".item-props"));
		
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
	
	po.previewAfterSave = false;
	
	po.element("button[id=saveAndShowChart]").click(function()
	{
		po.previewAfterSave = true;
		po.form().submit();
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
			var $itemSigns = $(".item-props" , $chartDataSetItem);
			
			if(po.element(".chartDataSetAttachment", $chartDataSetItem).prop("checked"))
				continue;
			
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
			$(form).ajaxSubmitJson(
			{
				ignore: ["dataSignValidation", "updateIntervalRadio", "resultDataFormatEnable"],
				handleData: function(data)
				{
					data["chartDataSetVOs"] = po.getFormChartDataSets();
				},
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
	
	po.initChartPlugin(po.chartPluginVO);
	po.element(".chart-plugin").tooltip({ classes:{ "ui-tooltip": "ui-corner-all ui-widget-shadow chart-plugin-tooltip" } });
	po.initChartDataSets();
	po.updateResultDataFormatPanelEnable();
})
(${pageId});
</script>
</body>
</html>