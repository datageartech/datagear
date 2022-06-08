/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
	@Autowired
	private CheckCodeManager checkCodeManager;

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

	@RequestMapping("")
	public void index(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @RequestParam("m") String module) throws IOException
	{
		if (!this.checkCodeManager.hasModule(module))
			throw new IllegalInputException();

		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);

		int fontSize = 16;
		int codeLength = CheckCodeManager.CODE_LEN;
		int imagePadding = 2;

		int height = fontSize + imagePadding * 2;
		int width = fontSize * codeLength + imagePadding * 2;

		Font font = new Font(getDefaultFontFamilyName(), Font.BOLD, fontSize);

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		g.setColor(this.getRandomColor(200, 50));
		g.setFont(font);
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

		String code = this.checkCodeManager.generate();
		g.setColor(getRandomColor(0, 80));
		FontMetrics fontMetrics = g.getFontMetrics();
		int codeWidth = fontMetrics.stringWidth(code);

		g.drawString(code, imagePadding + (width - codeWidth) / 2, imagePadding + fontMetrics.getAscent());

		this.checkCodeManager.setCheckCode(session, module, code);

		g.dispose();

		ImageIO.write(image, "png", response.getOutputStream());
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

	/**
	 * 获取默认字体名。
	 * 
	 * @return
	 */
	protected String getDefaultFontFamilyName()
	{
		return "Arial";
	}
}
