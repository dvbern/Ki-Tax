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
    Input,
    OnDestroy,
    OnInit
} from '@angular/core';
import {Observable, Subscription} from 'rxjs';
import {LogFactory} from '../../../../app/core/logging/LogFactory';
import {TSFinanzielleSituationResultateDTO} from '../../../../models/dto/TSFinanzielleSituationResultateDTO';

const LOG = LogFactory.createLog('ResultatComponent');

@Component({
    selector: ' dv-massgebendes-einkommen',
    templateUrl: './massgebendes-einkommen.component.html',
    styleUrls: ['./massgebendes-einkommen.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MassgebendesEinkommenComponent implements OnInit, OnDestroy {
    @Input()
    public massgebendesEinkommen$: Observable<TSFinanzielleSituationResultateDTO>;

    @Input()
    public isGemeinsam: boolean;

    @Input()
    public nameGS1: string;

    @Input()
    public nameGS2: string;

    @Input()
    public antragstellerNummer: number;

    public resultate?: TSFinanzielleSituationResultateDTO;
    private subscription: Subscription;

    public constructor(protected ref: ChangeDetectorRef) {}

    public ngOnInit(): void {
        this.setupCalculation();
    }

    public ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    public setupCalculation(): void {
        this.subscription = this.massgebendesEinkommen$.subscribe(
            (resultate: TSFinanzielleSituationResultateDTO) => {
                this.resultate = resultate;
                this.ref.markForCheck();
            },
            error => LOG.error(error)
        );
    }
}
