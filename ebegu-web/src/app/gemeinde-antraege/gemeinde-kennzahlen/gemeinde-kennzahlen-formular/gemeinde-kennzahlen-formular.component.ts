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
import {
    FormBuilder,
    NgForm,
} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {combineLatest, Observable, ReplaySubject, Subject} from 'rxjs';
import {map, takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSGemeindeKennzahlen} from '../../../../models/gemeindeantrag/gemeindekennzahlen/TSGemeindeKennzahlen';
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

    @ViewChild(NgForm) public form: NgForm;

    public canSeeSaveAndAbschliessen$: ReplaySubject<boolean> = new ReplaySubject<boolean>();
    public canSeeZurueckAnGemeinde$: ReplaySubject<boolean> = new ReplaySubject<boolean>();

    public validationTiggered: boolean = false;
    private unsubscribe$: Subject<any> = new Subject<any>();
    public antrag$: Observable<TSGemeindeKennzahlen>;

    public constructor(
        private readonly gemeindeKennzahlenService: GemeindeKennzahlenService,
        private readonly authService: AuthServiceRS,
        private readonly fb: FormBuilder,
        // TODO: replace with X
        private readonly errorService: ErrorService,
        private readonly translate: TranslateService,
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
        this.antrag$ = this.gemeindeKennzahlenService.getGemeindeKennzahlenAntrag();
    }

    public save(antrag: TSGemeindeKennzahlen): void {
        this.gemeindeKennzahlenService.saveGemeindeKennzahlen(antrag)
            .subscribe(() => this.handleSaveSuccess());
    }

    private handleSaveSuccess(): void {
        this.errorService.clearAll();
        this.errorService.addMesageAsInfo(this.translate.instant('SAVED'));
    }

    public abschliessen(antrag: TSGemeindeKennzahlen): void {
        this.validationTiggered = true;

        if (!this.form.valid) {
            return;
        }

        this.gemeindeKennzahlenService.gemeindeKennzahlenAbschliessen(antrag)
            .subscribe(() => this.handleSaveSuccess());
    }

}
