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

package org.datagear.util.sqlvalidator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * {@linkplain InvalidPatternSqlValidator}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class InvalidPatternSqlValidatorTest
{
	public InvalidPatternSqlValidatorTest()
	{
		super();
	}

	@Test
	public void validateTest()
	{
		Map<String, Pattern> patterns = new HashMap<String, Pattern>();
		patterns.put(InvalidPatternSqlValidator.DEFAULT_PATTERN_KEY,
				InvalidPatternSqlValidator.toKeywordPattern("DELETE", "ALTER"));
		patterns.put("mysql", InvalidPatternSqlValidator.toKeywordPattern("exec", "use"));
		patterns.put("postgres", InvalidPatternSqlValidator.toKeywordPattern("DROP", "CREATE"));
		patterns.put("jdbc://sqlserver", InvalidPatternSqlValidator.toKeywordPattern("dbo"));
		
		DatabaseProfile mysqlProfile = new DatabaseProfile("mysql", "", "`");

		InvalidPatternSqlValidator validator = new InvalidPatternSqlValidator(patterns);

		//基本
		{
			String sql = "DELETE";
			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertFalse(validation.isValid());
		}
		{
			String sql = "DELETE ";
			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertFalse(validation.isValid());
		}
		{
			String sql = " DELETE";
			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertFalse(validation.isValid());
		}
		{
			String sql = " DELETE ";
			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertFalse(validation.isValid());
		}
		{
			String sql = "delete";
			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertFalse(validation.isValid());
		}
		{
			String sql = "delete ";
			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertFalse(validation.isValid());
		}
		{
			String sql = " delete";
			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertFalse(validation.isValid());
		}
		{
			String sql = " delete ";
			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertFalse(validation.isValid());
		}
		{
			String sql = "DELETE_";
			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "_DELETE";
			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "_DELETE_";
			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "delete_";
			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "_delete";
			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "_delete_";
			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "deleteZ";
			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "Zdelete";
			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "ZdeleteZ";
			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "DELETE FROM TABLE WHERE VALUE='DROP'";
			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertFalse(validation.isValid());
		}
		{
			String sql = "delete from table where value='drop'";
			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertFalse(validation.isValid());
		}
		
		// 字符串、引用标识符里的关键字验证通过
		{
			String sql = "SELECT `DELETE`, `exec` FROM TABLE WHERE VALUE='DROP'";

			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "SELECT `DELETE`, `exec` FROM TABLE WHERE VALUE='DROP'";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "`");

			SqlValidation validation = validator.validate(sql, profile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "SELECT `DELETE`, `exec` FROM TABLE WHERE VALUE='DROP'";
			DatabaseProfile profile = new DatabaseProfile("mysql-postgresql", "", "`");

			SqlValidation validation = validator.validate(sql, profile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "SELECT `DELETE`, `exec` FROM TABLE WHERE VALUE='DROP'";
			DatabaseProfile profile = new DatabaseProfile("mysql-postgresql", "", "`");

			SqlValidation validation = validator.validate(sql, profile);

			assertTrue(validation.isValid());
		}

		// URL匹配
		{
			String sql = "SELECT \"DELETE\", dbo.\"ALTER\" FROM TABLE WHERE VALUE='DROP'";
			DatabaseProfile profile = new DatabaseProfile("SQL SERVER", "jdbc://sqlserver:1533", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertFalse(validation.isValid());
			assertEquals(" dbo.", validation.getInvalidValue());
		}

		// "default"里的关键字不通过
		{
			String sql = "SELECT `ALTER`, `use`, `CREATE`, DELETE, exec, DROP FROM TABLE";

			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertFalse(validation.isValid());
			assertEquals(" DELETE,", validation.getInvalidValue());
		}

		// "my"里的关键字不通过
		{
			String sql = "SELECT `ALTER`, `use`, `CREATE`, `DELETE`, exec, DROP FROM TABLE";

			SqlValidation validation = validator.validate(sql, mysqlProfile);

			assertFalse(validation.isValid());
			assertEquals(" exec,", validation.getInvalidValue());
		}

		// "postgres"里的关键字不通过
		{
			String sql = "SELECT \"ALTER\", \"use\", \"CREATE\", \"DELETE\", exec, DROP FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertFalse(validation.isValid());
			assertEquals(" DROP ", validation.getInvalidValue());
		}

		// 忽略大小写
		{
			String sql = "SELECT \"ALTER\", \"use\", \"CREATE\", \"DELETE\", exec, drop FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertFalse(validation.isValid());
			assertEquals(" drop ", validation.getInvalidValue());
		}
		
		//子串
		{
			String sql = "SELECT create FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertFalse(validation.isValid());
			assertEquals(" create ", validation.getInvalidValue());
		}
		{
			String sql = "SELECT create, FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertFalse(validation.isValid());
			assertEquals(" create,", validation.getInvalidValue());
		}
		{
			String sql = "SELECT ,create FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertFalse(validation.isValid());
			assertEquals(",create ", validation.getInvalidValue());
		}
		{
			String sql = "SELECT ,create, FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertFalse(validation.isValid());
			assertEquals(",create,", validation.getInvalidValue());
		}
		{
			String sql = "SELECT create_date FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "SELECT _create FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "SELECT create_ FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "SELECT _create_ FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "SELECT zcreate FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "SELECT createz FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "SELECT zcreatez FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "SELECT Zcreate FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "SELECT createZ FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "SELECT ZcreateZ FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "SELECT 1create FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "SELECT create1 FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertTrue(validation.isValid());
		}
		{
			String sql = "SELECT 1create1 FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertTrue(validation.isValid());
		}
	}
}
