package org.datagear.analysis.support;

import java.util.Map;

import org.datagear.analysis.ChartProperties;
import org.datagear.analysis.PropertyType;
import org.datagear.analysis.RenderStyle;
import org.datagear.util.i18n.Label;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain JsonChartPluginResolver}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class JsonChartPluginResolverTest
{
	private JsonChartPluginResolver jsonChartPluginResolver = new JsonChartPluginResolver();

	@Test
	public void resolveChartPluginPropertiesTest()
	{
		{
			String json = "{"
					+ " id : 'pie-chart',"
					+ " nameLabel : { value : '饼图', localeValues : { 'en' : 'pie chart', 'zh' : '饼图' } },"
					+ " descLabel : { value : '饼图描述', localeValues : { 'en' : 'pie chart', 'zh' : '饼图描述' } },"
					+ " manualLabel : { value : '饼图指南', localeValues : { 'en' : 'pie chart', 'zh' : '饼图指南' } },"
					+ " icons : { 'LIGHTNESS' : { 'location' : 'icon-0.png' }, 'DARK' : { 'location' : 'icon-1.png' } },"
					+ " chartProperties : [ { name : 'title', type : 'STRING'  }, { name : 'interval', type : 'NUMBER' } ]"
					+ "}";

			Map<String, Object> properties = jsonChartPluginResolver.resolveChartPluginProperties(json);

			Assert.assertEquals("pie-chart", properties.get(JsonChartPluginResolver.CHART_PLUGIN_ID));
			Assert.assertEquals("饼图",
					((Label) properties.get(JsonChartPluginResolver.CHART_PLUGIN_NAME_LABEL)).getValue());
			Assert.assertEquals("饼图描述",
					((Label) properties.get(JsonChartPluginResolver.CHART_PLUGIN_DESC_LABEL)).getValue());
			Assert.assertEquals("饼图指南",
					((Label) properties.get(JsonChartPluginResolver.CHART_PLUGIN_MANUAL_LABEL)).getValue());

			@SuppressWarnings("unchecked")
			Map<RenderStyle, LocationIcon> icons = (Map<RenderStyle, LocationIcon>) properties
					.get(JsonChartPluginResolver.CHART_PLUGIN_ICONS);

			Assert.assertEquals("icon-0.png", icons.get(RenderStyle.LIGHTNESS).getLocation());
			Assert.assertEquals("icon-1.png", icons.get(RenderStyle.DARK).getLocation());

			ChartProperties chartProperties = (ChartProperties) properties
					.get(JsonChartPluginResolver.CHART_PLUGIN_CHART_PROPERTIES);

			Assert.assertEquals("title", chartProperties.getByIndex(0).getName());
			Assert.assertEquals(PropertyType.STRING, chartProperties.getByIndex(0).getType());
			Assert.assertEquals("interval", chartProperties.getByIndex(1).getName());
			Assert.assertEquals(PropertyType.NUMBER, chartProperties.getByIndex(1).getType());
		}
	}
}
