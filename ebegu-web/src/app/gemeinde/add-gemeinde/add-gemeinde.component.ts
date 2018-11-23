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
import {from, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {TSGemeindeStatus} from '../../../models/enums/TSGemeindeStatus';
import TSBfsGemeinde from '../../../models/TSBfsGemeinde';
import TSGemeinde from '../../../models/TSGemeinde';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import EbeguUtil from '../../../utils/EbeguUtil';
import ErrorService from '../../core/errors/service/ErrorService';
import GesuchsperiodeRS from '../../core/service/gesuchsperiodeRS.rest';

@Component({
    selector: 'dv-add-gemeinde',
    templateUrl: './add-gemeinde.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddGemeindeComponent implements OnInit {

    @ViewChild(NgForm) public form: NgForm;

    public gemeinde: TSGemeinde = undefined;
    public adminMail: string = undefined;
    public beguStartDatumMin: moment.Moment;
    public gesuchsperiodeList: Array<TSGesuchsperiode>;
    public maxBFSNummer: number = 6806;

    public unregisteredGemeinden$: Observable<TSBfsGemeinde[]>;
    public selectedUnregisteredGemeinde: TSBfsGemeinde;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly translate: TranslateService,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
    ) {
    }

    public ngOnInit(): void {
        const gemeindeId: string = this.$transition$.params().gemeindeId;
        if (gemeindeId) { // edit
            this.gemeindeRS.findGemeinde(gemeindeId).then(result => {
                this.gemeinde = result;
            });
        } else { // add
            this.initGemeinde();
            this.unregisteredGemeinden$ = from(this.gemeindeRS.getUnregisteredBfsGemeinden())
                .pipe(map(bfsGemeinden => {
                    bfsGemeinden.sort(EbeguUtil.compareByName);
                    return bfsGemeinden;
                }));
        }
        this.adminMail = '';
        const currentDate = moment();
        const futureMonth = moment(currentDate).add(1, 'M');
        const futureMonthBegin = moment(futureMonth).startOf('month');
        this.gemeinde.betreuungsgutscheineStartdatum = futureMonthBegin;
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

    public bfsGemeindeSelected(filterVal: any): void {
        if (this.selectedUnregisteredGemeinde) {
            this.gemeinde.name = this.selectedUnregisteredGemeinde.name;
            this.gemeinde.bfsNummer = this.selectedUnregisteredGemeinde.bfsNummer;
        } else {
            this.gemeinde.name = undefined;
            this.gemeinde.bfsNummer = undefined;
        }
    }

    private isStartDateValid(): boolean {
        const day = this.gemeinde.betreuungsgutscheineStartdatum.format('D');
        if ('1' !== day) {
            this.errorService.addMesageAsError(this.translate.instant('ERROR_STARTDATUM_FIRST_OF_MONTH'));
            return false;
        }
        if (moment() >= this.gemeinde.betreuungsgutscheineStartdatum) {
            this.errorService.addMesageAsError(this.translate.instant('ERROR_STARTDATUM_FUTURE'));
            return false;
        }
        return true;
    }

    private persistGemeinde(): void {
        this.gemeindeRS.createGemeinde(this.gemeinde, this.gemeinde.betreuungsgutscheineStartdatum, this.adminMail)
            .then(neueGemeinde => {
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
