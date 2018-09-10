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

import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {NgForm} from '@angular/forms';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';

@Component({
    selector: 'dv-benutzer-rolle',
    templateUrl: './benutzer-rolle.component.html',
    styleUrls: ['./benutzer-rolle.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class BenutzerRolleComponent implements OnInit {

    @Input() public readonly inputName: string;
    @Input() public readonly inputId: string;
    @Input() public readonly inputRequired: boolean = false;
    @Input() public readonly inputDisabled: boolean = false;
    @Input() public readonly inputForm: NgForm;

    @Output() public benutzerRolleChange = new EventEmitter<TSRole>();

    private _benutzerRolle: TSRole;

    public test: string;

    public roles: Map<TSRole, string>;

    constructor(
        private readonly authServiceRS: AuthServiceRS
    ) {
    }

    public ngOnInit(): void {
        // TODO welche Rollen gelten für das Einladen?
        this.roles = this.getRollen()
            .reduce((rollenMap, role) => {
                    return rollenMap.set(role, TSRoleUtil.translationKeyForRole(role));
                },
                new Map<TSRole, string>()
            );
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
    public trackByRole(index: number, item: { key: TSRole, value: string }): string {
        return item.key;
    }

    private getRollen(): TSRole[] {
        if (EbeguUtil.isTagesschulangebotEnabled()) {
            return this.authServiceRS.isRole(TSRole.SUPER_ADMIN)
                ? TSRoleUtil.getAllRolesButAnonymous()
                : TSRoleUtil.getAllRolesButSuperAdminAndAnonymous();
        } else {
            return this.authServiceRS.isRole(TSRole.SUPER_ADMIN)
                ? TSRoleUtil.getAllRolesButSchulamtAndAnonymous()
                : TSRoleUtil.getAllRolesButSchulamtAndSuperAdminAndAnonymous();
        }
    }
}
