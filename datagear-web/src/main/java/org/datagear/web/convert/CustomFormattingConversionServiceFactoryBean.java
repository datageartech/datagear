package org.datagear.web.convert;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;

import org.springframework.format.support.FormattingConversionService;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;

/**
 * 自定义{@linkplain FormattingConversionServiceFactoryBean}。
 * <p>
 * 它为{@linkplain FormattingConversionService}添加如下转换支持类：
 * </p>
 * <ul>
 * <li>{@linkplain StringToArrayConverter}</li>
 * <li>{@linkplain JsonStructureConverterFactory}</li>
 * </ul>
 * 
 * @author datagear@163.com
 *
 */
public class CustomFormattingConversionServiceFactoryBean extends FormattingConversionServiceFactoryBean
{
	public CustomFormattingConversionServiceFactoryBean()
	{
		super();
	}

	@Override
	public FormattingConversionService getObject()
	{
		FormattingConversionService conversionService = super.getObject();

		conversionService.addConverter(new StringToArrayConverter(conversionService));
		conversionService
				.addConverterFactory(new JsonStructureConverterFactory<JsonStructure, Object>(conversionService)
				{
					{
					}
				});
		conversionService
				.addConverterFactory(new JsonStructureConverterFactory<JsonObject, Object>(conversionService)
				{
					{
					}
				});
		conversionService
				.addConverterFactory(new JsonStructureConverterFactory<JsonArray, Object>(conversionService)
				{
					{
					}
				});

		return conversionService;
	}
}
