package org.datagear.dataexchange;

import org.springframework.core.GenericTypeResolver;

/**
 * 类型参数{@linkplain DevotedModelDataReaderFactory}。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class TypedDevotedModelDataReaderFactory<T extends Import> implements DevotedModelDataReaderFactory
{
	private Class<?> supportedImportType;
	
	public TypedDevotedModelDataReaderFactory()
	{
		super();
		this.supportedImportType = resolveImportTypeParameter(getClass());
	}

	public Class<?> getSupportedImportType()
	{
		return supportedImportType;
	}

	protected void setSupportedImportType(Class<?> supportedImportType)
	{
		this.supportedImportType = supportedImportType;
	}

	@Override
	public boolean supports(Import impt)
	{
		if(impt == null)
			return false;
		
		return this.supportedImportType.isAssignableFrom(impt.getClass());
	}
	
	@Override
	public ModelDataReader get(Import impt)
	{
		@SuppressWarnings("unchecked")
		T timpt = (T) impt;
		
		return getModelDataReader(timpt);
	}
	
	/**
	 * 获取指定{@linkplain Import}的{@linkplain ModelDataReader}。
	 * 
	 * @param impt
	 * @return
	 */
	protected abstract ModelDataReader getModelDataReader(T impt);

	/**
	 * 解析{@linkplain TypedDevotedModelDataReaderFactory}子类的类型参数。
	 * 
	 * @param subClass
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected static Class<?> resolveImportTypeParameter(Class<? extends TypedDevotedModelDataReaderFactory> subClass)
	{
		Class<?> tp = GenericTypeResolver.resolveTypeArgument(subClass, TypedDevotedModelDataReaderFactory.class);
		
		return tp;
	}
}
