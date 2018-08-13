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
import {IComponentOptions, ILogService, IPromise} from 'angular';
import {takeUntil} from 'rxjs/operators';
import {Subject} from 'rxjs/Subject';
import {AuthLifeCycleService} from '../../authentication/service/authLifeCycle.service';
import MitteilungRS from '../../app/core/service/mitteilungRS.rest';
import AuthServiceRS from '../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../gesuch/service/gemeindeRS.rest';
import {getAemterForFilter, TSAmt} from '../../models/enums/TSAmt';
import {TSAuthEvent} from '../../models/enums/TSAuthEvent';
import {getTSMitteilungsStatusForFilter, TSMitteilungStatus} from '../../models/enums/TSMitteilungStatus';
import TSGemeinde from '../../models/TSGemeinde';
import TSMitteilung from '../../models/TSMitteilung';
import TSMtteilungSearchresultDTO from '../../models/TSMitteilungSearchresultDTO';
import EbeguUtil from '../../utils/EbeguUtil';
import {TSRoleUtil} from '../../utils/TSRoleUtil';

let template = require('./posteingangView.html');
require('./posteingangView.less');

export class PosteingangViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = require('./posteingangView.html');
    controller = PosteingangViewController;
    controllerAs = 'vm';
}

export class PosteingangViewController {

    private readonly unsubscribe$ = new Subject<void>();


    displayedCollection: Array<TSMitteilung> = []; //Liste die im Gui angezeigt wird
    pagination: any = {};
    totalResultCount: string = '0';
    myTableFilterState: any; // Muss hier gespeichert werden, damit es fuer den Aufruf ab "Inkl.Erledigt"-Checkbox vorhanden ist

    itemsByPage: number = 20;
    numberOfPages: number = 1;
    selectedAmt: string;
    selectedMitteilungsstatus: TSMitteilungStatus;
    includeClosed: boolean = false;
    gemeindenList: Array<TSGemeinde> = [];

    static $inject: string[] = ['MitteilungRS', 'EbeguUtil', 'CONSTANTS', '$state', 'AuthServiceRS', 'GemeindeRS',
        '$log', 'AuthLifeCycleService'];

    constructor(private readonly mitteilungRS: MitteilungRS,
                private readonly ebeguUtil: EbeguUtil,
                private readonly CONSTANTS: any,
                private readonly $state: StateService,
                private readonly authServiceRS: AuthServiceRS,
                private readonly gemeindeRS: GemeindeRS,
                private readonly $log: ILogService,
                private readonly authLifeCycleService: AuthLifeCycleService) {

        this.authLifeCycleService.get$(TSAuthEvent.LOGIN_SUCCESS)
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(() => this.initViewModel());
    }

    initViewModel() {
        this.updateGemeindenList();
    }

    public addZerosToFallNummer(fallnummer: number): string {
        return EbeguUtil.addZerosToFallNummer(fallnummer);
    }

    private gotoMitteilung(mitteilung: TSMitteilung) {
        this.$state.go('mitteilungen.view', {
            dossierId: mitteilung.dossier.id,
            fallId: mitteilung.dossier.fall.id,
        });
    }

    isCurrentUserSchulamt(): boolean {
        const isUserSchulamt: boolean = this.authServiceRS.isOneOfRoles(TSRoleUtil.getSchulamtOnlyRoles());
        return isUserSchulamt;
    }

    private updateGemeindenList(): void {
        this.gemeindeRS.getGemeindenForPrincipal(this.authServiceRS.getPrincipal())
            .then(gemeinden => {
                this.gemeindenList = gemeinden;
            });
    }

    getAemter(): Array<TSAmt> {
        return getAemterForFilter();
    }

    getMitteilungsStatus(): Array<TSMitteilungStatus> {
        return getTSMitteilungsStatusForFilter();
    }

    public clickedIncludeClosed(): void {
        this.passFilterToServer(this.myTableFilterState);
    }

    public passFilterToServer = (tableFilterState: any): IPromise<void> => {
        this.pagination = tableFilterState.pagination;
        this.myTableFilterState = tableFilterState;

        return this.mitteilungRS.searchMitteilungen(tableFilterState,
            this.includeClosed).then((result: TSMtteilungSearchresultDTO) => {
            this.setResult(result);
        });
    };

    private setResult(result: TSMtteilungSearchresultDTO): void {
        if (result) {
            this.pagination.totalItemCount = result.totalResultSize;
            this.pagination.numberOfPages = Math.ceil(result.totalResultSize / this.pagination.number);
            this.displayedCollection = [].concat(result.mitteilungen);
            this.totalResultCount = result.totalResultSize ? result.totalResultSize.toString() : '0';
        }
    }
}
