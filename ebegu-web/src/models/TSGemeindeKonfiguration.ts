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
import EbeguUtil from '../utils/EbeguUtil';
import {TSEinschulungTyp} from './enums/TSEinschulungTyp';
import {TSEinstellungKey} from './enums/TSEinstellungKey';
import TSEinstellung from './TSEinstellung';
import TSGesuchsperiode from './TSGesuchsperiode';

export default class TSGemeindeKonfiguration {
    public gesuchsperiodeName: string;
    public gesuchsperiode: TSGesuchsperiode;
    public konfigKontingentierung: boolean; // only on client
    public konfigBeguBisUndMitSchulstufe: TSEinschulungTyp; // only on client
    public konfigTagesschuleAktivierungsdatum: moment.Moment;
    public konfigTagesschuleErsterSchultag: moment.Moment;
    public erwerbspensumZuschlag: number;
    // never override this property. we just load it for validation reasons
    public erwerbspensumZuschlagMax: number;
    public erwerbspensumZuschlagOverriden: boolean;
    public editMode: boolean; // only on client
    public konfigurationen: TSEinstellung[];

    /**
     * Ein datumFreischaltungTagesschule, das nicht vor dem Gesuchsperiodeanfang liegt, wird als "nicht konfiguriert"
     * betrachtet. Dies ist so, weil ein datumFreischaltungTagesschule immer vor dem Gesuchsperiodeanfang liegen muss,
     * damit die Kinder sich rechtzeitig anmelden koennen.
     */
    public isTagesschulenAnmeldungKonfiguriert(): boolean {
        return this.hasTagesschulenAnmeldung()
            && (this.konfigTagesschuleAktivierungsdatum.isBefore(this.gesuchsperiode.gueltigkeit.gueltigAb)
                || this.konfigTagesschuleAktivierungsdatum.isSame(moment([])));
    }

    public isTageschulenAnmeldungAktiv(): boolean {
        return this.isTagesschulenAnmeldungKonfiguriert()
            && this.konfigTagesschuleAktivierungsdatum.isBefore(moment());
    }

    public hasTagesschulenAnmeldung(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.konfigTagesschuleAktivierungsdatum);
    }

    public initProperties(): void {
        this.konfigBeguBisUndMitSchulstufe = TSEinschulungTyp.KINDERGARTEN2;
        this.konfigKontingentierung = false;
        this.konfigTagesschuleAktivierungsdatum = this.gesuchsperiode.gueltigkeit.gueltigAb;
        this.konfigTagesschuleErsterSchultag = this.gesuchsperiode.gueltigkeit.gueltigAb;

        this.konfigurationen.forEach(property => {
            if (TSEinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE === property.key) {
                this.konfigBeguBisUndMitSchulstufe = (TSEinschulungTyp as any)[property.value];
            }
            if (TSEinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED === property.key) {
                this.konfigKontingentierung = (property.value === 'true');
            }
            if (TSEinstellungKey.GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB === property.key) {
                this.konfigTagesschuleAktivierungsdatum = moment(property.value, CONSTANTS.DATE_FORMAT);
            }
            if (TSEinstellungKey.GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG === property.key) {
                this.konfigTagesschuleErsterSchultag = moment(property.value, CONSTANTS.DATE_FORMAT);
            }
            if (TSEinstellungKey.ERWERBSPENSUM_ZUSCHLAG === property.key) {
                this.erwerbspensumZuschlag = Number(property.value);
            }
        });

        this.erwerbspensumZuschlagOverriden = this.erwerbspensumZuschlag !== this.erwerbspensumZuschlagMax;
    }
}
