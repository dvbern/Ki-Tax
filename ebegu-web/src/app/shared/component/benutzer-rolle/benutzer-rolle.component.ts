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

import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {EinstellungRS} from '../../../../admin/service/einstellungRS.rest';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {LogFactory} from '../../../core/logging/LogFactory';

const LOG = LogFactory.createLog('BenutzerRolleComponent');

@Component({
    selector: 'dv-benutzer-rolle',
    templateUrl: './benutzer-rolle.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
})
export class BenutzerRolleComponent implements OnInit, OnDestroy {

    @Input() public name: string;
    @Input() public readonly inputId: string;
    @Input() public readonly required: boolean = false;
    @Input() public readonly disabled: boolean = false;
    @Input() public readonly excludedRoles: TSRole[] = [];

    @Output() public readonly benutzerRolleChange = new EventEmitter<TSRole>();
    private readonly unsubscribe$ = new Subject<void>();

    public roles: Map<TSRole, string>;

    private _benutzerRolle: TSRole;

    public constructor(
        private readonly authServiceRS: AuthServiceRS,
        public readonly form: NgForm,
        public readonly einstellungRS: EinstellungRS,
    ) {
    }

    public ngOnInit(): void {
        this.einstellungRS.tageschuleEnabledForMandant$()
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(
                einstellung => {
                    this.roles = this.authServiceRS.getVisibleRolesForPrincipal(einstellung.getValueAsBoolean())
                        .filter(rolle => !this.excludedRoles.includes(rolle))
                        .reduce((rollenMap, rolle) => {
                                return rollenMap.set(rolle, TSRoleUtil.translationKeyForRole(rolle, true));
                            },
                            new Map<TSRole, string>(),
                        );
                },
                err => LOG.error(err)
        );
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    @Input()
    public get benutzerRolle(): TSRole {
        return this._benutzerRolle;
    }

    // noinspection JSUnusedGlobalSymbols
    public set benutzerRolle(value: TSRole) {
        this._benutzerRolle = value;
        this.benutzerRolleChange.emit(value);
    }

    // noinspection JSMethodCanBeStatic
    public trackByRole(_index: number, item: { key: TSRole, value: string }): string {
        return item.key;
    }
}
