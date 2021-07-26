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

import {IComponentOptions, IController} from 'angular';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSBenutzer} from '../../../../models/TSBenutzer';
import {TSBenutzerNoDetails} from '../../../../models/TSBenutzerNoDetails';
import {LogFactory} from '../../logging/LogFactory';
import {AngularXBenutzerRS} from '../../service/angularXBenutzerRS.rest';

const LOG = LogFactory.createLog('UserselectController');

export class DvUserSelectConfig implements IComponentOptions {
    public restrict = 'E';
    public require: any = {smartTable: '?^stTable'};
    public scope = {};
    public controller = UserselectController;
    public controllerAs = 'vm';
    public bindings = {
        ngModel: '=',
        inputId: '@',
        dvUsersearch: '@',
        ngDisabled: '<',
        initialAll: '=',
        showSelectionAll: '=',
        onUserChanged: '&',
        selectedUser: '=?',
        schulamt: '<',
        sachbearbeiterGemeinde: '=',
        angular2: '='
        // initialAll -> tritt nur ein, wenn explizit  { initial-all="true" } geschrieben ist
    };
    public template = require('./dv-userselect.html');
}

/**
 * Direktive  der initial die smart table nach dem aktuell eingeloggtem user filtert
 */
export class UserselectController implements IController {

    public static $inject: string[] = ['BenutzerRS', 'AuthServiceRS'];

    private readonly unsubscribe$ = new Subject<void>();
    public selectedUser?: TSBenutzer;
    public smartTable: any;
    public userList: TSBenutzerNoDetails[];
    public dvUsersearch: string;
    public initialAll: boolean;
    public showSelectionAll: boolean;
    public valueChanged: () => void;           // Methode, die beim Klick auf die Combobox aufgerufen wird
    public onUserChanged: (user: any) => void; // Callback, welche aus obiger Methode aufgerufen werden soll
    public schulamt: string;
    public sachbearbeiterGemeinde: boolean;

    public constructor(
        private readonly benutzerRS: AngularXBenutzerRS,
        private readonly authServiceRS: AuthServiceRS,
    ) {

    }

    public $onInit(): void {
        this.updateUserList();
        if (!this.initialAll) { // tritt nur ein, wenn explizit  { initial-all="true" } geschrieben ist
            this.authServiceRS.principal$
                .pipe(takeUntil(this.unsubscribe$))
                .subscribe(
                    principal => {
                        this.selectedUser = principal;
                    },
                    err => LOG.error(err),
                );
        }
        // initial nach aktuell eingeloggtem filtern
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
        if (this.sachbearbeiterGemeinde) {
            this.benutzerRS.getAllBenutzerBgTsOrGemeinde().then(response => {
                this.userList = response;
            });
        } else if (this.schulamt) {
            this.benutzerRS.getAllBenutzerTsOrGemeinde().then(response => {
                this.userList = response;
            });
        } else {
            this.benutzerRS.getAllBenutzerBgOrGemeinde().then(response => {
                this.userList = response;
            });
        }
    }
}
