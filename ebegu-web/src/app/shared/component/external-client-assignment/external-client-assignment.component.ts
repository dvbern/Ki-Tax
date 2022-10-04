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

import {ChangeDetectionStrategy, Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {externalClientComparator} from '../../../../models/TSExternalClient';
import {TSExternalClientAssignment} from '../../../../models/TSExternalClientAssignment';

@Component({
    selector: 'dv-external-client-assignment',
    templateUrl: './external-client-assignment.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExternalClientAssignmentComponent implements OnChanges {

    @Input() public externalClients: TSExternalClientAssignment;

    public assignedClients: string;

    public constructor(private readonly translate: TranslateService) {
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.externalClients) {
            this.assignedClients = this.getAssignedClientsLabel(changes.externalClients.currentValue);
        }
    }

    private getAssignedClientsLabel(clients?: TSExternalClientAssignment): string {
        if (!clients || clients.assignedClients.length === 0) {
            return this.translate.instant('LABEL_KEINE');
        }

        const copy = [...clients.assignedClients].sort(externalClientComparator);

        return copy.map(client => client.clientName).join(', ');
    }
}
