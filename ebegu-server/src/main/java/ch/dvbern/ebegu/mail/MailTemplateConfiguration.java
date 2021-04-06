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
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.enums.EinladungTyp;
import ch.dvbern.ebegu.enums.GemeindeAngebotTyp;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
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

	private final Configuration freeMarkerConfiguration;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private BenutzerService benutzerService;

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
		return processTemplateBetreuung(
			MailTemplate.InfoBetreuungAbgelehnt,
			betreuung,
			gesuchsteller,
			paramsWithEmpfaenger(empfaengerMail),
			sprache);
	}

	public String getInfoBetreuungenBestaetigt(
		@Nonnull Gesuch gesuch,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache
	) {

		return processTemplateGesuch(
			MailTemplate.InfoBetreuungenBestaetigt,
			gesuch,
			gesuchsteller,
			paramsWithEmpfaenger(empfaengerMail),
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
			paramsWithEmpfaenger(empfaengerMail),
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
			paramsWithEmpfaenger(empfaengerMail),
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
			paramsWithEmpfaenger(empfaengerMail),
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

		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail);
		paramMap.put("datumErstellung", Constants.DATE_FORMATTER.format(datumErstellung));
		paramMap.put("birthday", Constants.DATE_FORMATTER.format(birthdayKind));
		paramMap.put("status", ServerMessageUtil.translateEnumValue(betreuung.getBetreuungsstatus(), sprache.getLocale()));

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

		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail);
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
		@Nonnull Sprache sprache
	) {
		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail);
		paramMap.put("downloadurl", downloadurl);
		paramMap.put("footer", ServerMessageUtil.getMessage("EinladungEmail_FOOTER", sprache.getLocale()));
		return doProcessTemplate(appendLanguageToTemplateName(MailTemplate.InfoStatistikGeneriert, sprache), paramMap);
	}

	public String getInfoMitteilungErhalten(
		@Nonnull Mitteilung mitteilung,
		@Nonnull String empfaengerMail,
		@Nonnull List<Sprache> sprachen
	) {
		return processTemplateMitteilung(mitteilung, paramsWithEmpfaenger(empfaengerMail), sprachen);
	}

	public String getInfoVerfuegtGesuch(
		@Nonnull Gesuch gesuch,
		Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache
	) {

		return processTemplateGesuch(
			MailTemplate.InfoVerfuegtGesuch,
			gesuch,
			gesuchsteller,
			paramsWithEmpfaenger(empfaengerMail),
			sprache);
	}

	public String getInfoVerfuegtMutation(
		@Nonnull Gesuch gesuch,
		Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache) {

		return processTemplateGesuch(
			MailTemplate.InfoVerfuegtMutation,
			gesuch,
			gesuchsteller,
			paramsWithEmpfaenger(empfaengerMail),
			sprache);
	}

	public String getInfoMahnung(
		@Nonnull Gesuch gesuch,
		Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache
	) {
		return processTemplateGesuch(
			MailTemplate.InfoMahnung,
			gesuch,
			gesuchsteller,
			paramsWithEmpfaenger(empfaengerMail),
			sprache);
	}

	public String getWarnungGesuchNichtFreigegeben(
		@Nonnull Gesuch gesuch,
		Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		int anzahlTage,
		@Nonnull Sprache sprache) {

		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail);
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

		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail);

		GemeindeStammdaten stammdaten = gemeindeService
			.getGemeindeStammdatenByGemeindeId(gesuch.getDossier().getGemeinde().getId()).get();

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

		return processTemplateGesuch(
			MailTemplate.InfoGesuchGeloescht,
			gesuch,
			gesuchsteller,
			paramsWithEmpfaenger(empfaengerMail),
			sprache);
	}

	public String getInfoFreischaltungGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Gesuch gesuch,
		@Nonnull Sprache sprache
	) {

		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail);
		paramMap.put(GESUCHSPERIODE, gesuchsperiode);
		paramMap.put(START_DATUM, Constants.DATE_FORMATTER.format(gesuchsperiode.getGueltigkeit().getGueltigAb()));
		paramMap.put(SENDER_FULL_NAME, getSenderFullNameForEmail(gesuch, gesuchsteller));
		paramMap.put(EMPFAENGER_MAIL, empfaengerMail);
		paramMap.put(GESUCH, gesuch);

		return doProcessTemplate(appendLanguageToTemplateName(MailTemplate.InfoFreischaltungGesuchsperiode, sprache), paramMap);
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

		Map<Object, Object> paramMap = initParamMap();
		paramMap.put("acceptExpire", Constants.DATE_FORMATTER.format(LocalDate.now().plusDays(10)));
		paramMap.put("acceptLink", benutzerService.createInvitationLink(eingeladener, einladung));
		paramMap.put("eingeladener", eingeladener);

		addContentInLanguage(einladender, einladung, eingeladener, paramMap, "contentDE", "footerDE", Locale.GERMAN);

		addContentInLanguage(einladender, einladung, eingeladener, paramMap, "contentFR", "footerFR", Locale.FRENCH);

		return doProcessTemplate(MailTemplate.BenutzerEinladung.name() + FTL_FILE_EXTENSION, paramMap);
	}

	/**
	 * InfoOffenePendenzenInstitution is sent in two languages FR and DE since we don't know the language of the institution.
	 */
	@Nonnull
	public String getInfoOffenePendenzenInstitution(
		@Nonnull InstitutionStammdaten institutionStammdaten,
		@Nonnull String empfaengerMail
	) {
		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail);
		paramMap.put(INSTITUTION_STAMMDATEN, institutionStammdaten);

		return doProcessTemplate(MailTemplate.InfoOffenePendenzenInstitution.name() + FTL_FILE_EXTENSION, paramMap);
	}

	public String getInfoGemeindeAngebotAktiviert(
		@Nonnull Gemeinde gemeinde,
		@Nonnull String empfaengerMail,
		@Nonnull GemeindeAngebotTyp angebotName,
		@Nonnull List<Sprache> sprachen
	) {
		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail);
		paramMap.put("angebotNameDe", ServerMessageUtil.translateEnumValue(angebotName, new Locale("de")));
		paramMap.put("angebotNameFr", ServerMessageUtil.translateEnumValue(angebotName, new Locale("fr")));
		paramMap.put("gemeinde", gemeinde);
		return doProcessTemplate(appendLanguageToTemplateName(MailTemplate.InfoGemeindeAngebotAktiviert, sprachen), paramMap);
	}


	public String getInfoGesuchVerfuegtVerantwortlicherTS(@Nonnull Gesuch gesuch,
		@Nonnull String mailaddressTS,
		@Nonnull List<Sprache> sprachen) {

		Map<Object, Object> paramMap = paramsWithEmpfaenger(mailaddressTS);
		paramMap.put(EMPFAENGER_MAIL, mailaddressTS);
		paramMap.put(GESUCH, gesuch);

		return doProcessTemplate(appendLanguageToTemplateName(MailTemplate.InfoGesuchVerfuegtVerantwortlicherTS, sprachen),
			paramMap);
	}

	private void addContentInLanguage(
		@Nonnull Benutzer einladender,
		@Nonnull Einladung einladung,
		@Nonnull Benutzer eingeladener,
		@Nonnull Map<Object, Object> paramMap,
		@Nonnull String contentName,
		@Nonnull String footerName,
		@Nonnull Locale french
	) {
		paramMap.put(
			contentName,
			ServerMessageUtil.getMessage(
				"EinladungEmail_" + einladung.getEinladungTyp(),
				french,
				einladender.getFullName(),
				ServerMessageUtil.translateEnumValue(eingeladener.getRole(), french),
				getRollenZusatz(einladung, eingeladener)
			)
		);
		paramMap.put(footerName, ServerMessageUtil.getMessage("EinladungEmail_FOOTER", french));
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

	private String processTemplateGesuch(
		@Nonnull MailTemplate nameOfTemplate,
		@Nonnull Gesuch gesuch,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull Map<Object, Object> paramMap,
		@Nonnull Sprache sprache
	) {

		paramMap.put(GESUCH, gesuch);
		paramMap.put(SENDER_FULL_NAME, getSenderFullNameForEmail(gesuch, gesuchsteller));
		paramMap.put(GESUCHSTELLER, gesuchsteller);
		paramMap.put("isSozialdienst", gesuch.getFall().getSozialdienstFall() != null);

		return doProcessTemplate(appendLanguageToTemplateName(nameOfTemplate, sprache), paramMap);
	}

	private String getSenderFullNameForEmail(Gesuch gesuch, Gesuchsteller gesuchsteller){
		if(gesuch.getFall().getSozialdienstFall() != null) {
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

		paramMap.put(BETREUUNG, betreuung);
		paramMap.put(SENDER_FULL_NAME, getSenderFullNameForEmail(betreuung.extractGesuch(), gesuchsteller));

		return doProcessTemplate(appendLanguageToTemplateName(nameOfTemplate, sprache), paramMap);
	}

	private String processTemplateAnmeldung(
		@Nonnull MailTemplate nameOfTemplate,
		@Nonnull AbstractAnmeldung betreuung,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull Map<Object, Object> paramMap,
		@Nonnull Sprache sprache
	) {

		paramMap.put(BETREUUNG, betreuung);
		paramMap.put(SENDER_FULL_NAME, getSenderFullNameForEmail(betreuung.extractGesuch(), gesuchsteller));

		return doProcessTemplate(appendLanguageToTemplateName(nameOfTemplate, sprache), paramMap);
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

		paramMap.put(BETREUUNG, betreuung);
		paramMap.put("fall", fall);
		paramMap.put("kind", kind);
		paramMap.put(GESUCHSTELLER, gesuchsteller1);
		paramMap.put("institution", institution);

		return doProcessTemplate(appendLanguageToTemplateName(nameOfTemplate, sprache), paramMap);
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
		paramMap.put(MITTEILUNG, mitteilung);
		return doProcessTemplate(appendLanguageToTemplateName(MailTemplate.InfoMitteilungErhalten, sprachen), paramMap);
	}

	/**
	 * Appends the language and the file extension to the given name. Result will look like "name_de.ftl"
	 */
	private String appendLanguageToTemplateName(@Nonnull final MailTemplate mailTemplate, @Nonnull Sprache sprache) {
		return appendLanguageToTemplateName(mailTemplate, sprache.getLocale());
	}

	private String appendLanguageToTemplateName(@Nonnull final MailTemplate mailTemplate, @Nonnull List<Sprache> sprachen) {
		if (sprachen.size() == 1) {
			return appendLanguageToTemplateName(mailTemplate, sprachen.get(0).getLocale());
		}
		return mailTemplate.name() + "_defr" + FTL_FILE_EXTENSION;
	}

	private String appendLanguageToTemplateName(@Nonnull final MailTemplate mailTemplate, @Nonnull Locale locale) {
		return mailTemplate.name() + '_' + locale.getLanguage().toLowerCase(locale) + FTL_FILE_EXTENSION;
	}

	private String doProcessTemplate(@Nonnull final String name, final Map<Object, Object> rootMap) {

		try {
			final Template template = freeMarkerConfiguration.getTemplate(name, Constants.DEFAULT_LOCALE);
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
	private Map<Object, Object> initParamMap() {
		Map<Object, Object> paramMap = new HashMap<>();
		paramMap.put("configuration", ebeguConfiguration);
		paramMap.put("templateConfiguration", this);
		paramMap.put("base64Header", new UTF8Base64MailHeaderDirective());

		return paramMap;
	}

	@Nonnull
	private Map<Object, Object> paramsWithEmpfaenger(@Nonnull String empfaenger) {
		Map<Object, Object> paramMap = initParamMap();
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
			paramsWithEmpfaenger(empfaengerMail),
			sprache);
	}

	public String getNotrechtGenerischeMitteilung(
		String empfaengerMail,
		String betreff,
		String inhalt
	) {

		Map<Object, Object> paramMap = initParamMap();

		paramMap.put("empfaenger", empfaengerMail);
		paramMap.put("betreff", betreff);
		paramMap.put("inhalt", inhalt);

		return doProcessTemplate(MailTemplate.NotrechtGenerischeMitteilung.name() + FTL_FILE_EXTENSION, paramMap);
	}

	@Nonnull
	public String getNotrechtBestaetigungPruefungStufe1(
		@Nonnull InstitutionStammdaten institutionStammdaten,
		@Nonnull String betragRueckerstattungNichtAngeboten,
		@Nonnull String betragKostenuebernahmeNichtAnwesend
	) {
		Map<Object, Object> paramMap = initParamMap();
		paramMap.put(INSTITUTION_STAMMDATEN, institutionStammdaten);
		paramMap.put(BETRAG1, betragRueckerstattungNichtAngeboten);
		paramMap.put(BETRAG2, betragKostenuebernahmeNichtAnwesend);

		return doProcessTemplate(MailTemplate.NotrechtBestaetigungPruefungStufe1.name() + FTL_FILE_EXTENSION, paramMap);
	}

	public String getNotrechtProvisorischeVerfuegung(
		@Nonnull RueckforderungFormular rueckforderungFormular,
		@Nonnull InstitutionStammdaten institutionStammdaten,
		@Nonnull String empfaengerMail
	) {
		Map<Object, Object> paramMap = initParamMap();
		paramMap.put("rueckforderungFormular", rueckforderungFormular);
		paramMap.put(INSTITUTION_STAMMDATEN, institutionStammdaten);
		paramMap.put("empfaenger", empfaengerMail);

		return doProcessTemplate(MailTemplate.NotrechtProvisorischeVerfuegung.name() + FTL_FILE_EXTENSION, paramMap);
	}

	public String getInfoGemeindeLastenausgleichDurch(Lastenausgleich lastenausgleich, List<Sprache> sprachen, @Nonnull String empfaengerMail) {
		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail);
		paramMap.put("jahr", lastenausgleich.getJahr().toString());
		return doProcessTemplate(appendLanguageToTemplateName(MailTemplate.InfoGemeindeLastenausgleichDurch, sprachen), paramMap);
	}
}
