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

import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {ListResourceRS} from '../../../../../app/core/service/listResourceRS.rest';
import {TSAdresse} from '../../../../../models/TSAdresse';
import {TSFamiliensituation} from '../../../../../models/TSFamiliensituation';
import {TSLand} from '../../../../../models/types/TSLand';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

@Component({
    selector: 'dv-infoma-fields',
    templateUrl: './infoma-fields.component.html',
    styleUrls: ['./infoma-fields.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
})
export class InfomaFieldsComponent implements OnInit {

    @Input() public readonly: boolean;
    public laenderList: TSLand[];

    public constructor(
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly listResourceRS: ListResourceRS
    ) {
    }

    public ngOnInit(): void {
        this.listResourceRS.getLaenderList().then((laenderList: TSLand[]) => {
            this.laenderList = laenderList;
        });
    }

    public getFamiliensituation(): TSFamiliensituation {
        return this.gesuchModelManager.getFamiliensituation();
    }

    public getFamiliensituationGS(): TSFamiliensituation {
        return this.gesuchModelManager.getGesuch().familiensituationContainer.familiensituationGS;
    }

    public abweichendeZahlungsadresseChanged(): void {
        if (!this.getFamiliensituation().abweichendeZahlungsadresseInfoma) {
            this.getFamiliensituation().zahlungsadresseInfoma = null;
            return;
        }
        if (EbeguUtil.isNullOrUndefined(this.getFamiliensituation().zahlungsadresseInfoma)) {
            this.getFamiliensituation().zahlungsadresseInfoma = new TSAdresse();
        }
    }

    public isKorrekturModusOrFreigegeben(): boolean {
        return this.gesuchModelManager.getGesuch().isKorrekturModusOrFreigegeben();
    }
}
