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

import {IComponentOptions} from 'angular';
import {takeUntil} from 'rxjs/operators';
import {Subject} from 'rxjs';
import {AuthLifeCycleService} from '../../../authentication/service/authLifeCycle.service';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSAuthEvent} from '../../../models/enums/TSAuthEvent';
import TSGemeinde from '../../../models/TSGemeinde';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import TSInstitution from '../../../models/TSInstitution';
import {InstitutionRS} from '../../../core/service/institutionRS.rest';
import GesuchsperiodeRS from '../../../core/service/gesuchsperiodeRS.rest';
import GesuchModelManager from '../../../gesuch/service/gesuchModelManager';
import {StateService} from '@uirouter/core';
import BerechnungsManager from '../../../gesuch/service/berechnungsManager';
import PendenzBetreuungenRS from '../../service/PendenzBetreuungenRS.rest';
import {InstitutionStammdatenRS} from '../../../core/service/institutionStammdatenRS.rest';
import TSBetreuungsnummerParts from '../../../models/dto/TSBetreuungsnummerParts';
import TSPendenzBetreuung from '../../../models/TSPendenzBetreuung';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';

const template = require('./pendenzenBetreuungenListView.html');
require('./pendenzenBetreuungenListView.less');

export class PendenzenBetreuungenListViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = PendenzenBetreuungenListViewController;
    controllerAs = 'vm';
}

export class PendenzenBetreuungenListViewController {

    static $inject: string[] = ['PendenzBetreuungenRS', 'EbeguUtil', 'InstitutionRS', 'InstitutionStammdatenRS',
        'GesuchsperiodeRS', 'GesuchModelManager', 'BerechnungsManager', '$state', 'GemeindeRS', 'AuthServiceRS',
        'AuthLifeCycleService'];

    private readonly unsubscribe$ = new Subject<void>();
    private pendenzenList: Array<TSPendenzBetreuung>;
    selectedBetreuungsangebotTyp: string;
    selectedInstitution: string;
    selectedGesuchsperiode: string;
    selectedGemeinde: TSGemeinde;
    institutionenList: Array<TSInstitution>;
    betreuungsangebotTypList: Array<TSBetreuungsangebotTyp>;
    activeGesuchsperiodenList: Array<string> = [];
    gemeindenList: Array<TSGemeinde>;
    itemsByPage: number = 20;
    numberOfPages: number = 1;

    constructor(public pendenzBetreuungenRS: PendenzBetreuungenRS,
                private readonly ebeguUtil: EbeguUtil,
                private readonly institutionRS: InstitutionRS,
                private readonly institutionStammdatenRS: InstitutionStammdatenRS,
                private readonly gesuchsperiodeRS: GesuchsperiodeRS,
                private readonly gesuchModelManager: GesuchModelManager,
                private readonly berechnungsManager: BerechnungsManager,
                private readonly $state: StateService,
                private readonly gemeindeRS: GemeindeRS,
                private readonly authServiceRS: AuthServiceRS,
                private readonly authLifeCycleService: AuthLifeCycleService) {

        this.authLifeCycleService.get$(TSAuthEvent.LOGIN_SUCCESS)
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(() => this.initViewModel());
    }

    initViewModel() {
        this.updatePendenzenList();
        this.updateInstitutionenList();
        this.updateBetreuungsangebotTypList();
        this.updateActiveGesuchsperiodenList();
        this.updateGemeindenList();
    }

    $onDestroy() {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    public getTotalResultCount(): number {
        if (this.pendenzenList && this.pendenzenList.length) {
            return this.pendenzenList.length;
        }
        return 0;
    }

    private updatePendenzenList() {
        this.pendenzBetreuungenRS.getPendenzenBetreuungenList().then(response => {
            this.pendenzenList = response;
            this.numberOfPages = this.pendenzenList.length / this.itemsByPage;
        });
    }

    public updateActiveGesuchsperiodenList(): void {
        this.gesuchsperiodeRS.getAllNichtAbgeschlosseneGesuchsperioden().then(response => {
            this.extractGesuchsperiodeStringList(response);
        });
    }

    private extractGesuchsperiodeStringList(allActiveGesuchsperioden: TSGesuchsperiode[]) {
        allActiveGesuchsperioden.forEach(gesuchsperiode => {
            this.activeGesuchsperiodenList.push(gesuchsperiode.gesuchsperiodeString);
        });
    }

    public updateInstitutionenList(): void {
        this.institutionRS.getInstitutionenForCurrentBenutzer().then(response => {
            this.institutionenList = response;
        });
    }

    public updateBetreuungsangebotTypList(): void {
        this.institutionStammdatenRS.getBetreuungsangeboteForInstitutionenOfCurrentBenutzer().then(response => {
            this.betreuungsangebotTypList = response;
        });
    }

    private updateGemeindenList(): void {
        this.gemeindeRS.getGemeindenForPrincipal(this.authServiceRS.getPrincipal())
            .then(gemeinden => { this.gemeindenList = gemeinden; });
    }

    public getPendenzenList(): Array<TSPendenzBetreuung> {
        return this.pendenzenList;
    }

    public editPendenzBetreuungen(pendenz: TSPendenzBetreuung, event: any): void {
        if (pendenz) {
            const isCtrlKeyPressed: boolean = (event && event.ctrlKey);
            this.openBetreuung(pendenz, isCtrlKeyPressed);
        }
    }

    private openBetreuung(pendenz: TSPendenzBetreuung, isCtrlKeyPressed: boolean): void {
        const numberParts: TSBetreuungsnummerParts = this.ebeguUtil.splitBetreuungsnummer(pendenz.betreuungsNummer);
        if (numberParts && pendenz) {
            const kindNumber: number = parseInt(numberParts.kindnummer);
            const betreuungNumber: number = parseInt(numberParts.betreuungsnummer);
            if (betreuungNumber > 0) {
                this.berechnungsManager.clear(); // nur um sicher zu gehen, dass alle alte Werte geloescht sind

                // Reload Gesuch in gesuchModelManager on Init in fallCreationView because it has been changed since
                // last time
                this.gesuchModelManager.clearGesuch();
                const navObj: any = {
                    betreuungNumber: betreuungNumber,
                    kindNumber: kindNumber,
                    gesuchId: pendenz.gesuchId
                };
                if (isCtrlKeyPressed) {
                    const url = this.$state.href('gesuch.betreuung', navObj);
                    window.open(url, '_blank');
                } else {
                    this.$state.go('gesuch.betreuung', navObj);
                }
            }
        }
    }
}
