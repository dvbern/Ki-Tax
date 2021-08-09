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
import {GesuchModelManager} from '../../../../gesuch/service/gesuchModelManager';
import {TSBenutzer} from '../../../../models/TSBenutzer';
import {TSBenutzerNoDetails} from '../../../../models/TSBenutzerNoDetails';
import {TSGesuch} from '../../../../models/TSGesuch';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {BenutzerRSX} from '../../service/benutzerRSX.rest';
import ITranslateService = angular.translate.ITranslateService;

export class DvVerantwortlicherselect implements IDirective {
    public restrict = 'E';
    public scope = {};
    public controller = VerantwortlicherselectController;
    public controllerAs = 'vm';
    public bindToController = {
        isSchulamt: '<',
        gemeindeId: '<',
    };
    public template = require('./dv-verantwortlicherselect.html');

    public static factory(): IDirectiveFactory {
        const directive = () => new DvVerantwortlicherselect();
        // @ts-ignore
        directive.$inject = [];
        return directive;
    }
}

export class VerantwortlicherselectController implements IController {

    public static $inject: string[] = ['BenutzerRS', 'GesuchModelManager', '$translate'];

    public readonly TSRoleUtil = TSRoleUtil;
    public isSchulamt: boolean;
    public gemeindeId: string;

    public userList: Array<TSBenutzerNoDetails>;

    public constructor(
        private readonly benutzerRS: BenutzerRSX,
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly $translate: ITranslateService,
    ) {
    }

    public $onChanges(changes: any): void {
        if (changes.gemeindeId) {
            this.updateUserList();
        }
    }

    public getTitel(): string {
        if (!this.gesuchModelManager.isTagesschulangebotEnabled()) {
            return this.$translate.instant('VERANTWORTLICHER');
        }
        return this.$translate.instant(this.isSchulamt ? 'VERANTWORTLICHER_SCHULAMT' : 'VERANTWORTLICHER_JUGENDAMT');
    }

    public getGesuch(): TSGesuch {
        return this.gesuchModelManager.getGesuch();
    }

    public getVerantwortlicherFullName(): string {
        if (this.getGesuch() && this.getGesuch().dossier) {
            if (this.isSchulamt && this.getGesuch().dossier.verantwortlicherTS) {
                return this.getGesuch().dossier.verantwortlicherTS.getFullName();
            }
            if (!this.isSchulamt && this.getGesuch().dossier.verantwortlicherBG) {
                return this.getGesuch().dossier.verantwortlicherBG.getFullName();
            }
        }
        return this.$translate.instant('NO_VERANTWORTLICHER_SELECTED');
    }

    /**
     * Sets the given user as the verantworlicher fuer den aktuellen Fall
     */
    public setVerantwortlicher(verantwortlicher: TSBenutzerNoDetails): void {
        this.setVerantwortlicherGesuchModelManager(verantwortlicher);
        this.setUserAsFallVerantwortlicherLocal(verantwortlicher);
    }

    private setVerantwortlicherGesuchModelManager(verantwortlicher: TSBenutzerNoDetails): void {
        if (this.isSchulamt) {
            this.gesuchModelManager.setUserAsFallVerantwortlicherTS(verantwortlicher);
        } else {
            this.gesuchModelManager.setUserAsFallVerantwortlicherBG(verantwortlicher);
        }
    }

    public setUserAsFallVerantwortlicherLocal(user: TSBenutzerNoDetails): void {
        if (!(user && this.getGesuch() && this.getGesuch().dossier)) {
            return;
        }

        if (this.isSchulamt) {
            this.getGesuch().dossier.verantwortlicherTS = user;
        } else {
            this.getGesuch().dossier.verantwortlicherBG = user;
        }
    }

    /**
     * @returns true if the given user is already the verantwortlicherBG of the current fall
     */
    public isCurrentVerantwortlicher(user: TSBenutzer): boolean {
        return (user && this.getFallVerantwortlicher() && this.getFallVerantwortlicher().username === user.username);
    }

    public getFallVerantwortlicher(): TSBenutzerNoDetails {
        return this.isSchulamt ?
            this.gesuchModelManager.getFallVerantwortlicherTS() :
            this.gesuchModelManager.getFallVerantwortlicherBG();
    }

    private updateUserList(): void {
        if (!this.gemeindeId) {
            this.userList = [];

            return;
        }

        if (this.isSchulamt) {
            this.updateSchulamtUserList();
        } else {
            this.updateJugendAmtUserList();
        }
    }

    private updateSchulamtUserList(): void {
        this.benutzerRS.getBenutzerTsOrGemeindeForGemeinde(this.gemeindeId).then(response => {
            this.userList = this.sortUsers(this.filterUsers(response, this.gemeindeId));
        });
    }

    private updateJugendAmtUserList(): void {
        this.benutzerRS.getBenutzerBgOrGemeindeForGemeinde(this.gemeindeId).then(response => {
            this.userList = this.sortUsers(this.filterUsers(response, this.gemeindeId));
        });
    }

    private sortUsers(userList: Array<TSBenutzerNoDetails>): Array<TSBenutzerNoDetails> {
        return userList.sort((a, b) => a.getFullName().localeCompare(b.getFullName()));
    }

    /**
     *  Filters out users that have no berechtigung on the current gemeinde
     */
    private filterUsers(userList: Array<TSBenutzerNoDetails>, gemeindeId: string): Array<TSBenutzerNoDetails> {
        return userList.filter(user => user.gemeindeIds
            .some(id => id === gemeindeId));
    }
}
