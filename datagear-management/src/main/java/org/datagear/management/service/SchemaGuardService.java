/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service;

import org.datagear.management.domain.Schema;
import org.datagear.management.domain.SchemaGuard;

/**
 * {@linkplain SchemaGuard}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface SchemaGuardService extends EntityService<String, SchemaGuard>
{
	/**
	 * 是否允许创建指定的{@linkplain Schema#getUrl()}。
	 * <p>
	 * 实现类应支持{@code *}（表示任意多个字符）匹配规则，例如：
	 * </p>
	 * <ul>
	 * <li>{@code *}<br>
	 * 匹配任意URL</li>
	 * <li>{@code *abc}<br>
	 * 匹配以{@code abc}结尾的URL</li>
	 * <li>{@code abc*}<br>
	 * 匹配以{@code abc}开头的URL</li>
	 * <li>{@code abc*def}<br>
	 * 匹配以{@code abc}开头、以{@code def}结尾的URL</li>
	 * <li>{@code *abc*def*}<br>
	 * 匹配依次包含{@code abc}、{@code def}的URL</li>
	 * </ul>
	 * <p>
	 * 另外，如果没有定义任何{@linkplain SchemaGuard}，应返回{@code true}。
	 * </p>
	 * 
	 * @param schemaURL
	 * @return
	 */
	boolean isPermitted(String schemaUrl);
}
