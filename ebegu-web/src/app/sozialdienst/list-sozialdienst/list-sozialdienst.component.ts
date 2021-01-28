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
import {StateService} from '@uirouter/core';
import {Observable} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DVAntragListItem} from '../../shared/interfaces/DVAntragListItem';
import {DVEntitaetListItem} from '../../shared/interfaces/DVEntitaetListItem';

@Component({
    selector: 'dv-list-sozialdienst',
    templateUrl: './list-sozialdienst.component.html',
    styleUrls: ['./list-sozialdienst.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ListSozialdienstComponent implements OnInit {

    public hiddenDVTableColumns = [
        'type',
    ];

    public antragList$: Observable<DVEntitaetListItem[]>;

    public constructor(private readonly $state: StateService, private authServiceRS: AuthServiceRS) {
    }

    public ngOnInit(): void {
    }

    public hatBerechtigungHinzufuegen(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getSuperAdminRoles());
    }

    public addSozialdienst(): void {
        this.$state.go('sozialdienst.add');
    }

    public open(id: string): void {

    }
}
