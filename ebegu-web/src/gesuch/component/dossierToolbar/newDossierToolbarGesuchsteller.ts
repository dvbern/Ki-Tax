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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {Directive, ElementRef, Injector, Input} from '@angular/core';
import {UpgradeComponent} from '@angular/upgrade/static';

@Directive({
    selector: 'dossier-toolbar-gesuchsteller'
})
export class NewDossierToolbarGesuchsteller extends UpgradeComponent {

    @Input() public gesuchid: string;
    @Input() public dossierId: string;
    @Input() public fallId: string;
    @Input() public isDashboardScreen: string;
    @Input() public hideActionButtons: string;
    @Input() public forceLoadingFromFall: string;

    public constructor(elementRef: ElementRef, injector: Injector) {
        super('dossierToolbarGesuchsteller', elementRef, injector);
    }
}
