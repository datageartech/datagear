/*
 * Copyright 2018-present datagear.tech
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

package org.datagear.analysis.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.datagear.util.StringUtil;

/**
 * 范围解析器。
 * <p>
 * 此类用于解析诸如{@code "1, 2-3, 6-, -15}之类的范围表达式。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class RangeExpResolver
{
	public static final char RANGE_SPLITTER_CHAR = '-';

	public static final String RANGE_SPLITTER_STRING = "-";

	public static final char RANGE_GROUP_SPLITTER_CHAR = ',';

	public static final String RANGE_GROUP_SPLITTER_STRING = ",";

	private char rangeSplitter = RANGE_SPLITTER_CHAR;

	private char rangeGroupSplitter = RANGE_GROUP_SPLITTER_CHAR;

	public RangeExpResolver()
	{
		super();
	}

	public char getRangeSplitter()
	{
		return rangeSplitter;
	}

	public void setRangeSplitter(char rangeSplitter)
	{
		this.rangeSplitter = rangeSplitter;
	}

	public char getRangeGroupSplitter()
	{
		return rangeGroupSplitter;
	}

	public void setRangeGroupSplitter(char rangeGroupSplitter)
	{
		this.rangeGroupSplitter = rangeGroupSplitter;
	}

	@SuppressWarnings("unchecked")
	public List<IndexRange> resolveIndex(String exp) throws NumberFormatException
	{
		List<Range> ranges = resolve(exp);

		if (ranges.isEmpty())
			return Collections.EMPTY_LIST;

		List<IndexRange> indexRanges = new ArrayList<>(ranges.size());

		for (Range range : ranges)
			indexRanges.add(new IndexRange(range));

		return indexRanges;
	}

	/**
	 * 解析范围表达式组。
	 * <p>
	 * 例如：{@code "1, 2-5, 8-, -15"}
	 * </p>
	 * 
	 * @param exp
	 * @return 如果{@code exp}为{@code null}、或{@code ""}，将返回空列表
	 */
	@SuppressWarnings("unchecked")
	public List<Range> resolve(String exp)
	{
		if (exp != null)
			exp = exp.trim();

		if (exp == null || exp.isEmpty())
			return Collections.EMPTY_LIST;

		List<Range> ranges = new ArrayList<>();

		String[] ss = StringUtil.split(exp, this.rangeGroupSplitter + "", false);

		for (String s : ss)
		{
			Range range = resolveSingle(s);

			if (range != null)
				ranges.add(range);
		}

		return ranges;
	}

	public IndexRange resolveSingleIndex(String exp) throws NumberFormatException
	{
		Range range = resolveSingle(exp);

		if (range == null)
			return null;

		return new IndexRange(range);
	}

	/**
	 * 解析单个范围表达式。
	 * <p>
	 * 例如：{@code "1"}、{@code "4-5"}、{@code "8-"}、{@code "-15"}
	 * </p>
	 * 
	 * @param exp
	 * @return 如果{@code exp}为{@code null}、或{@code ""}，将返回{@code null}
	 */
	public Range resolveSingle(String exp)
	{
		if (exp != null)
			exp = exp.trim();

		if (exp == null || exp.isEmpty())
			return null;

		String from = "";
		String to = "";

		int idx = exp.indexOf(this.rangeSplitter);
		int len = exp.length();

		// 单个值："1"
		if (idx < 0)
		{
			from = exp;
			to = from;
		}
		// 都未指定："-"
		else if (idx == 0 && len == 1)
		{
			from = "";
			to = "";
		}
		// 仅指定截至值："-4"
		else if (idx == 0)
		{
			from = "";
			to = exp.substring(1);
		}
		// 仅指定起始值："4-"
		else if (idx == len - 1)
		{
			from = exp.substring(0, len - 1);
			to = "";
		}
		// 都指定："3-5"
		else
		{
			from = exp.substring(0, idx);
			to = exp.substring(idx + 1);
		}

		return new Range(from, to);
	}

	public static RangeExpResolver valueOf(char rangeSplitter, char rangeGroupSplitter)
	{
		RangeExpResolver resolver = new RangeExpResolver();
		resolver.setRangeSplitter(rangeSplitter);
		resolver.setRangeGroupSplitter(rangeGroupSplitter);

		return resolver;
	}

	/**
	 * 范围。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class Range implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 起始 */
		private String from = "";

		/** 截至 */
		private String to = "";

		public Range()
		{
			super();
		}

		public Range(String from)
		{
			super();
			this.from = from;
		}

		public Range(String from, String to)
		{
			super();
			this.from = from;
			this.to = to;
		}

		public boolean hasFrom()
		{
			return (this.from != null && !this.from.isEmpty());
		}

		public String trimFrom()
		{
			if (this.from == null)
				return "";

			return this.from.trim();
		}

		public String getFrom()
		{
			return from;
		}

		public void setFrom(String from)
		{
			this.from = from;
		}

		public boolean hasTo()
		{
			return (this.to != null && !this.to.isEmpty());
		}

		public String trimTo()
		{
			if (this.to == null)
				return "";

			return this.to.trim();
		}

		public String getTo()
		{
			return to;
		}

		public void setTo(String to)
		{
			this.to = to;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((from == null) ? 0 : from.hashCode());
			result = prime * result + ((to == null) ? 0 : to.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Range other = (Range) obj;
			if (from == null)
			{
				if (other.from != null)
					return false;
			}
			else if (!from.equals(other.from))
				return false;
			if (to == null)
			{
				if (other.to != null)
					return false;
			}
			else if (!to.equals(other.to))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [from=" + from + ", to=" + to + "]";
		}
	}

	/**
	 * 索引范围。
	 * <p>
	 * 索引指大于或等于{@code 0}的整数值。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class IndexRange implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 起始索引 */
		private int from = 0;

		/** 截至索引（包含） */
		private int to = -1;

		public IndexRange()
		{
			super();
			this.from = 0;
			this.to = -1;
		}

		public IndexRange(int from)
		{
			super();
			this.from = from;
			this.to = -1;
		}

		public IndexRange(int from, int to)
		{
			super();
			this.from = from;
			this.to = to;
		}

		public IndexRange(Range range) throws NumberFormatException
		{
			super();

			int from = 0;
			int to = -1;

			String fromStr = range.trimFrom();
			String toStr = range.trimTo();

			if (!StringUtil.isEmpty(fromStr))
				from = Integer.parseInt(fromStr);

			if (!StringUtil.isEmpty(toStr))
				to = Integer.parseInt(toStr);

			this.from = from;
			this.to = to;
		}

		public int getFrom()
		{
			return from;
		}

		/**
		 * 设置起始索引。
		 * 
		 * @param from
		 *            起始索引，小于{@code 0}表示不限定
		 */
		public void setFrom(int from)
		{
			this.from = from;
		}

		public int getTo()
		{
			return to;
		}

		/**
		 * 设置截至索引（包含）。
		 * 
		 * @param to
		 *            截至索引，小于{@code 0}表示不限定
		 */
		public void setTo(int to)
		{
			this.to = to;
		}

		/**
		 * 是否包含给定索引数值。
		 * 
		 * @param index
		 * @return
		 */
		public boolean includes(int index)
		{
			if (this.from > -1 && index < this.from)
				return false;

			if (this.to > -1 && index > this.to)
				return false;

			return true;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + from;
			result = prime * result + to;
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IndexRange other = (IndexRange) obj;
			if (from != other.from)
				return false;
			if (to != other.to)
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [from=" + from + ", to=" + to + "]";
		}

		public static boolean includes(List<IndexRange> indexRanges, int index)
		{
			for (int i = 0; i < indexRanges.size(); i++)
				if (indexRanges.get(i).includes(index))
					return true;

			return false;
		}
	}
}
