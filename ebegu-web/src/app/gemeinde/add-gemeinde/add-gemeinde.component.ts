/*
 * AGPL File-Header
 *
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
import {TSGemeindeStatus} from '../../../models/enums/TSGemeindeStatus';
import TSGemeinde from '../../../models/TSGemeinde';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import ErrorService from '../../core/errors/service/ErrorService';
import {LogFactory} from '../../core/logging/LogFactory';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import UserRS from '../../core/service/userRS.rest';

const LOG = LogFactory.createLog('AddGemeindeComponent');

@Component({
    selector: 'add-gemeinde',
    templateUrl: './add-gemeinde.component.html',
    styleUrls: ['../../benutzer/benutzer/benutzer.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AddGemeindeComponent implements OnInit {

    @ViewChild(NgForm) form: NgForm;

    gemeinde: TSGemeinde = undefined;
    adminMail: string = undefined;
    beguStartDatum: moment.Moment = undefined;
    isDisabled = false;

    constructor(private readonly $transition$: Transition,
                private readonly $state: StateService,
                private readonly errorService: ErrorService,
                private readonly gemeindeRS: GemeindeRS,
                private readonly einstellungRS: EinstellungRS,
                private readonly authServiceRS: AuthServiceRS,
                private readonly userRS: UserRS,
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
    }

    public cancel(): void {
        this.navigateBack();
    }

    gemeindeEinladen(): void {
        if (this.form.valid) {
            this.errorService.clearAll();

            console.log('Gemeinde    ' + this.gemeinde.name);
            console.log('Adminmail   ' + this.adminMail);
            console.log('Startdatum  ' + this.beguStartDatum.format('DD.MM.YYYY'));

            const valid = this.isStartDateValid();
            if (valid) {
                this.gemeindeRS.findGemeindeByName(this.gemeinde.name).then((result) => {
                    // Fehlerfall; die Gemeinde existiert bereits!
                    this.errorService.addMesageAsInfo('Die Gemeinde ' + result.name + ' existiert bereits!');
                }).catch(reason => {
                    // Normalfall; sie Gemeinde existiert noch nicht.

                    this.userRS.findBenutzerByEmail(this.adminMail).then((result) => {
                        // Der Benutzer existiert bereits.
                        const user = result;
                        this.errorService.addMesageAsInfo('Der Benutzer ' + user.vorname + ' ' + user.nachname + ' (' + user.email + ') existiert bereits!');
                    }).catch(reason => {
                        // Der Benutzer existiert noch nicht.
                        this.errorService.addMesageAsInfo('Für ' + this.adminMail + ' existiert noch kein Benutzer!');
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


    private createGemeinde(): void {
        this.gemeinde = new TSGemeinde();
        this.gemeinde.status = TSGemeindeStatus.EINGELADEN;
    }

    private navigateBack() {
        this.$state.go('admin.gemeindelist');
    }
}
