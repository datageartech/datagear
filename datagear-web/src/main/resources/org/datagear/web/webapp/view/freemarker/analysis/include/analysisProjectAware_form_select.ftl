<#--
数据分析项目相关表单页：所属数据分析项目选择框
-->
<div class="form-item form-item-analysisProject">
	<div class="form-item-label">
		<label><@spring.message code='analysisProject.ownerAnalysisProject' /></label>
	</div>
	<div class="form-item-value">
		<input type="text" name="analysisProject.name" value="" class="ui-widget ui-widget-content" readonly="readonly" />
		<input type="hidden" name="analysisProject.id" value="" />
		<#if !readonly>
		<div class="analysisProjectActionGroup">
			<button type="button" class="selectAnalysisProjectButton"><@spring.message code='select' /></button>
			<select class="analysisProjectActionSelect">
				<option value='del'><@spring.message code='delete' /></option>
			</select>
		</div>
		</#if>
	</div>
</div>
<script type="text/javascript">
(function(po)
{
	po.initAnalysisProject = function(id, name)
	{
		po.element("input[name='analysisProject.id']").attr("value", id || "");
		po.element("input[name='analysisProject.name']").attr("value", name || "");
		
		po.element(".analysisProjectActionSelect").selectmenu(
		{
			appendTo: po.element(),
			classes:
			{
		          "ui-selectmenu-button": "ui-button-icon-only"
		    },
		    select: function(event, ui)
	    	{
	    		var action = $(ui.item).attr("value");
	    		
				if("del" == action)
	    		{
					po.element("input[name='analysisProject.id']").val("");
					po.element("input[name='analysisProject.name']").val("");
	    		}
	    	}
		});
		
		po.element(".analysisProjectActionGroup").controlgroup();
		
		po.element(".selectAnalysisProjectButton").click(function()
		{
			var options =
			{
				pageParam :
				{
					select : function(analysisProject)
					{
						po.element("input[name='analysisProject.id']").val(analysisProject.id);
						po.element("input[name='analysisProject.name']").val(analysisProject.name);
					}
				}
			};
			
			$.setGridPageHeightOption(options);
			
			po.open("${contextPath}/analysis/project/select", options);
		});
	};
})
(${pageId});
</script>