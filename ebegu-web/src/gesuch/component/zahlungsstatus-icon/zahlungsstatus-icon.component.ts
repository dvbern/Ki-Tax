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

import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import {TSVerfuegungZeitabschnittZahlungsstatus} from '../../../models/enums/TSVerfuegungZeitabschnittZahlungsstatus';
import {TSBetreuung} from '../../../models/TSBetreuung';
import {TSZahlungsstatusIconLabel} from './TSZahlungsstatusIconLabel';

@Component({
    selector: 'dv-zahlungsstatus-icon',
    templateUrl: './zahlungsstatus-icon.component.html',
    styleUrls: ['./zahlungsstatus-icon.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ZahlungsstatusIconComponent implements OnInit {

    @Input()
    public zahlungsstatus: TSVerfuegungZeitabschnittZahlungsstatus;
    @Input()
    private readonly betreuung: TSBetreuung;
    public iconLabel: TSZahlungsstatusIconLabel;

    public constructor(
        private readonly translate: TranslateService,
        private readonly authService: AuthServiceRS
    ) {
    }

    public ngOnInit(): void {
        this.iconLabel = new TSZahlungsstatusIconLabel(this.translate);
        this.iconLabel.zahlungsstatusToIconLabel(this.zahlungsstatus, this.betreuung);
    }

    public getTitle(): string {
        let title = this.iconLabel.tooltipLabel;
        if (this.authService.isRole(TSRole.SUPER_ADMIN)) {
            title += ` (Superadmin: ${  this.zahlungsstatus})`;
        }
        return title;
    }
}
