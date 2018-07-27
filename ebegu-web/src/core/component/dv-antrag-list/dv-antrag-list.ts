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

import {IComponentOptions, IFilterService, ILogService, IOnDestroy, IOnInit, IPromise} from 'angular';
import {takeUntil} from 'rxjs/operators';
import {Subject} from 'rxjs/Subject';
import {AuthLifeCycleService} from '../../../authentication/service/authLifeCycle.service';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {
    getTSAntragStatusPendenzValues,
    getTSAntragStatusValuesByRole,
    TSAntragStatus
} from '../../../models/enums/TSAntragStatus';
import {getNormalizedTSAntragTypValues, TSAntragTyp} from '../../../models/enums/TSAntragTyp';
import {TSAuthEvent} from '../../../models/enums/TSAuthEvent';
import {getTSBetreuungsangebotTypValues, TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import TSAbstractAntragEntity from '../../../models/TSAbstractAntragEntity';
import TSAntragDTO from '../../../models/TSAntragDTO';
import TSAntragSearchresultDTO from '../../../models/TSAntragSearchresultDTO';
import TSGemeinde from '../../../models/TSGemeinde';
import TSInstitution from '../../../models/TSInstitution';
import TSUser from '../../../models/TSUser';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import GesuchsperiodeRS from '../../service/gesuchsperiodeRS.rest';
import {InstitutionRS} from '../../service/institutionRS.rest';

let template = require('./dv-antrag-list.html');
require('./dv-antrag-list.less');

export class DVAntragListConfig implements IComponentOptions {
    transclude = false;

    bindings: any = {
        onRemove: '&',
        onAdd: '&',
        onEdit: '&',
        onFilterChange: '&',
        totalResultCount: '<',
        tableId: '@',
        tableTitle: '@',
        actionVisible: '@',
        addButtonVisible: '@',
        addButtonText: '@',
        pendenz: '='
    };
    template = template;
    controller = DVAntragListController;
    controllerAs = 'vm';
}

export class DVAntragListController implements IOnInit, IOnDestroy {

    private readonly unsubscribe$ = new Subject<void>();
    totalResultCount: number;
    displayedCollection: Array<TSAntragDTO> = []; //Liste die im Gui angezeigt wird
    pagination: any;
    gesuchsperiodenList: Array<string>;
    institutionenList: Array<TSInstitution>;
    gemeindenList: Array<TSGemeinde>;

    selectedBetreuungsangebotTyp: string;
    selectedAntragTyp: string;
    selectedAntragStatus: string;
    selectedInstitution: TSInstitution;
    selectedGesuchsperiode: string;
    selectedFallNummer: string;
    selectedGemeinde: TSGemeinde;
    selectedFamilienName: string;
    selectedKinder: string;
    selectedAenderungsdatum: string;
    selectedEingangsdatum: string;
    selectedEingangsdatumSTV: string;
    selectedVerantwortlicherBG: TSUser;
    selectedVerantwortlicherTS: TSUser;
    selectedDokumenteHochgeladen: string;
    pendenz: boolean;

    tableId: string;
    tableTitle: string;
    actionVisible: string;

    addButtonText: string;
    addButtonVisible: string = 'false';
    onRemove: (pensumToRemove: any) => void;
    onFilterChange: (changedTableState: any) => IPromise<any>;
    onEdit: (pensumToEdit: any) => void;
    onAdd: () => void;
    TSRoleUtil: any;

    static $inject: ReadonlyArray<string> = ['EbeguUtil', '$filter', '$log', 'InstitutionRS', 'GesuchsperiodeRS', 'CONSTANTS',
        'AuthServiceRS', '$window', 'GemeindeRS', 'AuthLifeCycleService'];

    constructor(private ebeguUtil: EbeguUtil, private $filter: IFilterService, private $log: ILogService,
                private institutionRS: InstitutionRS, private gesuchsperiodeRS: GesuchsperiodeRS,
                private CONSTANTS: any, private authServiceRS: AuthServiceRS, private $window: ng.IWindowService,
                private gemeindeRS: GemeindeRS, private authLifeCycleService: AuthLifeCycleService) {

        this.TSRoleUtil = TSRoleUtil;
        this.authLifeCycleService.get$(TSAuthEvent.LOGIN_SUCCESS)
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(() => this.initViewModel());
    }

    private initViewModel() {
        //statt diese Listen zu laden koenne man sie auch von aussen setzen
        this.updateInstitutionenList();
        this.updateGesuchsperiodenList();
        this.updateGemeindenList();
    }

    $onInit() {
        if (!this.addButtonText) {
            this.addButtonText = 'add item';
        }
        if (this.pendenz === null || this.pendenz === undefined) {
            this.pendenz = false;
        }
        if (this.addButtonVisible === undefined) {
            this.addButtonVisible = 'false';
        }
    }

    $onDestroy() {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    public updateInstitutionenList(): void {
        this.institutionRS.getInstitutionenForCurrentBenutzer().then(response => {
            this.institutionenList = angular.copy(response);
        });
    }

    public updateGesuchsperiodenList(): void {
        this.gesuchsperiodeRS.getAllGesuchsperioden().then(response => {
            this.gesuchsperiodenList = [];
            response.forEach(gesuchsperiode => {
                this.gesuchsperiodenList.push(gesuchsperiode.gesuchsperiodeString);
            });
        });
    }

    private updateGemeindenList(): void {
        this.gemeindeRS.getGemeindenForPrincipal(this.authServiceRS.getPrincipal())
            .then(gemeinden => { this.gemeindenList = gemeinden; });
    }

    removeClicked(antragToRemove: TSAbstractAntragEntity) {
        this.onRemove({antrag: antragToRemove});
    }

    editClicked(antragToEdit: any, event: any) {
        this.onEdit({antrag: antragToEdit, event: event});
    }

    addClicked() {
        this.onAdd();
    }

    private callServer = (tableFilterState: any) => {
        let pagination = tableFilterState.pagination;
        this.pagination = pagination;

        // this.displaydAntraege = this.antraege;

        if (this.onFilterChange && angular.isFunction(this.onFilterChange)) {
            this.onFilterChange({tableState: tableFilterState}).then((result: TSAntragSearchresultDTO) => {
                // this.pagination.totalItemCount = result.totalResultSize;
                if (result) {
                    pagination.totalItemCount = result.totalResultSize;
                    pagination.numberOfPages = Math.ceil(result.totalResultSize / pagination.number);
                    this.displayedCollection = [].concat(result.antragDTOs);
                }
            });
        } else {
            this.$log.info('no callback function spcified for filtering');
        }
    }

    public getAntragTypen(): Array<TSAntragTyp> {
        return getNormalizedTSAntragTypValues();
    }

    /**
     * Alle TSAntragStatus fuer das Filterdropdown
     * @returns {Array<TSAntragStatus>}
     */
    public getAntragStatus(): Array<TSAntragStatus> {
        if (this.pendenz) {
            return getTSAntragStatusPendenzValues(this.authServiceRS.getPrincipalRole());
        } else {
            return getTSAntragStatusValuesByRole(this.authServiceRS.getPrincipalRole());
        }
    }

    /**
     * Alle Betreuungsangebot typen fuer das Filterdropdown
     * @returns {Array<TSBetreuungsangebotTyp>}
     */
    public getBetreuungsangebotTypen(): Array<TSBetreuungsangebotTyp> {
        return getTSBetreuungsangebotTypValues();
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
                for (let i = 0; i < betreuungsangebotTypList.length; i++) {
                    let tsBetreuungsangebotTyp = TSBetreuungsangebotTyp[betreuungsangebotTypList[i]];
                    result = result + prefix + this.$filter('translate')(tsBetreuungsangebotTyp).toString();
                    prefix = ', ';
                }
            }
        }
        return result;
    }

    public isAddButtonVisible(): boolean {
        return this.addButtonVisible === 'true';
    }

    public isActionsVisible() {
        return this.actionVisible === 'true';
    }

    /**
     * Provided there is a row with id antraegeHeadRow it will take this row to check how many
     * columns there are. Therefore this row cannot have any colspan inside any cell and any other
     * children but td or th
     */
    public getColumnsNumber(): number {
        let element = this.$window.document.getElementById('antraegeHeadRow');
        return element.childElementCount;
    }
}



