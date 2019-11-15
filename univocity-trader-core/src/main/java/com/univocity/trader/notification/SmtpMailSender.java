/*
 * Copyright (c) 2017 Univocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 *
 */

package com.univocity.trader.notification;

import org.apache.commons.lang3.*;
import org.slf4j.*;
import org.springframework.mail.*;
import org.springframework.mail.javamail.*;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;

public class SmtpMailSender {
	private static final Logger log = LoggerFactory.getLogger(SmtpMailSender.class);

	private JavaMailSender mailSender;
	private MailSenderConfig config;

	public SmtpMailSender(MailSenderConfig config) {
		this.config = config;
	}

	public String getSenderAddress(){
		return config.getSmtpSender();
	}

	public Email newEmail(String[] to, String title, String content) {
		if (ArrayUtils.isEmpty(to)) {
			log.warn("Unable to send email. No recipient emails provided.");
			return null;
		}

		Email email = new Email();
		email.setReplyTo(config.getReplyToAddress());
		email.setBody(content);
		email.setFrom(config.getSmtpSender());
		email.setTitle(title);
		email.setTo(to);
		return email;
	}

	public boolean sendEmail(String[] to, String title, String content) {
		Email email = newEmail(to, title, content);
		if (email == null) {
			return false;
		}

		if (!isConfigured()) {
			log.warn("Unable to send email. No email configuration available.");
			return false;
		}

		return true;
	}

	private boolean isConfigured() {
		return config != null && config.isConfigured();
	}

	public JavaMailSender getMailSender() {
		if (mailSender == null) {
			synchronized (this) {
				if (mailSender == null) {
					JavaMailSenderImpl sender = new JavaMailSenderImpl();
					sender.setHost(config.getSmtpHost());

					Properties properties = new Properties();
					properties.put("mail.transport.protocol", "smtps");
					if (config.isSmtpTlsSsl()) {
						properties.put("mail.smtps.starttls.enable", true);
					}
					properties.put("mail.smtps.auth", true);

					Integer port = config.getSmtpPort();
					if (port != null && port != 0) {
						properties.put("mail.smtps.socketFactory.port", port);
						sender.setPort(port);
					}

					properties.put("mail.smtps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
					properties.put("mail.smtps.socketFactory.fallback", false);

					final String user = config.getSmtpUsername();
					final char[] password = config.getSmtpPassword();

					Session session = Session.getInstance(properties, new Authenticator() {
						@Override
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(user, password == null ? null : new String(password));
						}
					});

					sender.setSession(session);
					mailSender = sender;
				}
			}
		}
		return mailSender;
	}

	public void sendEmailViaSmtp(Email email) {
		JavaMailSender mailSender = getMailSender();
		if (mailSender == null) {
			log.warn("Can't send e-mail: " + email + ". No mail sender available");
			return;
		}

		MimeMessage message = mailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(email.getFrom());
			helper.setTo(email.getTo());
			if (StringUtils.isNotBlank(email.getReplyTo())) {
				helper.setReplyTo(email.getReplyTo());
			}

			helper.setSubject(email.getTitle());
			helper.setText(email.getBody());
		} catch (MessagingException e) {
			log.warn("Unable to write e-mail", e);
		}

		try {
			mailSender.send(message);
		} catch (MailException e) {
			log.error("Error sending e-mail", e);
		}
	}
}
