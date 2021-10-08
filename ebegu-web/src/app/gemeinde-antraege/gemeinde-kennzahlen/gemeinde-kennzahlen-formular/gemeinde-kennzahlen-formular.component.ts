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
import {ChangeDetectionStrategy, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {combineLatest, Observable, ReplaySubject, Subject} from 'rxjs';
import {map, takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSGemeindeKennzahlen} from '../../../../models/gemeindeantrag/gemeindekennzahlen/TSGemeindeKennzahlen';
import {TSBenutzer} from '../../../../models/TSBenutzer';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {DvNgConfirmDialogComponent} from '../../../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {CONSTANTS} from '../../../core/constants/CONSTANTS';
import {ErrorServiceX} from '../../../core/errors/service/ErrorServiceX';
import {LogFactory} from '../../../core/logging/LogFactory';
import {GemeindeKennzahlenService} from '../gemeinde-kennzahlen.service';

const LOG = LogFactory.createLog('GemeindeKennzahlenFormularComponent');

@Component({
    selector: 'dv-gemeinde-kennzahlen-formular',
    templateUrl: './gemeinde-kennzahlen-formular.component.html',
    styleUrls: ['./gemeinde-kennzahlen-formular.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GemeindeKennzahlenFormularComponent implements OnInit, OnDestroy {

    @ViewChild(NgForm) public form: NgForm;

    public canSeeSaveAndAbschliessen$: ReplaySubject<boolean> = new ReplaySubject<boolean>();
    public canSeeZurueckAnGemeinde$: ReplaySubject<boolean> = new ReplaySubject<boolean>();

    public abschlussValidationTriggered: boolean = false;
    private readonly unsubscribe$: Subject<any> = new Subject<any>();
    public antragAndPrincipal$: Observable<[TSGemeindeKennzahlen, (TSBenutzer | null)]>;

    public readonly CONSTANTS = CONSTANTS;

    public constructor(
        private readonly gemeindeKennzahlenService: GemeindeKennzahlenService,
        private readonly authService: AuthServiceRS,
        private readonly fb: FormBuilder,
        private readonly errorService: ErrorServiceX,
        private readonly translate: TranslateService,
        private readonly dialog: MatDialog,
    ) {
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
            map(([isInBearbeitungGemeinde, isGemeindeBgSuperAdminRole]) => isInBearbeitungGemeinde && isGemeindeBgSuperAdminRole),
            takeUntil(this.unsubscribe$),
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
            map(([isAbgeschlossen, isMandantSuperAdmin]) => isAbgeschlossen && isMandantSuperAdmin),
            takeUntil(this.unsubscribe$),
        ).subscribe(this.canSeeZurueckAnGemeinde$);
    }

    private setupForm(): void {
        this.antragAndPrincipal$ = combineLatest([
            this.gemeindeKennzahlenService.getGemeindeKennzahlenAntrag(),
            this.authService.principal$,
        ]);
    }

    public save(antrag: TSGemeindeKennzahlen): void {

        if (!this.form.valid) {
            return;
        }
        this.gemeindeKennzahlenService.saveGemeindeKennzahlen(antrag)
            .subscribe(() => this.handleSaveSuccess(), err => LOG.error(err));
    }

    private handleSaveSuccess(): void {
        this.errorService.clearAll();
        this.errorService.addMesageAsInfo(this.translate.instant('SAVED'));
    }

    public async abschliessen(antrag: TSGemeindeKennzahlen): Promise<void> {
        this.abschlussValidationTriggered = true;

        setTimeout(async () => {
            if (!this.form.valid || !await this.confirmDialog('FRAGE_FORMULAR_ABSCHLIESSEN')) {
                return;
            }

            if (!antrag.gemeindeKontingentiert) {
                antrag.nachfrageDauer = null;
                antrag.nachfrageErfuellt = null;
                antrag.nachfrageAnzahl = null;
            }

            this.gemeindeKennzahlenService.gemeindeKennzahlenAbschliessen(antrag)
                .subscribe(() => this.handleSaveSuccess(), error => LOG.error(error));
        }, 0);
    }

    public isFormDisabledFor([antrag, principal]: [TSGemeindeKennzahlen, (TSBenutzer | null)]): boolean {
        if (principal === null) {
            return true;
        }
        return antrag.isAbgeschlossen() || !principal.hasOneOfRoles(TSRoleUtil.getGemeindeOrBGRoles()
            .concat(TSRole.SUPER_ADMIN));
    }

    public async zurueckAnGemeinde(antrag: TSGemeindeKennzahlen): Promise<void> {
        if (!await this.confirmDialog('ZURUECK_AN_GEMEINDE_DIREKT')) {
            return;
        }
        this.gemeindeKennzahlenService.gemeindeKennzahlenZurueckAnGemeinde(antrag).subscribe(() => {
            this.abschlussValidationTriggered = false;
        }, error => LOG.error(error));
    }

    private confirmDialog(frageKey: string): Promise<boolean> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: this.translate.instant(frageKey),
        };
        return this.dialog.open(DvNgConfirmDialogComponent, dialogConfig)
            .afterClosed()
            .toPromise();
    }
}
