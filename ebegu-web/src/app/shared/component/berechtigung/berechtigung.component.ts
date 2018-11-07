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

import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {from, Observable, of} from 'rxjs';
import {map, switchMap} from 'rxjs/operators';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../../models/enums/TSRole';
import TSBerechtigung from '../../../../models/TSBerechtigung';
import TSInstitution from '../../../../models/TSInstitution';
import {TSTraegerschaft} from '../../../../models/TSTraegerschaft';
import EbeguUtil from '../../../../utils/EbeguUtil';
import {InstitutionRS} from '../../../core/service/institutionRS.rest';
import {TraegerschaftRS} from '../../../core/service/traegerschaftRS.rest';
import {Displayable} from '../../interfaces/displayable';

let nextId = 0;

@Component({
    selector: 'dv-berechtigung',
    templateUrl: './berechtigung.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
})
export class BerechtigungComponent {

    @Input() public berechtigung: TSBerechtigung;
    @Input() public disabled: boolean = false;
    @Input() public readonly excludedRoles: TSRole[] = [];
    @Input() public readonly displayClass: string = 'col-sm-12';

    public readonly inputId = `berechtigung-${nextId++}`;
    public readonly rolleId: string;
    public readonly institutionId: string;
    public readonly traegerschaftId: string;

    public readonly institutionen$: Observable<TSInstitution[]>;
    public readonly traegerschaften$: Observable<TSTraegerschaft[]>;

    public readonly compareById = EbeguUtil.compareById;

    public constructor(
        public readonly form: NgForm,
        private readonly institutionRS: InstitutionRS,
        private readonly traegerschaftenRS: TraegerschaftRS,
        private readonly authServiceRS: AuthServiceRS,
    ) {
        this.rolleId = 'rolle-' + this.inputId;
        this.institutionId = 'institution-' + this.inputId;
        this.traegerschaftId = 'treagerschaft-' + this.inputId;

        this.institutionen$ = from(this.institutionRS.getInstitutionenForCurrentBenutzer())
            .pipe(map(BerechtigungComponent.sortByName));

        this.traegerschaften$ = this.traegerschaftenForPrincipal$();
    }

    private static sortByName<T extends Displayable>(arr: T[]): T[] {
        arr.sort(EbeguUtil.compareByName);

        return arr;
    }

    private traegerschaftenForPrincipal$(): Observable<TSTraegerschaft[]> {
        return this.authServiceRS.principal$
            .pipe(
                switchMap(principal => {
                    if (!principal) {
                        return of([]);
                    }

                    if (principal.currentBerechtigung.isSuperadmin()) {
                        return from(this.traegerschaftenRS.getAllTraegerschaften());
                    }

                    if (principal.currentBerechtigung.traegerschaft) {
                        return of([principal.currentBerechtigung.traegerschaft]);
                    }

                    return of([]);
                }),
                map(BerechtigungComponent.sortByName),
            );
    }
}
