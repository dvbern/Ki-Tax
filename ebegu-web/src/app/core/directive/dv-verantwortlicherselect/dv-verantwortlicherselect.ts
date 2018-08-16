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
import TSGemeinde from '../../../../models/TSGemeinde';
import EbeguUtil from '../../../../utils/EbeguUtil';
import ITranslateService = angular.translate.ITranslateService;
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import TSUser from '../../../../models/TSUser';
import TSAntragDTO from '../../../../models/TSAntragDTO';
import UserRS from '../../service/userRS.rest';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import TSGesuch from '../../../../models/TSGesuch';
import GesuchModelManager from '../../../../gesuch/service/gesuchModelManager';


export class DvVerantwortlicherselect implements IDirective {
    restrict = 'E';
    require = {};
    scope = {};
    controller = VerantwortlicherselectController;
    controllerAs = 'vm';
    bindToController = {
        schulamt: '<',
        userList: '<',
    };
    template = require('./dv-verantwortlicherselect.html');

    static factory(): IDirectiveFactory {
        const directive = () => new DvVerantwortlicherselect();
        directive.$inject = [];
        return directive;
    }
}

/**
 * Direktive  der initial die smart table nach dem aktuell eingeloggtem user filtert
 */
export class VerantwortlicherselectController implements IController {

    static $inject: string[] = ['UserRS', 'AuthServiceRS', 'GesuchModelManager', '$translate'];

    userList: Array<TSUser>;
    TSRoleUtil = TSRoleUtil;
    schulamt: boolean;

    constructor(private readonly userRS: UserRS,
                private readonly authServiceRS: AuthServiceRS,
                private readonly gesuchModelManager: GesuchModelManager,
                private readonly $translate: ITranslateService) {
    }


    public getTitel(): string {
        return this.$translate.instant(EbeguUtil.getTitleVerantwortlicher(this.schulamt));
    }

    public getGesuch(): TSGesuch {
        return this.gesuchModelManager.getGesuch();
    }

    public getVerantwortlicherFullName(): string {
        if (this.getGesuch() && this.getGesuch().dossier) {
            if (this.schulamt && this.getGesuch().dossier.verantwortlicherTS) {
                return this.getGesuch().dossier.verantwortlicherTS.getFullName();
            }
            if (!this.schulamt && this.getGesuch().dossier.verantwortlicherBG) {
                return this.getGesuch().dossier.verantwortlicherBG.getFullName();
            }
        }
        return this.$translate.instant('NO_VERANTWORTLICHER_SELECTED');
    }

    /**
     * Sets the given user as the verantworlicher fuer den aktuellen Fall
     * @param verantwortlicher
     */
    public setVerantwortlicher(verantwortlicher: TSUser): void {
        this.setVerantwortlicherGesuchModelManager(verantwortlicher);
        this.setUserAsFallVerantwortlicherLocal(verantwortlicher);
    }

    private setVerantwortlicherGesuchModelManager(verantwortlicher: TSUser) {
        if (this.schulamt) {
            this.gesuchModelManager.setUserAsFallVerantwortlicherTS(verantwortlicher);
        } else {
            this.gesuchModelManager.setUserAsFallVerantwortlicherBG(verantwortlicher);
        }
    }

    public setUserAsFallVerantwortlicherLocal(user: TSUser) {
        if (user && this.getGesuch() && this.getGesuch().dossier) {
            if (this.schulamt) {
                this.getGesuch().dossier.verantwortlicherTS = user;
            } else {
                this.getGesuch().dossier.verantwortlicherBG = user;
            }
        }
    }

    /**
     *
     * @param user
     * @returns {boolean} true if the given user is already the verantwortlicherBG of the current fall
     */
    public isCurrentVerantwortlicher(user: TSUser): boolean {
        return (user && this.getFallVerantwortlicher() && this.getFallVerantwortlicher().username === user.username);
    }

    public getFallVerantwortlicher(): TSUser {
        if (this.schulamt) {
            return this.gesuchModelManager.getFallVerantwortlicherTS();
        } else {
            return this.gesuchModelManager.getFallVerantwortlicherBG();
        }
    }
}
