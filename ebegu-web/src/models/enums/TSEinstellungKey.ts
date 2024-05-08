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

/* eslint-disable max-len */
export enum TSEinstellungKey {
    GEMEINDE_KONTINGENTIERUNG_ENABLED = 'GEMEINDE_KONTINGENTIERUNG_ENABLED' as any,
    GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE = 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE' as any,
    ANGEBOT_SCHULSTUFE = 'ANGEBOT_SCHULSTUFE' as any,
    GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' as any,
    GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' as any,
    GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' as any,
    GEMEINDE_TAGESSCHULE_TAGIS_ENABLED = 'GEMEINDE_TAGESSCHULE_TAGIS_ENABLED' as any,
    GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG =
        'GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG' as any,
    GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED' as any,
    GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA' as any,
    GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA =
        'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA' as any,
    GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO =
        'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO' as any,
    GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED =
        'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED' as any,
    GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO' as any,
    GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT =
        'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT' as any,
    GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA' as any,
    GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED' as any,
    GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO' as any,
    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED' as any,
    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT =
        'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT' as any,
    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN =
        'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN' as any,
    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT =
        'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT' as any,
    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN =
        'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN' as any,
    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT =
        'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT' as any,
    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED =
        'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED' as any,
    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT =
        'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT' as any,
    GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT = 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT' as any,
    GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT = 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT' as any,
    GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED = 'GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED' as any,
    GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT' as any,
    GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA' as any,
    GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO' as any,
    GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE' as any,
    GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MAX_MASSGEBENDEN_EINKOMMEN_FUER_BERECHNUNG = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MAX_MASSGEBENDEN_EINKOMMEN_FUER_BERECHNUNG' as any,
    GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER = 'GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER' as any,
    MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG' as any,
    MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG' as any,
    MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG' as any,
    MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD' as any,
    MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD' as any,
    MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD' as any,
    MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD = 'MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD' as any,
    MIN_MASSGEBENDES_EINKOMMEN = 'MIN_MASSGEBENDES_EINKOMMEN' as any,
    MAX_MASSGEBENDES_EINKOMMEN = 'MAX_MASSGEBENDES_EINKOMMEN' as any,
    OEFFNUNGSTAGE_KITA = 'OEFFNUNGSTAGE_KITA' as any,
    OEFFNUNGSTAGE_TFO = 'OEFFNUNGSTAGE_TFO' as any,
    OEFFNUNGSSTUNDEN_TFO = 'OEFFNUNGSSTUNDEN_TFO' as any,
    ZUSCHLAG_BEHINDERUNG_PRO_TG = 'ZUSCHLAG_BEHINDERUNG_PRO_TG' as any,
    ZUSCHLAG_BEHINDERUNG_PRO_STD = 'ZUSCHLAG_BEHINDERUNG_PRO_STD' as any,
    MIN_VERGUENSTIGUNG_PRO_TG = 'MIN_VERGUENSTIGUNG_PRO_TG' as any,
    MIN_VERGUENSTIGUNG_PRO_STD = 'MIN_VERGUENSTIGUNG_PRO_STD' as any,
    PARAM_PENSUM_KITA_MIN = 'PARAM_PENSUM_KITA_MIN' as any,
    PARAM_PENSUM_TAGESELTERN_MIN = 'PARAM_PENSUM_TAGESELTERN_MIN' as any,
    PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3 = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3' as any,
    PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4 = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4' as any,
    PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5 = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5' as any,
    PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6 = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6' as any,
    PARAM_MAX_TAGE_ABWESENHEIT = 'PARAM_MAX_TAGE_ABWESENHEIT' as any,
    FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION = 'FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION' as any,
    FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION = 'FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION' as any,
    FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION = 'FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION' as any,
    FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION = 'FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION' as any,
    ERWERBSPENSUM_ZUSCHLAG = 'ERWERBSPENSUM_ZUSCHLAG' as any,
    LATS_LOHNNORMKOSTEN = 'LATS_LOHNNORMKOSTEN' as any,
    LATS_LOHNNORMKOSTEN_LESS_THAN_50 = 'LATS_LOHNNORMKOSTEN_LESS_THAN_50' as any,
    LATS_STICHTAG = 'LATS_STICHTAG' as any,
    EINGEWOEHNUNG_TYP = 'EINGEWOEHNUNG_TYP' as any,
    SCHNITTSTELLE_STEUERN_AKTIV = 'SCHNITTSTELLE_STEUERN_AKTIV' as any,
    FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF = 'FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF' as any,
    FERIENBETREUUNG_CHF_PAUSCHALBETRAG = 'FERIENBETREUUNG_CHF_PAUSCHALBETRAG' as any,
    FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER = 'FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER' as any,
    FINANZIELLE_SITUATION_TYP = 'FINANZIELLE_SITUATION_TYP' as any,
    KITAPLUS_ZUSCHLAG_AKTIVIERT = 'KITAPLUS_ZUSCHLAG_AKTIVIERT' as any,
    ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM = 'ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM' as any,
    FKJV_FAMILIENSITUATION_NEU = 'FKJV_FAMILIENSITUATION_NEU' as any,
    MINIMALDAUER_KONKUBINAT = 'MINIMALDAUER_KONKUBINAT' as any,
    GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN = 'GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN' as any,
    KINDERABZUG_TYP = 'KINDERABZUG_TYP' as any,
    KESB_PLATZIERUNG_DEAKTIVIEREN = 'KESB_PLATZIERUNG_DEAKTIVIEREN' as any,
    BESONDERE_BEDUERFNISSE_LUZERN = 'BESONDERE_BEDUERFNISSE_LUZERN' as any,
    FKJV_MAX_PENSUM_AUSSERORDENTLICHER_ANSPRUCH = 'FKJV_MAX_PENSUM_AUSSERORDENTLICHER_ANSPRUCH' as any,
    AUSSERORDENTLICHER_ANSPRUCH_RULE = 'AUSSERORDENTLICHER_ANSPRUCH_RULE' as any,
    DAUER_BABYTARIF = 'DAUER_BABYTARIF' as any,
    FKJV_TEXTE = 'FKJV_TEXTE' as any,
    DIPLOMATENSTATUS_DEAKTIVIERT = 'DIPLOMATENSTATUS_DEAKTIVIERT' as any,
    SPRACHE_AMTSPRACHE_DISABLED = 'SPRACHE_AMTSPRACHE_DISABLED' as any,
    ZEMIS_DISABLED = 'ZEMIS_DISABLED' as any,
    FREIGABE_QUITTUNG_EINLESEN_REQUIRED = 'FREIGABE_QUITTUNG_EINLESEN_REQUIRED' as any,
    UNBEZAHLTER_URLAUB_AKTIV = 'UNBEZAHLTER_URLAUB_AKTIV' as any,
    FACHSTELLEN_TYP = 'FACHSTELLEN_TYP' as any,
    AUSWEIS_NACHWEIS_REQUIRED = 'AUSWEIS_NACHWEIS_REQUIRED' as any,
    PENSUM_ANZEIGE_TYP = 'PENSUM_ANZEIGE_TYP' as any,
    VERFUEGUNG_EINGESCHRIEBEN_VERSENDEN_AKTIVIERT = 'VERFUEGUNG_EINGESCHRIEBEN_VERSENDEN_AKTIVIERT' as any,
    ABWESENHEIT_AKTIV = 'ABWESENHEIT_AKTIV' as any,
    BEGRUENDUNG_MUTATION_AKTIVIERT = 'BEGRUENDUNG_MUTATION_AKTIVIERT' as any,
    VERFUEGUNG_EXPORT_ENABLED = 'VERFUEGUNG_EXPORT_ENABLED' as any,
    ZAHLUNGSANGABEN_ANTRAGSTELLER_REQUIRED = 'ZAHLUNGSANGABEN_ANTRAGSTELLER_REQUIRED' as any,
    VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK = 'VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK' as any,
    ANSPRUCH_AB_X_MONATEN = 'ANSPRUCH_AB_X_MONATEN' as any,
    PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG = 'PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG' as any,
    KITA_STUNDEN_PRO_TAG = 'KITA_STUNDEN_PRO_TAG' as any,
    FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE = 'FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE' as any,
    SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE = 'SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE' as any,
    ZUSATZLICHE_FELDER_ERSATZEINKOMMEN = 'ZUSATZLICHE_FELDER_ERSATZEINKOMMEN' as any,
    SPRACHFOERDERUNG_BESTAETIGEN = 'SPRACHFOERDERUNG_BESTAETIGEN' as any,
    GESUCH_BEENDEN_BEI_TAUSCH_GS2 = 'GESUCH_BEENDEN_BEI_TAUSCH_GS2' as any,
    SCHULERGAENZENDE_BETREUUNGEN = 'SCHULERGAENZENDE_BETREUUNGEN' as any,
    WEGZEIT_ERWERBSPENSUM = 'WEGZEIT_ERWERBSPENSUM' as any,
    ERWEITERTE_BEDUERFNISSE_AKTIV = 'ERWEITERTE_BEDUERFNISSE_AKTIV' as any,
}

export function getTSEinstellungenKeys(): Array<TSEinstellungKey> {
    return [
        TSEinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED,
        TSEinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE,
        TSEinstellungKey.ANGEBOT_SCHULSTUFE,
        TSEinstellungKey.GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB,
        TSEinstellungKey.GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG,
        TSEinstellungKey.GEMEINDE_TAGESSCHULE_TAGIS_ENABLED,
        TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED,
        TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA,
        TSEinstellungKey.GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB,
        TSEinstellungKey.ERWERBSPENSUM_ZUSCHLAG,
        TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA,
        TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO,
        TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED,
        TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO,
        TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT,
        TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA,
        TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED,
        TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO,
        TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED,
        TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT,
        TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN,
        TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT,
        TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN,
        TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT,
        TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED,
        TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT,
        TSEinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT,
        TSEinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT,
        TSEinstellungKey.GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED,
        TSEinstellungKey.GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT,
        TSEinstellungKey.GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA,
        TSEinstellungKey.GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO,
        TSEinstellungKey.GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE,
        TSEinstellungKey.GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MAX_MASSGEBENDEN_EINKOMMEN_FUER_BERECHNUNG,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD,
        TSEinstellungKey.MIN_MASSGEBENDES_EINKOMMEN,
        TSEinstellungKey.MAX_MASSGEBENDES_EINKOMMEN,
        TSEinstellungKey.OEFFNUNGSTAGE_KITA,
        TSEinstellungKey.OEFFNUNGSTAGE_TFO,
        TSEinstellungKey.OEFFNUNGSSTUNDEN_TFO,
        TSEinstellungKey.ZUSCHLAG_BEHINDERUNG_PRO_TG,
        TSEinstellungKey.ZUSCHLAG_BEHINDERUNG_PRO_STD,
        TSEinstellungKey.MIN_VERGUENSTIGUNG_PRO_TG,
        TSEinstellungKey.MIN_VERGUENSTIGUNG_PRO_STD,
        TSEinstellungKey.PARAM_PENSUM_KITA_MIN,
        TSEinstellungKey.PARAM_PENSUM_TAGESELTERN_MIN,
        TSEinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3,
        TSEinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4,
        TSEinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5,
        TSEinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6,
        TSEinstellungKey.PARAM_MAX_TAGE_ABWESENHEIT,
        TSEinstellungKey.FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION,
        TSEinstellungKey.FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION,
        TSEinstellungKey.FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION,
        TSEinstellungKey.FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION,
        TSEinstellungKey.ERWERBSPENSUM_ZUSCHLAG,
        TSEinstellungKey.LATS_LOHNNORMKOSTEN,
        TSEinstellungKey.LATS_LOHNNORMKOSTEN_LESS_THAN_50,
        TSEinstellungKey.LATS_STICHTAG,
        TSEinstellungKey.EINGEWOEHNUNG_TYP,
        TSEinstellungKey.SCHNITTSTELLE_STEUERN_AKTIV,
        TSEinstellungKey.FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF,
        TSEinstellungKey.FERIENBETREUUNG_CHF_PAUSCHALBETRAG,
        TSEinstellungKey.FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER,
        TSEinstellungKey.FINANZIELLE_SITUATION_TYP,
        TSEinstellungKey.KITAPLUS_ZUSCHLAG_AKTIVIERT,
        TSEinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
        TSEinstellungKey.GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN,
        TSEinstellungKey.KINDERABZUG_TYP,
        TSEinstellungKey.FKJV_FAMILIENSITUATION_NEU,
        TSEinstellungKey.MINIMALDAUER_KONKUBINAT,
        TSEinstellungKey.GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN,
        TSEinstellungKey.KESB_PLATZIERUNG_DEAKTIVIEREN,
        TSEinstellungKey.BESONDERE_BEDUERFNISSE_LUZERN,
        TSEinstellungKey.FKJV_MAX_PENSUM_AUSSERORDENTLICHER_ANSPRUCH,
        TSEinstellungKey.AUSSERORDENTLICHER_ANSPRUCH_RULE,
        TSEinstellungKey.DAUER_BABYTARIF,
        TSEinstellungKey.FKJV_TEXTE,
        TSEinstellungKey.DIPLOMATENSTATUS_DEAKTIVIERT,
        TSEinstellungKey.ZEMIS_DISABLED,
        TSEinstellungKey.SPRACHE_AMTSPRACHE_DISABLED,
        TSEinstellungKey.FREIGABE_QUITTUNG_EINLESEN_REQUIRED,
        TSEinstellungKey.UNBEZAHLTER_URLAUB_AKTIV,
        TSEinstellungKey.FACHSTELLEN_TYP,
        TSEinstellungKey.AUSWEIS_NACHWEIS_REQUIRED,
        TSEinstellungKey.PENSUM_ANZEIGE_TYP,
        TSEinstellungKey.VERFUEGUNG_EINGESCHRIEBEN_VERSENDEN_AKTIVIERT,
        TSEinstellungKey.BEGRUENDUNG_MUTATION_AKTIVIERT,
        TSEinstellungKey.VERFUEGUNG_EXPORT_ENABLED,
        TSEinstellungKey.ZAHLUNGSANGABEN_ANTRAGSTELLER_REQUIRED,
        TSEinstellungKey.VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK,
        TSEinstellungKey.PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG,
        TSEinstellungKey.ANSPRUCH_AB_X_MONATEN,
        TSEinstellungKey.KITA_STUNDEN_PRO_TAG,
        TSEinstellungKey.ZUSATZLICHE_FELDER_ERSATZEINKOMMEN,
        TSEinstellungKey.SPRACHFOERDERUNG_BESTAETIGEN,
        TSEinstellungKey.GESUCH_BEENDEN_BEI_TAUSCH_GS2,
        TSEinstellungKey.SCHULERGAENZENDE_BETREUUNGEN,
        TSEinstellungKey.WEGZEIT_ERWERBSPENSUM,
        TSEinstellungKey.ERWEITERTE_BEDUERFNISSE_AKTIV,
    ];
}

export function getGemeindspezifischeBGConfigKeys(): Array<TSEinstellungKey> {
    return [
        TSEinstellungKey.MIN_MASSGEBENDES_EINKOMMEN,
        TSEinstellungKey.MAX_MASSGEBENDES_EINKOMMEN,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD,
        TSEinstellungKey.ZUSCHLAG_BEHINDERUNG_PRO_TG,
        TSEinstellungKey.ZUSCHLAG_BEHINDERUNG_PRO_STD,
        TSEinstellungKey.MIN_VERGUENSTIGUNG_PRO_TG,
        TSEinstellungKey.MIN_VERGUENSTIGUNG_PRO_STD
    ];
}
