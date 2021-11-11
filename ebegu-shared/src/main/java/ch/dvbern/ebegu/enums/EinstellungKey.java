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
	GEMEINDE_KONTINGENTIERUNG_ENABLED(EinstellungTyp.GEMEINDE),

	// Bis zu welcher Schulstufe sollen Gutscheine ausgestellt werden?
	GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE(EinstellungTyp.GEMEINDE),

	// Ab welchem Datum können Anmeldungen für die Tagesschule erfasst werden
	GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB(EinstellungTyp.GEMEINDE),

	// Ab welchem Datum können Anmeldungen für die Ferieninsel erfasst werden
	GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB(EinstellungTyp.GEMEINDE),

	// Erster Schultag der Tagesschule
	GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG(EinstellungTyp.GEMEINDE),

	// Ob die Tageschulen koennen Tagis sein
	GEMEINDE_TAGESSCHULE_TAGIS_ENABLED(EinstellungTyp.GEMEINDE),

	// Die Gemeinde gewährt einen zusätzlichen Beitrag zum Gutschein
	GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED(EinstellungTyp.GEMEINDE),

	// Betrag des zusätzlichen Beitrags zum Gutschein
	GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA(EinstellungTyp.GEMEINDE),
	GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO(EinstellungTyp.GEMEINDE),

	// Zusaetzlichen Gutschein anbieten bis und mit
	GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA(EinstellungTyp.GEMEINDE),
	GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO(EinstellungTyp.GEMEINDE),

	// Die Gemeinde gewährt einen Zusatzbetrag für Babies
	GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED(EinstellungTyp.GEMEINDE),

	// Betrag des zusätzlichen Gutscheins für Babies
	GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA(EinstellungTyp.GEMEINDE),
	GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO(EinstellungTyp.GEMEINDE),

	// Die Gemeinde akzeptiert Freiwilligenarbeit als Erwerbspensum mit Anspruch
	GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED(EinstellungTyp.GEMEINDE),

	// Maximale Prozente, zu welchen Freiwilligenarbeit zu einem Anspruch führt
	GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT(EinstellungTyp.GEMEINDE),

	// Die Gemeinde gewährt eine Mahlzeitenvergünsgigung
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED(EinstellungTyp.GEMEINDE),

	// Mahlzeitenverguenstigungsstufen Verguenstigung Haupt- und Nebenmahlzeit sowie Maximaleinkommen
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT(EinstellungTyp.GEMEINDE),
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN(EinstellungTyp.GEMEINDE),
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT(EinstellungTyp.GEMEINDE),
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN(EinstellungTyp.GEMEINDE),
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT(EinstellungTyp.GEMEINDE),

	// Die Gemeinde gewährt die Mahlzeitenvergünstigung auch für Sozialhilfebezüger
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED(EinstellungTyp.GEMEINDE),
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT(EinstellungTyp.GEMEINDE),

	// Die Schnittstelle zu Ki-Tax ist aktiviert
	GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED(EinstellungTyp.GEMEINDE),

	// Minimales Erwerbspensum, wenn das Kind nicht eingeschult ist
	GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT(EinstellungTyp.GEMEINDE),
	// Minimales Erwerbspensum, wenn das Kind eingeschult ist
	GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT(EinstellungTyp.GEMEINDE),

	// *** Einstellungen fuer die Gutscheinberechnung

	// Maximale Vergünstigung für Vorschulkinder unter 12 Monaten
	MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG,
	// Maximale Vergünstigung für Vorschulkinder ab 12 Monaten
	MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG,
	// Maximale Vergünstigung bei Eintritt des Kindergartens
	MAX_VERGUENSTIGUNG_SCHULE_PRO_TG,

	// Maximale Vergünstigung für Vorschulkinder unter 12 Monaten
	MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD,
	// Maximale Vergünstigung für Vorschulkinder ab 12 Monaten
	MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD,
	// Maximale Vergünstigung bei Eintritt des Kindergartens
	MAX_VERGUENSTIGUNG_SCHULE_PRO_STD,

	// Minimal Massgebendes Einkommen
	MIN_MASSGEBENDES_EINKOMMEN,
	// Maximal Massgebendes Einkommen
	MAX_MASSGEBENDES_EINKOMMEN,

	OEFFNUNGSTAGE_KITA,
	OEFFNUNGSTAGE_TFO,
	OEFFNUNGSSTUNDEN_TFO,

	ZUSCHLAG_BEHINDERUNG_PRO_TG,
	ZUSCHLAG_BEHINDERUNG_PRO_STD,

	MIN_VERGUENSTIGUNG_PRO_TG,
	MIN_VERGUENSTIGUNG_PRO_STD,

	// *** Einstellungen fuer die Gutscheinberechnung

	// Minimales Erwerbspensum, wenn das Kind nicht eingeschult ist
	MIN_ERWERBSPENSUM_NICHT_EINGESCHULT,
	// Minimales Erwerbspensum, wenn das Kind eingeschult ist
	MIN_ERWERBSPENSUM_EINGESCHULT,
	// Zuschlag, um den der Anspruch aufgrund des Erwerbspensums automatisch erhöht wird
	ERWERBSPENSUM_ZUSCHLAG(EinstellungTyp.GEMEINDE),


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

	//Pensum Fachstelle soziale Integration
	FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION,
	FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION,

	//Pensum Fachstelle soziale Integration
	FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION,
	FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION,

	//Tagesschule Max Min Tarife
	MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG,
	MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG,
	MIN_TARIF,

	//LATS
	LATS_LOHNNORMKOSTEN,
	LATS_LOHNNORMKOSTEN_LESS_THAN_50,
	LATS_STICHTAG,

	// "FKJV: Eingewöhnung aktiviert".
	// Siehe KIBON-2078. Definiert, ob das Kind einen zusätzlichen Anspruch auf Eingewöhnung hat
	FKJV_EINGEWOEHNUNG,

	// "FKJV: Maximale Differenz zwischen erforderlichem und effektivem Beschäftigungspensum für ausserordentlicher Anspruch"
	// Siehe KIBON-2080. Definiert die maximale Differenz zwischen erforderlichem und effektiven Beschäftigunspensum für den ausserordentlichen Anspruch.
	// in Prozent.
	FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM,

	// "FKJV: Soziale Integration bis und mit Schulstufe"
	// Siehe KIBON-2081. Definiert bis zu welcher Schulstufe die soziale Integration ausbezahlt wird.
	FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE,

	// "FKJV: Pauschale nur möglich, wenn Anspruch auf Gutschein"
	// Siehe KIBON-2093. Falls true wird die Pauschale nur ausbezahlt, wenn auch ein Anspruch auf einen Gutschein besteht
	FKJV_PAUSCHALE_BEI_ANSPRUCH,

	// "FKJV: Pauschale auch rückwirkend ausbezahlen, sofern Anspruch vorhanden"
	// Siehe KIBON-2093. Falls true wird die Pauschale bei einer Mutation innerhalb der Gesuchperiode rückwirkend ausbezahlt
	FKJV_PAUSCHALE_RUECKWIRKEND,

	// "EKV nur bei Einkommen unter 80'000"
	// Siehe KIBON-2094. Falls eine Zahl definiert ist, dann besteht ein Anspruch auf eine Einkommensverschlechterung nur bis zu diesem Betrag
	FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF,

	// "FKJV: Anspruchsberechnung monatsweise"
	// Siehe KIBON-2095. Falls true wird der Anspruch nur monatsweise berechnet
	FJKV_ANSPRUCH_MONATSWEISE,

	// Definiert ob die Schnittstelle zu den Steuersystemen aktiv ist
	SCHNITTSTELLE_STEUERN_AKTIV,

	// Ferienbetreuung Kosten pro Tag
	FERIENBETREUUNG_CHF_PAUSCHALBETRAG,

	// Ferienbetreuung Kosten pro Tag für Sonderschüler
	FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER,

	// Neue Frage bei Kinderabzug für FJKV
	// Siehe KIBON-2115
	FJKV_KINDERABZUG_NEU,

	// Neue Familiensituation für FJKV
	// Siehe KIBON-2116
	FJKV_FAMILIENSITUATION_NEU,

	// Definiert die Minimallänge für das Konkubinat, damit zwei Antragstellende berücksichtigt werden
	MINIMALDAUER_KONKUBINAT,

	// Legt den Typen der finanziellen Verhältnisse fest. Z.B. BERN_ASIV oder LUZERN
	FINANZIELLE_SITUATION_TYP,

	// Kitaplus Zuschlag aktiviert (Luzern)
	KITAPLUS_ZUSCHLAG_AKTIVIERT,

	// Können BG Konfigurationen in den Gemeinde Einstellungen überschrieben werden (Solothurn)
	GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN,

	// (Solothurn)
	ANSPRUCH_UNABHAENGIG_BESCHAEFTIGUNGPENSUM;

	private EinstellungTyp typ;

	EinstellungKey () {
		this(EinstellungTyp.SYSTEM);
	}

	EinstellungKey(EinstellungTyp typ) {
		this.typ = typ;
	}

	public boolean isGemeindeEinstellung() {
		return EinstellungTyp.GEMEINDE == typ;
	}

	public boolean isMandantEinstellung() {
		return EinstellungTyp.MANDANT == typ;
	}
}
