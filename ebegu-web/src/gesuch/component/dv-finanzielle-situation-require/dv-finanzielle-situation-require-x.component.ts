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
import {ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import {EbeguNumberPipe} from '../../../app/shared/pipe/ebegu-number.pipe';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSFinanzielleSituationTyp} from '../../../models/enums/TSFinanzielleSituationTyp';
import {TSFinSitStatus} from '../../../models/enums/TSFinSitStatus';
import {TSRole} from '../../../models/enums/TSRole';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {FinanzielleSituationRS} from '../../service/finanzielleSituationRS.rest';
import {GesuchModelManager} from '../../service/gesuchModelManager';

const LOG = LogFactory.createLog('DvFinanzielleSituationRequireXComponent');

@Component({
    selector: 'dv-finanzielle-situation-require-x',
    templateUrl: './dv-finanzielle-situation-require-x.component.html',
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class DvFinanzielleSituationRequireXComponent implements OnInit {

    @Input()
    public hideVerguenstigungGewunscht: boolean = false;

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

    @Input()
    public areThereAnyBgBetreuungen: boolean;

    @Input()
    public hideSozialhilfeQuestion: boolean = false;

    @Input()
    public disabled = false;

    private maxMassgebendesEinkommen: number;
    private isFinSitTypFkjv: boolean = false;

    public allowedRoles: ReadonlyArray<TSRole>;

    public constructor(
        public form: NgForm,
        public readonly gesuchModelManager: GesuchModelManager,
        private readonly translate: TranslateService,
        private readonly einstellungRS: EinstellungRS,
        private readonly cd: ChangeDetectorRef,
        private readonly finanzielleSituationRS: FinanzielleSituationRS
    ) {
    }

    public ngOnInit(): void {
        this.setFinanziellesituationRequired();
        // Den Parameter fuer das Maximale Einkommen lesen
        this.einstellungRS.findEinstellung(TSEinstellungKey.MAX_MASSGEBENDES_EINKOMMEN,
            this.gesuchModelManager.getDossier().gemeinde.id,
            this.gesuchModelManager.getGesuchsperiode().id)
            .subscribe(response => {
                this.maxMassgebendesEinkommen = parseInt(response.value, 10);
            }, error => LOG.error(error));
        this.allowedRoles = TSRoleUtil.getAllRolesButTraegerschaftInstitution();

        this.finanzielleSituationRS.getFinanzielleSituationTyp(this.gesuchModelManager.getGesuchsperiode(),
            this.gesuchModelManager.getGemeinde())
            .subscribe(typ => {
                this.isFinSitTypFkjv = TSFinanzielleSituationTyp.BERN_FKJV === typ;
            },  err => LOG.error(err));
    }

    public setFinanziellesituationRequired(): void {
        const required = EbeguUtil.isFinanzielleSituationRequired(this.sozialhilfeBezueger,
            this.verguenstigungGewuenscht);
        // Wenn es sich ge�ndert und nicht den Initialwert "undefined" hat, m�ssen gewisse Daten gesetzt werden
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
     * Das Feld verguenstigungGewuenscht wird nur angezeigt,
     * wenn das Feld sozialhilfeBezueger eingeblendet ist und mit nein beantwortet wurde und
     * wenn es sich um keinen reinen BG-Antrag in der FKJV FinSit handelt
     */
    public showFinanzielleSituationDeklarieren(): boolean {
        if (this.hideVerguenstigungGewunscht) {
            return false;
        }
        const isNotSozialhilfeBezueger = EbeguUtil.isNotNullOrUndefined(this.sozialhilfeBezueger)
            && !this.sozialhilfeBezueger;

        if (this.isFinSitTypFkjv) {
            if (isNotSozialhilfeBezueger && !this.areThereAnyBgBetreuungen) {
                return true;
            }
            this.verguenstigungGewuenscht = true;
            this.setFinanziellesituationRequired();
            return false;
        }

        return isNotSozialhilfeBezueger;
    }

    public showSozialhilfeQuestion(): boolean {
        return !this.hideSozialhilfeQuestion;
    }

    public getMaxMassgebendesEinkommen(): string {
        const pipe = new EbeguNumberPipe();
        return pipe.transform(this.maxMassgebendesEinkommen);
    }

    public isGesuchReadonly(): boolean {
        return this.gesuchModelManager.isGesuchReadonly();
    }

    public isKorrekturModusJugendamt(): boolean {
        return this.gesuchModelManager.isKorrekturModusJugendamt();
    }

    public getLabel(): string {
        const key =
            this.gesuchModelManager.isFKJVTexte ?
                'FINANZIELLE_SITUATION_VERGUENSTIGUNG_GEWUENSCHT_FKJV' : 'FINANZIELLE_SITUATION_VERGUENSTIGUNG_GEWUENSCHT';
        const pipe = new EbeguNumberPipe();
        return this.translate.instant(key,
            {maxEinkommen: pipe.transform(this.maxMassgebendesEinkommen)});
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
