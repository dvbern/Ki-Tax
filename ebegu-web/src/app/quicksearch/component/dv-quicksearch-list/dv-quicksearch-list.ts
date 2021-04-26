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
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {getTSAntragStatusValuesByRole, TSAntragStatus} from '../../../../models/enums/TSAntragStatus';
import {getNormalizedTSAntragTypValues, TSAntragTyp} from '../../../../models/enums/TSAntragTyp';
import {getTSBetreuungsangebotTypValuesForMandant, TSBetreuungsangebotTyp} from '../../../../models/enums/TSBetreuungsangebotTyp';
import {TSAbstractAntragDTO} from '../../../../models/TSAbstractAntragDTO';
import {TSAntragDTO} from '../../../../models/TSAntragDTO';
import {TSBenutzerNoDetails} from '../../../../models/TSBenutzerNoDetails';
import {TSFallAntragDTO} from '../../../../models/TSFallAntragDTO';
import {TSGemeinde} from '../../../../models/TSGemeinde';
import {TSGesuchsperiode} from '../../../../models/TSGesuchsperiode';
import {TSInstitution} from '../../../../models/TSInstitution';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {LogFactory} from '../../../core/logging/LogFactory';
import {GesuchsperiodeRS} from '../../../core/service/gesuchsperiodeRS.rest';
import {InstitutionRS} from '../../../core/service/institutionRS.rest';

const LOG = LogFactory.createLog('DVQuicksearchListController');

export class DVQuicksearchListConfig implements IComponentOptions {
    public transclude = false;

    public bindings = {
        antraege: '<',
        itemsByPage: '<',
        initialAll: '=',
        showSelectionAll: '=',
        totalResultCount: '<',
        onUserChanged: '&',
        tableId: '@',
        tableTitle: '<',
    };

    public template = require('./dv-quicksearch-list.html');
    public controller = DVQuicksearchListController;
    public controllerAs = 'vm';
}

export class DVQuicksearchListController implements IController {

    public static $inject: string[] = [
        '$filter', 'InstitutionRS', 'GesuchsperiodeRS',
        '$state', 'AuthServiceRS', 'GemeindeRS'
    ];

    public antraege: Array<TSAntragDTO> = []; // muss hier gesuch haben damit Felder die wir anzeigen muessen da sind

    public itemsByPage: number;
    public initialAll: boolean;
    public showSelectionAll: boolean;
    public tableId: string;
    public tableTitle: string;

    public selectedVerantwortlicherBG: TSBenutzerNoDetails;
    public selectedVerantwortlicherTS: TSBenutzerNoDetails;
    public selectedEingangsdatum: string;
    public selectedKinder: string;
    public selectedFallNummer: string;
    public selectedFamilienName: string;
    public selectedBetreuungsangebotTyp: string;
    public selectedAntragTyp: string;
    public selectedAntragStatus: string;
    public selectedInstitution: TSInstitution;
    public selectedGesuchsperiode: string;
    public selectedGemeinde: TSGemeinde;
    public selectedDokumenteHochgeladen: string;

    public institutionenList: Array<TSInstitution>;
    public gesuchsperiodenList: Array<string>;
    public gemeindenList: Array<TSGemeinde>;
    public onUserChanged: (user: any) => void;

    private readonly unsubscribe$ = new Subject<void>();

    public constructor(
        private readonly $filter: IFilterService,
        private readonly institutionRS: InstitutionRS,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly $state: StateService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly gemeindeRS: GemeindeRS,
    ) {
    }

    public userChanged(selectedUser: TSBenutzerNoDetails): void {
        this.onUserChanged({user: selectedUser});
    }

    public $onInit(): void {
        this.updateInstitutionenList();
        this.updateGesuchsperiodenList();
        this.updateGemeindenList();
    }

    public $onDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    public getAntragTypen(): Array<TSAntragTyp> {
        return getNormalizedTSAntragTypValues();
    }

    public getAntragStatus(): Array<TSAntragStatus> {
        return getTSAntragStatusValuesByRole(this.authServiceRS.getPrincipalRole());
    }

    public getBetreuungsangebotTypen(): Array<TSBetreuungsangebotTyp> {
        return getTSBetreuungsangebotTypValuesForMandant(this.isTagesschulangebotEnabled());
    }

    public updateGesuchsperiodenList(): void {
        this.gesuchsperiodeRS.getAllAktivUndInaktivGesuchsperioden().then((response: any) => {
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
        this.gemeindeRS.getGemeindenForPrincipal$()
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(gemeinden => {
                    this.gemeindenList = gemeinden;
                },
                err => LOG.error(err),
            );
    }

    public getQuicksearchList(): Array<TSAntragDTO> {
        return this.antraege;
    }

    /**
     * Fallnummer muss 6-stellig dargestellt werden. Deshalb muessen so viele 0s am Anfang hinzugefuegt werden
     * bis die Fallnummer ein 6-stelliges String ist
     */
    public addZerosToFallnummer(fallnummer: number): string {
        return EbeguUtil.addZerosToFallNummer(fallnummer);
    }

    public translateBetreuungsangebotTypList(betreuungsangebotTypList: Array<TSBetreuungsangebotTyp>): string {
        let result = '';
        if (betreuungsangebotTypList) {
            let prefix = '';
            if (Array.isArray(betreuungsangebotTypList)) {
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
        if (!abstractAntrag) {
            return;
        }

        const isCtrlKeyPressed: boolean = (event && event.ctrlKey);
        if (abstractAntrag instanceof TSAntragDTO) {
            this.navigateToGesuch(abstractAntrag, isCtrlKeyPressed);
        } else if (abstractAntrag instanceof TSFallAntragDTO) {
            this.navigateToMitteilungen(isCtrlKeyPressed, abstractAntrag);
        }
    }

    private navigateToMitteilungen(isCtrlKeyPressed: boolean, fallAntrag: TSFallAntragDTO): void {
        if (isCtrlKeyPressed) {
            const url = this.$state.href('mitteilungen.view', {dossierId: fallAntrag.dossierId});
            window.open(url, '_blank');
        } else {
            this.$state.go('mitteilungen.view', {
                dossierId: fallAntrag.dossierId,
                fallId: fallAntrag.fallId,
            });
        }
    }

    private navigateToGesuch(antragDTO: TSAntragDTO, isCtrlKeyPressed: boolean): void {
        if (!antragDTO.antragId) {
            return;
        }
        const navObj: any = {
            gesuchId: antragDTO.antragId,
            dossierId: antragDTO.dossierId,
        };
        if (isCtrlKeyPressed) {
            const url = this.$state.href('gesuch.fallcreation', navObj);
            window.open(url, '_blank');
        } else {
            this.$state.go('gesuch.fallcreation', navObj);
        }
    }

    public showOnlineGesuchIcon(row: TSAbstractAntragDTO): boolean {
        return row instanceof TSAntragDTO && row.hasBesitzer();
    }

    public showPapierGesuchIcon(row: TSAbstractAntragDTO): boolean {
        return row instanceof TSAntragDTO && !row.hasBesitzer()  && !row.isSozialdienst;
    }

    public showSozialdienstGesuchIcon(row: TSAbstractAntragDTO): boolean {
        return row instanceof TSAntragDTO && row.isSozialdienst;
    }

    public isTagesschulangebotEnabled(): boolean {
        return this.authServiceRS.hasMandantAngebotTS();
    }
}
