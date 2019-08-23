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
import {take} from 'rxjs/operators';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSInstitutionStatus} from '../../../models/enums/TSInstitutionStatus';
import TSGemeinde from '../../../models/TSGemeinde';
import TSInstitution from '../../../models/TSInstitution';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import ErrorService from '../../core/errors/service/ErrorService';
import {LogFactory} from '../../core/logging/LogFactory';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';

const LOG = LogFactory.createLog('AddInstitutionComponent');

@Component({
    selector: 'dv-add-institution',
    templateUrl: './add-institution.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddInstitutionComponent implements OnInit {

    @ViewChild(NgForm) public form: NgForm;
    private isBGInstitution: boolean;
    public betreuungsangebote: TSBetreuungsangebotTyp[];
    public betreuungsangebot: TSBetreuungsangebotTyp;
    public traegerschaften: TSTraegerschaft[];
    public institution: TSInstitution = undefined;
    public beguStart: moment.Moment;
    public beguStartDatumMin: moment.Moment;
    public adminMail: string;
    public selectedGemeinde: TSGemeinde;
    public gemeinden: Array<TSGemeinde>;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly institutionRS: InstitutionRS,
        private readonly traegerschaftRS: TraegerschaftRS,
        private readonly translate: TranslateService,
        private readonly gemeindeRS: GemeindeRS,
    ) {
    }

    public ngOnInit(): void {
        this.betreuungsangebot = this.$transition$.params().betreuungsangebot;
        this.betreuungsangebote = this.$transition$.params().betreuungsangebote;

        // initally we think it is a Betreuungsgutschein Institution
        this.isBGInstitution = true;
        if (this.betreuungsangebot === TSBetreuungsangebotTyp.TAGESSCHULE
            || this.betreuungsangebot === TSBetreuungsangebotTyp.FERIENINSEL) {
            this.isBGInstitution = false;
        }
        this.initInstitution();

        this.traegerschaftRS.getAllActiveTraegerschaften().then(result => {
            this.traegerschaften = result;
        });
        const currentDate = moment();
        const futureMonth = moment(currentDate).add(1, 'M');
        const futureMonthBegin = moment(futureMonth).startOf('month');
        this.beguStart = futureMonthBegin;
        this.beguStartDatumMin = futureMonthBegin;

        // if it is not a Betreuungsgutschein Institution we have to load the Gemeinden
        if (!this.isBGInstitution) {
            this.loadGemeindenList();
        }
    }

    public cancel(): void {
        this.navigateBack();
    }

    public institutionEinladen(): void {
        if (!this.form.valid) {
            return;
        }

        this.errorService.clearAll();
        if (this.isStartDateValid()) {
            this.persistInstitution();
        }
    }

    public institutionErstellen(): void {
        if (!this.form.valid) {
            return;
        }
        this.errorService.clearAll();
        this.persistInstitution();
    }

    private isStartDateValid(): boolean {
        const day = this.beguStart.format('D');
        if ('1' !== day) {
            this.errorService.addMesageAsError(this.translate.instant('ERROR_STARTDATUM_FIRST_OF_MONTH'));
            return false;
        }
        if (moment() >= this.beguStart) {
            this.errorService.addMesageAsError(this.translate.instant('ERROR_STARTDATUM_FUTURE'));
            return false;
        }
        return true;
    }

    private persistInstitution(): void {
        this.institutionRS.createInstitution(
            this.institution,
            this.beguStart,
            this.betreuungsangebot,
            this.adminMail,
            this.selectedGemeinde.id,
        ).then(neueinstitution => {
            this.institution = neueinstitution;
            this.navigateBack();
        });
    }

    private initInstitution(): void {
        this.institution = new TSInstitution();
        if (this.isBGInstitution) {
            this.institution.status = TSInstitutionStatus.EINGELADEN;
        } else {
            this.institution.status = TSInstitutionStatus.KONFIGURATION;
        }
    }

    private navigateBack(): void {
        this.$state.go('institution.list');
    }

    public loadGemeindenList(): void {
        if (this.betreuungsangebot === TSBetreuungsangebotTyp.TAGESSCHULE) {
            this.gemeindeRS.getGemeindenForTSByPrincipal()
                .pipe(take(1))
                .subscribe(
                    gemeinden => {
                        this.gemeinden = gemeinden;
                    },
                    err => LOG.error(err),
                );
        }
        if (this.betreuungsangebot === TSBetreuungsangebotTyp.FERIENINSEL) {
            this.gemeindeRS.getGemeindenForFIByPrincipal()
                .pipe(take(1))
                .subscribe(
                    gemeinden => {
                        this.gemeinden = gemeinden;
                    },
                    err => LOG.error(err),
                );
        }
    }
}
