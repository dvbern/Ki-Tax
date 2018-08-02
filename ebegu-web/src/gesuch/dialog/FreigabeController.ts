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

import {ApplicationPropertyRS} from '../../admin/service/applicationPropertyRS.rest';
import AuthServiceRS from '../../authentication/service/AuthServiceRS.rest';
import UserRS from '../../core/service/userRS.rest';
import TSAntragDTO from '../../models/TSAntragDTO';
import TSUser from '../../models/TSUser';
import EbeguUtil from '../../utils/EbeguUtil';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import GesuchRS from '../service/gesuchRS.rest';
import IPromise = angular.IPromise;
import IDialogService = angular.material.IDialogService;
import ITranslateService = angular.translate.ITranslateService;

/**
 * Controller fuer das Freigabe Popup
 */
export class FreigabeController {

    static $inject: string[] = ['docID', '$mdDialog', 'GesuchRS', 'UserRS', 'AuthServiceRS',
        'EbeguUtil', 'CONSTANTS', '$translate', 'ApplicationPropertyRS'];

    private gesuch: TSAntragDTO;
    private selectedUserBG: string;
    private selectedUserTS: string;
    private userBGList: Array<TSUser>;
    private userTSList: Array<TSUser>;
    private fallNummer: string;
    private familie: string;
    private errorMessage: string;
    TSRoleUtil = TSRoleUtil;

    constructor(private docID: string, private $mdDialog: IDialogService, private gesuchRS: GesuchRS,
                private userRS: UserRS, private authService: AuthServiceRS, private ebeguUtil: EbeguUtil,
                CONSTANTS: any, private $translate: ITranslateService, private applicationPropertyRS: ApplicationPropertyRS) {

        gesuchRS.findGesuchForFreigabe(this.docID).then((response: TSAntragDTO) => {
            this.errorMessage = undefined; // just for safety replace old value
            if (response) {
                if (response.canBeFreigegeben()) {
                    this.gesuch = response;
                    this.fallNummer = EbeguUtil.addZerosToFallNummer(response.fallNummer);
                    this.familie = response.familienName;
                    this.setVerantwortliche();
                } else {
                    this.errorMessage = this.$translate.instant('FREIGABE_GESUCH_ALREADY_FREIGEGEBEN');
                }
            } else {
                this.errorMessage = this.$translate.instant('FREIGABE_GESUCH_NOT_FOUND');
            }
        }).catch(() => {
            this.cancel(); // close popup
        });

        this.updateUserList();

    }

    private setVerantwortliche() {
        // Verantwortlicher wird gemaess folgender Prioritaet festgestellt:
        // (1) Verantwortlicher des Vorjahresgesuchs
        // (2) Eingeloggter Benutzer (fuer jeweilige Amt-Verantwortung)
        // (3) Defaults aus Properties

        // Jugendamt
        if (this.gesuch.verantwortlicherBG && this.gesuch.verantwortlicherUsernameBG) {
            this.selectedUserBG = this.gesuch.verantwortlicherUsernameBG;
        } else {
            // Noch kein Verantwortlicher aus Vorjahr vorhanden
            if (this.authService.isOneOfRoles(this.TSRoleUtil.getSchulamtOnlyRoles())) {
                this.applicationPropertyRS.getByName('DEFAULT_VERANTWORTLICHER_BG').then(username => {
                    this.selectedUserBG = username.value;
                });
            } else {
                this.selectedUserBG = this.authService.getPrincipal().username;
            }
        }
        // Schulamt
        if (this.gesuch.verantwortlicherTS && this.gesuch.verantwortlicherUsernameTS) {
           this.selectedUserTS = this.gesuch.verantwortlicherUsernameTS;
        } else {
            // Noch kein Verantwortlicher aus Vorjahr vorhanden
            if (this.authService.isOneOfRoles(this.TSRoleUtil.getSchulamtOnlyRoles())) {
                this.selectedUserTS = this.authService.getPrincipal().username;
            } else {
                this.applicationPropertyRS.getByName('DEFAULT_VERANTWORTLICHER_TS').then(username => {
                    this.selectedUserTS = username.value;
                });
            }
        }
    }

    private updateUserList() {
        this.userRS.getBenutzerJAorAdmin().then((response: any) => {
            this.userBGList = angular.copy(response);
        });
        this.userRS.getBenutzerSCHorAdminSCH().then((response: any) => {
            this.userTSList = angular.copy(response);
        });
    }

    public isSchulamt(): boolean {
        return this.gesuch ? this.gesuch.hasAnySchulamtAngebot() : false;
    }

    public isJugendamt(): boolean {
        return this.gesuch ? this.gesuch.hasAnyJugendamtAngebot() : false;
    }

    public hasError(): boolean {
        return this.errorMessage != null;
    }

    public freigeben(): IPromise<any> {
        return this.gesuchRS.antragFreigeben(this.docID, this.selectedUserBG, this.selectedUserTS)
            .then(() => {
                return this.$mdDialog.hide();
            });
    }

    public cancel(): void {
        this.$mdDialog.cancel();
    }

}
