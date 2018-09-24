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

import {ChangeDetectionStrategy, Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {StateService, Transition} from '@uirouter/core';
import * as moment from 'moment';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSGemeindeStatus} from '../../../models/enums/TSGemeindeStatus';
import TSEinstellung from '../../../models/TSEinstellung';
import TSGemeinde from '../../../models/TSGemeinde';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import DateUtil from '../../../utils/DateUtil';
import ErrorService from '../../core/errors/service/ErrorService';
import BenutzerRS from '../../core/service/benutzerRS.rest';
import GesuchsperiodeRS from '../../core/service/gesuchsperiodeRS.rest';

@Component({
    selector: 'dv-add-gemeinde',
    templateUrl: './add-gemeinde.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AddGemeindeComponent implements OnInit {

    @ViewChild(NgForm) form: NgForm;

    gemeinde: TSGemeinde = undefined;
    adminMail: string = undefined;
    beguStartDatum: moment.Moment;
    beguStartDatumMin: moment.Moment;
    isDisabled = false;
    gesuchsperiodeList: Array<TSGesuchsperiode>;

    constructor(private readonly $transition$: Transition,
                private readonly $state: StateService,
                private readonly errorService: ErrorService,
                private readonly gemeindeRS: GemeindeRS,
                private readonly benutzerRS: BenutzerRS,
                private readonly einstellungRS: EinstellungRS,
                private readonly gesuchsperiodeRS: GesuchsperiodeRS) {
    }

    public ngOnInit(): void {
        const gemeindeId: string = this.$transition$.params().gemeindeId;
        if (gemeindeId) { // edit
            this.gemeindeRS.findGemeinde(gemeindeId).then((result) => {
                this.gemeinde = result;
            });
        } else { // add
            this.createGemeinde();
        }
        this.adminMail = '';
        const currentDate = moment();
        const futureMonth = moment(currentDate).add(1, 'M');
        const futureMonthBegin = moment(futureMonth).startOf('month');
        this.beguStartDatum = futureMonthBegin;
        this.beguStartDatumMin = futureMonthBegin;
        this.gesuchsperiodeRS.getAllGesuchsperioden().then((response: TSGesuchsperiode[]) => {
            this.gesuchsperiodeList = response;
        });
    }

    public cancel(): void {
        this.navigateBack();
    }

    gemeindeEinladen(): void {
        if (this.form.valid) {
            this.errorService.clearAll();
            const valid = this.isStartDateValid();
            if (valid) {
                this.gemeindeRS.findGemeindeByName(this.gemeinde.name).then((result) => {
                    // Fehlerfall; die Gemeinde existiert bereits!
                    this.errorService.addMesageAsError('Die Gemeinde ' + result.name + ' existiert bereits!');
                }).catch(() => {
                    // Normalfall; die Gemeinde existiert noch nicht.
                    this.benutzerRS.findBenutzerByEmail(this.adminMail).then((result) => {
                        // Der Benutzer existiert bereits.
                        const user = result;
                        // TODO hier kann man was machen, falls der Benutzer bereits exisiert
                        this.errorService.addMesageAsError('Der Benutzer ' + user.vorname + ' ' + user.nachname + ' (' + user.email + ') existiert bereits!');
                        this.persistGemeinde();
                    }).catch(() => {
                        // Der Benutzer existiert noch nicht.
                        // TODO hier kann man was machen, falls der Benutzer noch nicht exisiert
                        this.errorService.addMesageAsError('Für ' + this.adminMail + ' existiert noch kein Benutzer!');
                        this.persistGemeinde();
                    });
                });
            }
        }
    }

    private isStartDateValid(): boolean {
        const day = this.beguStartDatum.format('D');
        if ('1' !== day) {
            this.errorService.addMesageAsError('Das Startdatum muss am 1. des jeweiligen Monats beginnen!');
            return false;
        }
        if (moment() >= this.beguStartDatum) {
            this.errorService.addMesageAsError('Das Startdatum muss in der Zukunft liegen!');
            return false;
        }
        return true;
    }

    private persistGemeinde(): void {
        this.gemeindeRS.createGemeinde(this.gemeinde).then((neueGemeinde) => {
            this.gemeinde = neueGemeinde;
            this.persistEinstellung();
        });
    }

    private persistEinstellung(): void {
        const einstellung: TSEinstellung = new TSEinstellung();
        einstellung.key = TSEinstellungKey.BEGU_ANBIETEN_AB;
        einstellung.value = DateUtil.momentToLocalDate(this.beguStartDatum);
        einstellung.gemeindeId = this.gemeinde.id;
        einstellung.gesuchsperiodeId = this.findePassendeGesuchsperiode().id;
        this.einstellungRS.saveEinstellung(einstellung).then(() => {
            this.navigateBack();
        });
    }

    private findePassendeGesuchsperiode(): TSGesuchsperiode {
        for (const gp of this.gesuchsperiodeList) {
            if (this.beguStartDatum >= gp.gueltigkeit.gueltigAb && this.beguStartDatum <= gp.gueltigkeit.gueltigBis) {
                return gp;
            }
        }
        // Falls das BEGU Startdatum nicht innerhalb einer erfassten Gesuchsperiode liegt, nehmen wir die neuste GP
        return this.gesuchsperiodeList[0];
    }

    private createGemeinde(): void {
        this.gemeinde = new TSGemeinde();
        this.gemeinde.status = TSGemeindeStatus.EINGELADEN;
    }

    private navigateBack() {
        this.$state.go('admin.gemeindelist');
    }
}
