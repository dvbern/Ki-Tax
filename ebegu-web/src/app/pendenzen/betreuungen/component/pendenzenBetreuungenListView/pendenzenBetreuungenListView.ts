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
import {IComponentOptions, IController} from 'angular';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {BerechnungsManager} from '../../../../../gesuch/service/berechnungsManager';
import {GemeindeRS} from '../../../../../gesuch/service/gemeindeRS.rest';
import {GesuchModelManager} from '../../../../../gesuch/service/gesuchModelManager';
import {TSBetreuungsangebotTyp} from '../../../../../models/enums/TSBetreuungsangebotTyp';
import {TSGemeinde} from '../../../../../models/TSGemeinde';
import {TSGesuchsperiode} from '../../../../../models/TSGesuchsperiode';
import {TSInstitution} from '../../../../../models/TSInstitution';
import {TSPendenzBetreuung} from '../../../../../models/TSPendenzBetreuung';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {LogFactory} from '../../../../core/logging/LogFactory';
import {GesuchsperiodeRS} from '../../../../core/service/gesuchsperiodeRS.rest';
import {InstitutionRSX} from '../../../../core/service/institutionRS.rest';
import {InstitutionStammdatenRS} from '../../../../core/service/institutionStammdatenRS.rest';
import {PendenzBetreuungenRS} from '../../service/PendenzBetreuungenRS.rest';

const LOG = LogFactory.createLog('PendenzenBetreuungenListViewController');

export class PendenzenBetreuungenListViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./pendenzenBetreuungenListView.html');
    public controller = PendenzenBetreuungenListViewController;
    public controllerAs = 'vm';
}

export class PendenzenBetreuungenListViewController implements IController {

    public static $inject: string[] = [
        'PendenzBetreuungenRS',
        'EbeguUtil',
        'InstitutionRS',
        'InstitutionStammdatenRS',
        'GesuchsperiodeRS',
        'GesuchModelManager',
        'BerechnungsManager',
        '$state',
        'GemeindeRS',
        'AuthServiceRS'
    ];

    private pendenzenList: Array<TSPendenzBetreuung>;
    public selectedBetreuungsangebotTyp: string;
    public selectedInstitution: string;
    public selectedGesuchsperiode: string;
    public selectedGemeinde: TSGemeinde;
    public institutionenList: Array<TSInstitution>;
    public betreuungsangebotTypList: Array<TSBetreuungsangebotTyp>;
    public activeGesuchsperiodenList: Array<string> = [];
    public gemeindenList: Array<TSGemeinde>;
    public itemsByPage: number = 20;
    public numberOfPages: number = 1;
    public hasInstitutionenInStatusAngemeldet: boolean = false;
    public isStammdatenCheckRequired: boolean = false;

    private readonly unsubscribe$ = new Subject<void>();

    public constructor(
        public pendenzBetreuungenRS: PendenzBetreuungenRS,
        private readonly ebeguUtil: EbeguUtil,
        private readonly institutionRS: InstitutionRSX,
        private readonly institutionStammdatenRS: InstitutionStammdatenRS,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly berechnungsManager: BerechnungsManager,
        private readonly $state: StateService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly authServiceRS: AuthServiceRS
    ) {
    }

    public $onInit(): void {
        this.updatePendenzenList();
        this.updateInstitutionenList();
        this.updateBetreuungsangebotTypList();
        this.updateActiveGesuchsperiodenList();
        this.updateGemeindenList();
        this.initHasInstitutionenInStatusAngemeldet();
        this.initIsStammdatenCheckRequired();
    }

    public $onDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    public getTotalResultCount(): number {
        if (this.pendenzenList && this.pendenzenList.length) {
            return this.pendenzenList.length;
        }
        return 0;
    }

    private updatePendenzenList(): void {
        this.pendenzBetreuungenRS.getPendenzenBetreuungenList().then(response => {
            this.pendenzenList = response;
            this.numberOfPages = this.pendenzenList.length / this.itemsByPage;
        });
    }

    public updateActiveGesuchsperiodenList(): void {
        this.gesuchsperiodeRS.getAllAktivUndInaktivGesuchsperioden().then(response => {
            this.extractGesuchsperiodeStringList(response);
        });
    }

    private extractGesuchsperiodeStringList(allActiveGesuchsperioden: TSGesuchsperiode[]): void {
        allActiveGesuchsperioden.forEach(gesuchsperiode => {
            this.activeGesuchsperiodenList.push(gesuchsperiode.gesuchsperiodeString);
        });
    }

    public updateInstitutionenList(): void {
        this.institutionRS.getInstitutionenReadableForCurrentBenutzer().then(response => {
            this.institutionenList = response;
        });
    }

    public updateBetreuungsangebotTypList(): void {
        this.institutionStammdatenRS.getBetreuungsangeboteForInstitutionenOfCurrentBenutzer().then(response => {
            this.betreuungsangebotTypList = response;
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
        const numberParts = this.ebeguUtil.splitBetreuungsnummer(pendenz.betreuungsNummer);
        if (!numberParts || !pendenz) {
            return;
        }

        const kindNumber = parseInt(numberParts.kindnummer, 10);
        const betreuungNumber = parseInt(numberParts.betreuungsnummer, 10);
        if (betreuungNumber <= 0) {
            return;
        }

        this.berechnungsManager.clear();
        this.gesuchModelManager.clearGesuch();
        const navObj: any = {
            betreuungNumber,
            kindNumber,
            gesuchId: pendenz.gesuchId,
        };
        if (isCtrlKeyPressed) {
            const url = this.$state.href('gesuch.betreuung', navObj);
            window.open(url, '_blank');
        } else {
            this.$state.go('gesuch.betreuung', navObj);
        }
    }

    private initHasInstitutionenInStatusAngemeldet(): void {
        if (!this.authServiceRS.isOneOfRoles(TSRoleUtil.getInstitutionProfilEditRoles())) {
            return;
        }
        this.institutionRS.hasInstitutionenInStatusAngemeldet()
            .then(result => {
                this.hasInstitutionenInStatusAngemeldet = result;
            });
    }

    private initIsStammdatenCheckRequired(): void {
        if (!this.authServiceRS.isOneOfRoles(TSRoleUtil.getInstitutionProfilEditRoles())) {
            return;
        }
        this.institutionRS.isStammdatenCheckRequired()
            .then(result => {
                this.isStammdatenCheckRequired = result;
            });
    }
}
