/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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

		InvalidPatternSqlValidator validator = new InvalidPatternSqlValidator(patterns);

		// 字符串、引用标识符里的关键字验证通过
		{
			String sql = "SELECT `DELETE`, `exec` FROM TABLE WHERE VALUE='DROP'";
			DatabaseProfile profile = new DatabaseProfile("mysql", "", "`");

			SqlValidation validation = validator.validate(sql, profile);

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
			DatabaseProfile profile = new DatabaseProfile("mysql", "", "`");

			SqlValidation validation = validator.validate(sql, profile);

			assertFalse(validation.isValid());
			assertEquals(" DELETE,", validation.getInvalidValue());
		}

		// "my"里的关键字不通过
		{
			String sql = "SELECT `ALTER`, `use`, `CREATE`, `DELETE`, exec, DROP FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("mysql", "", "`");

			SqlValidation validation = validator.validate(sql, profile);

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
