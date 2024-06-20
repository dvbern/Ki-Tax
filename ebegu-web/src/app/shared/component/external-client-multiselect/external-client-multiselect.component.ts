/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {
    ChangeDetectionStrategy,
    Component,
    Input,
    OnChanges,
    SimpleChanges
} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {
    TSExternalClient,
    externalClientComparator
} from '../../../../models/TSExternalClient';
import {TSExternalClientAssignment} from '../../../../models/TSExternalClientAssignment';

let nextId = 0;

@Component({
    selector: 'dv-external-client-multiselect',
    templateUrl: './external-client-multiselect.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class ExternalClientMultiselectComponent implements OnChanges {
    @Input() public externalClients: TSExternalClientAssignment;

    public inputId = `external-client-multiselect-${nextId++}`;
    public options: TSExternalClient[] = [];

    public constructor(public readonly form: NgForm) {}

    private static getOptions(
        currentValue?: TSExternalClientAssignment
    ): TSExternalClient[] {
        if (currentValue) {
            return currentValue.assignedClients
                .concat(currentValue.availableClients)
                .sort(externalClientComparator);
        }

        return [];
    }

    public ngOnChanges(changes: SimpleChanges): void {
        this.options = ExternalClientMultiselectComponent.getOptions(
            changes.externalClients.currentValue
        );
    }
}
