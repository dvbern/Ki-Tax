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
import {TranslateService} from '@ngx-translate/core';
import {combineLatest, Observable, ReplaySubject, Subject} from 'rxjs';
import {map, takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSGemeindeKennzahlen} from '../../../../models/gemeindeantrag/gemeindekennzahlen/TSGemeindeKennzahlen';
import {TSBenutzer} from '../../../../models/TSBenutzer';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {ErrorService} from '../../../core/errors/service/ErrorService';
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

    public validationTiggered: boolean = false;
    private readonly unsubscribe$: Subject<any> = new Subject<any>();
    public antragAndPrincipal$: Observable<[TSGemeindeKennzahlen, (TSBenutzer | null)]>;

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
        this.gemeindeKennzahlenService.saveGemeindeKennzahlen(antrag)
            .subscribe(() => this.handleSaveSuccess(), err => LOG.error(err));
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
            .subscribe(() => this.handleSaveSuccess(), error => LOG.error(error));
    }

    public isFormDisabledFor([antrag, principal]: [TSGemeindeKennzahlen, (TSBenutzer | null)]): boolean {
        if (principal === null) {
            return true;
        }
        return antrag.isAbgeschlossen() || !principal.hasOneOfRoles(TSRoleUtil.getGemeindeOrBGRoles()
            .concat(TSRole.SUPER_ADMIN));
    }

    public zurueckAnGemeinde(antrag: TSGemeindeKennzahlen): void {
        this.gemeindeKennzahlenService.gemeindeKennzahlenZurueckAnGemeinde(antrag).subscribe(() => {
        }, error => LOG.error(error));
    }
}
