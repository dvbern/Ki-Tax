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
import {HttpErrorResponse} from '@angular/common/http';
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, NgForm, Validators} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {BehaviorSubject, combineLatest, from, NEVER, Observable, of} from 'rxjs';
import {catchError, concatMap, filter, map, mergeMap, tap} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSGemeindeAntragTyp} from '../../../models/enums/TSGemeindeAntragTyp';
import {TSLastenausgleichTagesschuleAngabenGemeindeStatus} from '../../../models/enums/TSLastenausgleichTagesschuleAngabenGemeindeStatus';
import {TSRole} from '../../../models/enums/TSRole';
import {TSGemeindeAntrag} from '../../../models/gemeindeantrag/TSGemeindeAntrag';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {TSPublicAppConfig} from '../../../models/TSPublicAppConfig';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvNgRemoveDialogComponent} from '../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {HTTP_ERROR_CODES} from '../../core/constants/CONSTANTS';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {LogFactory} from '../../core/logging/LogFactory';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {GesuchsperiodeRS} from '../../core/service/gesuchsperiodeRS.rest';
import {WizardStepXRS} from '../../core/service/wizardStepXRS.rest';
import {DVAntragListFilter} from '../../shared/interfaces/DVAntragListFilter';
import {DVAntragListItem} from '../../shared/interfaces/DVAntragListItem';
import {GemeindeAntragService} from '../services/gemeinde-antrag.service';

const LOG = LogFactory.createLog('GemeindeAntraegeComponent');

@Component({
    selector: 'dv-gemeinde-antraege',
    templateUrl: './gemeinde-antraege.component.html',
    styleUrls: ['./gemeinde-antraege.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GemeindeAntraegeComponent implements OnInit {

    @ViewChild(NgForm) public form: NgForm;

    public hiddenDVTableColumns = [
        'fallNummer',
        'familienName',
        'kinder',
        'dokumenteHochgeladen',
        'angebote',
        'institutionen',
        'verantwortlicheTS',
        'verantwortlicheBG',
    ];

    public antragList$: Observable<DVAntragListItem[]>;
    public gesuchsperioden: TSGesuchsperiode[];
    public formGroup: FormGroup;
    public totalItems: number;
    public gemeinden: TSGemeinde[];

    private readonly filterDebounceSubject: BehaviorSubject<DVAntragListFilter> =
        new BehaviorSubject<DVAntragListFilter>({});

    private readonly sortDebounceSubject: BehaviorSubject<{
        predicate?: string,
        reverse?: boolean
    }> = new BehaviorSubject<{ predicate?: string; reverse?: boolean }>({
        predicate: 'aenderungsdatum',
        reverse: true
    });
    public triedSending: boolean = false;
    public types: TSGemeindeAntragTyp[];
    public deletePossible$: Observable<boolean>;

    public constructor(
        private readonly gemeindeAntragService: GemeindeAntragService,
        private readonly gesuchsperiodenService: GesuchsperiodeRS,
        private readonly fb: FormBuilder,
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly translate: TranslateService,
        private readonly cd: ChangeDetectorRef,
        private readonly wizardStepXRS: WizardStepXRS,
        private readonly gemeindeRS: GemeindeRS,
        private readonly authService: AuthServiceRS,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly dialog: MatDialog,
    ) {
    }

    public ngOnInit(): void {
        this.formGroup = this.fb.group({
            periode: ['', Validators.required],
            antragTyp: ['', Validators.required],
            gemeinde: [''],
        });
        this.loadAntragList();
        this.loadGemeinden();
        this.gesuchsperiodenService.getAllActiveGesuchsperioden().then(result => this.gesuchsperioden = result);
        this.initAntragTypes();
        this.checkDeletePossible$();
    }

    private loadAntragList(): void {
        this.antragList$ = combineLatest([this.filterDebounceSubject, this.sortDebounceSubject]).pipe(
            mergeMap(filterAndSort => this.gemeindeAntragService.getGemeindeAntraege(filterAndSort[0], filterAndSort[1])
                .pipe(catchError(() => this.translate.get('DATA_RETRIEVAL_ERROR').pipe(
                    tap(msg => this.errorService.addMesageAsError(msg)),
                    mergeMap(() => of([] as TSGemeindeAntrag[])),
                )))),
            map(gemeindeAntraege => {
                return gemeindeAntraege.map(antrag => {
                    return {
                        antragId: antrag.id,
                        gemeinde: antrag.gemeinde.name,
                        status: antrag.statusString,
                        periode: antrag.gesuchsperiode.gesuchsperiodeString,
                        antragTyp: antrag.gemeindeAntragTyp,
                        aenderungsdatum: antrag.timestampMutiert
                    };
                });
            }),
            tap(gemeindeAntraege => this.totalItems = gemeindeAntraege.length),
        );
    }

    private loadGemeinden(): void {
        this.gemeindeRS.getGemeindenForPrincipal$()
            .subscribe(
                gemeinden => {
                    this.gemeinden = gemeinden;
                    // select gemeinde if only one is returned
                    if (gemeinden.length === 1) {
                        this.formGroup.get('gemeinde').setValue(gemeinden[0].id);
                    }
                },
                err => {
                    const msg = this.translate.instant('ERR_GEMEINDEN_LADEN');
                    this.errorService.addMesageAsError(msg);
                    LOG.error(err);
                },
            );
    }

    public createAllAntraege(): void {
        if (!this.formGroup.valid) {
            this.triedSending = true;
            return;
        }
        this.gemeindeAntragService.createAllAntrage(this.formGroup.value).subscribe(() => {
            this.loadAntragList();
            this.cd.markForCheck();
            this.errorService.addMesageAsInfo(this.translate.instant('ANTRAEGE_ERSTELLT'));
        }, err => {
            this.handleCreateAntragError(err);
        });
    }

    public deleteAntraege(): void {
        if (!this.formGroup.valid) {
            this.triedSending = true;
            return;
        }
        this.openRemoveDialog$().pipe(
            concatMap(answer => {
                if (!answer) {
                    return NEVER;
                }
                return this.gemeindeAntragService.deleteAntrage(this.formGroup.value);
            })
        ).subscribe(() => {
                this.loadAntragList();
                this.cd.markForCheck();
            }, err => {
                const msg = this.translate.instant('DELETE_ANTRAEGE_ERROR');
                this.errorService.addMesageAsError(msg);
                console.error(err);
            });
    }

    private openRemoveDialog$(): Observable<boolean> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: 'WIRKLICH_LOESCHEN',
            text: '',
        };
        return this.dialog.open(DvNgRemoveDialogComponent, dialogConfig).afterClosed();
    }

    private checkDeletePossible$(): void {
        const promise = this.applicationPropertyRS.isDevMode();
        this.deletePossible$ = from(promise)
            .pipe(map(isDevmode => {
                return this.authService.isRole(TSRole.SUPER_ADMIN) && isDevmode;
            }));
    }

    private initAntragTypes(): void {
        const principal$ = this.authService.principal$.pipe(
            filter(principal => !!principal),
        );
        const properties$ = from(this.applicationPropertyRS.getPublicPropertiesCached());

        combineLatest([principal$, properties$])
            .subscribe(data => {
                this.types = this.getFilterAntragTypes(data[1]);
                if (this.types.length === 1) {
                    this.formGroup.get('antragTyp').setValue(this.types[0]);
                }
            }, error => {
                console.error(error);
            });

    }

    private getFilterAntragTypes(config: TSPublicAppConfig): TSGemeindeAntragTyp[] {
        this.types = this.gemeindeAntragService.getTypesForRole();
        if (!config.ferienbetreuungAktiv) {
            this.types = this.types.filter(d => d !== TSGemeindeAntragTyp.FERIENBETREUUNG);
        }
        if (!config.lastenausgleichTagesschulenAktiv) {
            this.types = this.types.filter(d => d !== TSGemeindeAntragTyp.LASTENAUSGLEICH_TAGESSCHULEN);
        }
        return this.types;
    }

    public createAntrag(): void {
        if (!this.formGroup.valid) {
            this.triedSending = true;
            return;
        }
        this.gemeindeAntragService.createAntrag(this.formGroup.value).subscribe(() => {
            this.loadAntragList();
            this.cd.markForCheck();
            this.errorService.addMesageAsInfo(this.translate.instant('ANTRAG_ERSTELLT'));
        }, err => {
            this.handleCreateAntragError(err);
        });
    }

    private handleCreateAntragError(error: HttpErrorResponse): void {
        const errorMessage$ = error.status === HTTP_ERROR_CODES.CONFLICT ?
            this.translate.get('GEMEINDE_ANTRAG_EXISTS_ERROR') : this.translate.get('CREATE_ANTRAG_ERROR');

        errorMessage$.subscribe(message => {
            this.errorService.addMesageAsError(message);
        }, translateError => console.error('Could no translate', translateError));
    }

    public navigate(antrag: DVAntragListItem, event: MouseEvent): void {
        const wizardTyp = this.gemeindeAntragService.gemeindeAntragTypStringToWizardStepTyp(antrag.antragTyp);
        this.wizardStepXRS.initFirstStep(wizardTyp, antrag.antragId)
            .subscribe(step => {
                const pathName = `${step.wizardTyp}.${step.stepName}`;
                const navObj = {
                    id: antrag.antragId,
                };
                if (event.ctrlKey) {
                    const url = this.$state.href(pathName, navObj);
                    window.open(url, '_blank');
                } else {
                    this.$state.go(pathName, navObj);
                }
            }, error => {
                LOG.error(error);
            });
    }

    public onFilterChange(filterChange: DVAntragListFilter): void {
        this.filterDebounceSubject.next(filterChange);
    }

    public getStateFilter(): string[] {
        return Object.keys(TSLastenausgleichTagesschuleAngabenGemeindeStatus);
    }

    public onSortChange(sortChange: { predicate?: string; reverse?: boolean }): void {
        this.sortDebounceSubject.next(sortChange);
    }

    public ferienBetreuungSelected(): boolean {
        return this.formGroup?.get('antragTyp').value === TSGemeindeAntragTyp.FERIENBETREUUNG;
    }

    public onAntragTypChange(): void {
        const gemeindeControl = this.formGroup.get('gemeinde');
        if (this.ferienBetreuungSelected()) {
            gemeindeControl.setValidators([Validators.required]);
        } else {
            gemeindeControl.clearValidators();
        }
        gemeindeControl.updateValueAndValidity({onlySelf: true, emitEvent: false});
        this.formGroup.updateValueAndValidity();
    }

    public canCreateAntrag(): Observable<boolean> {
        return this.authService.principal$.pipe(
            filter(principal => !!principal),
            map(() => this.authService.isOneOfRoles(TSRoleUtil.getFerienbetreuungRoles()))
        );
    }
}
