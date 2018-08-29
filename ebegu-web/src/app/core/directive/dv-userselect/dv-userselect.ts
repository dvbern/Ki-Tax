/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

import {IController, IDirective, IDirectiveFactory} from 'angular';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import TSUser from '../../../../models/TSUser';
import {LogFactory} from '../../logging/LogFactory';
import UserRS from '../../service/userRS.rest';

const LOG = LogFactory.createLog('UserselectController');

export class DVUserselect implements IDirective {
    restrict = 'E';
    require: any = {smartTable: '?^stTable'};
    scope = {};
    controller = UserselectController;
    controllerAs = 'vm';
    bindToController = {
        ngModel: '=',
        inputId: '@',
        dvUsersearch: '@',
        ngDisabled: '<',
        initialAll: '=',
        showSelectionAll: '=',
        onUserChanged: '&',
        selectedUser: '=?',
        schulamt: '<'
        //initialAll -> tritt nur ein, wenn explizit  { initial-all="true" } geschrieben ist
    };
    template = require('./dv-userselect.html');

    static factory(): IDirectiveFactory {
        const directive = () => new DVUserselect();
        directive.$inject = [];
        return directive;
    }
}

/**
 * Direktive  der initial die smart table nach dem aktuell eingeloggtem user filtert
 */
export class UserselectController implements IController {

    static $inject: string[] = ['UserRS', 'AuthServiceRS'];

    private readonly unsubscribe$ = new Subject<void>();
    selectedUser?: TSUser;
    smartTable: any;
    userList: TSUser[];
    dvUsersearch: string;
    initialAll: boolean;
    showSelectionAll: boolean;
    valueChanged: () => void;           // Methode, die beim Klick auf die Combobox aufgerufen wird
    onUserChanged: (user: any) => void; // Callback, welche aus obiger Methode aufgerufen werden soll
    schulamt: string;

    constructor(private readonly userRS: UserRS,
                private readonly authServiceRS: AuthServiceRS) {

    }

    public $onInit(): void {
        this.updateUserList();
        if (!this.initialAll) { //tritt nur ein, wenn explizit  { initial-all="true" } geschrieben ist
            this.authServiceRS.principal$
                .pipe(takeUntil(this.unsubscribe$))
                .subscribe(
                    principal => {
                        this.selectedUser = principal;
                    },
                    err => LOG.error(err)
                );
        }
        //initial nach aktuell eingeloggtem filtern
        if (this.smartTable && !this.initialAll && this.selectedUser) {
            this.smartTable.search(this.selectedUser.getFullName(), this.dvUsersearch);
        }
        this.valueChanged = () => {
            this.onUserChanged({user: this.selectedUser});
        };
    }

    public $onDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    private updateUserList(): void {
        if (this.schulamt) {
            this.userRS.getBenutzerSCHorAdminSCH().then(response => {
                this.userList = response;
            });
        } else {
            this.userRS.getBenutzerJAorAdmin().then(response => {
                this.userList = response;
            });
        }
    }
}
