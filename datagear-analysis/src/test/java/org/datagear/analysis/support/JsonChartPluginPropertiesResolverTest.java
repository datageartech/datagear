package org.datagear.analysis.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartProperties;
import org.datagear.analysis.ChartProperty;
import org.datagear.analysis.ChartPropertyValues;
import org.datagear.analysis.DataSetFactory;
import org.datagear.analysis.PropertyType;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.constraint.Constraint;
import org.datagear.analysis.constraint.Max;
import org.datagear.analysis.constraint.MaxLength;
import org.datagear.analysis.constraint.Min;
import org.datagear.analysis.constraint.MinLength;
import org.datagear.analysis.constraint.Required;
import org.datagear.util.i18n.Label;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain JsonChartPluginPropertiesResolver}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class JsonChartPluginPropertiesResolverTest
{
	private JsonChartPluginPropertiesResolver jsonChartPluginPropertiesResolver = new JsonChartPluginPropertiesResolver();

	@Test
	public void resolveChartPluginPropertiesTest() throws IOException
	{
		{
			InputStream jsonInputStream = getClass().getClassLoader()
					.getResourceAsStream("org/datagear/analysis/support/JsonChartPluginPropertiesResolverTest.json");

			Map<String, Object> properties = jsonChartPluginPropertiesResolver
					.resolveChartPluginProperties(jsonInputStream, "UTF-8");

			Assert.assertEquals("pie-chart", properties.get(JsonChartPluginPropertiesResolver.CHART_PLUGIN_ID));

			{
				Label nameLabel = (Label) properties.get(JsonChartPluginPropertiesResolver.CHART_PLUGIN_NAME_LABEL);
				Assert.assertEquals("饼图", nameLabel.getValue());
				Assert.assertEquals("pie chart", nameLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("饼图中文", nameLabel.getValue(Label.toLocale("zh")));
			}

			{
				Label descLabel = (Label) properties.get(JsonChartPluginPropertiesResolver.CHART_PLUGIN_DESC_LABEL);
				Assert.assertEquals("饼图描述", descLabel.getValue());
				Assert.assertEquals("pie chart desc", descLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("饼图描述中文", descLabel.getValue(Label.toLocale("zh")));
			}

			{
				Label manualLabel = (Label) properties.get(JsonChartPluginPropertiesResolver.CHART_PLUGIN_MANUAL_LABEL);
				Assert.assertEquals("饼图指南", manualLabel.getValue());
				Assert.assertEquals("pie chart manual", manualLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("饼图指南中文", manualLabel.getValue(Label.toLocale("zh")));
			}

			{
				@SuppressWarnings("unchecked")
				Map<RenderStyle, LocationIcon> icons = (Map<RenderStyle, LocationIcon>) properties
						.get(JsonChartPluginPropertiesResolver.CHART_PLUGIN_ICONS);

				Assert.assertEquals("icon-0.png", icons.get(RenderStyle.LIGHT).getLocation());
				Assert.assertEquals("icon-1.png", icons.get(RenderStyle.DARK).getLocation());
			}

			ChartProperties chartProperties = (ChartProperties) properties
					.get(JsonChartPluginPropertiesResolver.CHART_PLUGIN_CHART_PROPERTIES);

			{
				ChartProperty chartProperty = chartProperties.get(0);

				Assert.assertEquals("title", chartProperty.getName());
				Assert.assertEquals(PropertyType.STRING, chartProperty.getType());
				Assert.assertEquals("pie chart", chartProperty.getDefaultValue());

				Label nameLabel = chartProperty.getNameLabel();
				Assert.assertEquals("标题", nameLabel.getValue());
				Assert.assertEquals("title", nameLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("标题中文", nameLabel.getValue(Label.toLocale("zh")));

				Label descLabel = chartProperty.getDescLabel();
				Assert.assertEquals("标题描述", descLabel.getValue());
				Assert.assertEquals("title desc", descLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("标题描述中文", descLabel.getValue(Label.toLocale("zh")));

				Set<Constraint> constraints = chartProperty.getConstraints();
				Set<Constraint> constraintsExpected = new HashSet<Constraint>();
				constraintsExpected.add(new Required(true));
				constraintsExpected.add(new MaxLength(20));
				constraintsExpected.add(new MinLength(10));

				Assert.assertEquals(constraintsExpected, constraints);
			}

			{
				ChartProperty chartProperty = chartProperties.get(1);

				Assert.assertEquals("interval", chartProperty.getName());
				Assert.assertEquals(PropertyType.NUMBER, chartProperty.getType());
				Assert.assertEquals(5, ((Number) chartProperty.getDefaultValue()).intValue());

				Label nameLabel = chartProperty.getNameLabel();
				Assert.assertEquals("间隔", nameLabel.getValue());
				Assert.assertEquals("interval", nameLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("间隔中文", nameLabel.getValue(Label.toLocale("zh")));

				Label descLabel = chartProperty.getDescLabel();
				Assert.assertEquals("间隔描述", descLabel.getValue());
				Assert.assertEquals("interval desc", descLabel.getValue(Label.toLocale("en")));
				Assert.assertEquals("间隔描述中文", descLabel.getValue(Label.toLocale("zh")));

				Set<Constraint> constraints = chartProperty.getConstraints();
				Set<Constraint> constraintsExpected = new HashSet<Constraint>();
				constraintsExpected.add(new Required(false));
				constraintsExpected.add(new Max(30));
				constraintsExpected.add(new Min(5));

				Assert.assertEquals(constraintsExpected, constraints);
			}
		}
	}

	@Test
	public void setChartPluginPropertiesTest() throws IOException
	{
		InputStream jsonInputStream = getClass().getClassLoader()
				.getResourceAsStream("org/datagear/analysis/support/JsonChartPluginPropertiesResolverTest.json");

		Map<String, Object> properties = jsonChartPluginPropertiesResolver.resolveChartPluginProperties(jsonInputStream,
				"UTF-8");

		TestChartPlugin chartPlugin = new TestChartPlugin();

		this.jsonChartPluginPropertiesResolver.setChartPluginProperties(chartPlugin, properties);

		Assert.assertNotNull(chartPlugin.getId());

		Assert.assertNotNull(chartPlugin.getNameLabel());
		Assert.assertNotNull(chartPlugin.getDescLabel());
		Assert.assertNotNull(chartPlugin.getManualLabel());
		Assert.assertNotNull(chartPlugin.getIcons());
		Assert.assertNotNull(chartPlugin.getChartProperties());
		Assert.assertEquals(2, chartPlugin.getOrder());
	}

	private static class TestChartPlugin extends AbstractChartPlugin<RenderContext>
	{
		public TestChartPlugin()
		{
			super();
		}

		@Override
		public Chart renderChart(RenderContext renderContext, ChartPropertyValues chartPropertyValues,
				DataSetFactory... dataSetFactories) throws RenderException
		{
			throw new UnsupportedOperationException();
		}
	}
}
