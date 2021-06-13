/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import {AuthServiceRS} from '../../authentication/service/AuthServiceRS.rest';
import {GesuchRS} from '../../gesuch/service/gesuchRS.rest';
import {TSAntragStatusHistory} from '../../models/TSAntragStatusHistory';
import {TSDossier} from '../../models/TSDossier';
import {TSGesuch} from '../../models/TSGesuch';
import {EbeguUtil} from '../../utils/EbeguUtil';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import {AntragStatusHistoryRS} from '../core/service/antragStatusHistoryRS.rest';

@Component({
    selector: 'dv-verlauf',
    templateUrl: './verlauf.component.html',
    styleUrls: ['./verlauf.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class VerlaufComponent implements OnInit {

    public dossier: TSDossier;
    public gesuche: { [gesuchId: string]: string } = {};
    public itemsByPage: number = 20;
    public readonly TSRoleUtil = TSRoleUtil;
    public verlauf: Array<TSAntragStatusHistory>;

    public constructor(
        private readonly $state: StateService,
        private readonly gesuchRS: GesuchRS,
        private readonly antragStatusHistoryRS: AntragStatusHistoryRS,
        private readonly uiRouterGlobals: UIRouterGlobals,
        private readonly ebeguUtil: EbeguUtil,
        private readonly authService: AuthServiceRS
    ) {
    }

    public ngOnInit(): void {
        if (!this.uiRouterGlobals.params.gesuchId) {
            this.cancel();
            return;
        }

        this.gesuchRS.findGesuch(this.uiRouterGlobals.params.gesuchId).then((gesuchResponse: TSGesuch) => {
            this.dossier = gesuchResponse.dossier;
            const gesuchsperiode = gesuchResponse.gesuchsperiode;
            if (this.dossier === undefined) {
                this.cancel();
            }
            this.antragStatusHistoryRS.loadAllAntragStatusHistoryByGesuchsperiode(this.dossier, gesuchsperiode)
                .then((response: TSAntragStatusHistory[]) => {
                    this.verlauf = response;
                });
            this.gesuchRS.getAllAntragDTOForDossier(this.dossier.id).then(response => {
                response.forEach(item => {
                    this.gesuche[item.antragId] = this.ebeguUtil.getAntragTextDateAsString(
                        item.antragTyp,
                        item.eingangsdatum,
                        item.laufnummer,
                    );
                });
            });
        });
    }

    public getVerlaufList(): Array<TSAntragStatusHistory> {
        return this.verlauf;
    }

    public getFallId(): string {
        if (this.dossier && this.dossier.fall) {
            return this.dossier.fall.id;
        }
        return '';
    }

    public cancel(): void {
        this.$state.go('pendenzen.list-view');
    }

    public getGesuch(gesuchid: string): TSGesuch {
        this.gesuchRS.findGesuch(gesuchid).then(response => {
            return response;
        });
        return undefined;
    }

    public dossierToolbarVisible(): boolean {
        return this.authService.isOneOfRoles(TSRoleUtil.getAllRolesButGesuchsteller());
    }

    public dossierToolbarGesuchstellerVisible(): boolean {
        return this.authService.isOneOfRoles(TSRoleUtil.getGesuchstellerOnlyRoles());
    }
}
