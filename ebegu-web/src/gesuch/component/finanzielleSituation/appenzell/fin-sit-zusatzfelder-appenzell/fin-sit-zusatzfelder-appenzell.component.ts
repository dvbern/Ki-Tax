/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {TSFinSitZusatzangabenAppenzell} from '../../../../../models/TSFinSitZusatzangabenAppenzell';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {FinanzielleSituationAppenzellService} from '../finanzielle-situation-appenzell.service';

const LOG = LogFactory.createLog('FinSitZusatzfelderAppenzell');

@Component({
    selector: 'dv-fin-sit-zusatzfelder-appenzell',
    templateUrl: './fin-sit-zusatzfelder-appenzell.component.html',
    styleUrls: ['./fin-sit-zusatzfelder-appenzell.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FinSitZusatzfelderAppenzellComponent implements OnInit {

    @Input()
    public isGemeinsam: boolean;

    @Input()
    public basisJahr: number;

    @Input()
    public basisJahrPlus: number; // number years added to basisjahr. for EKV. can be 1 oder 2

    @Input()
    public model: TSAbstractFinanzielleSituation;

    @Input()
    public readOnly: boolean = false;

    @Input()
    public finanzModel: TSFinanzModel;

    @Input()
    public isKorrekturModusJungendamtOrFreigegeben: boolean;

    @Input()
    public antragstellerNumber: number;

    public resultate: TSFinanzielleSituationResultateDTO;

    public constructor(
        private readonly finSitAppenzellService: FinanzielleSituationAppenzellService,
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly ref: ChangeDetectorRef
    ) {
    }

    public ngOnInit(): void {
        if (!this.model.finanzielleVerhaeltnisse) {
            this.model.finanzielleVerhaeltnisse = new TSFinSitZusatzangabenAppenzell();
        }
        // load initial results
        this.onValueChangeFunction();
        this.finSitAppenzellService.massgebendesEinkommenStore.subscribe(resultate => {
                this.resultate = resultate;
                this.ref.markForCheck();
            }, error => LOG.error(error)
        );
    }

    public onValueChangeFunction = (): void => {
        this.finSitAppenzellService.calculateMassgebendesEinkommen(this.finanzModel);
    };

    public getCurrentAntragstellerName(): string {
        if (this.isGemeinsam) {
            return `${this.antragsteller1Name()} ${this.antragsteller2Name()}`;
        }

        return this.antragsteller1Name();
    }

    public antragsteller1Name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller1?.extractFullName();
    }

    public antragsteller2Name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller2?.extractFullName();
    }

    public showBisher(abstractFinanzielleSituation: TSAbstractFinanzielleSituation): boolean {
        return (EbeguUtil.isNotNullOrUndefined(abstractFinanzielleSituation))
            && this.isKorrekturModusJungendamtOrFreigegeben;
    }

    public getVermoegenAnrechenbar(): number {
        if (!this.resultate) {
            return 0;
        }
        if (this.antragstellerNumber === 1) {
            return this.resultate.vermoegenXPercentAnrechenbarGS1;
        } else if (this.antragstellerNumber === 2) {
            return this.resultate.vermoegenXPercentAnrechenbarGS2;
        } else {
            throw new Error(`Falsche Antragsteller Nummer: ${  this.antragstellerNumber}`);
        }
    }
}
