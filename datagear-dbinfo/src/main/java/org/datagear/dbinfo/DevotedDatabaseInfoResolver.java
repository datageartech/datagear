/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.dbinfo;

import org.datagear.connection.ConnectionSensor;

/**
 * 专职{@linkplain DatabaseInfoResolver}。
 * <p>
 * 此类继承自{@linkplain DatabaseInfoResolver}的所有方法仅在{@linkplain #supports(java.sql.Connection)}返回{@code true}时可用。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface DevotedDatabaseInfoResolver extends DatabaseInfoResolver, ConnectionSensor
{
}
