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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.BiFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.DownloadFile;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.mail.MailTemplateConfiguration;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static java.util.Objects.requireNonNull;

/**
 * Service fuer Senden von E-Mails
 */
@Stateless
@Local(MailService.class)
@PermitAll
public class MailServiceBean extends AbstractMailServiceBean implements MailService {

	private static final Logger LOG = LoggerFactory.getLogger(MailServiceBean.class.getSimpleName());

	@Inject
	private MailTemplateConfiguration mailTemplateConfig;

	@Inject
	private FallService fallService;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private GemeindeService gemeindeService;

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION })
	public void sendInfoBetreuungenBestaetigt(@Nonnull Gesuch gesuch) throws MailException {
		final Sprache sprache = EbeguUtil.extractKorrespondezsprache(gesuch, gemeindeService);
		sendMail(
			gesuch,
			"InfoBetreuungBestaetigt",
			(gesuchsteller, adr) -> mailTemplateConfig.getInfoBetreuungenBestaetigt(gesuch, gesuchsteller, adr, sprache)
		);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION })
	public void sendInfoBetreuungAbgelehnt(@Nonnull Betreuung betreuung) throws MailException {
		final Sprache sprache = EbeguUtil.extractKorrespondezsprache(betreuung.extractGesuch(), gemeindeService);
		sendMail(
			betreuung.extractGesuch(),
			"InfoBetreuungAbgelehnt",
			(gesuchsteller, adr) -> mailTemplateConfig.getInfoBetreuungAbgelehnt(betreuung, gesuchsteller, adr, sprache)
		);
	}

	@Override
	public void sendInfoSchulamtAnmeldungUebernommen(@Nonnull Betreuung betreuung) throws MailException {
		final Sprache sprache = EbeguUtil.extractKorrespondezsprache(betreuung.extractGesuch(), gemeindeService);
		sendMail(
			betreuung.extractGesuch(),
			"InfoSchulamtAnmeldungUebernommen",
			(gesuchsteller, adr) ->
				mailTemplateConfig.getInfoSchulamtAnmeldungUebernommen(betreuung, gesuchsteller, adr, sprache)
		);
	}

	@Override
	public void sendInfoSchulamtAnmeldungAbgelehnt(@Nonnull Betreuung betreuung) throws MailException {
		final Sprache sprache = EbeguUtil.extractKorrespondezsprache(betreuung.extractGesuch(), gemeindeService);
		sendMail(
			betreuung.extractGesuch(),
			"InfoSchulamtAnmeldungAbgelehnt",
			(gesuchsteller, adr) -> mailTemplateConfig.getInfoSchulamtAnmeldungAbgelehnt(betreuung, gesuchsteller, adr, sprache)
		);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_TS, ADMIN_TS })
	public void sendInfoMitteilungErhalten(@Nonnull Mitteilung mitteilung) throws MailException {
		final Sprache sprache = EbeguUtil.extractKorrespondezsprache(mitteilung.getBetreuung().extractGesuch(), gemeindeService);
		if (doSendMail(mitteilung.getFall())) {
			String mailaddress = fallService.getCurrentEmailAddress(mitteilung.getFall().getId()).orElse(null);
			if (StringUtils.isNotEmpty(mailaddress)) {
				String message = mailTemplateConfig.getInfoMitteilungErhalten(mitteilung, mailaddress, sprache);
				sendMessageWithTemplate(message, mailaddress);
				LOG.debug("Email fuer InfoMitteilungErhalten wurde versendet an {}", mailaddress);
			} else {
				LOG.warn("skipping sendInfoMitteilungErhalten because Mitteilungsempfaenger is null");
			}
		}
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS,
		SACHBEARBEITER_TS })
	public void sendInfoVerfuegtGesuch(@Nonnull Gesuch gesuch) throws MailException {
		final Sprache sprache = EbeguUtil.extractKorrespondezsprache(gesuch, gemeindeService);
		sendMail(
			gesuch,
			"InfoVerfuegtGesuch",
			(gesuchsteller, adr) -> mailTemplateConfig.getInfoVerfuegtGesuch(gesuch, gesuchsteller, adr, sprache)
		);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS,
		SACHBEARBEITER_TS })
	public void sendInfoVerfuegtMutation(@Nonnull Gesuch gesuch) throws MailException {
		final Sprache sprache = EbeguUtil.extractKorrespondezsprache(gesuch, gemeindeService);
		sendMail(
			gesuch,
			"InfoVerfuegtMutation",
			(gesuchsteller, adr) -> mailTemplateConfig.getInfoVerfuegtMutation(gesuch, gesuchsteller, adr, sprache)
		);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS })
	public void sendInfoMahnung(@Nonnull Gesuch gesuch) throws MailException {
		final Sprache sprache = EbeguUtil.extractKorrespondezsprache(gesuch, gemeindeService);
		sendMail(
			gesuch,
			"InfoMahnung",
			(gesuchsteller, adr) -> mailTemplateConfig.getInfoMahnung(gesuch, gesuchsteller, adr, sprache)
		);
	}

	@Override
	@RolesAllowed(SUPER_ADMIN)
	public void sendWarnungGesuchNichtFreigegeben(@Nonnull Gesuch gesuch, int anzahlTageBisLoeschung)
		throws MailException {

		final Sprache sprache = EbeguUtil.extractKorrespondezsprache(gesuch, gemeindeService);
		sendMail(
			gesuch,
			"WarnungGesuchNichtFreigegeben",
			(gesuchsteller, adr) ->
				mailTemplateConfig.getWarnungGesuchNichtFreigegeben(gesuch, gesuchsteller, adr, anzahlTageBisLoeschung, sprache)
		);
	}

	@Override
	@RolesAllowed(SUPER_ADMIN)
	public void sendWarnungFreigabequittungFehlt(@Nonnull Gesuch gesuch, int anzahlTageBisLoeschung)
		throws MailException {

		final Sprache sprache = EbeguUtil.extractKorrespondezsprache(gesuch, gemeindeService);
		sendMail(
			gesuch,
			"WarnungFreigabequittungFehlt",
			(gesuchsteller, adr) ->
				mailTemplateConfig.getWarnungFreigabequittungFehlt(gesuch, gesuchsteller, adr, anzahlTageBisLoeschung, sprache)
		);
	}

	@Override
	@RolesAllowed(SUPER_ADMIN)
	public void sendInfoGesuchGeloescht(@Nonnull Gesuch gesuch) throws MailException {
		final Sprache sprache = EbeguUtil.extractKorrespondezsprache(gesuch, gemeindeService);
		sendMail(
			gesuch,
			"InfoGesuchGeloescht",
			(gesuchsteller, adr) -> mailTemplateConfig.getInfoGesuchGeloescht(gesuch, gesuchsteller, adr, sprache)
		);
	}

	@Override
	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE })
	public Future<Integer> sendInfoFreischaltungGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull List<Gesuch> gesucheToSendMail) {
		int i = 0;
		for (Gesuch gesuch : gesucheToSendMail) {
			try {
				if (doSendMail(gesuch.getFall())) {
					Optional<String> emailAddress = findEMailAddress(gesuch);
					Optional<Gesuchsteller> gesuchsteller = gesuch.extractGesuchsteller1();
					if (gesuchsteller.isPresent() && emailAddress.isPresent()) {
						String adr = emailAddress.get();

						final Sprache sprache = EbeguUtil.extractKorrespondezsprache(gesuch, gemeindeService);
						String message = mailTemplateConfig
							.getInfoFreischaltungGesuchsperiode(gesuchsperiode, gesuchsteller.get(), adr, gesuch, sprache);
						sendMessageWithTemplate(message, adr);

						LOG.debug("Email fuer InfoFreischaltungGesuchsperiode wurde versendet an {}", adr);
					} else {
						LOG.warn("skipping InfoFreischaltungGesuchsperiode because Gesuchsteller 1 is null");
					}
				}
				i++;
			} catch (Exception e) {
				LOG.error(
					"Mail InfoFreischaltungGesuchsperiode konnte nicht verschickt werden fuer Gesuch {}",
					gesuch.getId(),
					e);
			}
		}
		return new AsyncResult<>(i);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS })
	public void sendInfoBetreuungGeloescht(@Nonnull List<Betreuung> betreuungen) {

		for (Betreuung betreuung : betreuungen) {

			Institution institution = betreuung.getInstitutionStammdaten().getInstitution();
			String mailaddress = betreuung.getInstitutionStammdaten().getMail();
			Gesuch gesuch = betreuung.extractGesuch();
			Fall fall = gesuch.getFall();
			Gesuchsteller gesuchsteller1 = gesuch.extractGesuchsteller1()
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"sendInfoBetreuungGeloescht",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					"Gesuchsteller1"));
			Kind kind = betreuung.getKind().getKindJA();
			Betreuungsstatus status = betreuung.getBetreuungsstatus();
			LocalDate datumErstellung = requireNonNull(betreuung.getTimestampErstellt()).toLocalDate();
			LocalDate birthdayKind = kind.getGeburtsdatum();

			final Sprache sprache = EbeguUtil.extractKorrespondezsprache(gesuch, gemeindeService);
			String message = mailTemplateConfig.getInfoBetreuungGeloescht(betreuung, fall, gesuchsteller1, kind,
				institution, mailaddress, datumErstellung, birthdayKind, sprache);

			try {
				if (gesuch.getTyp().isMutation()) {
					// wenn Gesuch Mutation ist
					if (betreuung.getVorgaengerId() == null) { //this is a new Betreuung for this Antrag
						if (status.isSendToInstitution()) { //wenn status warten, abgewiesen oder bestaetigt ist
							sendMessageWithTemplate(message, mailaddress);
							LOG.info("Email fuer InfoBetreuungGeloescht wurde versendet an {}", mailaddress);
						}
					} else {
						Betreuung vorgaengerBetreuung = betreuungService.findBetreuung(betreuung.getVorgaengerId())
							.orElseThrow(() -> new EbeguEntityNotFoundException(
								"sendInfoBetreuungGeloescht",
								ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
								betreuung.getVorgaengerId()));

						// wenn Vorgaengerbetreuung vorhanden
						if ((status == Betreuungsstatus.BESTAETIGT && !betreuung.isSame(vorgaengerBetreuung))
							|| (status == Betreuungsstatus.WARTEN || status == Betreuungsstatus.ABGEWIESEN)) {
							// wenn status der aktuellen Betreuung bestaetigt ist UND wenn vorgaenger NICHT die gleiche
							// ist wie die aktuelle oder wenn status der aktuellen Betreuung warten oder abgewiesen ist
							sendMessageWithTemplate(message, mailaddress);
							LOG.info("Email fuer InfoBetreuungGeloescht wurde versendet an {}", mailaddress);
						}
					}
				} else {
					//wenn es keine Mutation ist
					if (status.isSendToInstitution()) {
						//wenn status warten, abgewiesen oder bestaetigt ist
						sendMessageWithTemplate(message, mailaddress);
						LOG.info("Email fuer InfoBetreuungGeloescht wurde versendet an {}", mailaddress);
					}

				}
			} catch (MailException e) {
				LOG.error(
					"Mail InfoBetreuungGeloescht konnte nicht verschickt werden fuer Betreuung {}",
					betreuung.getId(),
					e);
			}
		}
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS })
	public void sendInfoBetreuungVerfuegt(@Nonnull Betreuung betreuung) {

		Institution institution = betreuung.getInstitutionStammdaten().getInstitution();
		String mailaddress = betreuung.getInstitutionStammdaten().getMail();
		Gesuch gesuch = betreuung.extractGesuch();
		Fall fall = gesuch.getFall();
		Gesuchsteller gesuchsteller1 = gesuch.extractGesuchsteller1()
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"sendInfoBetreuungVerfuegt",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				"Gesuchsteller1"));
		Kind kind = betreuung.getKind().getKindJA();
		LocalDate birthdayKind = kind.getGeburtsdatum();

		final Sprache sprache = EbeguUtil.extractKorrespondezsprache(gesuch, gemeindeService);

		String message = mailTemplateConfig.getInfoBetreuungVerfuegt(betreuung, fall, gesuchsteller1, kind,
			institution, mailaddress, birthdayKind, sprache);

		try {
			sendMessageWithTemplate(message, mailaddress);
			LOG.info("Email fuer InfoBetreuungVerfuegt wurde versendet an {}", mailaddress);
		} catch (MailException e) {
			LOG.error(
				"Mail InfoBetreuungVerfuegt konnte nicht verschickt werden fuer Betreuung {}",
				betreuung.getId(),
				e);
		}
	}

	@Override
	public void sendDocumentCreatedEmail(
		@Nonnull String receiverEmail,
		@Nullable DownloadFile attachement,
		@Nonnull String downloadurl) throws MailException {
		try {
			final String subj = ServerMessageUtil.getMessage("MAIL_REPORT_SUBJECT");
			String body = ServerMessageUtil.getMessage("MAIL_REPORT_BODY");

			body = body + '\n' + downloadurl;
			if (attachement != null) {
				sendMessage(subj, body, receiverEmail, attachement);
			} else {
				sendMessage(subj, body, receiverEmail);
			}
			LOG.debug("E-Mail mit Report versendet an {}", receiverEmail);
		} catch (MailException e) {
			LOG.error("E-Mail mit Report versendet konnte nicht verschickt werden an {}", receiverEmail, e);
			throw e;
		}
	}

	@Override
	public void sendBenutzerEinladung(
		@Nonnull Benutzer einladender,
		@Nonnull Einladung einladung
	) throws MailException {
		requireNonNull(einladender);
		requireNonNull(einladung);

		String message = mailTemplateConfig.getBenutzerEinladung(einladender, einladung, LocaleThreadLocal.get());

		sendMessageWithTemplate(message, einladung.getEingeladener().getEmail());
	}

	private void sendMail(
		@Nonnull Gesuch gesuch,
		@Nonnull String logId,
		@Nonnull BiFunction<Gesuchsteller, String, String> messageProvider) throws MailException {
		if (!doSendMail(gesuch.getFall())) {
			return;
		}

		Optional<Gesuchsteller> gesuchsteller = gesuch.extractGesuchsteller1();
		Optional<String> emailAddress = findEMailAddress(gesuch);

		if (gesuchsteller.isPresent() && emailAddress.isPresent()) {
			String message = messageProvider.apply(gesuchsteller.get(), emailAddress.get());
			sendMessageWithTemplate(message, emailAddress.get());

			LOG.info("Sent Email for {} to {}", logId, emailAddress.get());

			return;
		}

		LOG.warn(
			"Not sending Email to {} because Gesuchsteller or Email Address is NULL: {}, {}",
			logId,
			gesuchsteller,
			emailAddress);
	}

	/**
	 * Hier wird an einer Stelle definiert, an welche Benutzergruppen ein Mail geschickt werden soll.
	 */
	private boolean doSendMail(@Nonnull Fall fall) {
		return fall.getBesitzer() != null;
	}

	@Nonnull
	private Optional<String> findEMailAddress(@Nonnull Gesuch gesuch) {
		return fallService.getCurrentEmailAddress(gesuch.getFall().getId())
			.filter(StringUtils::isNotEmpty);
	}
}
