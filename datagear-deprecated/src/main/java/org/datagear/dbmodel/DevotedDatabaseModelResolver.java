/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbmodel;

import org.datagear.connection.ConnectionSensor;

/**
 * 专职{@linkplain DatabaseModelResolver}。
 * <p>
 * 此类继承自{@linkplain DatabaseModelResolver}的所有方法仅在{@linkplain #supports(java.sql.Connection)}返回{@code true}时可用。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface DevotedDatabaseModelResolver extends DatabaseModelResolver, ConnectionSensor
{
}
