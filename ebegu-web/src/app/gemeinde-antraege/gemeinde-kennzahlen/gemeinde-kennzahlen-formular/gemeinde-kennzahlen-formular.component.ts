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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import {ChangeDetectionStrategy, Component, OnInit, ViewEncapsulation} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn} from '@angular/forms';
import {combineLatest, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../../models/enums/TSRole';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {GemeindeKennzahlenService} from '../gemeinde-kennzahlen.service';

@Component({
    selector: 'dv-gemeinde-kennzahlen-formular',
    templateUrl: './gemeinde-kennzahlen-formular.component.html',
    styleUrls: ['./gemeinde-kennzahlen-formular.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GemeindeKennzahlenFormularComponent implements OnInit {

    public canSeeSaveAndAbschliessen$: Observable<boolean>;
    public canSeeZurueckAnGemeinde$: Observable<boolean>;

    public form$: Observable<FormGroup>;
    private validationTiggered: boolean = false;

    public constructor(
        private readonly gemeindeKennzahlenService: GemeindeKennzahlenService,
        private readonly authService: AuthServiceRS,
        private readonly fb: FormBuilder,
    ) {
    }

    public ngOnInit(): void {
        this.setupCanSeeSaveAndAbschliessen();
        this.setupCanSeeZurueckAnGemeinde();
        this.setupForm();
    }

    private setupCanSeeSaveAndAbschliessen(): void {
        this.canSeeSaveAndAbschliessen$ = combineLatest(
            [
                this.gemeindeKennzahlenService.getGemeindeKennzahlenAntrag()
                    .pipe(
                        map(antrag => antrag.isInBearbeitungGemeinde()),
                    ),
                this.authService.principal$.pipe(
                    map(principal => principal.hasOneOfRoles(TSRoleUtil.getGemeindeOrBGRoles()
                        .concat(TSRole.SUPER_ADMIN))),
                ),
            ],
        ).pipe(
            map(([isInBearbeitungGemeinde, isGemeindeBgSuperAdminRole]) => isInBearbeitungGemeinde && isGemeindeBgSuperAdminRole),
        );
    }

    private setupCanSeeZurueckAnGemeinde(): void {
        this.canSeeZurueckAnGemeinde$ = combineLatest(
            [
                this.gemeindeKennzahlenService.getGemeindeKennzahlenAntrag()
                    .pipe(
                        map(antrag => antrag.isAbgeschlossen()),
                    ),
                this.authService.principal$.pipe(
                    map(principal => principal.hasOneOfRoles(TSRoleUtil.getMandantRoles())),
                ),
            ],
        ).pipe(
            map(([isAbgeschlossen, isMandantSuperAdmin]) => isAbgeschlossen && isMandantSuperAdmin),
        );
    }

    private setupForm(): void {
        this.form$ = this.gemeindeKennzahlenService.getGemeindeKennzahlenAntrag()
            .pipe(
                map(antrag => {
                    return this.fb.group({
                        nachfrageErfuellt: [antrag.nachfrageErfuellt, this.requiredIfAbschliessen],
                        nachfrageAnzahl: [antrag.nachfrageAnzahl, this.requiredIfAbschliessen],
                        nachfrageDauer: [antrag.nachfrageDauer, this.requiredIfAbschliessen],
                        kostenlenkung: this.fb.group({
                            kostenlenkungAndere: [antrag.kostenlenkungAndere],
                            welcheKostenlenkungsmassnahmen: [antrag.welcheKostenlenkungsmassnahmen],
                        }, {validators: this.kostenlenkungValidationIfAbschliessen}),
                    });
                }),
            );
    }

    private requiredIfAbschliessen(): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            return this.validationTiggered && EbeguUtil.isNullOrUndefined(control.value) ? {required: true} : null;
        };
    }

    private kostenlenkungValidationIfAbschliessen(): ValidatorFn {
        return (control: FormGroup): ValidationErrors | null => {
            const errors: {
                kostenlenkungAndere?: { required: boolean },
                welcheKostenlenkungsmassnahmen?: { required: boolean },
            } = {};
            const isKostenlenkungAndere = control.get('kostenlenkungAndere').value;
            if (EbeguUtil.isNullOrUndefined(isKostenlenkungAndere)) {
                errors.kostenlenkungAndere = {required: true};
            } else if (isKostenlenkungAndere === true && EbeguUtil.isNullOrUndefined(control.get(
                'welcheKostenlenkungsmassnahmen').value)) {
                errors.welcheKostenlenkungsmassnahmen = {required: true};
            }
            const hasErrors = EbeguUtil.isNotNullOrUndefined(errors.kostenlenkungAndere) || EbeguUtil.isNullOrUndefined(
                errors.welcheKostenlenkungsmassnahmen);
            return this.validationTiggered && hasErrors ? errors : null;
        };
    }

}
