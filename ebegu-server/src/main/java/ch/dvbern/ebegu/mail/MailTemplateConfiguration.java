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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.util.Constants;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Configuration For Freemarker Templates
 */
@Dependent
public class MailTemplateConfiguration {

	private static final Locale DEFAULT_LOCALE = new Locale("de", "CH");
	public static final String EMPFAENGER_MAIL = "empfaengerMail";
	public static final String ANZAHL_TAGE = "anzahlTage";
	public static final String DATUM_LOESCHUNG = "datumLoeschung";
	public static final String GESUCHSTELLER = "gesuchsteller";
	public static final String GESUCHSPERIODE = "gesuchsperiode";
	public static final String START_DATUM = "startDatum";

	private final Configuration freeMarkerConfiguration;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	public MailTemplateConfiguration() {
		final Configuration ourFreeMarkerConfig = new Configuration();
		ourFreeMarkerConfig.setClassForTemplateLoading(MailTemplateConfiguration.class, "/mail/templates");
		ourFreeMarkerConfig.setDefaultEncoding("UTF-8");
		this.freeMarkerConfiguration = ourFreeMarkerConfig;
	}

	public String getInfoBetreuungAbgelehnt(@Nonnull Betreuung betreuung, @Nonnull Gesuchsteller gesuchsteller, @Nonnull String empfaengerMail) {
		return processTemplateBetreuung("InfoBetreuungAbgelehnt.ftl", betreuung, gesuchsteller, toArgumentPair(EMPFAENGER_MAIL, empfaengerMail));
	}

	public String getInfoBetreuungenBestaetigt(@Nonnull Gesuch gesuch, @Nonnull Gesuchsteller gesuchsteller, @Nonnull String empfaengerMail) {
		return processTemplateGesuch("InfoBetreuungenBestaetigt.ftl", gesuch, gesuchsteller, toArgumentPair(EMPFAENGER_MAIL, empfaengerMail));
	}

	public String getInfoSchulamtAnmeldungUebernommen(@Nonnull Betreuung betreuung, @Nonnull Gesuchsteller gesuchsteller, @Nonnull String empfaengerMail) {
		return processTemplateBetreuung("InfoSchulamtAnmeldungUebernommen.ftl", betreuung, gesuchsteller, toArgumentPair(EMPFAENGER_MAIL, empfaengerMail));
	}

	public String getInfoSchulamtAnmeldungAbgelehnt(@Nonnull Betreuung betreuung, @Nonnull Gesuchsteller gesuchsteller, @Nonnull String empfaengerMail) {
		return processTemplateBetreuung("InfoSchulamtAnmeldungAbgelehnt.ftl", betreuung, gesuchsteller, toArgumentPair(EMPFAENGER_MAIL, empfaengerMail));
	}

	public String getInfoBetreuungGeloescht(@Nonnull Betreuung betreuung, @Nonnull Fall fall, @Nonnull Gesuchsteller gesuchsteller1, @Nonnull Kind kind,
		@Nonnull Institution institution, @Nonnull String empfaengerMail, @Nonnull LocalDate datumErstellung, @Nonnull LocalDate birthdayKind) {

		return processTemplateBetreuungGeloescht("InfoBetreuungGeloescht.ftl", betreuung, fall, kind, gesuchsteller1, institution,
			toArgumentPair(EMPFAENGER_MAIL, empfaengerMail),
			toArgumentPair("datumErstellung", Constants.DATE_FORMATTER.format(datumErstellung)),
			toArgumentPair("birthday", Constants.DATE_FORMATTER.format(birthdayKind)));
	}

	public String getInfoBetreuungVerfuegt(@Nonnull Betreuung betreuung, @Nonnull Fall fall, @Nonnull Gesuchsteller gesuchsteller1, @Nonnull Kind kind,
		@Nonnull Institution institution, @Nonnull String empfaengerMail, @Nonnull LocalDate birthdayKind) {

		return processTemplateBetreuungVerfuegt("InfoBetreuungVerfuegt.ftl", betreuung, fall, kind, gesuchsteller1, institution,
			toArgumentPair(EMPFAENGER_MAIL, empfaengerMail),
			toArgumentPair("birthday", Constants.DATE_FORMATTER.format(birthdayKind)));
	}

	public String getInfoMitteilungErhalten(@Nonnull Mitteilung mitteilung, @Nonnull String empfaengerMail) {
		return processTemplateMitteilung("InfoMitteilungErhalten.ftl", mitteilung, toArgumentPair(EMPFAENGER_MAIL, empfaengerMail));
	}

	public String getInfoVerfuegtGesuch(@Nonnull Gesuch gesuch, Gesuchsteller gesuchsteller, @Nonnull String empfaengerMail) {
		return processTemplateGesuch("InfoVerfuegtGesuch.ftl", gesuch, gesuchsteller, toArgumentPair(EMPFAENGER_MAIL, empfaengerMail));
	}

	public String getInfoVerfuegtMutation(@Nonnull Gesuch gesuch, Gesuchsteller gesuchsteller, @Nonnull String empfaengerMail) {
		return processTemplateGesuch("InfoVerfuegtMutation.ftl", gesuch, gesuchsteller, toArgumentPair(EMPFAENGER_MAIL, empfaengerMail));
	}

	public String getInfoMahnung(@Nonnull Gesuch gesuch, Gesuchsteller gesuchsteller, @Nonnull String empfaengerMail) {
		return processTemplateGesuch("InfoMahnung.ftl", gesuch, gesuchsteller, toArgumentPair(EMPFAENGER_MAIL, empfaengerMail));
	}

	public String getWarnungGesuchNichtFreigegeben(@Nonnull Gesuch gesuch, Gesuchsteller gesuchsteller, @Nonnull String empfaengerMail, int anzahlTage) {
		return processTemplateGesuch("WarnungGesuchNichtFreigegeben.ftl", gesuch, gesuchsteller, toArgumentPair(EMPFAENGER_MAIL, empfaengerMail), toArgumentPair(ANZAHL_TAGE, anzahlTage));
	}

	public String getWarnungFreigabequittungFehlt(@Nonnull Gesuch gesuch, Gesuchsteller gesuchsteller, @Nonnull String empfaengerMail, int anzahlTage) {
		LocalDate datumLoeschung = LocalDate.now().plusDays(anzahlTage).minusDays(1);
		return processTemplateGesuch("WarnungFreigabequittungFehlt.ftl", gesuch, gesuchsteller,
			toArgumentPair(EMPFAENGER_MAIL, empfaengerMail),
			toArgumentPair(ANZAHL_TAGE, anzahlTage),
			toArgumentPair(DATUM_LOESCHUNG, Constants.DATE_FORMATTER.format(datumLoeschung)));
	}

	public String getInfoGesuchGeloescht(@Nonnull Gesuch gesuch, Gesuchsteller gesuchsteller, @Nonnull String empfaengerMail) {
		return processTemplateGesuch("InfoGesuchGeloescht.ftl", gesuch, gesuchsteller, toArgumentPair(EMPFAENGER_MAIL, empfaengerMail));
	}

	public String getInfoFreischaltungGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode, @Nonnull Gesuchsteller gesuchsteller, @Nonnull String empfaengerMail) {
		return processTemplate("InfoFreischaltungGesuchsperiode.ftl",
			toArgumentPair(GESUCHSPERIODE, gesuchsperiode),
			toArgumentPair(START_DATUM, Constants.DATE_FORMATTER.format(gesuchsperiode.getGueltigkeit().getGueltigAb())),
			toArgumentPair(GESUCHSTELLER, gesuchsteller),
			toArgumentPair(EMPFAENGER_MAIL, empfaengerMail));
	}

	private String processTemplateGesuch(@Nonnull String nameOfTemplate, @Nonnull Gesuch gesuch, @Nonnull Gesuchsteller gesuchsteller, @Nonnull Object[]... extraValuePairs) {
		Object[][] paramsToPass = Arrays.copyOf(extraValuePairs, extraValuePairs.length + 2);
		paramsToPass[paramsToPass.length - 1] = new Object[] { "gesuch", gesuch };
		paramsToPass[paramsToPass.length - 2] = new Object[] { GESUCHSTELLER, gesuchsteller };
		return doProcessTemplate(nameOfTemplate, DEFAULT_LOCALE, paramsToPass);
	}

	private String processTemplateBetreuung(@Nonnull String nameOfTemplate, @Nonnull Betreuung betreuung, @Nonnull Gesuchsteller gesuchsteller, @Nonnull Object[]... extraValuePairs) {
		Object[][] paramsToPass = Arrays.copyOf(extraValuePairs, extraValuePairs.length + 2);
		paramsToPass[paramsToPass.length - 1] = new Object[] { "betreuung", betreuung };
		paramsToPass[paramsToPass.length - 2] = new Object[] { GESUCHSTELLER, gesuchsteller };
		return doProcessTemplate(nameOfTemplate, DEFAULT_LOCALE, paramsToPass);
	}

	@SuppressWarnings("Duplicates")
	private String processTemplateBetreuungGeloescht(String nameOfTemplate, Betreuung betreuung, Fall fall, Kind kind, Gesuchsteller gesuchsteller1, Institution institution, Object[]... extraValuePairs) {
		Object[][] paramsToPass = Arrays.copyOf(extraValuePairs, extraValuePairs.length + 5);
		paramsToPass[paramsToPass.length - 1] = new Object[] { "betreuung", betreuung };
		paramsToPass[paramsToPass.length - 2] = new Object[] { "fall", fall };
		paramsToPass[paramsToPass.length - 3] = new Object[] { "kind", kind };
		paramsToPass[paramsToPass.length - 4] = new Object[] { GESUCHSTELLER, gesuchsteller1 };
		paramsToPass[paramsToPass.length - 5] = new Object[] { "institution", institution };
		return doProcessTemplate(nameOfTemplate, DEFAULT_LOCALE, paramsToPass);
	}

	@SuppressWarnings("Duplicates")
	private String processTemplateBetreuungVerfuegt(String nameOfTemplate, Betreuung betreuung, Fall fall, Kind kind, Gesuchsteller gesuchsteller1, Institution institution, Object[]... extraValuePairs) {
		Object[][] paramsToPass = Arrays.copyOf(extraValuePairs, extraValuePairs.length + 5);
		paramsToPass[paramsToPass.length - 1] = new Object[] { "betreuung", betreuung };
		paramsToPass[paramsToPass.length - 2] = new Object[] { "fall", fall };
		paramsToPass[paramsToPass.length - 3] = new Object[] { "kind", kind };
		paramsToPass[paramsToPass.length - 4] = new Object[] { GESUCHSTELLER, gesuchsteller1 };
		paramsToPass[paramsToPass.length - 5] = new Object[] { "institution", institution };
		return doProcessTemplate(nameOfTemplate, DEFAULT_LOCALE, paramsToPass);
	}

	private String processTemplateMitteilung(@Nonnull String nameOfTemplate, @Nonnull Mitteilung mitteilung, @Nonnull Object[]... extraValuePairs) {
		Object[][] paramsToPass = Arrays.copyOf(extraValuePairs, extraValuePairs.length + 1);
		paramsToPass[paramsToPass.length - 1] = new Object[] { "mitteilung", mitteilung };
		return doProcessTemplate(nameOfTemplate, DEFAULT_LOCALE, paramsToPass);
	}

	private String processTemplate(@Nonnull String nameOfTemplate, @Nonnull Object[]... extraValuePairs) {
		Object[][] paramsToPass = Arrays.copyOf(extraValuePairs, extraValuePairs.length);
		return doProcessTemplate(nameOfTemplate, DEFAULT_LOCALE, paramsToPass);
	}

	private String doProcessTemplate(@Nonnull final String name, @Nonnull Locale loc, final Object[]... extraValuePairs) {
		try {
			final Map<Object, Object> rootMap = new HashMap<>();
			rootMap.put("configuration", ebeguConfiguration);
			rootMap.put("templateConfiguration", this);
			rootMap.put("base64Header", new UTF8Base64MailHeaderDirective());
			if (extraValuePairs != null) {
				for (final Object[] extraValuePair : extraValuePairs) {
					if (extraValuePair.length > 0) {
						assert extraValuePair.length == 2;
						rootMap.put(extraValuePair[0], extraValuePair[1]);
					}
				}
			}

			final Template template = freeMarkerConfiguration.getTemplate(name, loc);
			final StringWriter out = new StringWriter(50);
			template.process(rootMap, out);

			return out.toString();
		} catch (final IOException e) {
			throw new EbeguRuntimeException("doProcessTemplate()", String.format("Failed to load template %s.", name), e);
		} catch (final TemplateException e) {
			throw new EbeguRuntimeException("doProcessTemplate()", String.format("Failed to process template %s.", name), e);
		}
	}

	private Object[] toArgumentPair(String key, Object value) {
		Object[] args = new Object[2];
		args[0] = key;
		args[1] = value;
		return args;
	}

	public String getMailCss() {
		return "<style type=\"text/css\">\n" +
			"        body {\n" +
			"            font-family: \"Open Sans\", Arial, Helvetica, sans-serif;\n" +
			"        }\n" +
			"      .kursInfoHeader {background-color: #bce1ff; font-weight: bold;}" +
			"    </style>";
	}
}
