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

import {StateService} from '@uirouter/core';
import {IComponentOptions, IController, IFilterService} from 'angular';
import GesuchsperiodeRS from '../../../app/core/service/gesuchsperiodeRS.rest';
import {InstitutionRS} from '../../../app/core/service/institutionRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {getTSAntragStatusValuesByRole, TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {getNormalizedTSAntragTypValues, TSAntragTyp} from '../../../models/enums/TSAntragTyp';
import {getTSBetreuungsangebotTypValues, TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import TSAbstractAntragDTO from '../../../models/TSAbstractAntragDTO';
import TSAntragDTO from '../../../models/TSAntragDTO';
import TSFallAntragDTO from '../../../models/TSFallAntragDTO';
import TSGemeinde from '../../../models/TSGemeinde';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import TSInstitution from '../../../models/TSInstitution';
import TSUser from '../../../models/TSUser';
import EbeguUtil from '../../../utils/EbeguUtil';

export class DVQuicksearchListConfig implements IComponentOptions {
    transclude = false;

    bindings = {
        antraege: '<',
        itemsByPage: '<',
        initialAll: '=',
        showSelectionAll: '=',
        totalResultCount: '<',
        onUserChanged: '&',
        tableId: '@',
        tableTitle: '<'
    };

    template = require('./dv-quicksearch-list.html');
    controller = DVQuicksearchListController;
    controllerAs = 'vm';
}

export class DVQuicksearchListController implements IController {

    static $inject: string[] = ['EbeguUtil', '$filter', 'InstitutionRS', 'GesuchsperiodeRS',
        '$state', 'CONSTANTS', 'AuthServiceRS', 'GemeindeRS'];

    antraege: Array<TSAntragDTO> = []; //muss hier gesuch haben damit Felder die wir anzeigen muessen da sind

    itemsByPage: number;
    initialAll: boolean;
    showSelectionAll: boolean;
    tableId: string;
    tableTitle: string;

    selectedVerantwortlicherBG: TSUser;
    selectedVerantwortlicherTS: TSUser;
    selectedEingangsdatum: string;
    selectedKinder: string;
    selectedFallNummer: string;
    selectedFamilienName: string;
    selectedBetreuungsangebotTyp: string;
    selectedAntragTyp: string;
    selectedAntragStatus: string;
    selectedInstitution: TSInstitution;
    selectedGesuchsperiode: string;
    selectedGemeinde: TSGemeinde;
    selectedDokumenteHochgeladen: string;

    institutionenList: Array<TSInstitution>;
    gesuchsperiodenList: Array<string>;
    gemeindenList: Array<TSGemeinde>;
    onUserChanged: (user: any) => void;

    constructor(private readonly ebeguUtil: EbeguUtil,
                private readonly $filter: IFilterService,
                private readonly institutionRS: InstitutionRS,
                private readonly gesuchsperiodeRS: GesuchsperiodeRS,
                private readonly $state: StateService,
                private readonly CONSTANTS: any,
                private readonly authServiceRS: AuthServiceRS,
                private readonly gemeindeRS: GemeindeRS,
    ) {
    }

    public userChanged(selectedUser: TSUser): void {
        this.onUserChanged({user: selectedUser});
    }

    public $onInit(): void {
        this.updateInstitutionenList();
        this.updateGesuchsperiodenList();
        this.updateGemeindenList();
    }

    public getAntragTypen(): Array<TSAntragTyp> {
        return getNormalizedTSAntragTypValues();
    }

    public getAntragStatus(): Array<TSAntragStatus> {
        return getTSAntragStatusValuesByRole(this.authServiceRS.getPrincipalRole());
    }

    public getBetreuungsangebotTypen(): Array<TSBetreuungsangebotTyp> {
        return getTSBetreuungsangebotTypValues();
    }

    public updateGesuchsperiodenList(): void {
        this.gesuchsperiodeRS.getAllNichtAbgeschlosseneGesuchsperioden().then((response: any) => {
            this.gesuchsperiodenList = [];
            response.forEach((gesuchsperiode: TSGesuchsperiode) => {
                this.gesuchsperiodenList.push(gesuchsperiode.gesuchsperiodeString);
            });
        });
    }

    public updateInstitutionenList(): void {
        this.institutionRS.getAllInstitutionen().then((response: any) => {
            this.institutionenList = angular.copy(response);
        });
    }

    private updateGemeindenList(): void {
        this.gemeindeRS.getGemeindenForPrincipal(this.authServiceRS.getPrincipal())
            .then(gemeinden => {
                this.gemeindenList = gemeinden;
            });
    }

    public getQuicksearchList(): Array<TSAntragDTO> {
        return this.antraege;
    }

    /**
     * Fallnummer muss 6-stellig dargestellt werden. Deshalb muessen so viele 0s am Anfang hinzugefuegt werden
     * bis die Fallnummer ein 6-stelliges String ist
     * @param fallnummer
     */
    public addZerosToFallnummer(fallnummer: number): string {
        return this.ebeguUtil.addZerosToNumber(fallnummer, this.CONSTANTS.FALLNUMMER_LENGTH);
    }

    public translateBetreuungsangebotTypList(betreuungsangebotTypList: Array<TSBetreuungsangebotTyp>): string {
        let result: string = '';
        if (betreuungsangebotTypList) {
            let prefix: string = '';
            if (betreuungsangebotTypList && Array.isArray(betreuungsangebotTypList)) {
                // tslint:disable-next-line:prefer-for-of
                for (let i = 0; i < betreuungsangebotTypList.length; i++) {
                    const tsBetreuungsangebotTyp = TSBetreuungsangebotTyp[betreuungsangebotTypList[i]];
                    result = result + prefix + this.$filter('translate')(tsBetreuungsangebotTyp).toString();
                    prefix = ', ';
                }
            }
        }
        return result;
    }

    public editAntrag(abstractAntrag: TSAbstractAntragDTO, event: any): void {
        if (abstractAntrag) {
            const isCtrlKeyPressed: boolean = (event && event.ctrlKey);
            if (abstractAntrag instanceof TSAntragDTO) {
                this.navigateToGesuch(abstractAntrag, isCtrlKeyPressed);
            } else if (abstractAntrag instanceof TSFallAntragDTO) {
                this.navigateToMitteilungen(isCtrlKeyPressed, abstractAntrag);
            }
        }
    }

    private navigateToMitteilungen(isCtrlKeyPressed: boolean, fallAntrag: TSFallAntragDTO) {
        if (isCtrlKeyPressed) {
            const url = this.$state.href('mitteilungen.view', {dossierId: fallAntrag.dossierId});
            window.open(url, '_blank');
        } else {
            this.$state.go('mitteilungen.view', {dossierId: fallAntrag.dossierId});
        }
    }

    private navigateToGesuch(antragDTO: TSAntragDTO, isCtrlKeyPressed: boolean) {
        if (antragDTO.antragId) {
            const navObj: any = {
                createNew: false,
                gesuchId: antragDTO.antragId,
                dossierId: antragDTO.dossierId
            };
            if (isCtrlKeyPressed) {
                const url = this.$state.href('gesuch.fallcreation', navObj);
                window.open(url, '_blank');
            } else {
                this.$state.go('gesuch.fallcreation', navObj);
            }
        }
    }

    private showOnlineGesuchIcon(row: TSAbstractAntragDTO): boolean {
        return row instanceof TSAntragDTO && row.hasBesitzer();
    }

    private showPapierGesuchIcon(row: TSAbstractAntragDTO): boolean {
        return row instanceof TSAntragDTO && !row.hasBesitzer();
    }
}



