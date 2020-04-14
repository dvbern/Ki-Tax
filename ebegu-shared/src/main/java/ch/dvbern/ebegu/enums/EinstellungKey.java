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
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_HAUPTMAHLZEIT(EinstellungTyp.GEMEINDE),
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_NEBENMAHLZEIT(EinstellungTyp.GEMEINDE),
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN(EinstellungTyp.GEMEINDE),
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_HAUPTMAHLZEIT(EinstellungTyp.GEMEINDE),
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_NEBENMAHLZEIT(EinstellungTyp.GEMEINDE),
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN(EinstellungTyp.GEMEINDE),
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_HAUPTMAHLZEIT(EinstellungTyp.GEMEINDE),
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_NEBENMAHLZEIT(EinstellungTyp.GEMEINDE),

	// Die Gemeinde gewährt die Mahlzeitenvergünstigung auch für Sozialhilfebezüger
	GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED(EinstellungTyp.GEMEINDE),

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
	MIN_TARIF;


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
