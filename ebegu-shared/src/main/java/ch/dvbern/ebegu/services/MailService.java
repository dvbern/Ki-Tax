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

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.SupportAnfrageDTO;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.AbstractAnmeldung;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.entities.RueckforderungMitteilung;
import ch.dvbern.ebegu.enums.GemeindeAngebotTyp;
import ch.dvbern.ebegu.enums.RueckforderungStatus;
import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.ebegu.util.UploadFileInfo;

/**
 * Service zum Versenden von E-Mails
 */
public interface MailService {

	/**
	 * Sendet die Email mit gegebenem MessageBody an die gegebene Adresse. Dadurch kann eine beliebige Message gemailt
	 * werden
	 */
	void sendMessage(@Nonnull String subject, @Nonnull String messageBody, @Nonnull String mailadress)
		throws MailException;

	/**
	 * Sendet die Email mit gegebenem MessageBody an die gegebene Adresse. Dadurch kann eine beliebige Message gemailt
	 * werden. Das uebergebene UploadFileInfo wird als Attachment mitgeschickt.
	 */
	void sendMessageWithAttachment(
		@Nonnull String subject,
		@Nonnull String messageBody,
		@Nonnull String mailadress,
		@Nonnull UploadFileInfo uploadFileInfo
	) throws MailException;

	/**
	 * Sendet eine Supportanfrage an die definierte Support-Email
	 */
	void sendSupportAnfrage(@Nonnull SupportAnfrageDTO supportAnfrageDTO);

	/**
	 * Sendet eine Email mit der Information, dass alle Betreuungsplaetze bestaetigt wurden und das Gesuch freigegeben
	 * werden kann.
	 */
	void sendInfoBetreuungenBestaetigt(@Nonnull Gesuch gesuch) throws MailException;

	/**
	 * Sendet eine Email mit der Information, dass ein Betreuungsplatz abgelehnt wurde.
	 */
	void sendInfoBetreuungAbgelehnt(@Nonnull Betreuung betreuung) throws MailException;

	/**
	 * Sendet eine Email mit der Information, dass eine Anmeldung fuer ein Schulamt-Angebot ins Backend uebernommen
	 * wurde
	 */
	void sendInfoSchulamtAnmeldungTagesschuleUebernommen(@Nonnull AbstractAnmeldung abstractAnmeldung) throws MailException;

	/**
	 * Sendet eine Email mit der Information, dass eine Anmeldung fuer ein Schulamt-Angebot abgelehnt wurde.
	 */
	void sendInfoSchulamtAnmeldungAbgelehnt(@Nonnull AbstractAnmeldung abstractAnmeldung) throws MailException;

	/**
	 * Sendet eine Email mit der Information, dass eine Anmeldung für eine Ferieninsel angenommen wurde
	 */
	void sendInfoSchulamtAnmeldungFerieninselUebernommen(@Nonnull AbstractAnmeldung abstractAnmeldung) throws MailException;

	/**
	 * Sendet eine Email mit der Benachrichtigung, dass eine In-System Nachricht erhalten wurde.
	 */
	void sendInfoMitteilungErhalten(@Nonnull Mitteilung mitteilung) throws MailException;

	/**
	 * Sendet eine Email mit der Information, dass ein Gesuch Verfügt wurde.
	 */
	void sendInfoVerfuegtGesuch(@Nonnull Gesuch gesuch) throws MailException;

	/**
	 * Sendet eine Email mit der Information, dass eine Mutation Verfügt wurde.
	 */
	void sendInfoVerfuegtMutation(@Nonnull Gesuch gesuch) throws MailException;

	/**
	 * Sendet eine Email mit der Information, dass eine Mahnung versendet wurde.
	 */
	void sendInfoMahnung(@Nonnull Gesuch gesuch) throws MailException;

	/**
	 * Sendet eine Email mit der Information, dass ein Gesuch Verfügt wurde.
	 */
	void sendWarnungGesuchNichtFreigegeben(@Nonnull Gesuch gesuch, int anzahlTageBisLoeschung) throws MailException;

	/**
	 * Sendet eine Email mit der Information, dass ein Gesuch Verfügt wurde.
	 */
	void sendWarnungFreigabequittungFehlt(@Nonnull Gesuch gesuch, int anzahlTageBisLoeschung) throws MailException;

	/**
	 * Sendet eine Email mit der Information, dass ein Gesuch Verfügt wurde.
	 */
	void sendInfoGesuchGeloescht(@Nonnull Gesuch gesuch) throws MailException;

	/**
	 * Sendet eine Mail an den GS1 der übergebenen Gesuche, dass die übergebene Gesuchsperiode eröffnet wurde.
	 */
	Future<Integer> sendInfoFreischaltungGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull List<Gesuch> gesucheToSendMail);

	/**
	 * Sendet eine Mail an den GS1 des übergebenen Gesuchs, dass die übergebene Gesuchsperiode eröffnet wurde.
	 */
	boolean sendInfoFreischaltungGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gesuch gesuch);

	/**
	 * Sendet unter gewissen Bedingungen pro Betreuung eine Email mit der Information, dass ein Betreuungsplatz
	 * geloescht wurde.
	 */
	void sendInfoBetreuungGeloescht(@Nonnull List<Betreuung> betreuungen);

	/**
	 * Sendet eine Email mit der Information, dass eine Betreuung verfuegt wurde.
	 */
	void sendInfoBetreuungVerfuegt(@Nonnull Betreuung betreuung);

	/**
	 * Sendet eine E-Mail mit der Information, dass die Statistik erstellt wurde
	 */
	void sendInfoStatistikGeneriert(
		@Nonnull String receiverEmail, @Nonnull String downloadurl,
		@Nonnull Locale locale);

	/**
	 * Sends an Einladungsemail to the given user according to the type of the Einladung
	 */
	void sendBenutzerEinladung(@Nonnull Benutzer einladender, @Nonnull Einladung einladung) throws MailException;

	/**
	 * Sendet eine E-Mail an eine Institution mit der Info, dass es offene Pendenzen gibt
	 */
	void sendInfoOffenePendenzenInstitution(@Nonnull InstitutionStammdaten institutionStammdaten);

	/**
	 * Sendet eine Email mit der Information, dass eine Anmeldung fuer ein Schulamt-Angebot ins Backend uebernommen
	 * wurde
	 */
	void sendInfoSchulamtAnmeldungTagesschuleAkzeptiert(@Nonnull AbstractAnmeldung abstractAnmeldung) throws MailException;

	/**
	 * Sendet eine Email mit der Information, dass ein Angebot für eine Gemeinde aktiviert wurde
	 */
	void sendInfoGemeineAngebotAktiviert(@Nonnull Gemeinde gemeinde, @Nonnull GemeindeAngebotTyp angebot);
	/**
	 * schickt eine email an den Verantwortlichen Tagesschule und informiert, dass das Gesuch verfuegt wurde
	 */
	void sendInfoGesuchVerfuegtVerantwortlicherTS(@Nonnull Gesuch gesuch, @Nonnull Benutzer verantwortlicherTS) throws MailException;

	void sendNotrechtGenerischeMitteilung(
		@Nonnull RueckforderungMitteilung mitteilung,
		@Nonnull String empfaengerMail,
		@Nonnull List<RueckforderungStatus> statusList);

	/**
	 * Sendet eine Email mit der Informatiom, dass ein Ruckforderungformular bei der Kanton geprueft wurde
	 */
	@Nullable
	String sendNotrechtBestaetigungPruefungStufe1(@Nonnull RueckforderungFormular rueckforderungFormular);

	void sendInfoRueckforderungProvisorischVerfuegt(@Nonnull RueckforderungFormular rueckforderungFormular) throws MailException;
}
