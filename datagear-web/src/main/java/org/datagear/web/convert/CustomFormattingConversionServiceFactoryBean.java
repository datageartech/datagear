package org.datagear.web.convert;

import org.datagear.web.format.DateFormatter;
import org.datagear.web.format.SqlDateFormatter;
import org.datagear.web.format.SqlTimeFormatter;
import org.datagear.web.format.SqlTimestampFormatter;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;

/**
 * 自定义{@linkplain FormattingConversionServiceFactoryBean}。
 * <p>
 * 它为{@linkplain FormattingConversionService}添加如下转换支持类：
 * </p>
 * <ul>
 * <li>{@linkplain StringToArrayConverter}</li>
 * <li>{@linkplain StringToJsonConverter}</li>
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
		conversionService.addConverter(new StringToJsonConverter());
		// conversionService.addConverterFactory(new
		// JsonValueConverterFactory<JsonStructure, Object>(conversionService)
		// {
		// {
		// }
		// });
		// conversionService.addConverterFactory(new
		// JsonValueConverterFactory<JsonObject, Object>(conversionService)
		// {
		// {
		// }
		// });
		// conversionService.addConverterFactory(new
		// JsonValueConverterFactory<JsonArray, Object>(conversionService)
		// {
		// {
		// }
		// });
		// conversionService.addConverterFactory(new
		// JsonValueConverterFactory<JsonString, Object>(conversionService)
		// {
		// {
		// }
		// });
		// conversionService.addConverterFactory(new
		// JsonValueConverterFactory<JsonNumber, Object>(conversionService)
		// {
		// {
		// }
		// });

		conversionService.addFormatter(new SqlDateFormatter());
		conversionService.addFormatter(new SqlTimeFormatter());
		conversionService.addFormatter(new SqlTimestampFormatter());
		conversionService.addFormatter(new DateFormatter());

		return conversionService;
	}
}
