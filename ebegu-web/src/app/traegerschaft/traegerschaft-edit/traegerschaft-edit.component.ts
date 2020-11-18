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
import {StateDeclaration} from '@uirouter/core/lib/state/interface';
import {from, Observable} from 'rxjs';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';

@Component({
    selector: 'dv-traegerschaft-edit',
    templateUrl: './traegerschaft-edit.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TraegerschaftEditComponent implements OnInit {

    @ViewChild(NgForm, { static: false }) public form: NgForm;

    public traegerschaft$: Observable<TSTraegerschaft>;
    private traegerschaftId: string;
    private navigationSource: StateDeclaration;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly traegerschaftRS: TraegerschaftRS,

    ) { }

    public ngOnInit(): void {
        this.navigationSource = this.$transition$.from();
        this.traegerschaftId = this.$transition$.params().traegerschaftId;
        if (!this.traegerschaftId) {
            return;
        }
        this.traegerschaft$ = from(
            this.traegerschaftRS.findTraegerschaft(this.traegerschaftId).then(result => {
                return result;
            }));
    }

    public save(stammdaten: TSTraegerschaft): void {
        if (!this.form.valid) {
            return;
        }
        this.errorService.clearAll();
        this.traegerschaftRS.saveTraegerschaft(stammdaten).then(() => this.navigateBack());
    }

    public cancel(): void {
        this.navigateBack();
    }

    private navigateBack(): void {
        if (!this.navigationSource.name) {
            this.$state.go('traegerschaft.list');
            return;
        }
        const redirectTo = this.navigationSource.name === 'einladung.abschliessen'
            ? 'traegerschaft.view'
            : this.navigationSource;

        this.$state.go(redirectTo, {traegerschaftId: this.traegerschaftId});
    }
}
