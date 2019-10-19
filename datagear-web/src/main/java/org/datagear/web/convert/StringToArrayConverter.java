package org.datagear.web.convert;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Set;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

/**
 * 字符串至数组转换器。
 * <p>
 * 默认的{@linkplain ConversionService}会使用
 * {@code org.springframework.core.convert.support.StringToArrayConverter}转换字符串至数组，此类会根据分隔符（默认为：,）拆分字符串。
 * 当请求参数值不是数组并且带有分隔符，后台对象属性是数组类型时，转换结果将不符合预期。因此，这里重写转换类，不拆分参数值。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class StringToArrayConverter implements ConditionalGenericConverter
{
	private ConversionService conversionService;

	public StringToArrayConverter()
	{
		super();
	}

	public StringToArrayConverter(ConversionService conversionService)
	{
		this.conversionService = conversionService;
	}

	public ConversionService getConversionService()
	{
		return conversionService;
	}

	public void setConversionService(ConversionService conversionService)
	{
		this.conversionService = conversionService;
	}

	@Override
	public Set<ConvertiblePair> getConvertibleTypes()
	{
		return Collections.singleton(new ConvertiblePair(String.class, Object[].class));
	}

	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType)
	{
		return this.conversionService.canConvert(sourceType, targetType.getElementTypeDescriptor());
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType)
	{
		if (source == null)
		{
			return null;
		}

		String[] fields = new String[] { (String) source };
		Object target = Array.newInstance(targetType.getElementTypeDescriptor().getType(), fields.length);
		for (int i = 0; i < fields.length; i++)
		{
			String sourceElement = fields[i];
			Object targetElement = this.conversionService.convert(sourceElement.trim(), sourceType,
					targetType.getElementTypeDescriptor());
			Array.set(target, i, targetElement);
		}
		return target;
	}
}