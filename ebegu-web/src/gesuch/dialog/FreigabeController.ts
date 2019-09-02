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

import BenutzerRS from '../../app/core/service/benutzerRS.rest';
import AuthServiceRS from '../../authentication/service/AuthServiceRS.rest';
import TSAntragDTO from '../../models/TSAntragDTO';
import TSBenutzer from '../../models/TSBenutzer';
import EbeguUtil from '../../utils/EbeguUtil';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import DossierRS from '../service/dossierRS.rest';
import GemeindeRS from '../service/gemeindeRS.rest';
import GesuchRS from '../service/gesuchRS.rest';
import IPromise = angular.IPromise;
import IDialogService = angular.material.IDialogService;

/**
 * Controller fuer das Freigabe Popup
 */
export class FreigabeController {

    public static $inject: string[] = [
        'docID',
        'errorMessage',
        'gesuch',
        '$mdDialog',
        'GesuchRS',
        'BenutzerRS',
        'AuthServiceRS',
        'GemeindeRS',
        'DossierRS'
    ];

    public selectedUserBG: string;
    public selectedUserTS: string;
    public userBGList: Array<TSBenutzer>;
    public userTSList: Array<TSBenutzer>;
    public fallNummer: string;
    public familie: string;
    public readonly TSRoleUtil = TSRoleUtil;

    public constructor(
        private readonly docID: string,
        private readonly errorMessage: string,
        private readonly gesuch: TSAntragDTO,
        private readonly $mdDialog: IDialogService,
        private readonly gesuchRS: GesuchRS,
        private readonly benutzerRS: BenutzerRS,
        private readonly authService: AuthServiceRS,
        private readonly gemeindeRS: GemeindeRS,
        private readonly dossierRS: DossierRS,
    ) {
        this.fallNummer = EbeguUtil.addZerosToFallNummer(gesuch.fallNummer);
        this.familie = gesuch.familienName;
        this.setVerantwortliche();
        this.updateUserList();
    }

    private setVerantwortliche(): void {
        // Verantwortlicher wird gemaess folgender Prioritaet festgestellt:
        // (1) Verantwortlicher des Vorjahresgesuchs
        // (2) Eingeloggter Benutzer (fuer jeweilige Amt-Verantwortung)
        // (3) Defaults aus Properties

        // Jugendamt
        if (this.gesuch.verantwortlicherBG && this.gesuch.verantwortlicherUsernameBG) {
            this.selectedUserBG = this.gesuch.verantwortlicherUsernameBG;
        } else if (this.authService.isOneOfRoles(this.TSRoleUtil.getSchulamtOnlyRoles())) {
            // Noch kein Verantwortlicher aus Vorjahr vorhanden
            this.dossierRS.findDossier(this.gesuch.dossierId).then(dossier => {
                this.gemeindeRS.getGemeindeStammdaten(dossier.gemeinde.id).then(stammdaten => {
                    this.selectedUserBG = stammdaten.defaultBenutzerBG.username;
                });
            });
        } else {
            this.selectedUserBG = this.authService.getPrincipal().username;
        }

        // Schulamt
        if (this.gesuch.verantwortlicherTS && this.gesuch.verantwortlicherUsernameTS) {
            this.selectedUserTS = this.gesuch.verantwortlicherUsernameTS;
        } else if (this.authService.isOneOfRoles(this.TSRoleUtil.getSchulamtOnlyRoles())) {
            // Noch kein Verantwortlicher aus Vorjahr vorhanden
            this.selectedUserTS = this.authService.getPrincipal().username;
        } else {
            this.dossierRS.findDossier(this.gesuch.dossierId).then(dossier => {
                this.gemeindeRS.getGemeindeStammdaten(dossier.gemeinde.id).then(stammdaten => {
                    this.selectedUserTS = stammdaten.defaultBenutzerTS.username;
                });
            });
        }
    }

    private updateUserList(): void {
        this.benutzerRS.getBenutzerJAorAdmin().then((response: any) => {
            this.userBGList = angular.copy(response);
        });
        this.benutzerRS.getBenutzerSCHorAdminSCH().then((response: any) => {
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
        return EbeguUtil.isNotNullOrUndefined(this.errorMessage);
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
