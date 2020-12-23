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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.dto.SupportAnfrageDTO;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.AbstractAnmeldung;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.entities.RueckforderungMitteilung;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GemeindeAngebotTyp;
import ch.dvbern.ebegu.enums.RueckforderungStatus;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.ebegu.mail.MailTemplateConfiguration;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.EnumUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 * Service fuer Senden von E-Mails
 */
@Stateless
@Local(MailService.class)
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

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Override
	public void sendInfoBetreuungenBestaetigt(@Nonnull Gesuch gesuch) throws MailException {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);
		sendMail(
			gesuch,
			"InfoBetreuungBestaetigt",
			(gesuchsteller, adr) -> mailTemplateConfig.getInfoBetreuungenBestaetigt(gesuch, gesuchsteller, adr,
				sprache),
			AntragStatus.IN_BEARBEITUNG_GS
		);
	}

	@Override
	public void sendInfoBetreuungAbgelehnt(@Nonnull Betreuung betreuung) throws MailException {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(betreuung.extractGesuch(), gemeindeService);
		sendMail(
			betreuung.extractGesuch(),
			"InfoBetreuungAbgelehnt",
			(gesuchsteller, adr) -> mailTemplateConfig.getInfoBetreuungAbgelehnt(betreuung, gesuchsteller, adr,
				sprache),
			AntragStatus.values()
		);
	}

	@Override
	public void sendInfoSchulamtAnmeldungTagesschuleUebernommen(@Nonnull AbstractAnmeldung abstractAnmeldung)
		throws MailException {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(
			abstractAnmeldung.extractGesuch(),
			gemeindeService);
		sendMail(
			abstractAnmeldung.extractGesuch(),
			"InfoSchulamtAnmeldungTagesschuleUebernommen",
			(gesuchsteller, adr) ->
				mailTemplateConfig.getInfoSchulamtAnmeldungTagesschuleUebernommen(abstractAnmeldung, gesuchsteller,
					adr, sprache),
			AntragStatus.values()
		);
	}

	@Override
	public void sendInfoSchulamtAnmeldungAbgelehnt(@Nonnull AbstractAnmeldung abstractAnmeldung) throws MailException {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(
			abstractAnmeldung.extractGesuch(),
			gemeindeService);
		sendMail(
			abstractAnmeldung.extractGesuch(),
			"InfoSchulamtAnmeldungAbgelehnt",
			(gesuchsteller, adr) -> mailTemplateConfig.getInfoSchulamtAnmeldungAbgelehnt(
				abstractAnmeldung,
				gesuchsteller,
				adr,
				sprache),
			AntragStatus.values()
		);
	}

	@Override
	public void sendInfoSchulamtAnmeldungFerieninselUebernommen(@Nonnull AbstractAnmeldung abstractAnmeldung)
		throws MailException {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(
			abstractAnmeldung.extractGesuch(),
			gemeindeService);
		sendMail(
			abstractAnmeldung.extractGesuch(),
			"InfoSchulamtAnmeldungFerieninselUebernommen",
			(gesuchsteller, adr) ->
				mailTemplateConfig.getInfoSchulamtAnmeldungFerieninselUebernommen(abstractAnmeldung, gesuchsteller,
					adr,
					sprache),
			AntragStatus.values()
		);
	}

	@Override
	public void sendInfoMitteilungErhalten(@Nonnull Mitteilung mitteilung) throws MailException {
		List<Sprache> sprachen =
			EbeguUtil.extractGemeindeSprachen(mitteilung.getDossier().getGemeinde(), gemeindeService);
		if (doSendMail(mitteilung.getFall())) {
			String mailaddress = fallService.getCurrentEmailAddress(mitteilung.getFall().getId()).orElse(null);
			if (StringUtils.isNotEmpty(mailaddress)) {
				String message = mailTemplateConfig.getInfoMitteilungErhalten(mitteilung, mailaddress, sprachen);
				sendMessageWithTemplate(message, mailaddress);
				LOG.debug("Email fuer InfoMitteilungErhalten wurde versendet an {}", mailaddress);
			} else {
				LOG.warn("skipping sendInfoMitteilungErhalten because Mitteilungsempfaenger is null");
			}
		}
	}

	@Override
	public void sendInfoVerfuegtGesuch(@Nonnull Gesuch gesuch) throws MailException {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);
		sendMail(
			gesuch,
			"InfoVerfuegtGesuch",
			(gesuchsteller, adr) -> mailTemplateConfig.getInfoVerfuegtGesuch(gesuch, gesuchsteller, adr, sprache),
			AntragStatus.values()
		);
	}

	@Override
	public void sendInfoVerfuegtMutation(@Nonnull Gesuch gesuch) throws MailException {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);
		sendMail(
			gesuch,
			"InfoVerfuegtMutation",
			(gesuchsteller, adr) -> mailTemplateConfig.getInfoVerfuegtMutation(gesuch, gesuchsteller, adr, sprache),
			AntragStatus.values()
		);
	}

	@Override
	public void sendInfoMahnung(@Nonnull Gesuch gesuch) throws MailException {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);
		sendMail(
			gesuch,
			"InfoMahnung",
			(gesuchsteller, adr) -> mailTemplateConfig.getInfoMahnung(gesuch, gesuchsteller, adr, sprache),
			AntragStatus.values()
		);
	}

	@Override
	public void sendWarnungGesuchNichtFreigegeben(@Nonnull Gesuch gesuch, int anzahlTageBisLoeschung)
		throws MailException {

		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);
		sendMail(
			gesuch,
			"WarnungGesuchNichtFreigegeben",
			(gesuchsteller, adr) ->
				mailTemplateConfig.getWarnungGesuchNichtFreigegeben(
					gesuch,
					gesuchsteller,
					adr,
					anzahlTageBisLoeschung,
					sprache),
			AntragStatus.values()
		);
	}

	@Override
	public void sendWarnungFreigabequittungFehlt(@Nonnull Gesuch gesuch, int anzahlTageBisLoeschung)
		throws MailException {

		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);
		sendMail(
			gesuch,
			"WarnungFreigabequittungFehlt",
			(gesuchsteller, adr) ->
				mailTemplateConfig.getWarnungFreigabequittungFehlt(
					gesuch,
					gesuchsteller,
					adr,
					anzahlTageBisLoeschung,
					sprache),
			AntragStatus.values()
		);
	}

	@Override
	public void sendInfoGesuchGeloescht(@Nonnull Gesuch gesuch) throws MailException {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);
		sendMail(
			gesuch,
			"InfoGesuchGeloescht",
			(gesuchsteller, adr) -> mailTemplateConfig.getInfoGesuchGeloescht(gesuch, gesuchsteller, adr, sprache),
			AntragStatus.values()
		);
	}

	@Override
	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Future<Integer> sendInfoFreischaltungGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull List<Gesuch> gesucheToSendMail) {
		int versendetZaehler = 0;
		for (Gesuch gesuch : gesucheToSendMail) {
			if (sendInfoFreischaltungGesuchsperiode(gesuchsperiode, gesuch)) {
				versendetZaehler++;
			}
			;
		}
		return new AsyncResult<>(versendetZaehler);
	}

	@Override
	public boolean sendInfoFreischaltungGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gesuch gesuch) {
		try {
			if (doSendMail(gesuch.getFall())) {
				Optional<String> emailAddress = findEMailAddress(gesuch);
				Optional<Gesuchsteller> gesuchsteller = gesuch.extractGesuchsteller1();
				if (gesuchsteller.isPresent() && emailAddress.isPresent()) {
					String adr = emailAddress.get();

					final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);
					String message = mailTemplateConfig
						.getInfoFreischaltungGesuchsperiode(gesuchsperiode, gesuchsteller.get(), adr, gesuch, sprache);
					sendMessageWithTemplate(message, adr);

					LOG.debug("Email fuer InfoFreischaltungGesuchsperiode wurde versendet an {}", adr);
					return true;
				}

				LOG.info(
					"skipping InfoFreischaltungGesuchsperiode because Gesuchsteller 1 or email address are null: "
						+ "{} : {}",
					gesuchsteller,
					emailAddress);
				return false;
			}
		} catch (Exception e) {
			logExceptionAccordingToEnvironment(
				e,
				"Mail InfoFreischaltungGesuchsperiode konnte nicht verschickt werden fuer Gesuch",
				gesuch.getId());
		}
		return false;
	}

	@Override
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

			final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);
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
				logExceptionAccordingToEnvironment(
					e,
					"Mail InfoBetreuungGeloescht konnte nicht verschickt werden fuer Betreuung",
					betreuung.getId());
			}
		}
	}

	@Override
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

		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);

		String message = mailTemplateConfig.getInfoBetreuungVerfuegt(betreuung, fall, gesuchsteller1, kind,
			institution, mailaddress, birthdayKind, sprache);

		try {
			sendMessageWithTemplate(message, mailaddress);
			LOG.info("Email fuer InfoBetreuungVerfuegt wurde versendet an {}", mailaddress);
		} catch (MailException e) {
			logExceptionAccordingToEnvironment(
				e,
				"Mail InfoBetreuungVerfuegt konnte nicht verschickt werden fuer Betreuung",
				betreuung.getId());
		}
	}

	@Override
	public void sendInfoStatistikGeneriert(
		@Nonnull String receiverEmail,
		@Nonnull String downloadurl,
		@Nonnull Locale locale
	) {
		Sprache sprache = Sprache.DEUTSCH;
		if (Locale.FRENCH.getLanguage().equals(locale.getLanguage())) {
			sprache = Sprache.FRANZOESISCH;
		}
		String message = mailTemplateConfig.sendInfoStatistikGeneriert(receiverEmail, downloadurl, sprache);

		try {
			sendMessageWithTemplate(message, receiverEmail);
			LOG.info("Email fuer InfoStatistikGeneriert wurde versendet an {}", receiverEmail);
		} catch (MailException e) {
			logExceptionAccordingToEnvironment(
				e,
				"Mail InfoStatistikGeneriert konnte nicht verschickt werden an", receiverEmail);
		}
	}

	@Override
	public void sendBenutzerEinladung(
		@Nonnull Benutzer einladender,
		@Nonnull Einladung einladung
	) throws MailException {
		requireNonNull(einladender);
		requireNonNull(einladung);

		String message = mailTemplateConfig.getBenutzerEinladung(einladender, einladung);
		LOG.info("Benutzereinladung wird gesendet an {}", einladung.getEingeladener().getEmail());
		sendMessageWithTemplate(message, einladung.getEingeladener().getEmail());
	}

	@Override
	public void sendSupportAnfrage(@Nonnull SupportAnfrageDTO supportAnfrageDTO) {
		Benutzer benutzer = benutzerService.getCurrentBenutzer().orElseThrow(() -> new IllegalArgumentException());

		String subject = "Supportanfrage KiBon von " + benutzer.getFullName();
		StringBuilder content = new StringBuilder();

		content.append(supportAnfrageDTO.getBeschreibung()).append(Constants.LINE_BREAK);
		content.append(Constants.LINE_BREAK);

		content.append("Benutzer: ")
			.append(benutzer.getUsername())
			.append(" (")
			.append(benutzer.getFullName())
			.append(')')
			.append(Constants.LINE_BREAK);
		content.append("Email:").append(benutzer.getEmail()).append(Constants.LINE_BREAK);
		content.append("Rolle: ").append(benutzer.getRole()).append(Constants.LINE_BREAK);
		content.append(Constants.LINE_BREAK);
		content.append("Erstellt am: ")
			.append(Constants.FILENAME_DATE_TIME_FORMATTER.format(LocalDateTime.now()))
			.append(Constants.LINE_BREAK);
		content.append("Id: ").append(supportAnfrageDTO.getId()).append(Constants.LINE_BREAK);

		try {
			String supportMail = ebeguConfiguration.getSupportMail();
			sendMessage(subject, content.toString(), supportMail);
		} catch (MailException e) {
			logExceptionAccordingToEnvironment(e, "Senden der Mail nicht erfolgreich", "");
		}
	}

	@Override
	public void sendInfoOffenePendenzenInstitution(@Nonnull InstitutionStammdaten institutionStammdaten) {
		String mailaddress = institutionStammdaten.getMail();
		try {
			if (StringUtils.isNotEmpty(mailaddress)) {
				String message = mailTemplateConfig
					.getInfoOffenePendenzenInstitution(institutionStammdaten, mailaddress);
				sendMessageWithTemplate(message, mailaddress);
				LOG.debug("Email fuer InfoOffenePendenzenInstitution wurde versendet an {}", mailaddress);
			} else {
				LOG.warn("Skipping InfoOffenePendenzenInstitution because E-Mail of Institution is null");
			}
		} catch (Exception e) {
			logExceptionAccordingToEnvironment(
				e,
				"Mail InfoOffenePendenzenInstitution konnte nicht verschickt werden fuer Institution",
				institutionStammdaten.getInstitution().getName());
		}
	}

	private void sendMail(
		@Nonnull Gesuch gesuch,
		@Nonnull String mailTemplate,
		@Nonnull BiFunction<Gesuchsteller, String, String> messageProvider,
		@Nonnull AntragStatus... statusInWhichToSendMail
	) throws MailException {
		if (!doSendMail(gesuch)) {
			return;
		}
		// Gewisse Mails sollen nur in bestimmten Status gesendet werden.
		if (ArrayUtils.isNotEmpty(statusInWhichToSendMail) && EnumUtil.isNoneOf(
			gesuch.getStatus(),
			statusInWhichToSendMail)) {
			return;
		}

		Optional<Gesuchsteller> gesuchsteller = gesuch.extractGesuchsteller1();
		Optional<String> emailAddress = findEMailAddress(gesuch);

		if (gesuchsteller.isPresent() && emailAddress.isPresent()) {
			String message = messageProvider.apply(gesuchsteller.get(), emailAddress.get());
			sendMessageWithTemplate(message, emailAddress.get());

			LOG.info("Sent Email {} to {}", mailTemplate, emailAddress.get());

			return;
		}

		if (gesuch.getEingangsart().isOnlineGesuch()) {
			LOG.info(
				"Not sending Email {} because Gesuchsteller or Email Address is NULL: {}, {}",
				mailTemplate,
				gesuchsteller,
				emailAddress);
		}
	}

	/**
	 * Hier wird an einer Stelle definiert, an welche Benutzergruppen ein Mail geschickt werden soll.
	 */
	private boolean doSendMail(@Nonnull Fall fall) {
		// Mail nur schicken, wenn es der Fall einen Besitzer hat
		return fall.getBesitzer() != null;
	}

	/**
	 * Hier wird an einer Stelle definiert, an welche Benutzergruppen ein Mail geschickt werden soll.
	 */
	private boolean doSendMail(@Nonnull Gesuch gesuch) {
		// Mail nur schicken, wenn es der Fall einen Besitzer hat UND (das aktuelle Gesuch bzw. Mutation online
		// eingereicht wurde ODER die Papiermutation bereits verfügt wurde)
		return doSendMail(gesuch.getFall()) && (gesuch.getEingangsart().isOnlineGesuch() || gesuch.getStatus()
			.isAnyStatusOfVerfuegt());
	}

	@Nonnull
	private Optional<String> findEMailAddress(@Nonnull Gesuch gesuch) {
		return fallService.getCurrentEmailAddress(gesuch.getFall().getId())
			.filter(StringUtils::isNotEmpty);
	}

	@Override
	public void sendInfoSchulamtAnmeldungTagesschuleAkzeptiert(@Nonnull AbstractAnmeldung abstractAnmeldung)
		throws MailException {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(
			abstractAnmeldung.extractGesuch(),
			gemeindeService);
		sendMail(
			abstractAnmeldung.extractGesuch(),
			"InfoSchulamtAnmeldungTagesschuleAkzeptiert",
			(gesuchsteller, adr) ->
				mailTemplateConfig.getInfoSchulamtAnmeldungTagesschuleAkzeptiert(abstractAnmeldung, gesuchsteller, adr
					, sprache),
			AntragStatus.values()
		);
	}

	@Override
	public void sendInfoGemeineAngebotAktiviert(@Nonnull Gemeinde gemeinde, @Nonnull GemeindeAngebotTyp angebot) {
		List<Sprache> sprachen =
			EbeguUtil.extractGemeindeSprachen(gemeinde, gemeindeService);

		GemeindeStammdaten stammdaten =
			gemeindeService.getGemeindeStammdatenByGemeindeId(gemeinde.getId()).orElseThrow(() ->
				new EbeguEntityNotFoundException("sendInfoGemeineAngebotAktiviert",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeinde.getId()));

		String mailaddress = stammdaten.getMail();
		if (StringUtils.isNotEmpty(mailaddress)) {
			String message = mailTemplateConfig.getInfoGemeindeAngebotAktiviert(gemeinde, mailaddress,
				angebot, sprachen);
			try {
				sendMessageWithTemplate(message, mailaddress);
				LOG.debug("Email fuer InfoGemeineAngebotAktiviert wurde versendet an {}", mailaddress);
			} catch (Exception e) {
				logExceptionAccordingToEnvironment(
					e,
					"Mail InfoGemeineAngebotAktiviert konnte nicht verschickt werden fuer Gemeinde",
					gemeinde.getName());
			}
		} else {
			LOG.warn("skipping setInfoGemeineAngebotAktiviert because Mitteilungsempfaenger is null");
		}
	}

	@Override
	public void sendInfoGesuchVerfuegtVerantwortlicherTS(@Nonnull Gesuch gesuch, @Nonnull Benutzer verantwortlicherTS)
		throws MailException {
		String mailaddressTS = verantwortlicherTS.getEmail();
		List<Sprache> sprachen =
			EbeguUtil.extractGemeindeSprachen(gesuch.extractGemeinde(), gemeindeService);

		if (StringUtils.isNotEmpty(mailaddressTS)) {
			String message = mailTemplateConfig.getInfoGesuchVerfuegtVerantwortlicherTS(gesuch, mailaddressTS,
				sprachen);
			sendMessageWithTemplate(message, mailaddressTS);
			LOG.info("Email fuer InfoGesuchVerfuegtVerantwortlicherSCH wurde versendet an {}", mailaddressTS);
		} else {
			LOG.warn("skipping InfoGesuchVerfuegtVerantwortlicherSCH because verantwortlicherSCH has no mailaddress");
		}

	}

	@Override
	public void sendNotrechtGenerischeMitteilung(
		@Nonnull RueckforderungMitteilung mitteilung,
		@Nonnull String empfaengerMail,
		@Nonnull List<RueckforderungStatus> statusList
	) {
		if (StringUtils.isNotEmpty(empfaengerMail)) {
			String mail = mailTemplateConfig.getNotrechtGenerischeMitteilung(
				empfaengerMail, mitteilung.getBetreff(), mitteilung.getInhalt());
			String statusAsString = statusList.stream()
				.map(RueckforderungStatus::name)
				.collect(Collectors.joining(","));
			try {
				sendMessageWithTemplate(mail, empfaengerMail);
				LOG.debug("Email fuer NotrechtGenerischeMitteilung wurde versendet an {} für Status {}",
					empfaengerMail, statusAsString);
			} catch (Exception e) {
				logExceptionAccordingToEnvironment(
					e,
					"Mail NotrechtGenerischeMitteilung konnte nicht verschickt werden fuer Empfaenger ",
					empfaengerMail);
			}
		} else {
			LOG.warn("skipping NotrechtGenerischeMitteilung because Mitteilungsempfaenger is null");
		}
	}

	@Nullable
	@Override
	public String sendNotrechtBestaetigungPruefungStufe1(@Nonnull RueckforderungFormular rueckforderungFormular) {
		InstitutionStammdaten institutionStammdaten = rueckforderungFormular.getInstitutionStammdaten();
		String mailaddress = institutionStammdaten.getMail();
		try {
			if (StringUtils.isNotEmpty(mailaddress) && rueckforderungFormular.getStufe1FreigabeBetrag() != null) {
				BigDecimal betrag1 = null;
				if (rueckforderungFormular.getInstitutionStammdaten().getBetreuungsangebotTyp().isKita()) {
					Objects.requireNonNull(rueckforderungFormular.getStufe1KantonKostenuebernahmeAnzahlTage());
					betrag1 = rueckforderungFormular.getStufe1KantonKostenuebernahmeAnzahlTage();
				} else {
					Objects.requireNonNull(rueckforderungFormular.getStufe1KantonKostenuebernahmeAnzahlStunden());
					betrag1 = rueckforderungFormular.getStufe1KantonKostenuebernahmeAnzahlStunden();
				}
				BigDecimal betrag2 = rueckforderungFormular.getStufe1KantonKostenuebernahmeBetreuung();
				final String betrag1AsString = Constants.CURRENCY_FORMAT.format(betrag1);
				final String betrag2AsString = Constants.CURRENCY_FORMAT.format(betrag2);
				String message = mailTemplateConfig
					.getNotrechtBestaetigungPruefungStufe1(institutionStammdaten,
						betrag1AsString, betrag2AsString);
				sendMessageWithTemplate(message, mailaddress);
				LOG.debug("Email fuer NotrechtBestaetigungPruefungStufe1 wurde versendet an {}", mailaddress);
				return message;
			} else {
				LOG.warn("Skipping NotrechtBestaetigungPruefungStufe1 because E-Mail of Institution is null");
			}
		} catch (Exception e) {
			logExceptionAccordingToEnvironment(
				e,
				"Mail NotrechtBestaetigungPruefungStufe1 konnte nicht verschickt werden fuer Institution",
				institutionStammdaten.getInstitution().getName());
		}
		return null;
	}

	@Override
	public void sendInfoRueckforderungProvisorischVerfuegt(@Nonnull RueckforderungFormular rueckforderungFormular)
		throws MailException {
		InstitutionStammdaten institutionStammdaten = rueckforderungFormular.getInstitutionStammdaten();
		String mailaddress = ebeguConfiguration.getNotverordnungEmpfaengerMail();

		if (StringUtils.isNotEmpty(mailaddress)) {
			String message = mailTemplateConfig.getNotrechtProvisorischeVerfuegung(rueckforderungFormular,
				institutionStammdaten, mailaddress
			);
			sendMessageWithTemplate(message, mailaddress);
			LOG.debug("Email fuer RueckforderungProvisorischVerfuegt wurde versendet an {}", mailaddress);

		} else {
			LOG.warn("Skipping RueckforderungProvisorischVerfuegt because E-Mail of Institution is null");
		}
	}

	@Override
	public void sendInfoLastenausgleichGemeinde(@Nonnull Gemeinde gemeinde, @Nonnull Lastenausgleich lastenausgleich) {
		try {
			List<Sprache> sprachen =
				EbeguUtil.extractGemeindeSprachen(gemeinde, gemeindeService);

			GemeindeStammdaten stammdaten =
				gemeindeService.getGemeindeStammdatenByGemeindeId(gemeinde.getId()).orElseThrow(() ->
					new EbeguEntityNotFoundException("sendInfoLastenausgleichGemeinde",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeinde.getId()));

			String mailaddress = stammdaten.getMail();
			if (StringUtils.isNotEmpty(mailaddress)) {
				String message =
					mailTemplateConfig.getInfoGemeindeLastenausgleichDurch(lastenausgleich, sprachen, mailaddress);
				try {
					sendMessageWithTemplate(message, mailaddress);
					LOG.debug("Email fuer InfoGemeindeLastenausgleichDurch wurde versendet an {}", mailaddress);
				} catch (Exception e) {
					logExceptionAccordingToEnvironment(
						e,
						"Mail InfoGemeindeLastenausgleichDurch konnte nicht verschickt werden fuer Gemeinde",
						gemeinde.getName());
				}
			} else {
				LOG.warn("skipping InfoGemeindeLastenausgleichDurch because Gemeinde Email is null");
			}
		} catch (Exception e) {
			LOG.error("Error mit Gemeinde: {}, error message: {}", gemeinde.getName(), e.getMessage(), e);
		}
	}
}
