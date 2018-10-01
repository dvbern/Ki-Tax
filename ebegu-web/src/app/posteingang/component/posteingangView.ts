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
import {IComponentOptions, IController, IPromise} from 'angular';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {getAemterForFilter, TSAmt} from '../../../models/enums/TSAmt';
import {getTSMitteilungsStatusForFilter, TSMitteilungStatus} from '../../../models/enums/TSMitteilungStatus';
import TSGemeinde from '../../../models/TSGemeinde';
import TSMitteilung from '../../../models/TSMitteilung';
import TSMtteilungSearchresultDTO from '../../../models/TSMitteilungSearchresultDTO';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {LogFactory} from '../../core/logging/LogFactory';
import MitteilungRS from '../../core/service/mitteilungRS.rest';

const LOG = LogFactory.createLog('PosteingangViewController');

export class PosteingangViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = require('./posteingangView.html');
    controller = PosteingangViewController;
    controllerAs = 'vm';
}

export class PosteingangViewController implements IController {

    static $inject: string[] = ['MitteilungRS', 'EbeguUtil', 'CONSTANTS', '$state', 'AuthServiceRS', 'GemeindeRS'];

    private readonly unsubscribe$ = new Subject<void>();

    // Liste die im Gui angezeigt wird
    displayedCollection: Array<TSMitteilung> = [];
    pagination: any = {};
    totalResultCount: string = '0';
    // Muss hier gespeichert werden, damit es fuer den Aufruf ab "Inkl.Erledigt"-Checkbox vorhanden ist
    myTableFilterState: any;

    itemsByPage: number = 20;
    numberOfPages: number = 1;
    selectedAmt: string;
    selectedMitteilungsstatus: TSMitteilungStatus;
    includeClosed: boolean = false;
    gemeindenList: Array<TSGemeinde> = [];

    constructor(private readonly mitteilungRS: MitteilungRS,
                private readonly ebeguUtil: EbeguUtil,
                private readonly CONSTANTS: any,
                private readonly $state: StateService,
                private readonly authServiceRS: AuthServiceRS,
                private readonly gemeindeRS: GemeindeRS) {

        this.updateGemeindenList();
    }

    public $onDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
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
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getSchulamtOnlyRoles());
    }

    private updateGemeindenList(): void {
        this.gemeindeRS.getGemeindenForPrincipal$()
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(
                gemeinden => {
                    this.gemeindenList = gemeinden;
                },
                err => LOG.error(err)
            );
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
