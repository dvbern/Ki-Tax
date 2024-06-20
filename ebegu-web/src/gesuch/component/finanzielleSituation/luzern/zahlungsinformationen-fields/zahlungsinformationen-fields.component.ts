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
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {isAtLeastFreigegeben} from '../../../../../models/enums/TSAntragStatus';
import {TSAdresse} from '../../../../../models/TSAdresse';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {TSLand} from '../../../../../models/types/TSLand';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

@Component({
    selector: 'dv-zahlungsinformationen-fields',
    templateUrl: './zahlungsinformationen-fields.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class ZahlungsinformationenFieldsComponent implements OnInit {
    @Input() public readonly: boolean;
    @Input() public model: TSFinanzModel;

    @Input() public infomaAktiv: boolean;
    public laenderList: TSLand[];

    public constructor(
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly listResourceRS: ListResourceRS,
        private readonly authServiceRS: AuthServiceRS
    ) {}

    public ngOnInit(): void {
        this.listResourceRS.getLaenderList().then((laenderList: TSLand[]) => {
            this.laenderList = laenderList;
        });
    }

    public abweichendeZahlungsadresseChanged(): void {
        if (!this.model.zahlungsinformationen.abweichendeZahlungsadresse) {
            this.model.zahlungsinformationen.zahlungsadresse = null;
            return;
        }
        if (
            EbeguUtil.isNullOrUndefined(
                this.model.zahlungsinformationen.zahlungsadresse
            )
        ) {
            this.model.zahlungsinformationen.zahlungsadresse = new TSAdresse();
        }
    }

    public isKorrekturModusOrFreigegeben(): boolean {
        return this.gesuchModelManager
            .getGesuch()
            .isKorrekturModusOrFreigegeben();
    }

    public isAtLeastFreigegeben(): boolean {
        return isAtLeastFreigegeben(this.gesuchModelManager.getGesuch().status);
    }

    public isGemeindeOrMandant(): boolean {
        return this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getGemeindeRoles().concat(TSRoleUtil.getMandantRoles())
        );
    }
}
