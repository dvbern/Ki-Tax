/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, Component, EventEmitter, OnInit, Output, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {LogFactory} from '../../../../../app/core/logging/LogFactory';
import {
    TSAufteilungDTO,
    TSFinanzielleSituationAufteilungDTO
} from '../../../../../models/dto/TSFinanzielleSituationAufteilungDTO';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {FinanzielleSituationRS} from '../../../../service/finanzielleSituationRS.rest';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

const LOG = LogFactory.createLog('FinanzielleSituationAufteilungComponent');

@Component({
    selector: 'dv-finanzielle-situation-aufteilung',
    templateUrl: './finanzielle-situation-aufteilung.component.html',
    styleUrls: ['./finanzielle-situation-aufteilung.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FinanzielleSituationAufteilungComponent implements OnInit {

    @ViewChild(NgForm) public form: NgForm;

    @Output()
    public readonly closeEvent: EventEmitter<void> = new EventEmitter<void>();

    public aufteilungDTO: TSFinanzielleSituationAufteilungDTO = new TSFinanzielleSituationAufteilungDTO();

    public constructor(
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly finanzielleSituationRS: FinanzielleSituationRS
    ) {
    }

    public ngOnInit(): void {
        this.assertContainersNotNull();
        const finSitGS1 = this.gesuchModelManager.getGesuch().gesuchsteller1.finanzielleSituationContainer;
        const finSitGS2 = this.gesuchModelManager.getGesuch().gesuchsteller2.finanzielleSituationContainer;
        this.initAufteilngDTO(finSitGS1, finSitGS2);
    }

    private assertContainersNotNull(): void {
        if (this.gesuchModelManager.getGesuch().gesuchsteller1?.finanzielleSituationContainer === null) {
            LOG.error('Finanzielle Situation Antragsteller 1 nicht gesetzt');
        }
        if (this.gesuchModelManager.getGesuch().gesuchsteller2?.finanzielleSituationContainer === null) {
            LOG.error('Finanzielle Situation Antragsteller 2 nicht gesetzt');
        }
    }

    private initAufteilngDTO (
        finSitGS1: TSFinanzielleSituationContainer,
        finSitGS2: TSFinanzielleSituationContainer
    ): void {
        const bruttoertraegeVermoegen = new TSAufteilungDTO();
        bruttoertraegeVermoegen.gs1 = finSitGS1.finanzielleSituationJA.bruttoertraegeVermoegen;
        bruttoertraegeVermoegen.gs1Urspruenglich = finSitGS1.finanzielleSituationGS?.bruttoertraegeVermoegen;
        bruttoertraegeVermoegen.gs2 = finSitGS2.finanzielleSituationJA.bruttoertraegeVermoegen;
        bruttoertraegeVermoegen.gs2Urspruenglich = finSitGS2.finanzielleSituationGS?.bruttoertraegeVermoegen;
        bruttoertraegeVermoegen.calculateInitiaSum();
        this.aufteilungDTO.bruttoertraegeVermoegen = bruttoertraegeVermoegen;

        const nettoertraegeErbengemeinschaft = new TSAufteilungDTO();
        nettoertraegeErbengemeinschaft.gs1 = finSitGS1.finanzielleSituationJA.nettoertraegeErbengemeinschaft;
        nettoertraegeErbengemeinschaft.gs1Urspruenglich = finSitGS1.finanzielleSituationGS?.nettoertraegeErbengemeinschaft;
        nettoertraegeErbengemeinschaft.gs2 = finSitGS2.finanzielleSituationJA.nettoertraegeErbengemeinschaft;
        nettoertraegeErbengemeinschaft.gs2Urspruenglich = finSitGS2.finanzielleSituationGS?.nettoertraegeErbengemeinschaft;
        nettoertraegeErbengemeinschaft.calculateInitiaSum();
        this.aufteilungDTO.nettoertraegeErbengemeinschaft = nettoertraegeErbengemeinschaft;

        const abzugSchuldzinsen = new TSAufteilungDTO();
        abzugSchuldzinsen.gs1 = finSitGS1.finanzielleSituationJA.abzugSchuldzinsen;
        abzugSchuldzinsen.gs1Urspruenglich = finSitGS1.finanzielleSituationGS?.abzugSchuldzinsen;
        abzugSchuldzinsen.gs2 = finSitGS2.finanzielleSituationJA.abzugSchuldzinsen;
        abzugSchuldzinsen.gs2Urspruenglich = finSitGS2.finanzielleSituationGS?.abzugSchuldzinsen;
        abzugSchuldzinsen.calculateInitiaSum();
        this.aufteilungDTO.abzugSchuldzinsen = abzugSchuldzinsen;

        const gewinnungskosten = new TSAufteilungDTO();
        gewinnungskosten.gs1 = finSitGS1.finanzielleSituationJA.gewinnungskosten;
        gewinnungskosten.gs1Urspruenglich = finSitGS1.finanzielleSituationGS?.gewinnungskosten;
        gewinnungskosten.gs2 = finSitGS2.finanzielleSituationJA.gewinnungskosten;
        gewinnungskosten.gs2Urspruenglich = finSitGS2.finanzielleSituationGS?.gewinnungskosten;
        gewinnungskosten.calculateInitiaSum();
        this.aufteilungDTO.gewinnungskosten = gewinnungskosten;

        const geleisteteAlimente = new TSAufteilungDTO();
        geleisteteAlimente.gs1 = finSitGS1.finanzielleSituationJA.geleisteteAlimente;
        geleisteteAlimente.gs1Urspruenglich = finSitGS1.finanzielleSituationGS?.geleisteteAlimente;
        geleisteteAlimente.gs2 = finSitGS2.finanzielleSituationJA.geleisteteAlimente;
        geleisteteAlimente.gs2Urspruenglich = finSitGS2.finanzielleSituationGS?.geleisteteAlimente;
        geleisteteAlimente.calculateInitiaSum();
        this.aufteilungDTO.geleisteteAlimente = geleisteteAlimente;

        const nettovermoegen = new TSAufteilungDTO();
        nettovermoegen.gs1 = finSitGS1.finanzielleSituationJA.nettoVermoegen;
        nettovermoegen.gs1Urspruenglich = finSitGS1.finanzielleSituationGS?.nettoVermoegen;
        nettovermoegen.gs2 = finSitGS2.finanzielleSituationJA.nettoVermoegen;
        nettovermoegen.gs2Urspruenglich = finSitGS2.finanzielleSituationGS?.nettoVermoegen;
        nettovermoegen.calculateInitiaSum();
        this.aufteilungDTO.nettovermoegen = nettovermoegen;
    }

    public gs1Name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller1?.extractFullName();
    }

    public gs2Name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller2?.extractFullName();
    }

    public async save(): Promise<void> {
        if (!this.isValid()) {
            return;
        }
        await this.finanzielleSituationRS.updateFromAufteilung(this.aufteilungDTO, this.gesuchModelManager.getGesuch());
        await this.gesuchModelManager.reloadGesuch();
        this.close();
        return;
    }

    public isValid(): boolean {
        if (!this.form?.valid) {
            return false;
        }
        for (const val of Object.values(this.aufteilungDTO)) {
            if (val.getRest() !== 0) {
                return false;
            }
        }
        return true;
    }

    public close(): void {
        this.closeEvent.emit();
    }

}
