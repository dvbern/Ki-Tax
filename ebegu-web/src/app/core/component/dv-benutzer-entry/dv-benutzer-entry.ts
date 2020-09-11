/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {Component, Input} from '@angular/core';
import {TSVerantwortung} from '../../../../models/enums/TSVerantwortung';
import {TSBenutzer} from '../../../../models/TSBenutzer';
import {TSRole} from '../../../../models/enums/TSRole';
import {EbeguUtil} from '../../../../utils/EbeguUtil';

@Component({
    selector: 'dv-benutzer-entry',
    templateUrl: './dv-benutzer-entry.html',
    styleUrls: ['./dv-benutzer-entry.less'],
})
export class DvBenutzerEntry {

    @Input() public benutzer: TSBenutzer;
    @Input() public noIcons: boolean;
    @Input() public noName: boolean;

    public constructor() {
    }

    public getVerantwortungClasses(): string[][] {
        const currentRole = this.benutzer.getCurrentRole();
        if (EbeguUtil.isNullOrUndefined(currentRole)) {
            return [];
        }
        switch (currentRole) {
            case TSRole.SACHBEARBEITER_BG:
            case TSRole.ADMIN_BG:
                return [['fa fa-gift', TSVerantwortung.VERANTWORTUNG_BG]];
            case TSRole.SACHBEARBEITER_TS:
            case TSRole.ADMIN_TS:
                return [['fa fa-graduation-cap', TSVerantwortung.VERANTWORTUNG_TS]];
            case TSRole.ADMIN_GEMEINDE:
            case TSRole.SACHBEARBEITER_GEMEINDE:
                return [['fa fa-gift', TSVerantwortung.VERANTWORTUNG_BG],
                    ['fa fa-graduation-cap', TSVerantwortung.VERANTWORTUNG_TS]];
            case TSRole.ADMIN_TRAEGERSCHAFT:
            case TSRole.SACHBEARBEITER_TRAEGERSCHAFT:
            case TSRole.ADMIN_INSTITUTION:
            case TSRole.SACHBEARBEITER_INSTITUTION:
                return [['fa fa-home', 'VERANTWORTUNG_INSTITUTION']];
            case TSRole.GESUCHSTELLER:
                return [['fa fa-user', 'VERANTWORTUNG_GESUCHSTELLER']];
            case TSRole.SUPER_ADMIN:
                return [['fa fa-rocket', 'VERANTWORTUNG_SUPERADMIN']];
            default:
                return [];
        }
    }
}
