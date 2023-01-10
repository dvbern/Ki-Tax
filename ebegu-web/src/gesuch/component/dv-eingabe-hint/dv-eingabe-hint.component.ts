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

import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {EbeguUtil} from '../../../utils/EbeguUtil';

/**
 * Zeigt einen Hinweis, wie hoch die Eingabe im Basisjahr war.
 */
@Component({
    selector: 'dv-eingabe-hint',
    templateUrl: './dv-eingabe-hint.component.html',
    styleUrls: ['./dv-eingabe-hint.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DvEingabeHintComponent {
    @Input() public neuerWert: number;
    @Input() public vergleichswert: number;
    @Input() public label: string;

    public isVisible(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.vergleichswert) && (this.vergleichswert > 0 ||
            (this.vergleichswert !== this.neuerWert && this.neuerWert > 0));
    }
}
