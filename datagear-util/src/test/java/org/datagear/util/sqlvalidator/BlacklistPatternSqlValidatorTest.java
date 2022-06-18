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
 * {@linkplain BlacklistPatternSqlValidator}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class BlacklistPatternSqlValidatorTest
{
	public BlacklistPatternSqlValidatorTest()
	{
		super();
	}

	@Test
	public void validateTest()
	{
		Map<String, Pattern> patterns = new HashMap<String, Pattern>();
		patterns.put(BlacklistPatternSqlValidator.DEFAULT_PATTERN_KEY,
				BlacklistPatternSqlValidator.toKeywordPattern("DELETE", "ALTER"));
		patterns.put("my", BlacklistPatternSqlValidator.toKeywordPattern("exec", "use"));
		patterns.put("postgres", BlacklistPatternSqlValidator.toKeywordPattern("DROP", "CREATE"));

		BlacklistPatternSqlValidator validator = new BlacklistPatternSqlValidator(patterns);

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

		// "default"里的关键字不通过
		{
			String sql = "SELECT `ALTER`, `use`, `CREATE`, DELETE, exec, DROP FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("mysql", "", "`");

			SqlValidation validation = validator.validate(sql, profile);

			assertFalse(validation.isValid());
			assertEquals("DELETE", validation.getInvalidValue());
		}

		// "my"里的关键字不通过
		{
			String sql = "SELECT `ALTER`, `use`, `CREATE`, `DELETE`, exec, DROP FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("mysql", "", "`");

			SqlValidation validation = validator.validate(sql, profile);

			assertFalse(validation.isValid());
			assertEquals("exec", validation.getInvalidValue());
		}

		// "postgres"里的关键字不通过
		{
			String sql = "SELECT \"ALTER\", \"use\", \"CREATE\", \"DELETE\", exec, DROP FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertFalse(validation.isValid());
			assertEquals("DROP", validation.getInvalidValue());
		}

		// 忽略大小写
		{
			String sql = "SELECT \"ALTER\", \"use\", \"CREATE\", \"DELETE\", exec, drop FROM TABLE";
			DatabaseProfile profile = new DatabaseProfile("postgresql", "", "\"");

			SqlValidation validation = validator.validate(sql, profile);

			assertFalse(validation.isValid());
			assertEquals("drop", validation.getInvalidValue());
		}
	}
}
