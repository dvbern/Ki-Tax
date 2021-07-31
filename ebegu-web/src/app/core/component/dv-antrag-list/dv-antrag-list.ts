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
import {EinstellungRS} from '../../../../admin/service/einstellungRS.rest';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {
    getTSAntragStatusPendenzValues,
    getTSAntragStatusValuesByRole,
    TSAntragStatus
} from '../../../../models/enums/TSAntragStatus';
import {getNormalizedTSAntragTypValues, TSAntragTyp} from '../../../../models/enums/TSAntragTyp';
import {
    getTSBetreuungsangebotTypValuesForMandant,
    TSBetreuungsangebotTyp
} from '../../../../models/enums/TSBetreuungsangebotTyp';
import {TSAbstractAntragEntity} from '../../../../models/TSAbstractAntragEntity';
import {TSAntragDTO} from '../../../../models/TSAntragDTO';
import {TSBenutzerNoDetails} from '../../../../models/TSBenutzerNoDetails';
import {TSGemeinde} from '../../../../models/TSGemeinde';
import {TSInstitution} from '../../../../models/TSInstitution';
import {TSPaginationResultDTO} from '../../../../models/TSPaginationResultDTO';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {LogFactory} from '../../logging/LogFactory';
import {GesuchsperiodeRS} from '../../service/gesuchsperiodeRS.rest';
import {InstitutionRS} from '../../service/institutionRS.rest';
import ITranslateService = angular.translate.ITranslateService;

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
        'EinstellungRS',
        '$translate'
    ];

    public totalResultCount: number;
    public displayedCollection: Array<TSAntragDTO> = []; // Liste die im Gui angezeigt wird
    public pagination: any;
    public gesuchsperiodenList: Array<string> = [];
    public institutionenList: Array<TSInstitution> = [];
    public gemeindenList: Array<TSGemeinde> = [];

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
    public selectedVerantwortlicherBG: TSBenutzerNoDetails;
    public selectedVerantwortlicherTS: TSBenutzerNoDetails;
    public selectedVerantwortlicherGemeinde: TSBenutzerNoDetails;
    public selectedDokumenteHochgeladen: string;
    public pendenz: boolean;
    public selectedInstitutionName: string;

    public tableId: string;
    public tableTitle: string;

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
        private readonly einstellungRS: EinstellungRS,
        private readonly $translate: ITranslateService
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

    public updateInstitutionFilter(): void {
        const inputElement = angular.element('#institutionen');
        this.setSelectedInstitutionName();
        inputElement.val(this.selectedInstitutionName).trigger('input');
    }

    public updateInstitutionenList(): void {
        this.institutionRS.getInstitutionenReadableForCurrentBenutzer().then(response => {
            this.institutionenList = response;
        });
    }

    public updateGesuchsperiodenList(): void {
        this.gesuchsperiodeRS.getAllGesuchsperioden().then(response => {
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

        this.onFilterChange({tableState: tableFilterState}).then((result: TSPaginationResultDTO<TSAntragDTO>) => {
            if (!result) {
                return;
            }

            pagination.totalItemCount = result.totalResultSize;
            pagination.numberOfPages = Math.ceil(result.totalResultSize / pagination.number);
            this.displayedCollection = [].concat(result.resultList);
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
        return getTSBetreuungsangebotTypValuesForMandant(this.isTagesschulangebotEnabled());
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

    /**
     * Provided there is a row with id antraegeHeadRow it will take this row to check how many
     * columns there are. Therefore this row cannot have any colspan inside any cell and any other
     * children but td or th
     */
    public getColumnsNumber(): number {
        const element = this.$window.document.getElementById('antraegeHeadRow');
        return element.childElementCount;
    }

    public isTagesschulangebotEnabled(): boolean {
        return this.authServiceRS.hasMandantAngebotTS();
    }

    public querySearch(query: string): Array<TSInstitution> {
        const searchString = query.toLocaleLowerCase();
        return this.institutionenList.filter(item => {
            return (item.name.toLocaleLowerCase().indexOf(searchString) > -1);
        });
    }

    public setSelectedInstitutionName(): void {
        this.selectedInstitutionName = this.selectedInstitution ? this.selectedInstitution.name : null;
    }

    public getAntragTypBezeichnung(antrag: TSAntragDTO): string {
        let bezeichnung = this.$translate.instant(antrag.antragTyp);
        if (antrag.laufnummer && antrag.laufnummer > 0) {
            bezeichnung = `${bezeichnung} ${antrag.laufnummer}`;
        }
        return bezeichnung;
    }

    public isPendenzGemeindeRolle(): boolean {
        return this.pendenz && this.authServiceRS.isOneOfRoles(this.TSRoleUtil.getGemeindeOnlyRoles());
    }

    public getVerantwortlicheBgAndTs(antrag: TSAntragDTO): string {
        const verantwortliche: string[] = [];
        if (EbeguUtil.isNotNullOrUndefined(antrag.verantwortlicherBG)) {
            verantwortliche.push(antrag.verantwortlicherBG);
        }
        if (EbeguUtil.isNotNullOrUndefined(antrag.verantwortlicherTS)) {
            verantwortliche.push(antrag.verantwortlicherTS);
        }
        return verantwortliche.join(', ');
    }
}
