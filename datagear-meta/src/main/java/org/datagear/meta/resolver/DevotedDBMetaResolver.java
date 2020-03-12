/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
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
