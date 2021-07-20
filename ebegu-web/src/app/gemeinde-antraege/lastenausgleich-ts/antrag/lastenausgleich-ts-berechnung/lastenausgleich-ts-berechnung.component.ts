/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {BehaviorSubject, combineLatest} from 'rxjs';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {TSBenutzer} from '../../../../../models/TSBenutzer';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {ErrorService} from '../../../../core/errors/service/ErrorService';
import {LastenausgleichTSService} from '../../services/lastenausgleich-ts.service';

@Component({
    selector: 'dv-lastenausgleich-ts-berechnung',
    templateUrl: './lastenausgleich-ts-berechnung.component.html',
    styleUrls: ['./lastenausgleich-ts-berechnung.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class LastenausgleichTsBerechnungComponent implements OnInit {

    public canViewDokumentErstellenButton: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    private latsContainer: TSLastenausgleichTagesschuleAngabenGemeindeContainer;
    private principal: TSBenutzer | null;

    public constructor(
        private readonly translate: TranslateService,
        private readonly errorService: ErrorService,
        private readonly latsService: LastenausgleichTSService,
        private readonly authService: AuthServiceRS,
    ) {
    }

    public ngOnInit(): void {
        combineLatest([
            this.latsService.getLATSAngabenGemeindeContainer(),
            this.authService.principal$,
        ]).subscribe(values => {
            this.latsContainer = values[0];
            this.principal = values[1];
            this.canViewDokumentErstellenButton.next(this.principal.hasOneOfRoles(TSRoleUtil.getMandantRoles()));
        }, () => this.errorService.addMesageAsInfo(this.translate.instant('DATA_RETRIEVAL_ERROR')));
    }

    public latsDokumentErstellen(): void {

    }
}
