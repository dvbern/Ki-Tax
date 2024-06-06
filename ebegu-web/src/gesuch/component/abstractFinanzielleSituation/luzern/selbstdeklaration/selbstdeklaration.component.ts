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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {LogFactory} from '../../../../../app/core/logging/LogFactory';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSAbstractFinanzielleSituation} from '../../../../../models/TSAbstractFinanzielleSituation';
import {TSFinanzielleSituationSelbstdeklaration} from '../../../../../models/TSFinanzielleSituationSelbstdeklaration';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {FinanzielleSituationLuzernService} from '../../../finanzielleSituation/luzern/finanzielle-situation-luzern.service';

const LOG = LogFactory.createLog('SelbstdeklarationComponent');

@Component({
    selector: 'dv-selbstdeklaration',
    templateUrl: './selbstdeklaration.component.html',
    styleUrls: ['./selbstdeklaration.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SelbstdeklarationComponent implements OnInit {

    @Input()
    public antragstellerNummer: number; // antragsteller 1 or 2

    @Input()
    public isGemeinsam: boolean;

    @Input()
    public basisJahr: string;

    @Input()
    public basisJahrPlus: number; // number years added to basisjahr. for EKV. can be 1 oder 2

    @Input()
    public model: TSAbstractFinanzielleSituation;

    @Input()
    public readOnly: boolean = false;

    @Input()
    public isEKV: boolean = false;

    @Input()
    public finanzModel: TSFinanzModel;

    @Input()
    public isKorrekturModusJungendamtOrFreigegeben: boolean;

    @Input()
    public isQuellenbesteuert: boolean = false;

    public resultate: TSFinanzielleSituationResultateDTO;

    public constructor(
        private readonly finSitLuService: FinanzielleSituationLuzernService,
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly ref: ChangeDetectorRef
    ) {
    }

    public ngOnInit(): void {
        if (!this.model.selbstdeklaration) {
            this.model.selbstdeklaration = new TSFinanzielleSituationSelbstdeklaration();
        }
        // load initial results
        this.onValueChangeFunction();
        this.finSitLuService.massgebendesEinkommenStore.subscribe(resultate => {
                this.resultate = resultate;
                this.ref.markForCheck();
            }, error => LOG.error(error)
        );
    }

    public onValueChangeFunction = (): void => {
        if (this.isEKV) {
            this.finSitLuService.calculateEinkommensverschlechterung(this.finanzModel, this.basisJahrPlus);
        } else {
            this.finSitLuService.calculateMassgebendesEinkommen(this.finanzModel);
        }
    };

    public getCurrentAntragstellerName(): string {
        if (this.antragstellerNummer === 2) {
            return this.antragsteller2Name();
        }

        return this.antragsteller1Name();
    }

    public antragsteller1Name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller1?.extractFullName();
    }

    public antragsteller2Name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller2?.extractFullName();
    }

    public getEinkommenForCurrentAntragsteller(): number {
        if (!this.resultate) {
            return null;
        }
        if (this.antragstellerNummer === 1) {
            return this.resultate.einkommenGS1;
        }
        if (this.antragstellerNummer === 2) {
            return this.resultate.einkommenGS2;
        }
        return null;
    }

    public getAbzuegeForCurrentAntragsteller(): number {
        if (!this.resultate) {
            return null;
        }
        if (this.antragstellerNummer === 1) {
            return this.resultate.abzuegeGS1;
        }
        if (this.antragstellerNummer === 2) {
            return this.resultate.abzuegeGS2;
        }
        return null;
    }

    public getVermoegenForCurrentAntragsteller(): number {
        if (!this.resultate) {
            return null;
        }
        if (this.antragstellerNummer === 1) {
            return this.resultate.vermoegenXPercentAnrechenbarGS1;
        }
        if (this.antragstellerNummer === 2) {
            return this.resultate.vermoegenXPercentAnrechenbarGS2;
        }
        return null;
    }

    public showBisher(abstractFinanzielleSituation: TSAbstractFinanzielleSituation): boolean {
        return (EbeguUtil.isNotNullOrUndefined(abstractFinanzielleSituation))
            && this.isKorrekturModusJungendamtOrFreigegeben;
    }
}
