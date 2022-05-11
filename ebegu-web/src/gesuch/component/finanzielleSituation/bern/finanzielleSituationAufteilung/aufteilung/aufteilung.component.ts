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

import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {BehaviorSubject} from 'rxjs';
import {debounceTime} from 'rxjs/operators';
import {TSAufteilungDTO} from '../../../../../../models/dto/TSFinanzielleSituationAufteilungDTO';
import {GesuchModelManager} from '../../../../../service/gesuchModelManager';

@Component({
    selector: 'dv-aufteilung',
    templateUrl: './aufteilung.component.html',
    styleUrls: ['./aufteilung.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class AufteilungComponent implements OnInit {

    @Input()
    public aufteilung: TSAufteilungDTO;

    @Input()
    public groupName: string;

    @Input()
    public label: string;

    @Input() public allowNegative: boolean = false;

    public readonly = false;

    public gs1Name: string;
    public gs2Name: string;

    public restDebounce: BehaviorSubject<number>;
    public rest: number;

    private readonly _dueTime = 500;

    public constructor(
        private readonly gesuchModelManger: GesuchModelManager,
    ) {
    }

    public ngOnInit(): void {
        this.gs1Name = this.gesuchModelManger.getGesuch().gesuchsteller1?.extractFullName();
        this.gs2Name = this.gesuchModelManger.getGesuch().gesuchsteller2?.extractFullName();
        this.restDebounce = new BehaviorSubject<number>(this.aufteilung.getRest())
            .pipe(debounceTime(this._dueTime)) as BehaviorSubject<number>;
    }

    public updateRest(): void {
        this.restDebounce.next(this.aufteilung.getRest());
    }

}
