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
import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {combineLatest, Observable, ReplaySubject, Subject} from 'rxjs';
import {map, takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSGemeindeKennzahlen} from '../../../../models/gemeindeantrag/gemeindekennzahlen/TSGemeindeKennzahlen';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {GemeindeKennzahlenService} from '../gemeinde-kennzahlen.service';

@Component({
    selector: 'dv-gemeinde-kennzahlen-formular',
    templateUrl: './gemeinde-kennzahlen-formular.component.html',
    styleUrls: ['./gemeinde-kennzahlen-formular.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GemeindeKennzahlenFormularComponent implements OnInit, OnDestroy {

    public canSeeSaveAndAbschliessen$: ReplaySubject<boolean> = new ReplaySubject<boolean>();
    public canSeeZurueckAnGemeinde$: ReplaySubject<boolean> = new ReplaySubject<boolean>();

    public form$: Observable<FormGroup>;
    private validationTiggered: boolean = false;
    private unsubscribe$: Subject<any> = new Subject<any>();

    public constructor(
        private readonly gemeindeKennzahlenService: GemeindeKennzahlenService,
        private readonly authService: AuthServiceRS,
        private readonly fb: FormBuilder,
        // TODO: replace with X
        private readonly errorService: ErrorService,
        private readonly translate: TranslateService,
    ) {
    }

    private static formToModel(form: any): TSGemeindeKennzahlen {
        const model = new TSGemeindeKennzahlen();
        model.id = form.id;
        model.version = form.version;
        model.nachfrageErfuellt = form.nachfrageErfuellt;
        model.nachfrageAnzahl = form.nachfrageAnzahl;
        model.nachfrageDauer = form.nachfrageDauer;
        model.kostenlenkungAndere = form.kostenlenkung.kostenlenkungAndere;
        model.welcheKostenlenkungsmassnahmen = form.kostenlenkung.welcheKostenlenkungsmassnahmen;

        return model;
    }

    public ngOnInit(): void {
        this.setupCanSeeSaveAndAbschliessen();
        this.setupCanSeeZurueckAnGemeinde();
        this.setupForm();
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
    }

    private setupCanSeeSaveAndAbschliessen(): void {
        combineLatest(
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
            takeUntil(this.unsubscribe$),
            map(([isInBearbeitungGemeinde, isGemeindeBgSuperAdminRole]) => isInBearbeitungGemeinde && isGemeindeBgSuperAdminRole),
        ).subscribe(this.canSeeSaveAndAbschliessen$);
    }

    private setupCanSeeZurueckAnGemeinde(): void {
        combineLatest(
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
            takeUntil(this.unsubscribe$),
            map(([isAbgeschlossen, isMandantSuperAdmin]) => isAbgeschlossen && isMandantSuperAdmin),
        ).subscribe(this.canSeeZurueckAnGemeinde$);
    }

    private setupForm(): void {
        this.form$ = this.gemeindeKennzahlenService.getGemeindeKennzahlenAntrag()
            .pipe(
                map(antrag => {
                    return this.fb.group({
                        id: [antrag.id],
                        version: [antrag.version],
                        nachfrageErfuellt: [antrag.nachfrageErfuellt, [this.requiredIfAbschliessen()]],
                        nachfrageAnzahl: [antrag.nachfrageAnzahl, this.requiredIfAbschliessen()],
                        nachfrageDauer: [antrag.nachfrageDauer, this.requiredIfAbschliessen()],
                        kostenlenkung: this.fb.group({
                            kostenlenkungAndere: [antrag.kostenlenkungAndere],
                            welcheKostenlenkungsmassnahmen: [antrag.welcheKostenlenkungsmassnahmen],
                        }, {validators: this.kostenlenkungValidationIfAbschliessen()}),
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

    public save(form: any): void {
        this.gemeindeKennzahlenService.saveGemeindeKennzahlen(GemeindeKennzahlenFormularComponent.formToModel(form))
            .subscribe(() => this.handleSaveSuccess());
    }

    private handleSaveSuccess(): void {
        this.errorService.clearAll();
        this.errorService.addMesageAsInfo(this.translate.instant('SAVED'));
    }

    public abschliessen(form: FormGroup): void {
        this.validationTiggered = true;
        this.triggerFormValidation(form);

        if (!form.valid) {
            return;
        }

        this.gemeindeKennzahlenService.gemeindeKennzahlenAbschliessen(GemeindeKennzahlenFormularComponent.formToModel(
            form.value))
            .subscribe(() => this.handleSaveSuccess());
    }

    private triggerFormValidation(form: FormGroup): void {
        for (const key in form.controls) {
            if (form.get(key) !== null) {
                form.get(key).markAsTouched();
                form.get(key).updateValueAndValidity();
            }
        }
        form.markAsTouched();
        form.updateValueAndValidity();
    }
}
