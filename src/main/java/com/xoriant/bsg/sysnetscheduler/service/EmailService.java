package com.xoriant.bsg.sysnetscheduler.service;

import java.io.File;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.xoriant.bsg.sysnetscheduler.common.ApplicationConstants;
import com.xoriant.bsg.sysnetscheduler.common.Email;



@Service
public class EmailService {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${spring.mail.username}")
	private String from;

	@Value("${sysnet.email.recipients}")
	private String recipientsEmail;

	@Value("${sysnet.email.support}")
	private String supportEmail;
	
	@Value("${csv.file.transfer.name}")
	private String fileTransferName;

	@Autowired
	private JavaMailSender mailSender;

	public void sendMail(String subject, String[] to, String[] cc, String body) {
		logger.info("Send mail from-[{}] to-[{}] with subject-[{}]",from ,to, subject);
		SimpleMailMessage message = new SimpleMailMessage();
		message.setSubject(subject);
		message.setFrom(from);
		message.setTo(to);
		message.setCc(cc);
		message.setText(body);
		mailSender.send(message);
	}

	public void sendNoFileToProcessNotification() {
		Email email = Email.EmailBuilder.newInstance()
				.withSubject(ApplicationConstants.NO_FILE_EMAIL_SUBJECT)
				.withTo(recipientsEmail.split(","))
				.withCC(supportEmail.split(","))
				.withBody(ApplicationConstants.NO_FILE_EMAIL_BODY)
				.build();

		sendAttachmentMessage(email);
	}

	public void sendNotificationWithAttachment(File[] attachments) {
		String body = ApplicationConstants.EMAIL_BODY;
		if(ObjectUtils.isEmpty(attachments) || attachments.length == 0) {
			body = ApplicationConstants.EMAIL_BODY_NO_NEW_DATA;
		}
		Email email = Email.EmailBuilder.newInstance()
				.withSubject(ApplicationConstants.EMAIL_SUBJECT)
				.withTo(recipientsEmail.split(","))
				.withCC(supportEmail.split(","))
				.withBody(body)
				.withAttachments(attachments)
				.build();

		sendAttachmentMessage(email);
	}

	private void sendAttachmentMessage(Email email) {

		MimeMessage message = mailSender.createMimeMessage();

		MimeMessageHelper helper = null;
		try {
			helper = new MimeMessageHelper(message, true);
			
			helper.setSubject(email.getSubject());
			helper.setFrom(from);
			
			if(ObjectUtils.isEmpty(email.getTo())) {
				helper.setTo(from);
			} else {
				helper.setTo(email.getTo());
			}
			
			if(!ObjectUtils.isEmpty(email.getCc())) {
				helper.setCc(email.getCc());
			}
			
			helper.setText(email.getBody(), true);
			
			if(!ObjectUtils.isEmpty(email.getAttachments())) {
				for(File attachment : email.getAttachments()) {
					FileSystemResource file = new FileSystemResource(new File(attachment.getAbsolutePath()));
					helper.addAttachment(fileTransferName, file);
				}
			}
			
		} catch (MessagingException e) {
			logger.info("Error sending email with attachment.", e);
		}

		mailSender.send(message);
		logger.info("Mail sent successfully.");
	}

}
