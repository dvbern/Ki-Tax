/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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
import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {StateService, Transition} from '@uirouter/core';
import {Observable} from 'rxjs';
import {TSSozialdienstStammdaten} from '../../../models/sozialdienst/TSSozaildienstStammdaten';
import {SozialdienstRS} from '../../core/service/SozialdienstRS.rest';

@Component({
    selector: 'dv-edit-sozialdienst',
    templateUrl: './edit-sozialdienst.component.html',
    styleUrls: ['./edit-sozialdienst.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditSozialdienstComponent implements OnInit {

    public stammdaten$: Observable<TSSozialdienstStammdaten>;
    public sozialdienstId: string;

    public constructor(private readonly $transition$: Transition, private readonly $state: StateService,
                       private readonly sozialdienstRS: SozialdienstRS,
    ) {
    }

    public ngOnInit(): void {
        this.sozialdienstId = this.$transition$.params().sozialdienstId;
        if (!this.sozialdienstId) {
            return;
        }
        this.loadStammdaten();
    }

    public onSubmit(): void {

    }

    public navigateBack(): void {
        this.$state.go('sozialdienst.list');
    }

    public isStammdatenEditable(): boolean {
        return false;
    }

    private loadStammdaten(): void {
        this.stammdaten$ = this.sozialdienstRS.getSozialdienstStammdaten(this.sozialdienstId);
    }
}
