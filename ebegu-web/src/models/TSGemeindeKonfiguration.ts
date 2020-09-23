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

import * as moment from 'moment';
import {CONSTANTS} from '../app/core/constants/CONSTANTS';
import {EbeguUtil} from '../utils/EbeguUtil';
import {TSEinschulungTyp} from './enums/TSEinschulungTyp';
import {TSEinstellungKey} from './enums/TSEinstellungKey';
import {TSEinstellung} from './TSEinstellung';
import {TSFerieninselStammdaten} from './TSFerieninselStammdaten';
import {TSGesuchsperiode} from './TSGesuchsperiode';

export class TSGemeindeKonfiguration {
    public gesuchsperiodeName: string;
    public gesuchsperiodeStatusName: string;
    public gesuchsperiode: TSGesuchsperiode;
    public konfigKontingentierung: boolean; // only on client
    public konfigBeguBisUndMitSchulstufe: TSEinschulungTyp; // only on client
    public konfigTagesschuleTagisEnabled: boolean;
    public konfigFerieninselAktivierungsdatum: moment.Moment;
    public konfigTagesschuleAktivierungsdatum: moment.Moment;
    public konfigTagesschuleErsterSchultag: moment.Moment;
    public konfigZusaetzlicherGutscheinEnabled: boolean; // only on client
    public konfigZusaetzlicherGutscheinBetragKita: number; // only on client
    public konfigZusaetzlicherGutscheinBetragTfo: number; // only on client
    public konfigZusaetzlicherGutscheinBisUndMitSchulstufeKita: TSEinschulungTyp; // only on client
    public konfigZusaetzlicherGutscheinBisUndMitSchulstufeTfo: TSEinschulungTyp; // only on client
    public konfigZusaetzlicherBabybeitragEnabled: boolean; // only on client
    public konfigZusaetzlicherBabybeitragBetragKita: number; // only on client
    public konfigZusaetzlicherBabybeitragBetragTfo: number; // only on client
    public konfigZusaetzlicherAnspruchFreiwilligenarbeitEnabled: boolean; // only on client
    public konfigZusaetzlicherAnspruchFreiwilligenarbeitMaxprozent: number; // only on client
    public konfigMahlzeitenverguenstigungEnabled: boolean; // only on client
    public konfigMahlzeitenverguenstigungEinkommensstufe1VerguenstigungMahlzeit: number; // only on client
    public konfigMahlzeitenverguenstigungEinkommensstufe1VerguenstigungNebenmahlzeit: number; // only on client
    public konfigMahlzeitenverguenstigungEinkommensstufe1MaxEinkommen: number; // only on client
    public konfigMahlzeitenverguenstigungEinkommensstufe2VerguenstigungMahlzeit: number; // only on client
    public konfigMahlzeitenverguenstigungEinkommensstufe2VerguenstigungNebenmahlzeit: number; // only on client
    public konfigMahlzeitenverguenstigungEinkommensstufe2MaxEinkommen: number; // only on client
    public konfigMahlzeitenverguenstigungEinkommensstufe3VerguenstigungMahlzeit: number; // only on client
    public konfigMahlzeitenverguenstigungEinkommensstufe3VerguenstigungNebenmahlzeit: number; // only on client
    public konfigMahlzeitenverguenstigungFuerSozialhilfebezuegerEnabled: boolean; // only on client
    public konfigMahlzeitenverguenstigungMinimalerElternbeitragMahlzeit: number; // only on client
    public konfigMahlzeitenverguenstigungMinimalerElternbeitragNebenmahlzeit: number; // only on client
    public erwerbspensumMinimumOverriden: boolean;
    public erwerbspensumMiminumVorschule: number;
    public erwerbspensumMiminumVorschuleMax: number;
    public erwerbspensumMiminumSchulkinder: number;
    public erwerbspensumMiminumSchulkinderMax: number;
    public konfigSchnittstelleKitaxEnabled: boolean;
    public erwerbspensumZuschlag: number;
    // never override this property. we just load it for validation reasons
    public erwerbspensumZuschlagMax: number;
    public erwerbspensumZuschlagOverriden: boolean;
    public editMode: boolean; // only on client
    public konfigurationen: TSEinstellung[];
    public ferieninselStammdaten: TSFerieninselStammdaten[];

    /**
     * Wir muessen TS Anmeldungen nehmen ab das TagesschuleAktivierungsdatum
     * Es kann also sein das Kinder sich nach den ersten Schultag anmelden
     */
    public isTagesschulenAnmeldungKonfiguriert(): boolean {
        return this.hasTagesschulenAnmeldung()
            && (this.konfigTagesschuleAktivierungsdatum.isBefore(moment([]))
                || this.konfigTagesschuleAktivierungsdatum.isSame(moment([])));
    }

    public isFerieninselanmeldungKonfiguriert(): boolean {
        return this.hasFerieninseAnmeldung()
            && (this.konfigFerieninselAktivierungsdatum.isBefore(moment([]))
                || this.konfigFerieninselAktivierungsdatum.isSame(moment([])));
    }

    public isTageschulenAnmeldungAktiv(): boolean {
        return this.isTagesschulenAnmeldungKonfiguriert()
            && this.konfigTagesschuleAktivierungsdatum.isBefore(moment());
    }

    public isFerieninselAnmeldungAktiv(): boolean {
        return this.isFerieninselanmeldungKonfiguriert()
            && this.konfigFerieninselAktivierungsdatum.isBefore(moment());
    }

    public hasTagesschulenAnmeldung(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.konfigTagesschuleAktivierungsdatum);
    }

    public hasFerieninseAnmeldung(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.konfigFerieninselAktivierungsdatum);
    }

    public isTagesschulAnmeldungBeforePeriode(): boolean {
        return this.hasTagesschulenAnmeldung()
            && this.konfigTagesschuleAktivierungsdatum.isBefore(this.gesuchsperiode.gueltigkeit.gueltigAb);
    }

    public isFerieninselAnmeldungBeforePeriode(): boolean {
        return this.hasFerieninseAnmeldung()
            && this.konfigFerieninselAktivierungsdatum.isBefore(this.gesuchsperiode.gueltigkeit.gueltigAb);
    }

    public initProperties(): void {
        this.konfigBeguBisUndMitSchulstufe = TSEinschulungTyp.KINDERGARTEN2;
        this.konfigKontingentierung = false;
        this.konfigTagesschuleAktivierungsdatum = this.gesuchsperiode.gueltigkeit.gueltigAb;
        this.konfigTagesschuleErsterSchultag = this.gesuchsperiode.gueltigkeit.gueltigAb;
        this.konfigFerieninselAktivierungsdatum = this.gesuchsperiode.gueltigkeit.gueltigAb;
        this.konfigTagesschuleTagisEnabled = false;
        this.konfigurationen.forEach(property => {
            // tslint:disable-next-line:max-switch-cases
            switch (property.key) {
                case TSEinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE: {
                    this.konfigBeguBisUndMitSchulstufe = (TSEinschulungTyp as any)[property.value];
                    break;
                }
                case TSEinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED: {
                    this.konfigKontingentierung = (property.value === 'true');
                    break;
                }
                case TSEinstellungKey.GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB: {
                    this.konfigTagesschuleAktivierungsdatum = moment(property.value, CONSTANTS.DATE_FORMAT);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB: {
                    this.konfigFerieninselAktivierungsdatum = moment(property.value, CONSTANTS.DATE_FORMAT);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG: {
                    this.konfigTagesschuleErsterSchultag = moment(property.value, CONSTANTS.DATE_FORMAT);
                    break;
                }
                case TSEinstellungKey.ERWERBSPENSUM_ZUSCHLAG: {
                    this.erwerbspensumZuschlag = Number(property.value);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT: {
                    this.erwerbspensumMiminumVorschule = Number(property.value);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT: {
                    this.erwerbspensumMiminumSchulkinder = Number(property.value);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED: {
                    this.konfigZusaetzlicherGutscheinEnabled = (property.value === 'true');
                    break;
                }
                case TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA: {
                    this.konfigZusaetzlicherGutscheinBetragKita = Number(property.value);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO: {
                    this.konfigZusaetzlicherGutscheinBetragTfo = Number(property.value);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA: {
                    this.konfigZusaetzlicherGutscheinBisUndMitSchulstufeKita = (TSEinschulungTyp as any)[property.value];
                    break;
                }
                case TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO: {
                    this.konfigZusaetzlicherGutscheinBisUndMitSchulstufeTfo = (TSEinschulungTyp as any)[property.value];
                    break;
                }
                case TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED: {
                    this.konfigZusaetzlicherAnspruchFreiwilligenarbeitEnabled = (property.value === 'true');
                    break;
                }
                case TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED: {
                    this.konfigZusaetzlicherBabybeitragEnabled = (property.value === 'true');
                    break;
                }
                case TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA: {
                    this.konfigZusaetzlicherBabybeitragBetragKita = Number(property.value);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO: {
                    this.konfigZusaetzlicherBabybeitragBetragTfo = Number(property.value);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT: {
                    this.konfigZusaetzlicherAnspruchFreiwilligenarbeitMaxprozent = Number(property.value);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED: {
                    this.konfigMahlzeitenverguenstigungEnabled = (property.value === 'true');
                    break;
                }
                case TSEinstellungKey.
                    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT: {
                    this.konfigMahlzeitenverguenstigungEinkommensstufe1VerguenstigungMahlzeit = Number(property.value);
                    break;
                }
                case TSEinstellungKey.
                    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_NEBENMAHLZEIT: {
                    this.konfigMahlzeitenverguenstigungEinkommensstufe1VerguenstigungNebenmahlzeit = Number(property.value);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN: {
                    this.konfigMahlzeitenverguenstigungEinkommensstufe1MaxEinkommen = Number(property.value);
                    break;
                }
                case TSEinstellungKey.
                    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT: {
                    this.konfigMahlzeitenverguenstigungEinkommensstufe2VerguenstigungMahlzeit = Number(property.value);
                    break;
                }
                case TSEinstellungKey.
                    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_NEBENMAHLZEIT: {
                    this.konfigMahlzeitenverguenstigungEinkommensstufe2VerguenstigungNebenmahlzeit = Number(property.value);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN: {
                    this.konfigMahlzeitenverguenstigungEinkommensstufe2MaxEinkommen = Number(property.value);
                    break;
                }
                case TSEinstellungKey.
                    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT: {
                    this.konfigMahlzeitenverguenstigungEinkommensstufe3VerguenstigungMahlzeit = Number(property.value);
                    break;
                }
                case TSEinstellungKey.
                    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_NEBENMAHLZEIT: {
                    this.konfigMahlzeitenverguenstigungEinkommensstufe3VerguenstigungNebenmahlzeit = Number(property.value);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED: {
                    this.konfigMahlzeitenverguenstigungFuerSozialhilfebezuegerEnabled = (property.value === 'true');
                    break;
                }
                case TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT: {
                    this.konfigMahlzeitenverguenstigungMinimalerElternbeitragMahlzeit = Number(property.value);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_NEBENMAHLZEIT: {
                    this.konfigMahlzeitenverguenstigungMinimalerElternbeitragNebenmahlzeit = Number(property.value);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_TAGESSCHULE_TAGIS_ENABLED: {
                    this.konfigTagesschuleTagisEnabled = (property.value === 'true');
                    break;
                }
                case TSEinstellungKey.GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED: {
                    this.konfigSchnittstelleKitaxEnabled = (property.value === 'true');
                    break;
                }
                default: {
                    break;
                }
            }
        });

        this.erwerbspensumZuschlagOverriden = this.erwerbspensumZuschlag !== this.erwerbspensumZuschlagMax;
        this.erwerbspensumMinimumOverriden =
            (this.erwerbspensumMiminumVorschule !== this.erwerbspensumMiminumVorschuleMax) ||
            (this.erwerbspensumMiminumSchulkinder !== this.erwerbspensumMiminumSchulkinderMax);
    }
}
