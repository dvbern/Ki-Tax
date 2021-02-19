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
import {from, Observable} from 'rxjs';
import {IPromise} from 'angular';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSSozialdienst} from '../../../models/sozialdienst/TSSozialdienst';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {SozialdienstRS} from '../../core/service/SozialdienstRS.rest';
import {DVEntitaetListItem} from '../../shared/interfaces/DVEntitaetListItem';

@Component({
    selector: 'dv-list-sozialdienst',
    templateUrl: './list-sozialdienst.component.html',
    styleUrls: ['./list-sozialdienst.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ListSozialdienstComponent implements OnInit {

    public hiddenDVTableColumns = [
        'institutionCount',
        'type',
        'remove',
    ];

    public antragList$: Observable<DVEntitaetListItem[]>;

    public constructor(private readonly $state: StateService, private readonly authServiceRS: AuthServiceRS,
                       private readonly sozialdienstRS: SozialdienstRS,
    ) {
    }

    public ngOnInit(): void {
        this.loadData();
    }

    public hatBerechtigungHinzufuegen(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getSuperAdminRoles());
    }

    public addSozialdienst(): void {
        this.$state.go('sozialdienst.add');
    }

    /**
     * Route not yet implemented as it's gonna be part of another story
     */
    public open(id: string): void {
        this.$state.go('sozialdienst.edit', {sozialdienstId: id});
    }

    private loadData(): void {
        // For now only SuperAdmin
        const editPossible = this.authServiceRS.isOneOfRoles(TSRoleUtil.getAllRolesForSozialdienst());
        this.antragList$ = from(this.getSozialdienstForPrincipal().then(sozialdienstList => {
            const entitaetListItems: DVEntitaetListItem[] = [];
            sozialdienstList.forEach(
                sozialdienst => {
                    const dvListItem = {
                        id: sozialdienst.id,
                        name: sozialdienst.name,
                        status: sozialdienst.status.toString(),
                        canEdit: editPossible,
                        canRemove: false,
                    };
                    entitaetListItems.push(dvListItem);
                },
            );
            return entitaetListItems;
        }));
    }

    private getSozialdienstForPrincipal(): IPromise<TSSozialdienst[]> {
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getSozialdienstRolle())) {
            const sozialDienstList =
                [this.authServiceRS.getPrincipal().currentBerechtigung.sozialdienst];
            return Promise.resolve(sozialDienstList);
        }
        return this.sozialdienstRS.getSozialdienstList().toPromise();
    }
}
