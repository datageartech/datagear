package org.datagear.util;

import org.junit.Test;

/**
 * {@linkplain IDUtil}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class IDUtilTest
{
	@Test
	public void randomIdOnTime20Test()
	{
		for (int i = 0; i < 100; i++)
		{
			String id = IDUtil.randomIdOnTime20();
			System.out.println(id);
		}
	}

	@Test
	public void randomTest()
	{
		for (int i = 0; i < 100; i++)
		{
			String id = IDUtil.random(20);
			System.out.println(id);
		}
	}
}
