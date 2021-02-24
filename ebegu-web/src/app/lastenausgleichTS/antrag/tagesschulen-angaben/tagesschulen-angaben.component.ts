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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {combineLatest, Subscription} from 'rxjs';
import {startWith} from 'rxjs/operators';
import {TSLastenausgleichTagesschuleAngabenGemeindeStatus} from '../../../../models/enums/TSLastenausgleichTagesschuleAngabenGemeindeStatus';
import {TSLastenausgleichTagesschuleAngabenInstitution} from '../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenInstitution';
import {TSLastenausgleichTagesschuleAngabenInstitutionContainer} from '../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenInstitutionContainer';
import {TSGesuchsperiode} from '../../../../models/TSGesuchsperiode';
import {HTTP_ERROR_CODES} from '../../../core/constants/CONSTANTS';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LastenausgleichTSService} from '../../services/lastenausgleich-ts.service';
import {TagesschuleAngabenRS} from '../../services/tagesschule-angaben.service.rest';

@Component({
    selector: 'dv-tagesschulen-angaben',
    templateUrl: './tagesschulen-angaben.component.html',
    styleUrls: ['./tagesschulen-angaben.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TagesschulenAngabenComponent {

    @Input() public institutionContainerId: string;

    public form: FormGroup;

    private subscription: Subscription;
    public latsAngabenInstitutionContainer: TSLastenausgleichTagesschuleAngabenInstitutionContainer;
    public angabenAusKibon: boolean;
    public gesuchsPeriode: TSGesuchsperiode;

    public constructor(
        private readonly lastenausgleichTSService: LastenausgleichTSService,
        private readonly tagesschulenAngabenRS: TagesschuleAngabenRS,
        private readonly fb: FormBuilder,
        private readonly cd: ChangeDetectorRef,
        private readonly errorService: ErrorService,
        private readonly translate: TranslateService,
    ) {
    }

    public ngOnInit(): void {
        this.subscription = this.lastenausgleichTSService.getLATSAngabenGemeindeContainer().subscribe(container => {
            this.latsAngabenInstitutionContainer =
                container.angabenInstitutionContainers?.find(institutionContainer => {
                    return institutionContainer.id === this.institutionContainerId;
                });
            this.gesuchsPeriode = container.gesuchsperiode;
            this.form = this.setupForm(this.latsAngabenInstitutionContainer?.angabenDeklaration);
            if (container.status === TSLastenausgleichTagesschuleAngabenGemeindeStatus.NEU) {
                this.form.disable();
            } else {
                this.setupCalculation();
            }
            this.angabenAusKibon = container.alleAngabenInKibonErfasst;
            this.cd.markForCheck();
        }, () => {
            this.errorService.addMesageAsError(this.translate.instant('DATA_RETRIEVAL_ERROR'));
        });
    }

    private setupForm(latsAngabenInstiution: TSLastenausgleichTagesschuleAngabenInstitution): FormGroup {
        const form = this.fb.group({
            // A
            isLehrbetrieb: latsAngabenInstiution?.isLehrbetrieb,
            // B
            anzahlEingeschriebeneKinder: latsAngabenInstiution?.anzahlEingeschriebeneKinder,
            anzahlEingeschriebeneKinderKindergarten: latsAngabenInstiution?.anzahlEingeschriebeneKinderKindergarten,
            anzahlEingeschriebeneKinderBasisstufe: latsAngabenInstiution?.anzahlEingeschriebeneKinderBasisstufe,
            anzahlEingeschriebeneKinderPrimarstufe: latsAngabenInstiution?.anzahlEingeschriebeneKinderPrimarstufe,
            anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen: latsAngabenInstiution?.anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen,
            durchschnittKinderProTagFruehbetreuung: latsAngabenInstiution?.durchschnittKinderProTagFruehbetreuung,
            durchschnittKinderProTagMittag: latsAngabenInstiution?.durchschnittKinderProTagMittag,
            durchschnittKinderProTagNachmittag1: latsAngabenInstiution?.durchschnittKinderProTagNachmittag1,
            durchschnittKinderProTagNachmittag2: latsAngabenInstiution?.durchschnittKinderProTagNachmittag2,
            // C
            schuleAufBasisOrganisatorischesKonzept: latsAngabenInstiution?.schuleAufBasisOrganisatorischesKonzept,
            schuleAufBasisPaedagogischesKonzept: latsAngabenInstiution?.schuleAufBasisPaedagogischesKonzept,
            raeumlicheVoraussetzungenEingehalten: latsAngabenInstiution?.raeumlicheVoraussetzungenEingehalten,
            betreuungsverhaeltnisEingehalten: latsAngabenInstiution?.betreuungsverhaeltnisEingehalten,
            ernaehrungsGrundsaetzeEingehalten: latsAngabenInstiution?.ernaehrungsGrundsaetzeEingehalten,
            // Bemerkungen
            bemerkungen: latsAngabenInstiution?.bemerkungen,
            // Calculations
            anzahlEingeschriebeneKinderSekundarstufe: '',
        });
        form.get('anzahlEingeschriebeneKinderSekundarstufe').disable();

        return form;
    }

    private setupCalculation(): void {
        combineLatest(
            [
                this.form.get('anzahlEingeschriebeneKinder').valueChanges.pipe(startWith(0)),
                this.form.get('anzahlEingeschriebeneKinderKindergarten').valueChanges.pipe(startWith(0)),
                this.form.get('anzahlEingeschriebeneKinderBasisstufe').valueChanges.pipe(startWith(0)),
                this.form.get('anzahlEingeschriebeneKinderPrimarstufe').valueChanges.pipe(startWith(0)),
            ],
        ).subscribe(values => {
            this.form.get('anzahlEingeschriebeneKinderSekundarstufe')
                .setValue(values[0] - values[1] - values[2] - values[3]);
        }, () => {
            this.errorService.addMesageAsError('BAD_NUMBER_ERROR');
        });
    }

    public onFormSubmit(): void {
        this.latsAngabenInstitutionContainer.angabenDeklaration = this.form.value;

        this.tagesschulenAngabenRS.saveTagesschuleAngaben(this.latsAngabenInstitutionContainer).subscribe(result => {
            this.form = this.setupForm(result.angabenDeklaration);
        }, error => {
            if (error.status === HTTP_ERROR_CODES.BAD_REQUEST) {
                this.errorService.addMesageAsError(this.translate.instant('ERROR_NUMBER'));
            }
        });
    }

    public onFreigeben(): void {
        this.latsAngabenInstitutionContainer.angabenDeklaration = this.form.value;

        this.tagesschulenAngabenRS.tagesschuleAngabenFreigeben(this.latsAngabenInstitutionContainer)
            .subscribe(() => {
                this.form.disable();
            }, error => {
                this.errorService.addMesageAsError(this.translate.instant('ERROR_SAVE'));
            });
    }
}
