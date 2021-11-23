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

export enum TSEinstellungKey {
    GEMEINDE_KONTINGENTIERUNG_ENABLED = 'GEMEINDE_KONTINGENTIERUNG_ENABLED' as any,
    GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE = 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE' as any,
    GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' as any,
    GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' as any,
    GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' as any,
    GEMEINDE_TAGESSCHULE_TAGIS_ENABLED = 'GEMEINDE_TAGESSCHULE_TAGIS_ENABLED' as any,
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
    MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG' as any,
    MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG' as any,
    MAX_VERGUENSTIGUNG_SCHULE_PRO_TG = 'MAX_VERGUENSTIGUNG_SCHULE_PRO_TG' as any,
    MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD' as any,
    MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD' as any,
    MAX_VERGUENSTIGUNG_SCHULE_PRO_STD = 'MAX_VERGUENSTIGUNG_SCHULE_PRO_STD' as any,
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
    FKJV_EINGEWOEHNUNG = 'FKJV_EINGEWOEHNUNG' as any,
    SCHNITTSTELLE_STEUERN_AKTIV = 'SCHNITTSTELLE_STEUERN_AKTIV' as any,
    FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF = 'FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF' as any,
    FERIENBETREUUNG_CHF_PAUSCHALBETRAG = 'FERIENBETREUUNG_CHF_PAUSCHALBETRAG' as any,
    FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER = 'FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER' as any,
    FINANZIELLE_SITUATION_TYP= 'FINANZIELLE_SITUATION_TYP' as any,
    KITAPLUS_ZUSCHLAG_AKTIVIERT= 'KITAPLUS_ZUSCHLAG_AKTIVIERT' as any
}

export function getTSEinstellungenKeys(): Array<TSEinstellungKey> {
    return [
        TSEinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED,
        TSEinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE,
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
        TSEinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_SCHULE_PRO_TG,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_SCHULE_PRO_STD,
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
        TSEinstellungKey.FKJV_EINGEWOEHNUNG,
        TSEinstellungKey.SCHNITTSTELLE_STEUERN_AKTIV,
        TSEinstellungKey.FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF,
        TSEinstellungKey.FERIENBETREUUNG_CHF_PAUSCHALBETRAG,
        TSEinstellungKey.FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER,
        TSEinstellungKey.FINANZIELLE_SITUATION_TYP,
        TSEinstellungKey.KITAPLUS_ZUSCHLAG_AKTIVIERT
    ];
}
