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

import {IComponentOptions, IController, IFilterService, IPromise, IWindowService} from 'angular';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../../../gesuch/service/gemeindeRS.rest';
import {
    getTSAntragStatusPendenzValues,
    getTSAntragStatusValuesByRole,
    TSAntragStatus,
} from '../../../../models/enums/TSAntragStatus';
import {getNormalizedTSAntragTypValues, TSAntragTyp} from '../../../../models/enums/TSAntragTyp';
import {getTSBetreuungsangebotTypValues, TSBetreuungsangebotTyp} from '../../../../models/enums/TSBetreuungsangebotTyp';
import TSAbstractAntragEntity from '../../../../models/TSAbstractAntragEntity';
import TSAntragDTO from '../../../../models/TSAntragDTO';
import TSAntragSearchresultDTO from '../../../../models/TSAntragSearchresultDTO';
import TSBenutzer from '../../../../models/TSBenutzer';
import TSGemeinde from '../../../../models/TSGemeinde';
import TSInstitution from '../../../../models/TSInstitution';
import EbeguUtil from '../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {LogFactory} from '../../logging/LogFactory';
import GesuchsperiodeRS from '../../service/gesuchsperiodeRS.rest';
import {InstitutionRS} from '../../service/institutionRS.rest';

const LOG = LogFactory.createLog('DVAntragListController');

export class DVAntragListConfig implements IComponentOptions {
    public transclude = false;

    public bindings = {
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
        pendenz: '=',
    };
    public template = require('./dv-antrag-list.html');
    public controller = DVAntragListController;
    public controllerAs = 'vm';
}

export class DVAntragListController implements IController {

    public static $inject: ReadonlyArray<string> = [
        '$filter',
        'InstitutionRS',
        'GesuchsperiodeRS',
        'AuthServiceRS',
        '$window',
        'GemeindeRS',
        'AuthLifeCycleService',
    ];

    public totalResultCount: number;
    public displayedCollection: Array<TSAntragDTO> = []; // Liste die im Gui angezeigt wird
    public pagination: any;
    public gesuchsperiodenList: Array<string>;
    public institutionenList: Array<TSInstitution>;
    public gemeindenList: Array<TSGemeinde>;

    public selectedBetreuungsangebotTyp: string;
    public selectedAntragTyp: string;
    public selectedAntragStatus: string;
    public selectedInstitution: TSInstitution;
    public selectedGesuchsperiode: string;
    public selectedFallNummer: string;
    public selectedGemeinde: TSGemeinde;
    public selectedFamilienName: string;
    public selectedKinder: string;
    public selectedAenderungsdatum: string;
    public selectedEingangsdatum: string;
    public selectedEingangsdatumSTV: string;
    public selectedVerantwortlicherBG: TSBenutzer;
    public selectedVerantwortlicherTS: TSBenutzer;
    public selectedDokumenteHochgeladen: string;
    public pendenz: boolean;

    public tableId: string;
    public tableTitle: string;
    public actionVisible: string;

    public addButtonText: string;
    public addButtonVisible: string = 'false';
    public onRemove: (pensumToRemove: any) => void;
    public onFilterChange: (changedTableState: any) => IPromise<any>;
    public onEdit: (pensumToEdit: any) => void;
    public onAdd: () => void;
    public readonly TSRoleUtil = TSRoleUtil;

    private readonly unsubscribe$ = new Subject<void>();

    public constructor(
        private readonly $filter: IFilterService,
        private readonly institutionRS: InstitutionRS,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly $window: IWindowService,
        private readonly gemeindeRS: GemeindeRS,
    ) {
    }

    public $onInit(): void {
        // statt diese Listen zu laden koenne man sie auch von aussen setzen
        this.updateInstitutionenList();
        this.updateGesuchsperiodenList();
        this.updateGemeindenList();

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

    public $onDestroy(): void {
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
        this.gemeindeRS.getGemeindenForPrincipal$()
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(gemeinden => {
                    this.gemeindenList = gemeinden;
                },
                err => LOG.error(err),
            );
    }

    public removeClicked(antragToRemove: TSAbstractAntragEntity): void {
        this.onRemove({antrag: antragToRemove});
    }

    public editClicked(antragToEdit: any, event: any): void {
        this.onEdit({antrag: antragToEdit, event});
    }

    public addClicked(): void {
        this.onAdd();
    }

    public readonly callServer = (tableFilterState: any) => {
        const pagination = tableFilterState.pagination;
        this.pagination = pagination;

        if (!this.onFilterChange || !angular.isFunction(this.onFilterChange)) {
            LOG.info('no callback function spcified for filtering');

            return;
        }

        this.onFilterChange({tableState: tableFilterState}).then((result: TSAntragSearchresultDTO) => {
            if (!result) {
                return;
            }

            pagination.totalItemCount = result.totalResultSize;
            pagination.numberOfPages = Math.ceil(result.totalResultSize / pagination.number);
            this.displayedCollection = [].concat(result.antragDTOs);
        });
    };

    public getAntragTypen(): Array<TSAntragTyp> {
        return getNormalizedTSAntragTypValues();
    }

    /**
     * Alle TSAntragStatus fuer das Filterdropdown
     */
    public getAntragStatus(): Array<TSAntragStatus> {
        return this.pendenz ?
            getTSAntragStatusPendenzValues(this.authServiceRS.getPrincipalRole()) :
            getTSAntragStatusValuesByRole(this.authServiceRS.getPrincipalRole());
    }

    /**
     * Alle Betreuungsangebot typen fuer das Filterdropdown
     */
    public getBetreuungsangebotTypen(): Array<TSBetreuungsangebotTyp> {
        return getTSBetreuungsangebotTypValues();
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
        if (Array.isArray(betreuungsangebotTypList)) {
            let prefix = '';
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

    public isAddButtonVisible(): boolean {
        return this.addButtonVisible === 'true';
    }

    public isActionsVisible(): boolean {
        return this.actionVisible === 'true';
    }

    /**
     * Provided there is a row with id antraegeHeadRow it will take this row to check how many
     * columns there are. Therefore this row cannot have any colspan inside any cell and any other
     * children but td or th
     */
    public getColumnsNumber(): number {
        const element = this.$window.document.getElementById('antraegeHeadRow');
        return element.childElementCount;
    }
}
