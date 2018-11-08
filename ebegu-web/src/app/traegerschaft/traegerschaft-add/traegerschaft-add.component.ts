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
import {ChangeDetectionStrategy, Component, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {StateService} from '@uirouter/core';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import ErrorService from '../../core/errors/service/ErrorService';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';

@Component({
    selector: 'dv-traegerschaft-add',
    templateUrl: './traegerschaft-add.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TraegerschaftAddComponent {

    @ViewChild(NgForm) public form: NgForm;

    public traegerschaft: TSTraegerschaft = undefined;

    public constructor(
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly traegerschaftRS: TraegerschaftRS,
    ) {
        this.traegerschaft = new TSTraegerschaft();
    }

    public cancel(): void {
        this.navigateBack();
    }

    public traegerschaftEinladen(): void {
        if (!this.form.valid) {
            return;
        }
        this.errorService.clearAll();
        this.save();
    }

    private save(): void {
        this.traegerschaftRS.createTraegerschaft(this.traegerschaft, this.traegerschaft.mail)
            .then(neueTraegerschaft => {
                this.traegerschaft = neueTraegerschaft;
                this.navigateBack();
            });
    }

    private navigateBack(): void {
        this.$state.go('traegerschaft.list');
    }
}
