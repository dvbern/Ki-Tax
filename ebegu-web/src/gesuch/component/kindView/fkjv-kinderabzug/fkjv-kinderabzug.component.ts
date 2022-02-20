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
import {MatRadioChange} from '@angular/material/radio';
import {TSKind} from '../../../../models/TSKind';
import {TSKindContainer} from '../../../../models/TSKindContainer';
import {GesuchModelManager} from '../../../service/gesuchModelManager';

@Component({
    selector: 'dv-fkjv-kinderabzug',
    templateUrl: './fkjv-kinderabzug.component.html',
    styleUrls: ['./fkjv-kinderabzug.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FkjvKinderabzugComponent implements OnInit {

    @Input()
    public kindContainer: TSKindContainer;

    public constructor(
        private readonly gesuchModelManager: GesuchModelManager
    ) {
    }

    public ngOnInit(): void {
    }

    public getModel(): TSKind | undefined {
        if (this.kindContainer?.kindJA) {
            return this.kindContainer.kindJA;
        }
        return undefined;
    }

    public change($event: MatRadioChange): void {
        console.log($event);
    }
}
