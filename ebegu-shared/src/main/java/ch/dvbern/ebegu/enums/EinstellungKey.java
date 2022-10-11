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

import java.util.List;

import ch.dvbern.ebegu.util.mandant.MandantIdentifier;

/**
 * Keys für die Einstellungen
 */
public enum EinstellungKey {

	// Die Gemeinde kennt eine Kontingentierung der Gutscheine
	GEMEINDE_KONTINGENTIERUNG_ENABLED(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),

	// Bis zu welcher Schulstufe sollen Gutscheine ausgestellt werden?
	GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),

	// Ab welchem Datum können Anmeldungen für die Tagesschule erfasst werden
	GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),

	// Ab welchem Datum können Anmeldungen für die Ferieninsel erfasst werden
	GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),

	// Erster Schultag der Tagesschule
	GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),

	// Ob die Tageschulen koennen Tagis sein
	GEMEINDE_TAGESSCHULE_TAGIS_ENABLED(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),

	GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),

	// Die Gemeinde gewährt einen zusätzlichen Beitrag zum Gutschein
	GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),

	// Betrag des zusätzlichen Beitrags zum Gutschein
	GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),
	GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),

	// Zusaetzlichen Gutschein anbieten bis und mit
	GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),
	GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),

	// Die Gemeinde gewährt einen Zusatzbetrag für Babies
	GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),

	// Betrag des zusätzlichen Gutscheins für Babies
	GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),
	GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),

	// Die Gemeinde akzeptiert Freiwilligenarbeit als Erwerbspensum mit Anspruch
	GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),

	// Maximale Prozente, zu welchen Freiwilligenarbeit zu einem Anspruch führt
	GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),

	// Die Gemeinde gewährt eine Mahlzeitenvergünsgigung
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),

	// Mahlzeitenverguenstigungsstufen Verguenstigung Haupt- und Nebenmahlzeit sowie Maximaleinkommen
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),

	// Die Gemeinde gewährt die Mahlzeitenvergünstigung auch für Sozialhilfebezüger
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),

	// Die Schnittstelle zu Ki-Tax ist aktiviert
	GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),

	// Minimales Erwerbspensum, wenn das Kind nicht eingeschult ist
	GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),
	// Minimales Erwerbspensum, wenn das Kind eingeschult ist
	GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),

	// *** Einstellungen fuer die Gutscheinberechnung

	// Maximale Vergünstigung für Vorschulkinder unter 12 Monaten
	MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG(MandantIdentifier.getAll()),
	// Maximale Vergünstigung für Vorschulkinder ab 12 Monaten
	MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG(MandantIdentifier.getAll()),
	// Maximale Vergünstigung bei Eintritt des Kindergartens
	MAX_VERGUENSTIGUNG_SCHULE_PRO_TG(MandantIdentifier.getAll()),

	// Maximale Vergünstigung für Vorschulkinder unter 12 Monaten
	MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD(MandantIdentifier.getAll()),
	// Maximale Vergünstigung für Vorschulkinder ab 12 Monaten
	MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD(MandantIdentifier.getAll()),
	// Maximale Vergünstigung bei Eintritt des Kindergartens
	MAX_VERGUENSTIGUNG_SCHULE_PRO_STD(MandantIdentifier.getAll()),

	// Minimal Massgebendes Einkommen
	MIN_MASSGEBENDES_EINKOMMEN(MandantIdentifier.getAll()),
	// Maximal Massgebendes Einkommen
	MAX_MASSGEBENDES_EINKOMMEN(MandantIdentifier.getAll()),

	OEFFNUNGSTAGE_KITA(MandantIdentifier.getAll()),
	OEFFNUNGSTAGE_TFO(MandantIdentifier.getAll()),
	OEFFNUNGSSTUNDEN_TFO(MandantIdentifier.getAll()),

	ZUSCHLAG_BEHINDERUNG_PRO_TG(MandantIdentifier.getAll()),
	ZUSCHLAG_BEHINDERUNG_PRO_STD(MandantIdentifier.getAll()),

	MIN_VERGUENSTIGUNG_PRO_TG(MandantIdentifier.getAll()),
	MIN_VERGUENSTIGUNG_PRO_STD(MandantIdentifier.getAll()),

	// *** Einstellungen fuer die Gutscheinberechnung

	// Minimales Erwerbspensum, wenn das Kind nicht eingeschult ist
	MIN_ERWERBSPENSUM_NICHT_EINGESCHULT(MandantIdentifier.getAll()),
	// Minimales Erwerbspensum, wenn das Kind eingeschult ist
	MIN_ERWERBSPENSUM_EINGESCHULT(MandantIdentifier.getAll()),
	// Zuschlag, um den der Anspruch aufgrund des Erwerbspensums automatisch erhöht wird
	ERWERBSPENSUM_ZUSCHLAG(MandantIdentifier.getAll(), EinstellungTyp.GEMEINDE),


	// Min Pensum Kitas
	PARAM_PENSUM_KITA_MIN(MandantIdentifier.getAll()),

	// Min Pensum Tageseltern
	PARAM_PENSUM_TAGESELTERN_MIN(MandantIdentifier.getAll()),

	// Min Pensum Tagesschule
	PARAM_PENSUM_TAGESSCHULE_MIN(MandantIdentifier.getAll()),

	// Pauschalabzug bei einer Familiengrösse von drei Personen pauschal pro Person
	PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3(MandantIdentifier.getAll()),

	// Pauschalabzug bei einer Familiengrösse von vier Personen pauschal pro Person
	PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4(MandantIdentifier.getAll()),

	// Pauschalabzug bei einer Familiengrösse von fünf Personen pauschal pro Person
	PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5(MandantIdentifier.getAll()),

	// Pauschalabzug bei einer Familiengrösse von sechs Personen pauschal pro Person
	PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6(MandantIdentifier.getAll()),

	// Max Abwesenheit
	PARAM_MAX_TAGE_ABWESENHEIT(MandantIdentifier.getAll()),

	// Eine Einkommensverschlechterung wird nur berücksichtigt, wenn diese höher als 20% des Ausgangswertes ist.
	PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG(MandantIdentifier.getAll()),

	//Pensum Fachstelle soziale Integration
	FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION(MandantIdentifier.getAll()),
	FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION(MandantIdentifier.getAll()),

	//Pensum Fachstelle soziale Integration
	FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION(MandantIdentifier.getAll()),
	FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION(MandantIdentifier.getAll()),

	//Tagesschule Max Min Tarife
	MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG(MandantIdentifier.getAll()),
	MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG(MandantIdentifier.getAll()),
	MIN_TARIF(MandantIdentifier.getAll()),

	//LATS
	LATS_LOHNNORMKOSTEN(MandantIdentifier.getAll()),
	LATS_LOHNNORMKOSTEN_LESS_THAN_50(MandantIdentifier.getAll()),
	LATS_STICHTAG(MandantIdentifier.getAll()),

	// "FKJV: Eingewöhnung aktiviert".
	// Siehe KIBON-2078. Definiert, ob das Kind einen zusätzlichen Anspruch auf Eingewöhnung hat
	FKJV_EINGEWOEHNUNG(MandantIdentifier.getAll()),

	// "FKJV: Maximale Differenz zwischen erforderlichem und effektivem Beschäftigungspensum für ausserordentlicher Anspruch"
	// Siehe KIBON-2080. Definiert die maximale Differenz zwischen erforderlichem und effektiven Beschäftigunspensum für den ausserordentlichen Anspruch.
	// in Prozent.
	FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM(MandantIdentifier.getAll()),

	// "FKJV: Soziale Integration bis und mit Schulstufe"
	// Siehe KIBON-2081. Definiert bis zu welcher Schulstufe die soziale Integration ausbezahlt wird.
	FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE(MandantIdentifier.getAll()),

	// "Alle: Sprachliche Integration bis und mit Schulstufe"
	// Siehe KIBON-2081. Definiert bis zu welcher Schulstufe die soziale Integration ausbezahlt wird.
	SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE(MandantIdentifier.getAll()),

	// "FKJV: Pauschale nur möglich, wenn Anspruch auf Gutschein"
	// Siehe KIBON-2093. Falls true wird die Pauschale nur ausbezahlt, wenn auch ein Anspruch auf einen Gutschein besteht
	FKJV_PAUSCHALE_BEI_ANSPRUCH(MandantIdentifier.getAll()),

	// "FKJV: Pauschale auch rückwirkend ausbezahlen, sofern Anspruch vorhanden"
	// Siehe KIBON-2093. Falls true wird die Pauschale bei einer Mutation innerhalb der Gesuchperiode rückwirkend ausbezahlt
	FKJV_PAUSCHALE_RUECKWIRKEND(MandantIdentifier.getAll()),

	// "EKV nur bei Einkommen unter 80'000"
	// Siehe KIBON-2094. Falls eine Zahl definiert ist, dann besteht ein Anspruch auf eine Einkommensverschlechterung nur bis zu diesem Betrag
	FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF(MandantIdentifier.getAll()),

	// "FKJV: Anspruchsberechnung monatsweise"
	// Siehe KIBON-2095. Falls true wird der Anspruch nur monatsweise berechnet
	FKJV_ANSPRUCH_MONATSWEISE(MandantIdentifier.getAll()),

	// "FKJV: Textanpassungen"
	// Siehe KIBON-2194. Für FKJV Perioden müssen gewisse Texte angepasst werden
	FKJV_TEXTE(MandantIdentifier.getAll()),

	// Definiert ob die Schnittstelle zu den Steuersystemen aktiv ist
	SCHNITTSTELLE_STEUERN_AKTIV(MandantIdentifier.getAll()),

	// Ferienbetreuung Kosten pro Tag
	FERIENBETREUUNG_CHF_PAUSCHALBETRAG(MandantIdentifier.getAll()),

	// Ferienbetreuung Kosten pro Tag für Sonderschüler
	FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER(MandantIdentifier.getAll()),

	// Neue Familiensituation für FJKV
	// Siehe KIBON-2116
	FKJV_FAMILIENSITUATION_NEU(MandantIdentifier.getAll()),

	// Definiert die Minimallänge für das Konkubinat, damit zwei Antragstellende berücksichtigt werden
	MINIMALDAUER_KONKUBINAT(MandantIdentifier.getAll()),

	// Legt den Typen der finanziellen Verhältnisse fest. Z.B. BERN_ASIV oder LUZERN
	FINANZIELLE_SITUATION_TYP(MandantIdentifier.getAll()),

	// Kitaplus Zuschlag aktiviert (Luzern)
	// Siehe KIBON-2131
	KITAPLUS_ZUSCHLAG_AKTIVIERT(MandantIdentifier.LUZERN),

	// Können BG Konfigurationen in den Gemeinde Einstellungen überschrieben werden (Solothurn)
	// siehe KIBON-2133
	GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN(MandantIdentifier.getAll()),

	// (Solothurn)
	// Siehe KIBON-2134
	ANSPRUCH_UNABHAENGIG_BESCHAEFTIGUNGPENSUM(MandantIdentifier.getAll()),

	// Kinderabzug Typ (Solothurn und FKJV)
	// Siehe KIBON-2182
	KINDERABZUG_TYP(MandantIdentifier.getAll()),

	// Soll die KESB-Platzierung in der Betreuung deaktiviert sein
	// Siehe KIBON-2177
	KESB_PLATZIERUNG_DEAKTIVIEREN(MandantIdentifier.getAll()),

	// Frage für Besondere Beduerfnisse in Luzern aktivieren
	// Siehe KIBON-2189
	BESONDERE_BEDUERFNISSE_LUZERN(MandantIdentifier.getAll()),

	// Wie hich ist das maximale Pensum bei ausserordentlichem Anspruch
	FKJV_MAX_PENSUM_AUSSERORDENTLICHER_ANSPRUCH(MandantIdentifier.getAll()),

	// Welche ausserordentliche Anspruch Rule soll verwendet werden
	AUSSERORDENTLICHER_ANSPRUCH_RULE(MandantIdentifier.getAll()),

	// Luzern: definiert, ob ein Geschwisternbonus ausbezahlt wird für Kind 2 oder 3
	GESCHWISTERNBONUS_AKTIVIERT(MandantIdentifier.getAll()),

	// Wie lange soll der Babytarif angewendet werden
	DAUER_BABYTARIF(MandantIdentifier.getAll()),

	// Soll die Diplomatenstatusfrage angezeigt werden
	DIPLOMATENSTATUS_DEAKTIVIERT(MandantIdentifier.getAll()),

	// Soll die Zemis-Nr. verwendet werden
	ZEMIS_DISABLED(MandantIdentifier.getAll()),

	// Soll die Frage, ob die Sprache die Amtsprache ist, gestellt werden
	SPRACHE_AMTSPRACHE_DISABLED(MandantIdentifier.getAll()),

	// falls diese Einstellung disabled ist, dann wechselt der Status des Antrags nach der Freigabe durch den Antragstellenden
	// direkt auf Freigegeben. "Freigabequittung ausstehend" wird übersprungen.
	FREIGABE_QUITTUNG_EINLESEN_REQUIRED(MandantIdentifier.getAll()),

	// Unbezahlter Urlaub kann mit dieser Einstellung aktivert oder deaktiviert werden
	UNBEZAHLTER_URLAUB_AKTIV(MandantIdentifier.getAll()),

	// Fachstellen Typ (KIBON-2360)
	// BERN oder LUZERN
	FACHSTELLEN_TYP(MandantIdentifier.getAll()),

	// LU: falls diese Einstellung aktiviert ist, wird bei den Gesuchstellenden ein Ausweisnachweis verlangt
	// Siehe KIBON-2310
	AUSWEIS_NACHWEIS_REQUIRED(MandantIdentifier.getAll()),

	// Switch Eingabe des Betreuungspensums in Tagen oder Prozent erlauben (KIBON-2404)
	BETREUUNG_INPUT_SWITCH_ENABLED(MandantIdentifier.getAll()),

	// Aktiviert die Checkbox, um die Verfügung eingschrieben zu versenden
	VERFUEGUNG_EINGESCHRIEBEN_VERSENDEN_AKTIVIERT(MandantIdentifier.getAll()),

	// Erlaubt Abwesenheit zu erfassen in einer Mutation
	ABWESENHEIT_AKTIV(MandantIdentifier.getAll()),

	// Aktiviert das Input Feld zur Eingabe einer Begründung einer Mutation (KIBON-2538)
	BEGRUENDUNG_MUTATION_AKTIVIERT(MandantIdentifier.getAll());


	private EinstellungTyp typ;
	private List<MandantIdentifier> activeForMandant;

	EinstellungKey (MandantIdentifier activeForMandant) {
		this(activeForMandant, EinstellungTyp.SYSTEM);
	}

	EinstellungKey (List<MandantIdentifier> activeForMandants) {
		this(activeForMandants, EinstellungTyp.SYSTEM);
	}

	EinstellungKey(MandantIdentifier activeForMandant, EinstellungTyp typ) {
		this(List.of(activeForMandant), typ);
	}

	EinstellungKey(List<MandantIdentifier> activeForMandants, EinstellungTyp typ) {
		this.typ = typ;
		this.activeForMandant = activeForMandants;
	}

	public boolean isGemeindeEinstellung() {
		return EinstellungTyp.GEMEINDE == typ;
	}

	public boolean isMandantEinstellung() {
		return EinstellungTyp.MANDANT == typ;
	}

	public boolean isEinstellungActivForMandant(MandantIdentifier mandantIdentifier) {
		return this.activeForMandant.contains(mandantIdentifier);
	}
}
