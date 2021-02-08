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

import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnInit,
    ViewChild,
} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatSort} from '@angular/material/sort';
import {StateService} from '@uirouter/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {AbstractAdminViewController} from '../../../admin/abstractAdminView';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSGemeindeStatus} from '../../../models/enums/TSGemeindeStatus';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DVEntitaetListItem} from '../../shared/interfaces/DVEntitaetListItem';

@Component({
    selector: 'dv-gemeinde-list',
    templateUrl: './gemeinde-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GemeindeListComponent extends AbstractAdminViewController implements OnInit {
    public hiddenDVTableColumns = [
        'type',
        'remove',
    ];

    public antragList$: Observable<DVEntitaetListItem[]>;

    @ViewChild(NgForm) public form: NgForm;
    @ViewChild(MatSort, {static: true}) public sort: MatSort;

    public constructor(
        private readonly gemeindeRS: GemeindeRS,
        private readonly $state: StateService,
        private readonly changeDetectorRef: ChangeDetectorRef,
        authServiceRS: AuthServiceRS,
    ) {
        super(authServiceRS);
    }

    public ngOnInit(): void {
        this.updateGemeindenList();
    }

    public updateGemeindenList(): void {
        this.antragList$ = this.gemeindeRS.getGemeindenForPrincipal$().pipe(
            map(
            gemeindeList => {
                const entitaetListItems: DVEntitaetListItem[] = [];
                gemeindeList.forEach(
                    gemeinde => {
                        const dvListItem = {
                            id: gemeinde.id,
                            name: gemeinde.name,
                            status: gemeinde.status.toString(),
                            canEdit: this.hatBerechtigungEditieren(gemeinde),
                        };
                        entitaetListItems.push(dvListItem);
                    },
                );
                return entitaetListItems;
            }));
    }

    public openGemeinde(id: string): void {
        this.$state.go('gemeinde.edit', {gemeindeId: id});
        return;
    }

    public addGemeinde(): void {
        this.$state.go('gemeinde.add');
    }

    public hatBerechtigungHinzufuegen(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }

    public hatBerechtigungEditieren(gemeinde: TSGemeinde): boolean {
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantOnlyRoles())) {
            return gemeinde.status === TSGemeindeStatus.AKTIV;
        }
        // Alle anderen Rollen, die die Institution sehen, duerfen sie oeffnen
        return true;
    }
}
