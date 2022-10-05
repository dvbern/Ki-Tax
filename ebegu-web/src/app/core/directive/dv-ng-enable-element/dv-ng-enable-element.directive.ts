/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import {AfterViewInit, Directive, ElementRef, Input, OnChanges} from '@angular/core';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../../models/enums/TSRole';
import {EbeguUtil} from '../../../../utils/EbeguUtil';

/**
 * Attribute Directive um native HTML Elemente zu enablen/disablen. Funktioniert nicht bei Material Komponenten
 * Die Direktive muss folgendermasse benutzt werden:
 *     dvNgEnableElement - diese Attribute muss in jedem Element gesetzt werden, das die Direktive braucht
 *     [dvEnable]="condition" - Condition für die das Element nicht disabled ist
 *     [dvEnableAllowedRoles]="[TSRole.X, TSRole.Y, ...]" - Array mit allen Rollen, für die das Element nicht disabled sein soll
 * werden muss.
 *
 * ACHTUNG! Diese Direktive darf nicht mit disable zusammen benutzt werden
 */
@Directive({
    selector: '[dvNgEnableElement]',
})
export class DvNgEnableElementDirective implements AfterViewInit, OnChanges {

    @Input()
    private dvEnableAllowedRoles: ReadonlyArray<TSRole>;

    @Input()
    private dvEnabled: boolean;

    constructor(
        private readonly elementRef: ElementRef,
        private authServiceRS: AuthServiceRS,
    ) {
    }

    ngAfterViewInit(): void {
        this.handleElement();
    }

    public ngOnChanges(): void {
        this.handleElement();
    }

    private handleElement(): void {
        this.elementRef.nativeElement.disabled = !this.evaluateCondition();
    }

    /**
     * Condition must be true.
     * Roles must be defined, for an empty Roles array no permission is granted.
     * The current user must have one of the passed roles.
     */
    private evaluateCondition(): boolean {
        return this.dvEnabled &&
            EbeguUtil.isNotNullOrUndefined(this.dvEnableAllowedRoles) &&
            this.authServiceRS.isOneOfRoles(this.dvEnableAllowedRoles);
    }
}
