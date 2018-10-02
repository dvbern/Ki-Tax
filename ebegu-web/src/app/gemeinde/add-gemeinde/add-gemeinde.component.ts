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
import {TranslateService} from '@ngx-translate/core';
import {StateService, Transition} from '@uirouter/core';
import * as moment from 'moment';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {TSGemeindeStatus} from '../../../models/enums/TSGemeindeStatus';
import TSGemeinde from '../../../models/TSGemeinde';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import ErrorService from '../../core/errors/service/ErrorService';
import BenutzerRS from '../../core/service/benutzerRS.rest';
import GesuchsperiodeRS from '../../core/service/gesuchsperiodeRS.rest';

@Component({
    selector: 'dv-add-gemeinde',
    templateUrl: './add-gemeinde.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AddGemeindeComponent implements OnInit {

    @ViewChild(NgForm) public form: NgForm;

    public gemeinde: TSGemeinde = undefined;
    public adminMail: string = undefined;
    public beguStartDatum: moment.Moment;
    public beguStartDatumMin: moment.Moment;
    public gesuchsperiodeList: Array<TSGesuchsperiode>;

    public constructor(private readonly $transition$: Transition,
                       private readonly $state: StateService,
                       private readonly errorService: ErrorService,
                       private readonly gemeindeRS: GemeindeRS,
                       private readonly benutzerRS: BenutzerRS,
                       private readonly einstellungRS: EinstellungRS,
                       private readonly translate: TranslateService,
                       private readonly gesuchsperiodeRS: GesuchsperiodeRS) {
    }

    public ngOnInit(): void {
        const gemeindeId: string = this.$transition$.params().gemeindeId;
        if (gemeindeId) { // edit
            this.gemeindeRS.findGemeinde(gemeindeId).then(result => {
                this.gemeinde = result;
            });
        } else { // add
            this.initGemeinde();
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

    public gemeindeEinladen(): void {
        if (!this.form.valid) {
            return;
        }

        this.errorService.clearAll();
        if (this.isStartDateValid()) {
            this.persistGemeinde();
        }
    }

    private isStartDateValid(): boolean {
        const day = this.beguStartDatum.format('D');
        if ('1' !== day) {
            this.errorService.addMesageAsError(this.translate.instant('ERROR_STARTDATUM_FIRST_OF_MONTH'));
            return false;
        }
        if (moment() >= this.beguStartDatum) {
            this.errorService.addMesageAsError(this.translate.instant('ERROR_STARTDATUM_FUTURE'));
            return false;
        }
        return true;
    }

    private persistGemeinde(): void {
        this.gemeindeRS.createGemeinde(this.gemeinde, this.beguStartDatum).then(neueGemeinde => {
            this.gemeinde = neueGemeinde;
            this.navigateBack();
        });
    }

    private initGemeinde(): void {
        this.gemeinde = new TSGemeinde();
        this.gemeinde.status = TSGemeindeStatus.EINGELADEN;
    }

    private navigateBack(): void {
        this.$state.go('gemeinde.list');
    }
}
