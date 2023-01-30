/*
 * Copyright 2018-2023 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
