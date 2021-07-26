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

import {AngularXBenutzerRS} from '../../app/core/service/angularXBenutzerRS.rest';
import {AuthServiceRS} from '../../authentication/service/AuthServiceRS.rest';
import {TSAntragDTO} from '../../models/TSAntragDTO';
import {TSBenutzer} from '../../models/TSBenutzer';
import {TSDossier} from '../../models/TSDossier';
import {EbeguUtil} from '../../utils/EbeguUtil';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import {DossierRS} from '../service/dossierRS.rest';
import {GemeindeRS} from '../service/gemeindeRS.rest';
import {GesuchRS} from '../service/gesuchRS.rest';
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
        private readonly benutzerRS: AngularXBenutzerRS,
        private readonly authService: AuthServiceRS,
        private readonly gemeindeRS: GemeindeRS,
        private readonly dossierRS: DossierRS,
    ) {
        this.fallNummer = EbeguUtil.addZerosToFallNummer(gesuch.fallNummer);
        this.familie = gesuch.familienName;
        this.readDossier();
    }

    private readDossier(): void {
        // Als erstes das Dossier lesen, da wir dieses fuer weitere Aufrufe brauchen
        let dossierOfFreizugebenderAntrag: TSDossier;
        this.dossierRS.findDossier(this.gesuch.dossierId).then(dossier => {
            dossierOfFreizugebenderAntrag = dossier;
            this.updateUserList(dossierOfFreizugebenderAntrag);
        });
    }

    private updateUserList(dossier: TSDossier): void {
        this.benutzerRS.getBenutzerBgOrGemeindeForGemeinde(dossier.gemeinde.id).then((responseBG: any) => {
            this.userBGList = angular.copy(responseBG);
            this.benutzerRS.getBenutzerTsOrGemeindeForGemeinde(dossier.gemeinde.id).then((responseTS: any) => {
                this.userTSList = angular.copy(responseTS);
                this.setVerantwortliche(dossier);
            });
        });
    }

    private setVerantwortliche(dossier: TSDossier): void {
        // Verantwortlicher wird gemaess folgender Prioritaet festgestellt:
        // (1) Verantwortlicher des Vorjahresgesuchs
        // (2) Eingeloggter Benutzer (fuer jeweilige Amt-Verantwortung)
        // (3) Defaults aus Properties

        // Jugendamt
        const userVorjahrOrCurrentBG = this.getVerantwortlichenAusVorjahrOrCurrentBenutzer(
            this.gesuch.verantwortlicherUsernameBG, this.userBGList);
        if (EbeguUtil.isNotNullOrUndefined(userVorjahrOrCurrentBG)) {
            this.selectedUserBG = userVorjahrOrCurrentBG;
        } else {
            // Es gibt keinen Vorjahres-Verantwortlichen und der eingeloggte Benutzer ist nicht fuer BG berechtigt.
            // Wir suchen nach einem anderen Kandidaten
            this.gemeindeRS.getGemeindeStammdaten(dossier.gemeinde.id).then(stammdaten => {
                this.selectedUserBG = stammdaten.getDefaultBenutzerWithRoleBG().username;
            });
        }

        // Schulamt
        const userVorjahrOrCurrentTS = this.getVerantwortlichenAusVorjahrOrCurrentBenutzer(
            this.gesuch.verantwortlicherUsernameTS, this.userTSList);
        // tslint:disable-next-line:early-exit
        if (EbeguUtil.isNotNullOrUndefined(userVorjahrOrCurrentTS)) {
            this.selectedUserTS = userVorjahrOrCurrentTS;
        } else {
            // Es gibt keinen Vorjahres-Verantwortlichen und der eingeloggte Benutzer ist nicht fuer BG berechtigt.
            // Wir suchen nach einem anderen Kandidaten
            this.gemeindeRS.getGemeindeStammdaten(dossier.gemeinde.id).then(stammdaten => {
                this.selectedUserTS = stammdaten.getDefaultBenutzerWithRoleTS().username;
            });
        }
    }

    private getVerantwortlichenAusVorjahrOrCurrentBenutzer(
        usernameVorjahr: string, listOfBerechtigteUser: Array<TSBenutzer>
    ): string | undefined {
        if (usernameVorjahr) {
            // Falls schon ein Verantwortlicher aus dem Vorjahr gesetzt ist: Pruefen, ob dieser noch existiert
            // und die richtige Rolle hat Dazu muss er in der vorher gelesenen Liste der berechtigen Benutzer sein
            if (this.isUserInList(usernameVorjahr, listOfBerechtigteUser)) {
                return usernameVorjahr;
            }

        } else if (this.isUserInList(this.authService.getPrincipal().username, listOfBerechtigteUser)) {
            // Der eingeloggte Benutzer ist berechtigt fuer was wir suchen -> Wir nehmen diesen
            return this.authService.getPrincipal().username;
        }
        return undefined;
    }

    private isUserInList(username: string, list: Array<TSBenutzer>): boolean {
        for (const tsBenutzer of list) {
            if (tsBenutzer.username === username) {
                return true;
            }
        }
        return false;
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
