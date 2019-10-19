package org.datagear.web.convert;

import org.springframework.format.support.FormattingConversionService;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;

/**
 * 自定义{@linkplain FormattingConversionServiceFactoryBean}。
 * <p>
 * 它为{@linkplain FormattingConversionService}添加{@linkplain StringToArrayConverter}。
 * </p>
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

		return conversionService;
	}
}
