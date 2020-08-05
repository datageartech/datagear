/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support;

/**
 * 模板解析器。
 * <p>
 * 此类解析由模板语言（比如Freemarker）构建的字符串，并返回模板执行结果。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface TemplateResolver
{
	/**
	 * 解析。
	 * 
	 * @param template
	 * @param templateContext
	 * @return
	 * @throws TemplateResolverException
	 */
	String resolve(String template, TemplateContext templateContext) throws TemplateResolverException;
}
