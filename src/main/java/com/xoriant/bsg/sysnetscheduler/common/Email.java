package com.xoriant.bsg.sysnetscheduler.common;

import java.io.File;

public class Email {
	
	private String subject = "";
	
	private String[] to = {};
	
	private String[] cc = {};
	
	private File[] attachments = {};
	
	private String body = "";
	
	public Email(EmailBuilder builder) {
		this.subject = builder.subject;
		this.to = builder.to;
		this.cc = builder.cc;
		this.attachments = builder.attachments;
		this.body = builder.body;
	}

	public String getSubject() {
		return subject;
	}

	public String[] getTo() {
		return to;
	}

	public String[] getCc() {
		return cc;
	}

	public File[] getAttachments() {
		return attachments;
	}

	public String getBody() {
		return body;
	}

	public static class EmailBuilder{
		
		private String subject;
		
		private String[] to;
		
		private String[] cc;
		
		private File[] attachments;
		
		private String body;
		
		private EmailBuilder() {

		}
		
		public static EmailBuilder newInstance() {
			return new EmailBuilder();
		}
		
		public EmailBuilder withSubject(String subject) {
			this.subject = subject;
			return this;
		}
		
		public EmailBuilder withTo(String[] to) {
			this.to = to;
			return this;
		}
		
		public EmailBuilder withCC(String[] cc) {
			this.cc = cc;
			return this;
		}
		
		public EmailBuilder withAttachments(File[] attachments) {
			this.attachments = attachments;
			return this;
		}
		
		public EmailBuilder withBody(String body) {
			this.body = body;
			return this;
		}
		
		public Email build() {
			return new Email(this);
		}
	}	

}
