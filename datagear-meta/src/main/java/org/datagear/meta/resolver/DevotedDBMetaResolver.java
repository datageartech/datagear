/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.meta.resolver;

import org.datagear.connection.ConnectionSensor;

/**
 * 专职{@linkplain DBMetaResolver}。
 * <p>
 * 此类继承自{@linkplain DBMetaResolver}的所有方法仅在{@linkplain #supports(java.sql.Connection)}返回{@code true}时可用。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface DevotedDBMetaResolver extends DBMetaResolver, ConnectionSensor
{
}
