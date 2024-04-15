/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.VersendeteMail;
import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.lib.cdipersistence.Persistence;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.mail.*;
import org.apache.commons.net.smtp.SMTPReply;
import org.apache.commons.net.smtp.SMTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.transaction.Status;
import javax.transaction.TransactionSynchronizationRegistry;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static ch.dvbern.ebegu.util.Constants.NEW_LINE_CHAR_PATTERN;

/**
 * Allgemeine Mailing-Funktionalität
 */
public abstract class AbstractMailServiceBean extends AbstractBaseService {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractMailServiceBean.class.getSimpleName());
	private static final int CONNECTION_TIMEOUT = 15000;

	@Inject
	private Persistence persistence;

	@Inject
	private EbeguConfiguration configuration;

	@Inject
	private VersendeteMailsService versendeteMailsService;

	@Resource
	private TransactionSynchronizationRegistry txReg;

	public void sendMessage(
		@Nonnull String subject,
		@Nonnull String messageBody,
		@Nonnull String mailadress)
		throws MailException {

		Objects.requireNonNull(subject);
		Objects.requireNonNull(messageBody);
		Objects.requireNonNull(mailadress);

		if (configuration.isSendingOfMailsDisabled()) {
			pretendToSendMessage(messageBody, mailadress);
		} else {
			doSendMessage(subject, messageBody, mailadress);
			saveSentMails(subject, mailadress);
		}
	}

	public void sendMessageWithAttachment(
		@Nonnull String subject,
		@Nonnull String messageBody,
		@Nonnull String mailadress,
		@Nonnull UploadFileInfo uploadFileInfo
	) throws MailException {

		Objects.requireNonNull(subject);
		Objects.requireNonNull(messageBody);
		Objects.requireNonNull(mailadress);
		Objects.requireNonNull(uploadFileInfo);

		if (configuration.isSendingOfMailsDisabled()) {
			pretendToSendMessage(messageBody, mailadress);
		} else {
			doSendMessageWithAttachment(subject, messageBody, mailadress, uploadFileInfo);
			saveSentMails(subject, mailadress);
		}
	}

	private void doSendMessage(
		@Nonnull String subject,
		@Nonnull String messageBody,
		@Nonnull String mailadress)
		throws MailException {
		try {
			Email email = new SimpleEmail();
			email.setHostName(configuration.getSMTPHost());
			email.setSmtpPort(configuration.getSMTPPort());
			email.setSSLOnConnect(false);
			email.setFrom(configuration.getSenderAddress());
			email.setSubject(subject);
			email.setMsg(messageBody);
			email.addTo(mailadress);
			email.send();
		} catch (final EmailException e) {
			throw new MailException("Error while sending Mail to: '" + mailadress + '\'', e);
		}
	}

	private void doSendMessageWithAttachment(
		@Nonnull String subject,
		@Nonnull String messageBody,
		@Nonnull String mailadress,
		@Nonnull UploadFileInfo uploadFileInfo)
		throws MailException {
		try {
			// Create the attachment
			EmailAttachment attachment = new EmailAttachment();
			final String pathOfAttachment = "File://" + uploadFileInfo.getPathAsString();
			attachment.setURL(new URL(pathOfAttachment));
			attachment.setDisposition(EmailAttachment.ATTACHMENT);
			attachment.setDescription(uploadFileInfo.getFilename());
			attachment.setName(uploadFileInfo.getFilename());

			// Create the email message
			MultiPartEmail email = new MultiPartEmail();
			email.setHostName(configuration.getSMTPHost());
			email.setSmtpPort(configuration.getSMTPPort());
			email.setSSLOnConnect(false);

			email.setFrom(configuration.getSenderAddress());
			email.setSubject(subject);
			email.setMsg(messageBody);
			email.addTo(mailadress);

			// add the attachment
			email.attach(attachment);

			// send the email
			email.send();
		} catch (final EmailException | MalformedURLException e) {
			throw new MailException("Error while sending Mail with Attachment to: '" + mailadress + '\'', e);
		}
	}

	@SuppressFBWarnings("REC_CATCH_EXCEPTION")
	private void  doSendMessage(@Nonnull String messageBody, @Nonnull String mailadress, @Nonnull MandantIdentifier mandantIdentifier) throws MailException {
		final SMTPSClient client = new SMTPSClient("TLS", false, StandardCharsets.UTF_8.displayName());
		Writer writer = null;
		try {
			client.setDefaultTimeout(CONNECTION_TIMEOUT);
			client.connect(configuration.getSMTPHost(), configuration.getSMTPPort());
			if (!client.execTLS()) {
				LOG.warn("connecting to %s without {}", configuration.getSMTPHost());
			}
			client.setSoTimeout(CONNECTION_TIMEOUT);
			assertPositiveCompletion(client);
			client.helo(configuration.getHostname(mandantIdentifier));
			assertPositiveCompletion(client);
			client.setSender(configuration.getSenderAddress());
			assertPositiveCompletion(client);
			client.addRecipient(mailadress);
			assertPositiveCompletion(client);
			writer = client.sendMessageData();
			writer.write(messageBody);
			writer.close();
			assertPositiveIntermediate(client);
			client.quit();
		} catch (final Exception e) {
			throw new MailException("Error while sending Mail to: '" + mailadress + '\'', e);
		} finally {
			if (client.isConnected()) {
				try {
					client.disconnect();
				} catch (final IOException e) {
					LOG.error("Could not disconnetct client", e);
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ignore) {
					// NOP
				}
			}
		}
	}

	/**
	 * Emails should only be sent when all actions were performed withou any error.
	 * For this reason this method flushes the EntityManager before sending emails.
	 */
	protected void sendMessageWithTemplate(@Nonnull final String messageBody, @Nonnull final String mailadress, @Nonnull final MandantIdentifier mandantIdentifier)
		throws MailException {
		Objects.requireNonNull(mailadress);
		Objects.requireNonNull(messageBody);
		// wir haben hier nicht zwingend immer eine transaktion
		final int transactionStatus = txReg.getTransactionStatus();
		if (Status.STATUS_NO_TRANSACTION != transactionStatus) {
			// nur wenn eine Transaction existiert, macht ein flush Sinn
			persistence.getEntityManager().flush();
		}
		if (configuration.isSendingOfMailsDisabled()) {
			pretendToSendMessage(messageBody, mailadress);
		} else {
			doSendMessage(messageBody, mailadress, mandantIdentifier);
			String subject = extractSubjectFromMessageBody(messageBody);
			saveSentMails(subject, mailadress);
		}
	}

	private String extractSubjectFromMessageBody(String messageBody) {
		String decodedSubject = messageBody.substring(messageBody.indexOf("Subject: ")+9, messageBody.indexOf("Content-Type"));
		return decodeMixedBase64String(decodedSubject);
	}

	public static String decodeMixedBase64String(String mixedString) {
		StringBuilder decodedBuilder = new StringBuilder();
		int start = 0; // Start index for the non-encoded part

		while (start < mixedString.length()) {
			int startIndex = mixedString.indexOf("=?", start);
			if (startIndex == -1) {
				// No more encoded parts, append the rest of the string and break
				decodedBuilder.append(mixedString.substring(start));
				return decodedBuilder.toString();
			}
			// Append non-encoded part before the encoded section
			if (startIndex != start) {
				decodedBuilder.append(mixedString.substring(start, startIndex));
			}
			int endIndex = mixedString.indexOf("?=", startIndex) + 2;
			if (endIndex == 1) { // No closing tag found, break to avoid an infinite loop
				return decodedBuilder.toString();
			}
			// Extract the encoded part without the MIME and encoding prefix and suffix
			String encodedPart = mixedString.substring(startIndex + 10, endIndex - 2);
			// Decode and append the encoded part
			byte[] decodedBytes = Base64.getDecoder().decode(encodedPart);
			decodedBuilder.append(new String(decodedBytes, StandardCharsets.UTF_8));

			// Move start index forward
			start = endIndex;
		}

		return decodedBuilder.toString();
	}

	private void pretendToSendMessage(final String messageBody, final String mailadress) {
		LOG.info("Sending of Emails disabled. Mail would be sent to {} : {}", removeNewLineChar(mailadress), removeNewLineChar(messageBody));
	}

	protected String removeNewLineChar(String str) {
		return NEW_LINE_CHAR_PATTERN.matcher(str).replaceAll("_");
	}

	private void assertPositiveIntermediate(final SMTPSClient client) {
		assertPositiveIntermediate(client.getReplyCode());
	}

	private void assertPositiveIntermediate(final int replyCode) {
		if (!SMTPReply.isPositiveIntermediate(replyCode)) {
			throw new IllegalStateException("Reply code is not as expected: " + replyCode);
		}
	}

	private void assertPositiveCompletion(final int replyCode) {
		if (!SMTPReply.isPositiveCompletion(replyCode)) {
			throw new IllegalStateException("Reply code is not as expected: " + replyCode);
		}
	}

	private void assertPositiveCompletion(final SMTPSClient client) {
		assertPositiveCompletion(client.getReplyCode());
	}

	private void saveSentMails(String subject, String mailadress) {
		LocalDateTime zeitpunktVersand = LocalDateTime.now();
		String empfaengerAdresse = mailadress;
		String betreff = subject;
		VersendeteMail VersendeteMail = new VersendeteMail(zeitpunktVersand, empfaengerAdresse, betreff);
		versendeteMailsService.saveVersendeteMail(VersendeteMail);
	}
}
