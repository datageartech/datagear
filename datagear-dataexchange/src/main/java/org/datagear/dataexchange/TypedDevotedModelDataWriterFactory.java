package org.datagear.dataexchange;

import org.springframework.core.GenericTypeResolver;

/**
 * 类型参数{@linkplain DevotedModelDataWriterFactory}。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class TypedDevotedModelDataWriterFactory<T extends Export> implements DevotedModelDataWriterFactory
{
	private Class<?> supportedExportType;
	
	public TypedDevotedModelDataWriterFactory()
	{
		super();
		this.supportedExportType = resolveExportTypeParameter(getClass());
	}

	public Class<?> getSupportedExportType()
	{
		return supportedExportType;
	}

	protected void setSupportedExportType(Class<?> supportedExportType)
	{
		this.supportedExportType = supportedExportType;
	}

	@Override
	public boolean supports(Export expt)
	{
		if(expt == null)
			return false;
		
		return this.supportedExportType.isAssignableFrom(expt.getClass());
	}
	
	@Override
	public ModelDataWriter get(Export expt)
	{
		@SuppressWarnings("unchecked")
		T texpt = (T)expt;
		
		return getModelDataWriter(texpt);
	}
	
	/**
	 * 获取指定{@linkplain Export}的{@linkplain ModelDataWriter}。
	 * 
	 * @param expt
	 * @return
	 */
	protected abstract ModelDataWriter getModelDataWriter(T expt);

	/**
	 * 解析{@linkplain TypedDevotedModelDataWriterFactory}子类的类型参数。
	 * 
	 * @param subClass
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected static Class<?> resolveExportTypeParameter(Class<? extends TypedDevotedModelDataWriterFactory> subClass)
	{
		Class<?> tp = GenericTypeResolver.resolveTypeArgument(subClass, TypedDevotedModelDataWriterFactory.class);
		
		return tp;
	}
}
