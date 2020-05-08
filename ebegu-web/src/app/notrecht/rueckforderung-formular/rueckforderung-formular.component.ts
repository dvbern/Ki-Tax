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

import {Component, ChangeDetectionStrategy, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {Transition} from '@uirouter/core';
import {from, Observable} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {
    isNeuOrEingeladenStatus,
    TSRueckforderungStatus
} from '../../../models/enums/TSRueckforderungStatus';
import {TSRueckforderungFormular} from '../../../models/TSRueckforderungFormular';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {NotrechtRS} from '../../core/service/notrechtRS.rest';
import {TSRole} from '../../../models/enums/TSRole';

@Component({
    selector: 'dv-rueckforderung-formular',
    templateUrl: './rueckforderung-formular.component.html',
    styleUrls: ['./rueckforderung-formular.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class RueckforderungFormularComponent implements OnInit {

    @ViewChild(NgForm) private readonly form: NgForm;

    public rueckforderungFormular$: Observable<TSRueckforderungFormular>;

    // Checkbox for Institution:
    public betreuungKorrektAusgewiesen: boolean;
    public gutscheinPlaetzenReduziert: boolean;
    public erstattungGemaessKanton: boolean;
    public mahlzeitenBGSubventionenGebuehrensystem: boolean;
    public belegeEinreichenBetrageKantonZurueckfordern: boolean;

    public constructor(
        private readonly $transition$: Transition,
        private readonly notrechtRS: NotrechtRS,
        private readonly authServiceRS: AuthServiceRS
    ) {
    }

    public ngOnInit(): void {
        const rueckforederungFormId: string = this.$transition$.params().rueckforderungId;

        if (!rueckforederungFormId) {
            return;
        }
        this.rueckforderungFormular$ = from(
            this.notrechtRS.findRueckforderungFormular(rueckforederungFormId).then(
                (response: TSRueckforderungFormular) => {
                    return response;
                }));
    }

    public saveRueckforderungFormular(rueckforderungFormular: TSRueckforderungFormular): void {
        if (!this.form.valid) {
            EbeguUtil.selectFirstInvalid();
            return;
        }
        if (isNeuOrEingeladenStatus(rueckforderungFormular.status)) {
            return;
        }

        //Status wechseln:
        if (rueckforderungFormular.status === TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_1) {
            if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getInstitutionRoles())) {
                rueckforderungFormular.status = TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_1;
            } else {
                // ERROR transition not accepted
                return;
            }
        } else if (rueckforderungFormular.status === TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_1) {
            if (this.authServiceRS.isOneOfRoles([TSRole.SUPER_ADMIN, TSRole.ADMIN_MANDANT, TSRole.SACHBEARBEITER_MANDANT])) {
                rueckforderungFormular.status = TSRueckforderungStatus.GEPRUEFT_STUFE_1;
            } else {
                // ERROR transition not accepted
                return;
            }
        }

        this.notrechtRS.saveRueckforderungFormular(rueckforderungFormular);
    }

    public rueckforderungAbschliessen(rueckforderungFormular: TSRueckforderungFormular): void {
        if (rueckforderungFormular.status === TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_1
            && this.authServiceRS.isOneOfRoles(TSRoleUtil.getInstitutionRoles())) {
            rueckforderungFormular.status = TSRueckforderungStatus.ABGESCHLOSSEN_OHNE_GESUCH;
        } else {
            // ERROR transition not accepted
            return;
        }

        this.saveRueckforderungFormular(rueckforderungFormular);
    }

    public enableRueckforderungAbschliessen(): boolean {
        return this.betreuungKorrektAusgewiesen === true
        && this.gutscheinPlaetzenReduziert === true
        && this.erstattungGemaessKanton === true
        && this.mahlzeitenBGSubventionenGebuehrensystem === true
        && this.belegeEinreichenBetrageKantonZurueckfordern === true;
    }

    public showButtonAbsenden(rueckforderungFormular: TSRueckforderungFormular): boolean {
        if (rueckforderungFormular.status === TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_1
            && this.authServiceRS.isOneOfRoles(TSRoleUtil.getInstitutionRoles())) {
            return true;
        }
        else{
            return false;
        }
    }

    public showButtonGeprueft(rueckforderungFormular: TSRueckforderungFormular): boolean {
        if (rueckforderungFormular.status === TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_1
            && this.authServiceRS.isOneOfRoles([TSRole.SUPER_ADMIN, TSRole.ADMIN_MANDANT, TSRole.SACHBEARBEITER_MANDANT])) {
            return true;
        }
        else{
            return false;
        }
    }

}
