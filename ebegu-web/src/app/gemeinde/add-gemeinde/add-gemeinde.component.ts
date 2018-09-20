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
import {MatDialog} from '@angular/material';
import {StateService, Transition} from '@uirouter/core';
import * as moment from 'moment';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSGemeindeStatus} from '../../../models/enums/TSGemeindeStatus';
import TSEinstellung from '../../../models/TSEinstellung';
import TSGemeinde from '../../../models/TSGemeinde';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import {TSDateRange} from '../../../models/types/TSDateRange';
import DateUtil from '../../../utils/DateUtil';
import ErrorService from '../../core/errors/service/ErrorService';
import {LogFactory} from '../../core/logging/LogFactory';
import BenutzerRS from '../../core/service/benutzerRS.rest';
import GesuchsperiodeRS from '../../core/service/gesuchsperiodeRS.rest';

const LOG = LogFactory.createLog('AddGemeindeComponent');

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
                private readonly gesuchsperiodeRS: GesuchsperiodeRS,
                private readonly authServiceRS: AuthServiceRS,
                private readonly dialog: MatDialog) {
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
                    this.errorService.addMesageAsInfo('Die Gemeinde ' + result.name + ' existiert bereits!');
                }).catch(reason => {
                    // Normalfall; sie Gemeinde existiert noch nicht.
                    this.benutzerRS.findBenutzerByEmail(this.adminMail).then((result) => {
                        // Der Benutzer existiert bereits.
                        const user = result;
                        this.errorService.addMesageAsInfo('Der Benutzer ' + user.vorname + ' ' + user.nachname + ' (' + user.email + ') existiert bereits!');
                        this.persistGemeinde();
                    }).catch(reason => {
                        // Der Benutzer existiert noch nicht.
                        this.errorService.addMesageAsInfo('Für ' + this.adminMail + ' existiert noch kein Benutzer!');
                        this.persistGemeinde();
                    });
                });
            }
        }
    }

    private isStartDateValid(): boolean {
        const day = this.beguStartDatum.format('D');
        if ('1' !== day) {
            this.errorService.addMesageAsInfo('Das Startdatum muss am 1. des jeweiligen Monats beginnen!');
            return false;
        }
        if (moment() >= this.beguStartDatum) {
            this.errorService.addMesageAsInfo('Das Startdatum muss in der Zukunft liegen!');
            return false;
        }
        return true;
    }

    private persistGemeinde(): void {
        this.errorService.addMesageAsInfo('Die Gemeinde ' + this.gemeinde.name + 'wird eingeladen...');
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
        //einstellung.gueltigkeit = this.calcGueltigkeit();
        this.einstellungRS.saveEinstellung(einstellung).then((einstllung) => {
            this.navigateBack();
        });
    }

    private calcGueltigkeit(): TSDateRange {
        let rangeEnd: moment.Moment;
        if (this.beguStartDatum.month() < 8) {
            rangeEnd = moment([this.beguStartDatum.year(), 7, 31 ]);
        } else {
            rangeEnd = moment([this.beguStartDatum.year() + 1, 7, 31 ]);
        }
        const gueltigkeit: TSDateRange = new TSDateRange(this.beguStartDatum, rangeEnd);
        return gueltigkeit;
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
