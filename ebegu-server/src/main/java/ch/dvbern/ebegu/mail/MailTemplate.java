/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.mail;

/**
 * Enum für die Namen der MailTemplates (FreeMarkerTemplates). Der Name des Files muss z.B. XY_de.ftl heissen
 */
public enum MailTemplate {

	InfoMitteilungErhalten,
	InfoBetreuungVerfuegt,
	InfoBetreuungGeloescht,
	BenutzerEinladung,
	InfoFreischaltungGesuchsperiode,
	InfoGesuchGeloescht,
	WarnungFreigabequittungFehlt,
	WarnungGesuchNichtFreigegeben,
	InfoMahnung,
	InfoVerfuegtMutation,
	InfoVerfuegtGesuch,
	InfoSchulamtAnmeldungAbgelehnt,
	InfoSchulamtAnmeldungTagesschuleUebernommen,
	InfoSchulamtAnmeldungFerieninselUebernommen,
	InfoBetreuungenBestaetigt,
	InfoBetreuungAbgelehnt,
	InfoStatistikGeneriert,
	InfoOffenePendenzenNeueMitteilungInstitution,
	InfoSchulamtAnmeldungTagesschuleAkzeptiert,
	InfoGemeindeAngebotAktiviert,
	NotrechtGenerischeMitteilung,
	NotrechtBestaetigungPruefungStufe1,
	NotrechtProvisorischeVerfuegung,
	InfoGesuchVerfuegtVerantwortlicherTS,
	InfoGemeindeLastenausgleichDurch,
	InfoSchulamtAnmeldungStorniert
}
