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

package org.datagear.web.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.datagear.util.StringUtil;
import org.datagear.web.config.ApplicationProperties;
import org.datagear.web.config.CheckCodeProperties;
import org.datagear.web.util.CheckCodeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 验证码控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/checkCode")
public class CheckCodeController extends AbstractController
{
	protected static final int IMAGE_PADDING = 2;

	@Autowired
	private CheckCodeManager checkCodeManager;

	@Autowired
	private ApplicationProperties applicationProperties;

	private volatile Font _drawFont = null;

	public CheckCodeController()
	{
		super();
	}

	public CheckCodeManager getCheckCodeManager()
	{
		return checkCodeManager;
	}

	public void setCheckCodeManager(CheckCodeManager checkCodeManager)
	{
		this.checkCodeManager = checkCodeManager;
	}

	public ApplicationProperties getApplicationProperties()
	{
		return applicationProperties;
	}

	public void setApplicationProperties(ApplicationProperties applicationProperties)
	{
		this.applicationProperties = applicationProperties;
	}

	@RequestMapping("")
	public void index(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @RequestParam("m") String module) throws IOException
	{
		if (!this.checkCodeManager.hasModule(module))
			throw new IllegalInputException();

		CheckCodeProperties properties = getCheckCodeProperties();

		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/" + properties.getImageFormatName());

		int codeLength = CheckCodeManager.CODE_LEN;
		int width = properties.getFontSize() * codeLength + IMAGE_PADDING * 2;
		int height = properties.getFontSize() + IMAGE_PADDING * 2;
		String code = this.checkCodeManager.generate();

		if (properties.getImageWidth() > -1)
		{
			width = properties.getImageWidth();
		}
		if (properties.getImageHeight() > -1)
		{
			height = properties.getImageHeight();
		}

		BufferedImage image = createInitBufferedImage(width, height);
		drawCheckCode(image, code);
		this.checkCodeManager.setCheckCode(session, module, code);
		image.getGraphics().dispose();

		ImageIO.write(image, properties.getImageFormatName(), response.getOutputStream());
	}

	protected CheckCodeProperties getCheckCodeProperties()
	{
		return getApplicationProperties().getCheckCodeProperties();
	}

	protected void drawCheckCode(BufferedImage image, String code)
	{
		CheckCodeProperties properties = getCheckCodeProperties();
		Graphics g = image.getGraphics();
		Font font = getDrawFont();

		g.setColor(getRandomColor(0, 80));
		g.setFont(font);
		FontMetrics fontMetrics = g.getFontMetrics();
		int codeWidth = fontMetrics.stringWidth(code);
		int x = IMAGE_PADDING + (image.getWidth() - codeWidth) / 2;
		int y = IMAGE_PADDING + fontMetrics.getAscent();

		if (properties.getCodeLeft() > -1)
		{
			x = properties.getCodeLeft();
		}
		if (properties.getCodeTop() > -1)
		{
			y = properties.getCodeTop();
		}

		g.drawString(code, x, y);
	}

	protected BufferedImage createInitBufferedImage(int width, int height)
	{
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		g.setColor(this.getRandomColor(200, 50));
		g.fillRect(0, 0, width, height);

		Random random = new Random();

		// 干扰线
		for (int i = 0; i < 50; i++)
		{
			g.setColor(this.getRandomColor(150, 50));

			int x = random.nextInt(width);
			int y = random.nextInt(height);
			int x1 = random.nextInt(width);
			int y1 = random.nextInt(height);

			g.drawLine(x, y, x1, y1);
		}

		return image;
	}

	/**
	 * 获取随机颜色。
	 * 
	 * @param base
	 * @param seed
	 * @return
	 */
	protected Color getRandomColor(int base, int seed)
	{
		Random random = new Random();

		int r = base + random.nextInt(seed);
		int g = base + random.nextInt(seed);
		int b = base + random.nextInt(seed);

		if (r < 0)
			r = 0;
		if (r > 255)
			r = 255;

		if (g < 0)
			g = 0;
		if (g > 255)
			g = 255;

		if (b < 0)
			b = 0;
		if (b > 255)
			b = 255;

		return new Color(r, g, b);
	}

	protected Font getDrawFont()
	{
		if (this._drawFont == null)
		{
			CheckCodeProperties properties = getCheckCodeProperties();
			Font font = null;

			String name = getDrawFontName();

			if (!StringUtil.isEmpty(name))
			{
				font = new Font(name, properties.getFontStyle(), properties.getFontSize());
			}
			else
			{
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				Font[] fonts = ge.getAllFonts();

				font = (fonts != null && fonts.length > 0 ? fonts[0] : null);
			}

			if (font == null)
				throw new UnsupportedOperationException("No font found");

			this._drawFont = font;
		}

		return this._drawFont;
	}

	/**
	 * 获取字体名。
	 * 
	 * @return
	 */
	protected String getDrawFontName()
	{
		String name = "";

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] avaNames = ge.getAvailableFontFamilyNames();
		avaNames = (StringUtil.isEmpty(avaNames) ? new String[0] : avaNames);

		CheckCodeProperties properties = getCheckCodeProperties();
		String[] fontNames = properties.getFontNames();

		if (!StringUtil.isEmpty(fontNames))
		{
			for (String fontName : fontNames)
			{
				for (String avaName : avaNames)
				{
					if (avaName.equalsIgnoreCase(fontName))
					{
						name = avaName;
						break;
					}
				}

				if (!StringUtil.isEmpty(name))
					break;
			}
		}

		// 自动选择字体
		if (StringUtil.isEmpty(name))
		{
			// 最优字体
			String bestName = "Arial";
			String bestNameLower = bestName.toLowerCase();

			for (String avaName : avaNames)
			{
				// 完全匹配
				if (avaName.equalsIgnoreCase(bestName))
				{
					name = avaName;
					break;
				}

				// 否则，使用第一个部分匹配的
				if (StringUtil.isEmpty(name) && avaName.toLowerCase().indexOf(bestNameLower) >= 0)
				{
					name = avaName;
				}
			}

			if (StringUtil.isEmpty(name))
				name = avaNames[0];
		}

		return name;
	}
}
