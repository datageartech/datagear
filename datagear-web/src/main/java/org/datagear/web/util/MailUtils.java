/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.datagear.management.domain.SmtpSetting;
import org.datagear.management.domain.SmtpSetting.ConnectionType;

/**
 * 邮件工具类。
 * 
 * @author datagear@163.com
 *
 */
public class MailUtils
{
	private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

	/**
	 * 发送邮件。
	 * 
	 * @param smtpSetting
	 * @param receiverEmail
	 * @param subject
	 * @param content
	 */
	public static void send(SmtpSetting smtpSetting, String receiverEmail, String subject, String content)
			throws MessagingException
	{
		Properties properties = new Properties();
		properties.setProperty("mail.host", smtpSetting.getHost());
		properties.setProperty("mail.smtp.port", Integer.toString(smtpSetting.getPort()));
		properties.setProperty("mail.transport.protocol", "smtp");
		properties.setProperty("mail.smtp.auth", "true");

		if (ConnectionType.SSL.equals(smtpSetting.getConnectionType()))
		{
			properties.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
			properties.setProperty("mail.smtp.socketFactory.fallback", "false");
			properties.setProperty("mail.smtp.socketFactory.port", "465");
		}
		else if (ConnectionType.TLS.equals(smtpSetting.getConnectionType()))
		{
			properties.put("mail.smtp.starttls.enable", "true");
		}

		Session session = Session.getInstance(properties);

		Transport transport = session.getTransport();

		transport.connect(smtpSetting.getUsername(), smtpSetting.getPassword());

		Message message = createTextMimeMessage(session, smtpSetting, receiverEmail, subject, content);

		try
		{
			transport.sendMessage(message, message.getAllRecipients());
		}
		finally
		{
			transport.close();
		}
	}

	/**
	 * 创建一个文本邮件。
	 * 
	 * @param session
	 * @param smtpSetting
	 * @param receiverEmail
	 * @param subject
	 * @param content
	 * @return
	 * @throws MessagingException
	 */
	public static MimeMessage createTextMimeMessage(Session session, SmtpSetting smtpSetting, String receiverEmail,
			String subject, String content) throws MessagingException
	{
		MimeMessage message = new MimeMessage(session);

		message.setFrom(new InternetAddress(smtpSetting.getSystemEmail()));
		message.setRecipient(Message.RecipientType.TO, new InternetAddress(receiverEmail));

		message.setSubject(subject);
		message.setContent(content, "text/html;charset=UTF-8");

		return message;
	}
}
