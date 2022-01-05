/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2021 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
import {TS} from '@angular/compiler-cli/src/transformers/util';
import {ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSFinSitStatus} from '../../../models/enums/TSFinSitStatus';
import {TSRole} from '../../../models/enums/TSRole';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {GesuchModelManager} from '../../service/gesuchModelManager';

@Component({
    selector: 'dv-finanzielle-situation-require-x',
    templateUrl: './dv-finanzielle-situation-require-x.component.html',
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
})
export class DvFinanzielleSituationRequireX implements OnInit {

    @Input()
    public sozialhilfeBezueger: boolean;
    @Output()
    public readonly sozialhilfeBezuegerChange = new EventEmitter<boolean>();

    @Input()
    public verguenstigungGewuenscht: boolean;
    @Output()
    public readonly verguenstigungGewuenschtChange = new EventEmitter<boolean>();

    @Input()
    public finanzielleSituationRequired: boolean;
    @Output()
    public readonly finanzielleSituationRequiredChange = new EventEmitter<boolean>();
    private maxMassgebendesEinkommen: string;

    public allowedRoles: ReadonlyArray<TSRole>;

    public constructor(
        public form: NgForm,
        public readonly gesuchModelManager: GesuchModelManager,
        private readonly translate: TranslateService,
        private readonly einstellungRS: EinstellungRS,
        private readonly cd: ChangeDetectorRef
    ) {
    }

    public ngOnInit(): void {
        this.setFinanziellesituationRequired();
        // Den Parameter fuer das Maximale Einkommen lesen
        this.einstellungRS.findEinstellung(TSEinstellungKey.MAX_MASSGEBENDES_EINKOMMEN,
            this.gesuchModelManager.getDossier().gemeinde.id,
            this.gesuchModelManager.getGesuchsperiode().id)
            .then(response => {
                this.maxMassgebendesEinkommen = response.value;
            });
        this.allowedRoles = TSRoleUtil.getAllRolesButTraegerschaftInstitution();
    }

    public setFinanziellesituationRequired(): void {
        const required = EbeguUtil.isFinanzielleSituationRequired(this.sozialhilfeBezueger,
            this.verguenstigungGewuenscht);
        // Wenn es sich geändert und nicht den Initialwert "undefined" hat, müssen gewisse Daten gesetzt werden
        if (EbeguUtil.isNotNullOrUndefined(this.finanzielleSituationRequired) &&
            required !== this.finanzielleSituationRequired &&
            this.gesuchModelManager.getGesuch()) {
            this.gesuchModelManager.getGesuch().finSitStatus = required ? null : TSFinSitStatus.AKZEPTIERT;
        }
        this.finanzielleSituationRequired = required;
        this.finanzielleSituationRequiredChange.emit(this.finanzielleSituationRequired);
        this.cd.markForCheck();
    }

    /**
     * Das Feld verguenstigungGewuenscht wird nur angezeigt, wenn das Feld sozialhilfeBezueger eingeblendet ist und mit
     * nein beantwortet wurde.
     */
    public showFinanzielleSituationDeklarieren(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.sozialhilfeBezueger)
            && !this.sozialhilfeBezueger;
    }

    public getMaxMassgebendesEinkommen(): string {
        return this.maxMassgebendesEinkommen;
    }

    public isGesuchReadonly(): boolean {
        return this.gesuchModelManager.isGesuchReadonly();
    }

    public isKorrekturModusJugendamt(): boolean {
        return this.gesuchModelManager.isKorrekturModusJugendamt();
    }

    public getLabel(): string {
        return this.translate.instant('FINANZIELLE_SITUATION_VERGUENSTIGUNG_GEWUENSCHT',
            {maxEinkommen: this.maxMassgebendesEinkommen});
    }

    public updateVerguenstigungGewuenscht(value: any): void {
        this.verguenstigungGewuenschtChange.emit(value);
        this.setFinanziellesituationRequired();
    }

    public updateSozialhilfeBezueger(value: any): void {
        this.sozialhilfeBezuegerChange.emit(value);
        this.setFinanziellesituationRequired();
    }
}
