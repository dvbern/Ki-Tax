/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {Directive, Input, OnInit, TemplateRef, ViewContainerRef} from '@angular/core';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../models/enums/TSRole';

/**
 * dvNgShowElement
 *
 * Structural directive that will hide or show a DOM element depending on the given condition and the given roles.
 * Both parameters must always be passed. If condition is not important then give it the value true
 * An undefined or empty roles-array will block the element to any role and will hide it
 *
 * # SYNTAX
 * As a structural directive it should be used with an asterisk. Using it inside an ng-template is allowed though.
 *
 * *dvNgShowElement="true; roles: ['ADMIN']"                  --->  will grant permission if the role of the current user is ADMIN
 * *dvNgShowElement="true; roles: ['ADMIN', 'GESUCHSTELLER']" --->  will grant permission if the role of the current user is ADMIN or GESUCHSTELLER
 * *dvNgShowElement="true; roles: ['']"                       --->  won't grant any permission. DOM element will always be hidden
 * *dvNgShowElement="true;"                                   --->  won't grant any permission. DOM element will always be hidden
 * *dvNgShowElement="false; roles: ['ADMIN']"                 --->  won't grant any permission. DOM element will always be hidden
 * *dvNgShowElement="getBooleanValue(); roles: ['ADMIN']"     --->  will show the DOM element for users of role ADMIN when getBooleanValue() evaluates to true
 *
 */
@Directive({ selector: '[dvNgShowElement]' })
export class DvNgShowElementDirective implements OnInit {

    private hasView = false;
    // @Input('dvNgShowElementRoles') roles: TSRole[];
    private _roles: TSRole[];
    private _condition: boolean;

    constructor(private readonly templateRef: TemplateRef<any>,
                private readonly viewContainer: ViewContainerRef,
                private readonly authServiceRS: AuthServiceRS) {
    }

    public ngOnInit(): void {
        this.handleElement();
    }

    /**
     * Using a setter to respond to dynamic changes of input value
     */
    @Input('dvNgShowElementRoles') set roles(roles: TSRole[]) {
        this._roles = roles;
        this.handleElement();
    }

    /**
     * Using a setter to respond to dynamic changes of input value
     */
    @Input('dvNgShowElement') set condition(condition: boolean) {
        this._condition = condition;
        this.handleElement();
    }

    private handleElement() {
        const result: boolean = this.evaluateCondition();

        if (result && !this.hasView) {
            this.viewContainer.createEmbeddedView(this.templateRef);
            this.hasView = true;

        } else if (!result && this.hasView) {
            this.viewContainer.clear();
            this.hasView = false;
        }
    }

    /**
     * Condition must be true.
     * Roles must be defined, for an empty Roles array no permission is granted.
     * The current user must have one of the passed roles.
     *
     * @returns {boolean} evaluated condition
     */
    private evaluateCondition() {
        return this._condition && this._roles && this.authServiceRS.isOneOfRoles(this._roles);
    }
}
