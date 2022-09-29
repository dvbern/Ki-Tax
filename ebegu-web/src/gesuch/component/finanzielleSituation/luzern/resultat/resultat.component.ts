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
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {FinanzielleSituationLuzernService} from '../finanzielle-situation-luzern.service';

const LOG = LogFactory.createLog('ResultatComponent');

@Component({
    selector: 'dv-resultat',
    templateUrl: './resultat.component.html',
    styleUrls: ['./resultat.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ResultatComponent implements OnInit {

    @Input()
    public isGemeinsam: boolean;

    @Input()
    public year: number | string;

    @Input()
    public antragstellerNummer: number;

    public resultate?: TSFinanzielleSituationResultateDTO;

    public constructor(
        protected ref: ChangeDetectorRef,
        private readonly finSitLuService: FinanzielleSituationLuzernService,
        private readonly gesuchModelManager: GesuchModelManager
    ) {
    }

    public ngOnInit(): void {
        this.setupCalculation();
    }

    public setupCalculation(): void {
        this.finSitLuService.massgebendesEinkommenStore.subscribe(resultate => {
                this.resultate = resultate;
                this.ref.markForCheck();
            }, error => LOG.error(error)
        );
    }

    public getAntragsteller1Name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller1.extractFullName();
    }

    public getAntragsteller2Name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller2.extractFullName();
    }
}
