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

import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    EventEmitter,
    Input,
    OnInit,
    Output
} from '@angular/core';
import {LogFactory} from '../../../../../app/core/logging/LogFactory';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSAbstractFinanzielleSituation} from '../../../../../models/TSAbstractFinanzielleSituation';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {TSFinSitZusatzangabenAppenzell} from '../../../../../models/TSFinSitZusatzangabenAppenzell';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {FinanzielleSituationAppenzellService} from '../../../finanzielleSituation/appenzell/finanzielle-situation-appenzell.service';

const LOG = LogFactory.createLog('FinSitZusatzfelderAppenzell');

@Component({
    selector: 'dv-fin-sit-felder-appenzell',
    templateUrl: './fin-sit-felder-appenzell.component.html',
    styleUrls: ['./fin-sit-felder-appenzell.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FinSitFelderAppenzellComponent implements OnInit {
    @Input()
    public finSitZusatzangabenAppenzell: TSFinSitZusatzangabenAppenzell;

    @Input()
    public readOnly: boolean = false;

    @Input()
    public finanzModel: TSFinanzModel;

    @Input()
    public showBisher: boolean;

    @Input()
    public antragstellerNumber: number;

    @Input()
    public deklaration: TSFinSitZusatzangabenAppenzell;

    @Output()
    private readonly valueChanges =
        new EventEmitter<TSFinSitZusatzangabenAppenzell>();

    public resultate: TSFinanzielleSituationResultateDTO;

    public constructor(
        private readonly finSitAppenzellService: FinanzielleSituationAppenzellService,
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly ref: ChangeDetectorRef
    ) {}

    public ngOnInit(): void {
        this.finSitAppenzellService.massgebendesEinkommenStore.subscribe(
            resultate => {
                this.resultate = resultate;
                this.ref.markForCheck();
            },
            error => LOG.error(error)
        );
    }

    public onValueChangeFunction = (): void => {
        this.valueChanges.emit(this.finSitZusatzangabenAppenzell);
    };

    public antragsteller1Name(): string {
        return this.gesuchModelManager
            .getGesuch()
            .gesuchsteller1?.extractFullName();
    }

    public antragsteller2Name(): string {
        return this.gesuchModelManager
            .getGesuch()
            .gesuchsteller2?.extractFullName();
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
            throw new Error(
                `Falsche Antragsteller Nummer: ${this.antragstellerNumber}`
            );
        }
    }

    public getJahr(): number {
        return EbeguUtil.isNullOrUndefined(this.finanzModel.getBasisJahrPlus())
            ? this.finanzModel.getBasisjahr()
            : this.finanzModel.getBasisjahr() +
                  this.finanzModel.getBasisJahrPlus();
    }
}
