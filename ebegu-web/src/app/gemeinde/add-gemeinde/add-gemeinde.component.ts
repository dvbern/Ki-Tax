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

import {ChangeDetectionStrategy, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {StateService, Transition} from '@uirouter/core';
import * as moment from 'moment';
import {from, Observable, Subject} from 'rxjs';
import {map, takeUntil} from 'rxjs/operators';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {TSGemeindeStatus} from '../../../models/enums/TSGemeindeStatus';
import TSBfsGemeinde from '../../../models/TSBfsGemeinde';
import TSGemeinde from '../../../models/TSGemeinde';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import EbeguUtil from '../../../utils/EbeguUtil';
import ErrorService from '../../core/errors/service/ErrorService';
import {LogFactory} from '../../core/logging/LogFactory';
import GesuchsperiodeRS from '../../core/service/gesuchsperiodeRS.rest';

const LOG = LogFactory.createLog('AddGemeindeComponent');

@Component({
    selector: 'dv-add-gemeinde',
    templateUrl: './add-gemeinde.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddGemeindeComponent implements OnInit, OnDestroy {

    @ViewChild(NgForm) public form: NgForm;

    public gemeinde: TSGemeinde = undefined;
    public adminMail: string = undefined;
    public beguStartDatumMin: moment.Moment;
    public gesuchsperiodeList: Array<TSGesuchsperiode>;
    public maxBFSNummer: number = 6806;

    public unregisteredGemeinden$: Observable<TSBfsGemeinde[]>;
    public selectedUnregisteredGemeinde: TSBfsGemeinde;

    public gemeindeHasBetreuungsgutscheine: boolean = false;
    public gemeindeHasTagesschule: boolean = false;
    public gemeindeHasFerieninsel: boolean = false;

    public tageschuleEnabledForMandant: boolean;
    private readonly unsubscribe$ = new Subject<void>();

    public showMessageKeinAngebotSelected: boolean = false;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly translate: TranslateService,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly einstellungRS: EinstellungRS,
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
        this.einstellungRS.tageschuleEnabledForMandant$()
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(tsEnabledForMandantEinstellung => {
                    this.tageschuleEnabledForMandant = tsEnabledForMandantEinstellung.getValueAsBoolean();
                },
                err => LOG.error(err)
            );
        if (!this.tageschuleEnabledForMandant) {
            this.gemeindeHasBetreuungsgutscheine = true;
        }
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    public cancel(): void {
        this.navigateBack();
    }

    public gemeindeEinladen(): void {
        if (!this.form.valid) {
            return;
        }

        this.errorService.clearAll();
        if (this.isAtLeastOneAngebotSelected() && this.isStartDateValid()) {
            this.persistGemeinde();
        }
    }

    public bfsGemeindeSelected(): void {
        if (this.selectedUnregisteredGemeinde) {
            this.gemeinde.name = this.selectedUnregisteredGemeinde.name;
            this.gemeinde.bfsNummer = this.selectedUnregisteredGemeinde.bfsNummer;
        } else {
            this.gemeinde.name = undefined;
            this.gemeinde.bfsNummer = undefined;
        }
    }

    private isAtLeastOneAngebotSelected(): boolean {
        const hasAngebot =
            this.gemeindeHasBetreuungsgutscheine || this.gemeindeHasTagesschule || this.gemeindeHasFerieninsel;
        this.showMessageKeinAngebotSelected = !hasAngebot;
        return hasAngebot;
    }

    private isStartDateValid(): boolean {
        if (!this.gemeindeHasBetreuungsgutscheine) {
            return true;
        }
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
        this.gemeindeRS.createGemeinde(
            this.gemeinde,
            this.adminMail,
            this.gemeindeHasBetreuungsgutscheine,
            this.gemeindeHasTagesschule,
            this.gemeindeHasFerieninsel
        )
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
