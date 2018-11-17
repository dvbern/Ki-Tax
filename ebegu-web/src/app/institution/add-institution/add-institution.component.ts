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
import {TranslateService} from '@ngx-translate/core';
import {StateService, Transition} from '@uirouter/core';
import * as moment from 'moment';
import {TSInstitutionStatus} from '../../../models/enums/TSInstitutionStatus';
import TSInstitution from '../../../models/TSInstitution';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import ErrorService from '../../core/errors/service/ErrorService';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';

@Component({
    selector: 'dv-add-institution',
    templateUrl: './add-institution.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddInstitutionComponent implements OnInit {

    @ViewChild(NgForm) public form: NgForm;

    public traegerschaften: TSTraegerschaft[];
    public institution: TSInstitution = undefined;
    public beguStart: moment.Moment;
    public beguStartDatumMin: moment.Moment;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly institutionRS: InstitutionRS,
        private readonly traegerschaftRS: TraegerschaftRS,
        private readonly translate: TranslateService,
    ) {
    }

    public ngOnInit(): void {
        const institutionId = this.$transition$.params().institutionId;
        if (institutionId) { // edit
            this.institutionRS.findInstitution(institutionId).then(result => {
                this.institution = result;
            });
        } else { // add
            this.initInstitution();
        }
        this.traegerschaftRS.getAllActiveTraegerschaften().then(result => {
            this.traegerschaften = result;
        });
        this.adminMail = '';
        const currentDate = moment();
        const futureMonth = moment(currentDate).add(1, 'M');
        const futureMonthBegin = moment(futureMonth).startOf('month');
        this.beguStart = futureMonthBegin;
        this.beguStartDatumMin = futureMonthBegin;
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
        this.institutionRS.createInstitution(this.institution, this.beguStart)
            .then(neueinstitution => {
                this.institution = neueinstitution;
                this.navigateBack();
            });
    }

    private initInstitution(): void {
        this.institution = new TSInstitution();
        this.institution.status = TSInstitutionStatus.EINGELADEN;
    }

    private navigateBack(): void {
        this.$state.go('institution.list');
    }
}
