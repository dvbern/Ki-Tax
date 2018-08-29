/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.enums;

/**
 * Keys für die Einstellungen
 */
public enum EinstellungKey {

	// Die Gemeinde kennt eine Kontingentierung der Gutscheine
	KONTINGENTIERUNG_ENABLED,

	// Bis zu welcher Schulstufe sollen Gutscheine ausgestellt werden?
	BG_BIS_UND_MIT_SCHULSTUFE,

	// Fixbetrag der Stadt Bern. Eigentlich Jahesabhängig und vermutlich nicht mehr gebraucht. Vorerst mal Param pro Halbjahr
	PARAM_FIXBETRAG_STADT_PRO_TAG_KITA_HALBJAHR_1,
	PARAM_FIXBETRAG_STADT_PRO_TAG_KITA_HALBJAHR_2,

	// Anzahl Tage Kita Max
	PARAM_ANZAL_TAGE_MAX_KITA,

	// Stunden / Tag Max
	PARAM_STUNDEN_PRO_TAG_MAX_KITA,

	// Kosten pro Stunde Max
	PARAM_KOSTEN_PRO_STUNDE_MAX,

	// Kosten pro Stunde Max Tageseltern
	PARAM_KOSTEN_PRO_STUNDE_MAX_TAGESELTERN,

	// Kosten pro Stunde Min
	PARAM_KOSTEN_PRO_STUNDE_MIN,

	// Minimal Massgebendes Einkommen
	PARAM_MASSGEBENDES_EINKOMMEN_MIN,

	// Maximal Massgebendes Einkommen
	PARAM_MASSGEBENDES_EINKOMMEN_MAX,

	// Anzahl Tage Kanton
	PARAM_ANZAHL_TAGE_KANTON,

	// Stunden / Tag Tagi
	PARAM_STUNDEN_PRO_TAG_TAGI,

	// Min Pensum Tagesstätten
	PARAM_PENSUM_TAGI_MIN,

	// Min Pensum Kitas
	PARAM_PENSUM_KITA_MIN,

	// Min Pensum Tageseltern
	PARAM_PENSUM_TAGESELTERN_MIN,

	// Min Pensum Tagesschule
	PARAM_PENSUM_TAGESSCHULE_MIN,

	// Pauschalabzug bei einer Familiengrösse von drei Personen pauschal pro Person
	PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3,

	// Pauschalabzug bei einer Familiengrösse von vier Personen pauschal pro Person
	PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4,

	// Pauschalabzug bei einer Familiengrösse von fünf Personen pauschal pro Person
	PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5,

	// Pauschalabzug bei einer Familiengrösse von sechs Personen pauschal pro Person
	PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6,

	// Max Abwesenheit
	PARAM_MAX_TAGE_ABWESENHEIT,

	// Eine Einkommensverschlechterung wird nur berücksichtigt, wenn diese höher als 20% des Ausgangswertes ist.
	PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG,

	// Baby-Faktor
	PARAM_BABY_FAKTOR,

	// Baby-Faktor bis N – Monate
	PARAM_BABY_ALTER_IN_MONATEN,

	// Abgeltung des Kantons pro Tag
	PARAM_ABGELTUNG_PRO_TAG_KANTON,

	// Maximaler Zuschlag zum Erwerbspensum
	PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM;
}
