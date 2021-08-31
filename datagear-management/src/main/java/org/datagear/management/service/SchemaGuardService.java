/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service;

import org.datagear.management.domain.Schema;
import org.datagear.management.domain.SchemaGuard;
import org.datagear.util.AsteriskPatternMatcher;

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
	 * 实现类应支持{@linkplain AsteriskPatternMatcher}匹配规则，
	 * 并且，如果没有定义任何{@linkplain SchemaGuard}，应返回{@code true}。
	 * </p>
	 * 
	 * @param schemaURL
	 * @return
	 */
	boolean isPermitted(String schemaUrl);
}
