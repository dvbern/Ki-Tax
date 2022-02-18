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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {BehaviorSubject, Subscription} from 'rxjs';
import {debounceTime} from 'rxjs/operators';
import {LogFactory} from '../../../../../../app/core/logging/LogFactory';
import {TSAufteilungDTO} from '../../../../../../models/dto/TSFinanzielleSituationAufteilungDTO';
import {GesuchModelManager} from '../../../../../service/gesuchModelManager';

const LOG = LogFactory.createLog('AufteilungComponent');

@Component({
    selector: 'dv-aufteilung',
    templateUrl: './aufteilung.component.html',
    styleUrls: ['./aufteilung.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class AufteilungComponent implements OnInit, OnDestroy {

    @Input()
    public aufteilung: TSAufteilungDTO;

    @Input()
    public groupName: string;

    @Input()
    public label: string;

    public readonly = false;

    public gs1Name: string;
    public gs2Name: string;

    private restDebounce: BehaviorSubject<number>;
    private subscription: Subscription;
    public rest: number;

    public constructor(
        private readonly gesuchModelManger: GesuchModelManager,
        private readonly cd: ChangeDetectorRef
    ) {
    }

    public ngOnInit(): void {
        this.gs1Name = this.gesuchModelManger.getGesuch().gesuchsteller1?.extractFullName();
        this.gs2Name = this.gesuchModelManger.getGesuch().gesuchsteller2?.extractFullName();
        this.restDebounce = new BehaviorSubject<number>(this.aufteilung.getRest());

        this.subscription = this.restDebounce
            .asObservable()
            // tslint:disable-next-line:no-magic-numbers
            .pipe(debounceTime(500))
            .subscribe(res => {
                this.rest = res;
                this.cd.markForCheck();
            }, err => {
                LOG.error(err);
            });
    }

    public updateRest(): void {
        this.restDebounce.next(this.aufteilung.getRest());
    }

    public ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }
}
