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

package ch.dvbern.ebegu.mail;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;
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
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.EinladungTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GemeindeAngebotTyp;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.ApplicationPropertyService;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MandantLocaleVisitor;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import static java.util.Objects.requireNonNull;

/**
 * Configuration For Freemarker Templates
 */
@Dependent
public class MailTemplateConfiguration {

	public static final String EMPFAENGER_MAIL = "empfaengerMail";
	public static final String ANZAHL_TAGE = "anzahlTage";
	public static final String DATUM_LOESCHUNG = "datumLoeschung";
	public static final String TS_ONLY_ANTRAG = "tsOnlyAntrag";
	public static final String GESUCHSTELLER = "gesuchsteller";
	public static final String GESUCHSPERIODE = "gesuchsperiode";
	public static final String FALL = "fall";
	public static final String START_DATUM = "startDatum";
	public static final String GESUCH = "gesuch";
	public static final String SENDER_FULL_NAME = "senderFullName";
	public static final String ADRESSE = "adresse";
	public static final String MITTEILUNG = "mitteilung";
	public static final String TEMPLATES_FOLDER = "/mail/templates";
	public static final String INSTITUTION_STAMMDATEN = "institutionStammdaten";
	public static final String BETRAG1 = "betrag1";
	public static final String BETRAG2 = "betrag2";
	public static final String BETREUUNG = "betreuung";
	public static final String FTL_FILE_EXTENSION = ".ftl";
	public static final String UNGELESENDE_MITTEILUNG = "ungelesendeMitteilung";
	public static final String OFFENE_PENDENZEN = "offenePendenzen";
	public static final String HOSTNAME = "hostname";
	public static final String GRUSS = "gruss";
	public static final String GRUSS_FR = "gruss_fr";

	private static final Locale DEUTSCH_FRENCH_LOCALE = new Locale("defr", "CH");

	private final Configuration freeMarkerConfiguration;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	public MailTemplateConfiguration() {
		this.freeMarkerConfiguration = new Configuration();
		this.freeMarkerConfiguration.setClassForTemplateLoading(MailTemplateConfiguration.class, TEMPLATES_FOLDER);
		this.freeMarkerConfiguration.setDefaultEncoding("UTF-8");
	}

	public String getInfoBetreuungAbgelehnt(
		@Nonnull Betreuung betreuung,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache
	) {
		Mandant mandant = betreuung.extractGesuch().extractMandant();
		return processTemplateBetreuung(
			MailTemplate.InfoBetreuungAbgelehnt,
			betreuung,
			gesuchsteller,
			paramsWithEmpfaenger(empfaengerMail, mandant.getMandantIdentifier()),
			sprache);
	}

	public String getInfoBetreuungenBestaetigt(
		@Nonnull Gesuch gesuch,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache
	) {
		return processTemplateGesuchWithEmpfaengerParam(
			MailTemplate.InfoBetreuungenBestaetigt,
			gesuch,
			gesuchsteller,
			empfaengerMail,
			sprache);
	}

	public String getInfoSchulamtAnmeldungTagesschuleUebernommen(
		@Nonnull AbstractAnmeldung betreuung,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache
	) {

		return processTemplateAnmeldung(
			MailTemplate.InfoSchulamtAnmeldungTagesschuleUebernommen,
			betreuung,
			gesuchsteller,
			empfaengerMail,
			sprache);
	}

	public String getInfoSchulamtAnmeldungFerieninselUebernommen(
		@Nonnull AbstractAnmeldung betreuung,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache
	) {

		return processTemplateAnmeldung(
			MailTemplate.InfoSchulamtAnmeldungFerieninselUebernommen,
			betreuung,
			gesuchsteller,
			empfaengerMail,
			sprache);
	}

	public String getInfoSchulamtAnmeldungAbgelehnt(
		@Nonnull AbstractAnmeldung betreuung,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache) {

		return processTemplateAnmeldung(
			MailTemplate.InfoSchulamtAnmeldungAbgelehnt,
			betreuung,
			gesuchsteller,
			empfaengerMail,
			sprache);
	}

	public String getInfoBetreuungGeloescht(
		@Nonnull Betreuung betreuung,
		@Nonnull Fall fall,
		@Nonnull Gesuchsteller gesuchsteller1,
		@Nonnull Kind kind,
		@Nonnull Institution institution,
		@Nonnull String empfaengerMail,
		@Nonnull LocalDate datumErstellung,
		@Nonnull LocalDate birthdayKind,
		@Nonnull Sprache sprache) {

		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail, fall.getMandant().getMandantIdentifier());
		paramMap.put("datumErstellung", Constants.DATE_FORMATTER.format(datumErstellung));
		paramMap.put("birthday", Constants.DATE_FORMATTER.format(birthdayKind));
		paramMap.put("status", ServerMessageUtil.translateEnumValue(betreuung.getBetreuungsstatus(), sprache.getLocale(),
			requireNonNull(fall.getMandant())));

		return processTemplateBetreuungGeloescht(
			betreuung,
			fall,
			kind,
			gesuchsteller1,
			institution,
			paramMap,
			sprache);
	}

	public String getInfoBetreuungVerfuegt(
		@Nonnull Betreuung betreuung,
		@Nonnull Fall fall,
		@Nonnull Gesuchsteller gesuchsteller1,
		@Nonnull Kind kind,
		@Nonnull Institution institution,
		@Nonnull String empfaengerMail,
		@Nonnull LocalDate birthdayKind,
		@Nonnull Sprache sprache) {

		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail, fall.getMandant().getMandantIdentifier());
		paramMap.put("birthday", Constants.DATE_FORMATTER.format(birthdayKind));

		return processTemplateBetreuungVerfuegt(
			betreuung,
			fall,
			kind,
			gesuchsteller1,
			institution,
			paramMap,
			sprache);
	}

	public String sendInfoStatistikGeneriert(
		@Nonnull String empfaengerMail,
		@Nonnull String downloadurl,
		@Nonnull Sprache sprache,
		@Nonnull Mandant mandant
	) {
		Locale mandantLocale = new MandantLocaleVisitor(sprache.getLocale()).process(mandant);
		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail, mandant.getMandantIdentifier());
		paramMap.put("downloadurl", downloadurl);
		paramMap.put("footer", ServerMessageUtil.getMessage("EinladungEmail_FOOTER", sprache.getLocale(), mandant));
		return doProcessTemplate(getTemplateFileName(MailTemplate.InfoStatistikGeneriert), mandantLocale, paramMap);
	}

	public String getInfoMitteilungErhalten(
		@Nonnull Mitteilung mitteilung,
		@Nonnull String empfaengerMail,
		@Nonnull List<Sprache> sprachen
	) {
		Mandant mandant = mitteilung.getFall().getMandant();
		return processTemplateMitteilung(
			mitteilung,
			paramsWithEmpfaenger(empfaengerMail, mandant.getMandantIdentifier()),
			sprachen);
	}

	private Locale getLocaleFromSprachen(List<Sprache> sprachen) {
		if (sprachen.contains(Sprache.DEUTSCH) && sprachen.contains(Sprache.FRANZOESISCH)) {
			return DEUTSCH_FRENCH_LOCALE;
		}
		if (sprachen.contains(Sprache.FRANZOESISCH)) {
			return Constants.FRENCH_LOCALE;
		}
		return Constants.DEFAULT_LOCALE;
	}

	public String getInfoVerfuegtGesuch(
		@Nonnull Gesuch gesuch,
		Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache
	) {

		return processTemplateGesuchWithEmpfaengerParam(
			MailTemplate.InfoVerfuegtGesuch,
			gesuch,
			gesuchsteller,
			empfaengerMail,
			sprache);
	}

	public String getInfoVerfuegtMutation(
		@Nonnull Gesuch gesuch,
		Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache) {

		return processTemplateGesuchWithEmpfaengerParam(
			MailTemplate.InfoVerfuegtMutation,
			gesuch,
			gesuchsteller,
			empfaengerMail,
			sprache);
	}

	public String getInfoMahnung(
		@Nonnull Gesuch gesuch,
		Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache
	) {
		return processTemplateGesuchWithEmpfaengerParam(
			MailTemplate.InfoMahnung,
			gesuch,
			gesuchsteller,
			empfaengerMail,
			sprache);
	}

	public String getWarnungGesuchNichtFreigegeben(
		@Nonnull Gesuch gesuch,
		Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		int anzahlTage,
		@Nonnull Sprache sprache) {

		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail, gesuch.extractMandant().getMandantIdentifier());
		paramMap.put(ANZAHL_TAGE, anzahlTage);
		paramMap.put(TS_ONLY_ANTRAG, gesuch.hasOnlyBetreuungenOfSchulamt());

		return processTemplateGesuch(
			MailTemplate.WarnungGesuchNichtFreigegeben,
			gesuch,
			gesuchsteller,
			paramMap,
			sprache);
	}

	public String getWarnungFreigabequittungFehlt(
		@Nonnull Gesuch gesuch,
		Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		int anzahlTage,
		@Nonnull Sprache sprache) {

		LocalDate datumLoeschung = LocalDate.now().plusDays(anzahlTage).minusDays(1);

		final Mandant mandant = gesuch.extractMandant();
		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail, mandant.getMandantIdentifier());

		GemeindeStammdaten stammdaten = gemeindeService
			.getGemeindeStammdatenByGemeindeId(gesuch.getDossier().getGemeinde().getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"getWarnungFreigabequittungFehlt",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuch.getDossier().getGemeinde().getId()));

		paramMap.put(ADRESSE, stammdaten.getAdresseForGesuch(gesuch).getAddressAsStringInOneLine());
		paramMap.put(ANZAHL_TAGE, anzahlTage);
		paramMap.put(DATUM_LOESCHUNG, Constants.DATE_FORMATTER.format(datumLoeschung));
		paramMap.put(TS_ONLY_ANTRAG, gesuch.hasOnlyBetreuungenOfSchulamt());

		return processTemplateGesuch(
			MailTemplate.WarnungFreigabequittungFehlt,
			gesuch,
			gesuchsteller,
			paramMap,
			sprache);
	}

	public String getInfoGesuchGeloescht(
		@Nonnull Gesuch gesuch,
		Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache) {

		return processTemplateGesuchWithEmpfaengerParam(
			MailTemplate.InfoGesuchGeloescht,
			gesuch,
			gesuchsteller,
			empfaengerMail,
			sprache);
	}

	public String getInfoFreischaltungGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Gesuch gesuch,
		@Nonnull Sprache sprache
	) {
		final Mandant mandant = gesuch.extractMandant();
		Locale mandantLocale = new MandantLocaleVisitor(sprache.getLocale()).process(mandant);

		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail, mandant.getMandantIdentifier());
		paramMap.put(GESUCHSPERIODE, gesuchsperiode);
		paramMap.put(START_DATUM, Constants.DATE_FORMATTER.format(gesuchsperiode.getGueltigkeit().getGueltigAb()));
		paramMap.put(SENDER_FULL_NAME, getSenderFullNameForEmail(gesuch, gesuchsteller));
		paramMap.put(EMPFAENGER_MAIL, empfaengerMail);
		paramMap.put(GRUSS, getEmailGruss(mandant, mandantLocale, gesuch.extractGemeinde().getName()));
		paramMap.put(GESUCH, gesuch);

		return doProcessTemplate(getTemplateFileName(MailTemplate.InfoFreischaltungGesuchsperiode), mandantLocale, paramMap);
	}

	/**
	 * Benutzereinladung is sent in two languages FR and DE since we don't know which the right language is.
	 */
	@Nonnull
	public String getBenutzerEinladung(
		@Nonnull Benutzer einladender,
		@Nonnull Einladung einladung
	) {

		Benutzer eingeladener = einladung.getEingeladener();
		Mandant mandant = eingeladener.getMandant();

		Map<Object, Object> paramMap = initParamMap(mandant.getMandantIdentifier());
		paramMap.put("acceptExpire", Constants.DATE_FORMATTER.format(LocalDate.now().plusDays(10)));
		paramMap.put("acceptLink", benutzerService.createInvitationLink(eingeladener, einladung));
		paramMap.put("eingeladener", eingeladener);

		final boolean isFrenchEnabled = Boolean.TRUE.equals(this.applicationPropertyService.findApplicationPropertyAsBoolean(
			ApplicationPropertyKey.FRENCH_ENABLED,
			eingeladener.getMandant()));
		Locale locale = isFrenchEnabled ? new MandantLocaleVisitor(DEUTSCH_FRENCH_LOCALE).process(mandant) :
			new MandantLocaleVisitor(Constants.DEUTSCH_LOCALE).process(mandant);

		addRoleContentInLanguage(einladender, einladung, eingeladener, paramMap, "contentDE", "footerDE", Constants.DEUTSCH_LOCALE);
		if (isFrenchEnabled) {
			addRoleContentInLanguage(einladender, einladung, eingeladener, paramMap, "contentFR", "footerFR", Constants.FRENCH_LOCALE);
		}
		return doProcessTemplate(getTemplateFileName(MailTemplate.BenutzerEinladung), locale, paramMap);
	}

	/**
	 * InfoOffenePendenzenInstitution is sent in two languages FR and DE since we don't know the language of the institution.
	 */
	@Nonnull
	public String getInfoOffenePendenzenNeuMitteilungInstitution(
		@Nonnull InstitutionStammdaten institutionStammdaten,
		@Nonnull String empfaengerMail,
		boolean offenePendenzen,
		boolean ungelesendeMitteilung
	) {
		Mandant mandant = institutionStammdaten.getInstitution().getMandant();
		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail, mandant.getMandantIdentifier());
		Locale locale = getMandantLocale(institutionStammdaten.getInstitution()
			.getMandant());
		paramMap.put(INSTITUTION_STAMMDATEN, institutionStammdaten);
		paramMap.put(UNGELESENDE_MITTEILUNG, ungelesendeMitteilung);
		paramMap.put(OFFENE_PENDENZEN, offenePendenzen);

		return doProcessTemplate(getTemplateFileName(MailTemplate.InfoOffenePendenzenNeueMitteilungInstitution), locale, paramMap);
	}

	public String getInfoGemeindeAngebotAktiviert(
		@Nonnull Gemeinde gemeinde,
		@Nonnull String empfaengerMail,
		@Nonnull GemeindeAngebotTyp angebotName,
		@Nonnull List<Sprache> sprachen
	) {
		Mandant mandant = gemeinde.getMandant();
		Locale mandantLocale = new MandantLocaleVisitor(getLocaleFromSprachen(sprachen)).process(mandant);

		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail, mandant.getMandantIdentifier());
		paramMap.put("angebotNameDe", ServerMessageUtil.translateEnumValue(angebotName, new Locale("de"), mandant));
		paramMap.put("angebotNameFr", ServerMessageUtil.translateEnumValue(angebotName, new Locale("fr"), mandant));
		paramMap.put("gemeinde", gemeinde);
		return doProcessTemplate(getTemplateFileName(MailTemplate.InfoGemeindeAngebotAktiviert), mandantLocale, paramMap);
	}

	public String getInfoGesuchVerfuegtVerantwortlicherTS(
		@Nonnull Gesuch gesuch,
		@Nonnull String mailaddressTS,
		@Nonnull List<Sprache> sprachen) {

		Locale mandantLocale = new MandantLocaleVisitor(getLocaleFromSprachen(sprachen)).process(gesuch.extractMandant());
		Map<Object, Object> paramMap = paramsWithEmpfaenger(mailaddressTS, gesuch.extractMandant().getMandantIdentifier());
		paramMap.put(EMPFAENGER_MAIL, mailaddressTS);
		paramMap.put(GESUCH, gesuch);
		paramMap.put(FALL, gesuch.getDossier().getFall());
		paramMap.put(GESUCHSPERIODE, gesuch.getGesuchsperiode());

		return doProcessTemplate(getTemplateFileName(MailTemplate.InfoGesuchVerfuegtVerantwortlicherTS), mandantLocale,
			paramMap);
	}

	private void addRoleContentInLanguage(
		@Nonnull Benutzer einladender,
		@Nonnull Einladung einladung,
		@Nonnull Benutzer eingeladener,
		@Nonnull Map<Object, Object> paramMap,
		@Nonnull String contentName,
		@Nonnull String footerName,
		@Nonnull Locale locale
	) {

		String content = getAbsatzForInstitution(einladung, einladender, locale);
		content += ServerMessageUtil.getMessage(
			"EinladungEmail_" + einladung.getEinladungTyp(),
			locale,
			einladender.getMandant(),
			einladender.getFullName(),
			ServerMessageUtil.translateEnumValue(eingeladener.getRole(), locale, eingeladener.getMandant()),
			getRollenZusatz(einladung, eingeladener)
		);

		paramMap.put(contentName, content);
		paramMap.put(
			footerName,
			ServerMessageUtil.getMessage(
				getFooterKeyForEinladungTyp(einladung.getEinladungTyp()),
				locale,
				einladender.getMandant()));
	}

	private String getFooterKeyForEinladungTyp(EinladungTyp einladungTyp) {
		if (einladungTyp == EinladungTyp.INSTITUTION) {
			return "EinladungEmail_FOOTER_INSTITUTION";
		}

		if (einladungTyp == EinladungTyp.TRAEGERSCHAFT) {
			return "EinladungEmail_FOOTER_TRAEGERSCHAFT";
		}

		return "EinladungEmail_FOOTER";
	}

	private String getAbsatzForInstitution(Einladung einladung, Benutzer einladender, Locale locale) {
		if (einladung.getEinladungTyp() != EinladungTyp.INSTITUTION) {
			return "";
		}

		String institutionsName = einladung.getEinladungObjectName().orElse("");
		String institutionBGStartdatum = einladung.getOptionalStartDatumForInstitution()
			.map(Constants.DATE_FORMATTER::format)
			.orElse("");

		String result = ServerMessageUtil.getMessage(
			"EinladungEmail_INSTITUTION_BG_ZUGELASSEN",
			locale,
			einladender.getMandant(),
			institutionsName,
			institutionBGStartdatum);

		result += einladung.getOptionalTraegerschaftNameForInstitution()
			.map(name -> ServerMessageUtil.getMessage(
				"EinladungEmail_INSTITUTION_TRAEGERSCHAFT",
				locale,
				einladender.getMandant(),
				name))
			.orElse("");

		return result;
	}

	@Nonnull
	private String getRollenZusatz(@Nonnull Einladung einladung, @Nullable Benutzer eingeladener) {
		if (einladung.getEinladungTyp() == EinladungTyp.MITARBEITER) {
			requireNonNull(eingeladener, "For an Einladung of the type Mitarbeiter a user must be set");
			String abhaengigkeitAsString = eingeladener.extractRollenAbhaengigkeitAsString();

			return abhaengigkeitAsString.isEmpty() ? "" : '(' + abhaengigkeitAsString + ')';
		}

		return einladung.getEinladungObjectName()
			.orElse("");
	}

	private String processTemplateGesuchWithEmpfaengerParam(
		@Nonnull MailTemplate nameOfTemplate,
		@Nonnull Gesuch gesuch,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache
	) {
		final Mandant mandant = gesuch.extractMandant();
		Locale mandantLocale = new MandantLocaleVisitor(sprache.getLocale()).process(mandant);
		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail, mandant.getMandantIdentifier());
		paramMap.put(GRUSS, getEmailGruss(mandant, mandantLocale, gesuch.extractGemeinde().getName()));
		return processTemplateGesuch(nameOfTemplate, gesuch, gesuchsteller, paramMap, sprache);
	}

	private String processTemplateGesuch(
		@Nonnull MailTemplate nameOfTemplate,
		@Nonnull Gesuch gesuch,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull Map<Object, Object> paramMap,
		@Nonnull Sprache sprache
	) {
		final Mandant mandant = gesuch.extractMandant();
		Locale mandantLocale = new MandantLocaleVisitor(sprache.getLocale()).process(mandant);

		paramMap.put(GESUCH, gesuch);
		paramMap.put(SENDER_FULL_NAME, getSenderFullNameForEmail(gesuch, gesuchsteller));
		paramMap.put(GESUCHSTELLER, gesuchsteller);
		paramMap.put(GRUSS, getEmailGruss(mandant, mandantLocale, gesuch.extractGemeinde().getName()));
		paramMap.put("isSozialdienst", gesuch.getFall().getSozialdienstFall() != null);
		paramMap.put(FALL, gesuch.getDossier().getFall());
		paramMap.put(GESUCHSPERIODE, gesuch.getGesuchsperiode());

		return doProcessTemplate(getTemplateFileName(nameOfTemplate), mandantLocale, paramMap);
	}

	private String getSenderFullNameForEmail(Gesuch gesuch, Gesuchsteller gesuchsteller) {
		if (gesuch.getFall().getSozialdienstFall() != null) {
			return gesuch.getFall().getSozialdienstFall().getSozialdienst().getName();
		}
		return gesuchsteller.getFullName();
	}

	private String processTemplateBetreuung(
		@Nonnull MailTemplate nameOfTemplate,
		@Nonnull Betreuung betreuung,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull Map<Object, Object> paramMap,
		@Nonnull Sprache sprache
	) {

		final Mandant mandant = betreuung.extractGesuch().extractMandant();
		Locale mandantLocale = new MandantLocaleVisitor(sprache.getLocale())
			.process(mandant);

		paramMap.put(BETREUUNG, betreuung);
		paramMap.put(GRUSS, getEmailGruss(mandant, mandantLocale, betreuung.extractGemeinde().getName()));
		paramMap.put(SENDER_FULL_NAME, getSenderFullNameForEmail(betreuung.extractGesuch(), gesuchsteller));
		paramMap.put(FALL, betreuung.extractGesuch().getDossier().getFall());
		paramMap.put(GESUCHSPERIODE, betreuung.extractGesuch().getGesuchsperiode());

		return doProcessTemplate(getTemplateFileName(nameOfTemplate), mandantLocale, paramMap);
	}

	private String processTemplateAnmeldung(
		@Nonnull MailTemplate nameOfTemplate,
		@Nonnull AbstractAnmeldung betreuung,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache
	) {
		Mandant mandant = betreuung.extractGesuch().extractMandant();
		Locale mandantLocale = new MandantLocaleVisitor(sprache.getLocale()).process(mandant);
		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail, mandant.getMandantIdentifier());
		paramMap.put(BETREUUNG, betreuung);
		paramMap.put(SENDER_FULL_NAME, getSenderFullNameForEmail(betreuung.extractGesuch(), gesuchsteller));
		paramMap.put(
			GRUSS,
			getEmailGruss(mandant, mandantLocale, betreuung.extractGesuch().getDossier().getGemeinde().getName()));
		paramMap.put(FALL, betreuung.extractGesuch().getDossier().getFall());
		paramMap.put(GESUCHSPERIODE, betreuung.extractGesuch().getGesuchsperiode());

		return doProcessTemplate(getTemplateFileName(nameOfTemplate), mandantLocale, paramMap);
	}

	private static String getEmailGruss(Mandant mandant, Locale mandantLocale, String gemeindeName) {
		return ServerMessageUtil.getMessage("Email_GEMEINDE_GRUSS", mandantLocale, mandant, gemeindeName);
	}

	private String processTemplateBetreuungGeloescht(
		Betreuung betreuung,
		Fall fall,
		Kind kind,
		Gesuchsteller gesuchsteller1,
		Institution institution,
		@Nonnull Map<Object, Object> paramMap,
		@Nonnull Sprache sprache
	) {

		return processTemplateBetreuungStatus(
			MailTemplate.InfoBetreuungGeloescht,
			betreuung,
			fall,
			kind,
			gesuchsteller1,
			institution,
			paramMap,
			sprache);
	}

	private String processTemplateBetreuungStatus(
		MailTemplate nameOfTemplate,
		Betreuung betreuung,
		Fall fall,
		Kind kind,
		Gesuchsteller gesuchsteller1,
		Institution institution,
		@Nonnull Map<Object, Object> paramMap,
		@Nonnull Sprache sprache
	) {
		final Mandant mandant = betreuung.extractGesuch().extractMandant();
		Locale mandantLocale = new MandantLocaleVisitor(sprache.getLocale())
			.process(mandant);

		paramMap.put(BETREUUNG, betreuung);
		paramMap.put(GRUSS, getEmailGruss(mandant, mandantLocale, betreuung.extractGemeinde().getName()));
		paramMap.put(FALL, fall);
		paramMap.put("kind", kind);
		paramMap.put(GESUCHSTELLER, gesuchsteller1);
		paramMap.put("institution", institution);
		paramMap.put(GESUCHSPERIODE, betreuung.extractGesuch().getGesuchsperiode());

		return doProcessTemplate(getTemplateFileName(nameOfTemplate), mandantLocale, paramMap);
	}

	private String processTemplateBetreuungVerfuegt(
		Betreuung betreuung,
		Fall fall,
		Kind kind,
		Gesuchsteller gesuchsteller1,
		Institution institution,
		@Nonnull Map<Object, Object> paramMap,
		@Nonnull Sprache sprache
	) {

		return processTemplateBetreuungStatus(
			MailTemplate.InfoBetreuungVerfuegt,
			betreuung,
			fall,
			kind,
			gesuchsteller1,
			institution,
			paramMap,
			sprache);
	}

	private String processTemplateMitteilung(
		@Nonnull Mitteilung mitteilung,
		@Nonnull Map<Object, Object> paramMap,
		@Nonnull List<Sprache> sprachen
	) {
		final Mandant mandant = mitteilung.getFall().getMandant();
		Locale locale = new MandantLocaleVisitor(getLocaleFromSprachen(sprachen)).process(mandant);
		paramMap.put(MITTEILUNG, mitteilung);
		paramMap.put(GRUSS, getEmailGruss(mandant, locale, mitteilung.getDossier().getGemeinde().getName()));
		addBilingualFrenchGrussToParamMap(mitteilung, paramMap, sprachen, mandant);
		return doProcessTemplate(getTemplateFileName(MailTemplate.InfoMitteilungErhalten), locale, paramMap);
	}

	private void addBilingualFrenchGrussToParamMap(
		@Nonnull Mitteilung mitteilung,
		@Nonnull Map<Object, Object> paramMap,
		@Nonnull List<Sprache> sprachen,
		Mandant mandant) {
		if (sprachen.contains(Sprache.FRANZOESISCH)) {
			Locale localeFr = new MandantLocaleVisitor(getLocaleFromSprachen(List.of(Sprache.FRANZOESISCH))).process(
				mandant);
			paramMap.put(GRUSS_FR, getEmailGruss(mandant, localeFr, mitteilung.getDossier().getGemeinde().getName()));
		}
	}

	private String getTemplateFileName(@Nonnull final MailTemplate mailTemplate) {
		return mailTemplate.name() + FTL_FILE_EXTENSION;
	}

	private String doProcessTemplate(@Nonnull final String name, @Nonnull Locale locale, final Map<Object, Object> rootMap) {

		try {
			final Template template = freeMarkerConfiguration.getTemplate(name, locale);
			final StringWriter out = new StringWriter(50);
			template.process(rootMap, out);

			return out.toString();
		} catch (final IOException e) {
			throw new EbeguRuntimeException(
				"doProcessTemplate()",
				String.format("Failed to load template %s.", name),
				e);
		} catch (final TemplateException e) {
			throw new EbeguRuntimeException(
				"doProcessTemplate()",
				String.format("Failed to process template %s.", name),
				e);
		}
	}

	public String getMailCss() {
		return "<style type=\"text/css\">\n" +
			"        body {\n" +
			"            font-family: \"Open Sans\", Arial, Helvetica, sans-serif;\n" +
			"        }\n" +
			"      .kursInfoHeader {background-color: #bce1ff; font-weight: bold;}\n" +
			"    </style>\n" +
			"    <link href=\"https://fonts.googleapis.com/css?family=Open+Sans\" rel=\"stylesheet\">";
	}

	@Nonnull
	private Map<Object, Object> initParamMap(MandantIdentifier mandantIdentifier) {
		Map<Object, Object> paramMap = initParamMapWithoutHostname();
		paramMap.put(HOSTNAME, this.ebeguConfiguration.getHostname(mandantIdentifier));
		return paramMap;
	}

	@Nonnull
	private Map<Object, Object> initParamMapWithoutHostname() {
		Map<Object, Object> paramMap = new HashMap<>();
		paramMap.put("configuration", ebeguConfiguration);
		paramMap.put("templateConfiguration", this);
		paramMap.put("base64Header", new UTF8Base64MailHeaderDirective());
		return paramMap;
	}

	@Nonnull
	private Map<Object, Object> paramsWithEmpfaenger(@Nonnull String empfaenger, MandantIdentifier mandantIdentifier) {
		Map<Object, Object> paramMap = initParamMap(mandantIdentifier);
		paramMap.put(EMPFAENGER_MAIL, empfaenger);

		return paramMap;
	}

	public String getInfoSchulamtAnmeldungTagesschuleAkzeptiert(
		@Nonnull AbstractAnmeldung betreuung,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache
	) {

		return processTemplateAnmeldung(
			MailTemplate.InfoSchulamtAnmeldungTagesschuleAkzeptiert,
			betreuung,
			gesuchsteller,
			empfaengerMail,
			sprache);
	}

	public String getNotrechtGenerischeMitteilung(
		String empfaengerMail,
		String betreff,
		String inhalt,
		Mandant mandant
	) {
		Map<Object, Object> paramMap = initParamMap(mandant.getMandantIdentifier());
		Locale locale = getMandantLocale(mandant);

		paramMap.put("empfaenger", empfaengerMail);
		paramMap.put("betreff", betreff);
		paramMap.put("inhalt", inhalt);

		return doProcessTemplate(getTemplateFileName(MailTemplate.NotrechtGenerischeMitteilung), locale, paramMap);
	}

	@Nonnull
	public String getNotrechtBestaetigungPruefungStufe1(
		@Nonnull InstitutionStammdaten institutionStammdaten,
		@Nonnull String betragRueckerstattungNichtAngeboten,
		@Nonnull String betragKostenuebernahmeNichtAnwesend
	) {
		Mandant mandant = institutionStammdaten.getInstitution().getMandant();
		Map<Object, Object> paramMap = initParamMap(mandant.getMandantIdentifier());
		Locale locale = getMandantLocale(institutionStammdaten.getInstitution().getMandant());

		paramMap.put(INSTITUTION_STAMMDATEN, institutionStammdaten);
		paramMap.put(BETRAG1, betragRueckerstattungNichtAngeboten);
		paramMap.put(BETRAG2, betragKostenuebernahmeNichtAnwesend);

		return doProcessTemplate(getTemplateFileName(MailTemplate.NotrechtBestaetigungPruefungStufe1), locale, paramMap);
	}

	public String getNotrechtProvisorischeVerfuegung(
		@Nonnull RueckforderungFormular rueckforderungFormular,
		@Nonnull InstitutionStammdaten institutionStammdaten,
		@Nonnull String empfaengerMail
	) {
		Mandant mandant = institutionStammdaten.getInstitution().getMandant();
		Locale locale = getMandantLocale(institutionStammdaten.getInstitution().getMandant());
		Map<Object, Object> paramMap = initParamMap(mandant.getMandantIdentifier());
		paramMap.put("rueckforderungFormular", rueckforderungFormular);
		paramMap.put(INSTITUTION_STAMMDATEN, institutionStammdaten);
		paramMap.put("empfaenger", empfaengerMail);

		return doProcessTemplate(getTemplateFileName(MailTemplate.NotrechtProvisorischeVerfuegung), locale, paramMap);
	}

	public String getInfoGemeindeLastenausgleichDurch(
		Lastenausgleich lastenausgleich,
		List<Sprache> sprachen,
		@Nonnull String empfaengerMail) {
		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail, lastenausgleich.getMandant().getMandantIdentifier());
		paramMap.put("jahr", lastenausgleich.getJahr().toString());
		return doProcessTemplate(
			getTemplateFileName(MailTemplate.InfoGemeindeLastenausgleichDurch),
			getLocaleFromSprachen(sprachen),
			paramMap);
	}

	public String getInfoSchulamtAnmeldungStorniert(
		@Nonnull AbstractAnmeldung betreuung,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache) {

		return processTemplateAnmeldung(
			MailTemplate.InfoSchulamtAnmeldungStorniert,
			betreuung,
			gesuchsteller,
			empfaengerMail,
			sprache);
	}

	public String getInfoGemeindeLastenausgleichTagesschuleZurueckAnGemeinde(
		LastenausgleichTagesschuleAngabenGemeindeContainer container,
		List<Sprache> sprachen,
		@Nonnull String empfaengerMail) {
		Mandant mandant = container.getGemeinde().getMandant();
		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail, mandant.getMandantIdentifier());
		paramMap.put("id", container.getId());
		return doProcessTemplate(
			getTemplateFileName(MailTemplate.InfoGemeindeLastenausgleichZurueckAnGemeinde),
			getLocaleFromSprachen(sprachen),
			paramMap);
	}

	public String getInitGSZPVNr(
		String url,
		List<Sprache> sprachen,
		@Nonnull String empfaengerMail,
		String trunctatedHostname) {
		Map<Object, Object> paramMap = initParamMapWithoutHostname();
		paramMap.put(EMPFAENGER_MAIL, empfaengerMail);
		paramMap.put("link", url);
		paramMap.put(HOSTNAME, trunctatedHostname);
		return doProcessTemplate(
			getTemplateFileName(MailTemplate.GesuchstellerInitZPV),
			getLocaleFromSprachen(sprachen),
			paramMap);
	}

	private Locale getMandantLocale(Mandant mandant) {
		final boolean frenchEnabled = Boolean.TRUE.equals(applicationPropertyService.findApplicationPropertyAsBoolean(
			ApplicationPropertyKey.FRENCH_ENABLED,
			mandant));
		return frenchEnabled ? DEUTSCH_FRENCH_LOCALE : Constants.DEFAULT_LOCALE;
	}
}
