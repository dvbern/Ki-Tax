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

import {ChangeDetectionStrategy, Component, Input, OnInit, ViewEncapsulation} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {BehaviorSubject, combineLatest, Observable} from 'rxjs';
import {filter, first, map, mergeMap} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSLastenausgleichTagesschuleAngabenGemeindeStatus} from '../../../../../models/enums/TSLastenausgleichTagesschuleAngabenGemeindeStatus';
import {TSRole} from '../../../../../models/enums/TSRole';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {TSExceptionReport} from '../../../../../models/TSExceptionReport';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {DvNgConfirmDialogComponent} from '../../../../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {DvNgOkDialogComponent} from '../../../../core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';
import {ErrorService} from '../../../../core/errors/service/ErrorService';
import {LastenausgleichTSService} from '../../services/lastenausgleich-ts.service';

@Component({
    selector: 'dv-freigabe',
    templateUrl: './freigabe.component.html',
    styleUrls: ['./freigabe.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    encapsulation: ViewEncapsulation.None,
})
export class FreigabeComponent implements OnInit {

    private readonly ROUTING_DELAY = 3000; // ms

    @Input() public lastenausgleichID: string;

    private container: TSLastenausgleichTagesschuleAngabenGemeindeContainer;

    public canSeeFreigabeButton: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    public canSeeGeprueftButton: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    public canSeeZurueckGemeindeButton: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    public canSeeZurueckInPruefungButton: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    public canSeeFreigegebenText: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    public canSeeAbgeschlossenText: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

    public constructor(
        private readonly translate: TranslateService,
        private readonly errorService: ErrorService,
        private readonly latsService: LastenausgleichTSService,
        private readonly dialog: MatDialog,
        private readonly $state: StateService,
        private readonly authService: AuthServiceRS,
    ) {
    }

    public ngOnInit(): void {
        combineLatest([
            this.latsService.getLATSAngabenGemeindeContainer(),
            this.authService.principal$,
        ]).subscribe(([container, principal]) => {
            this.container = container;
            if (container.isAbgeschlossen()) {
                this.canSeeAbgeschlossenText.next(true);
            }
            if (principal.hasRole(TSRole.SUPER_ADMIN) && container.isInBearbeitungGemeinde()) {
                this.canSeeFreigabeButton.next(true);
                this.canSeeGeprueftButton.next(false);
                this.canSeeZurueckGemeindeButton.next(false);
                this.canSeeFreigegebenText.next(false);
            }
            if (principal.hasOneOfRoles(TSRoleUtil.getMandantRoles()) && container.isInBearbeitungKanton()) {
                this.canSeeFreigabeButton.next(false);
                this.canSeeGeprueftButton.next(true);
                this.canSeeZurueckGemeindeButton.next(true);
                this.canSeeZurueckInPruefungButton.next(false);
                this.canSeeFreigegebenText.next(false);
            }
            if (principal.hasOneOfRoles(TSRoleUtil.getGemeindeOrBGOrTSRoles())) {
                this.canSeeFreigabeButton.next(container.isInBearbeitungGemeinde());
                this.canSeeGeprueftButton.next(false);
                this.canSeeZurueckGemeindeButton.next(false);
                this.canSeeFreigegebenText.next(container.isAtLeastInBearbeitungKanton());
            }
            // tslint:disable-next-line:early-exit
            if (container.isGeprueft()) {
                this.canSeeFreigabeButton.next(false);
                this.canSeeGeprueftButton.next(false);
                this.canSeeZurueckGemeindeButton.next(false);
                this.canSeeZurueckInPruefungButton.next(principal.hasOneOfRoles(TSRoleUtil.getMandantRoles()));
                this.canSeeFreigegebenText.next(principal.hasOneOfRoles(TSRoleUtil.getGemeindeOrBGOrTSRoles()));
            }
        }, () => this.errorService.addMesageAsInfo(this.translate.instant('DATA_RETRIEVAL_ERROR')));
    }

    public freigeben(): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: this.translate.instant('LATS_FRAGE_GEMEINDE_ANTRAG_FREIGABE'),
        };
        this.dialog.open(DvNgConfirmDialogComponent, dialogConfig)
            .afterClosed()
            .pipe(
                filter(result => !!result),
                mergeMap(() => this.latsService.getLATSAngabenGemeindeContainer().pipe(first())),
                mergeMap(container => this.latsService.latsGemeindeAntragFreigeben(container)),
            )
            .subscribe(() => {
                this.$state.go('gemeindeantraege.view');
            }, (errors: TSExceptionReport[]) => {
                // tslint:disable-next-line:early-exit
                errors.forEach(error => {
                        if (error.customMessage.includes('angabenDeklaration')) {
                            this.errorService.addMesageAsError(this.translate.instant('LATS_GEMEINDE_ANGABEN_ERROR'));
                            setTimeout(() => this.$state.go('LASTENAUSGLEICH_TAGESSCHULEN.ANGABEN_GEMEINDE',
                                    {triggerValidation: true},
                                    {}),
                                this.ROUTING_DELAY);
                        } else if (error.customMessage.includes('LastenausgleichAngabenInstitution')) {
                            this.errorService.addMesageAsError(this.translate.instant(
                                'LATS_NICHT_ALLE_INSTITUTIONEN_ABGESCHLOSSEN'));
                            setTimeout(() => this.$state.go('LASTENAUSGLEICH_TAGESSCHULEN.ANGABEN_TAGESSCHULEN.LIST'),
                                this.ROUTING_DELAY);
                        }
                });
            });
    }

    public isInBearbeitungGemeinde(): Observable<boolean> {
        return this.latsService.getLATSAngabenGemeindeContainer().pipe(
            map(latsContainer => latsContainer.isInBearbeitungGemeinde()),
        );
    }

    public geprueft(): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: this.translate.instant('LATS_FRAGE_GEMEINDE_ANTRAG_FREIGABE_GEPRUEFT'),
        };
        this.dialog.open(DvNgConfirmDialogComponent, dialogConfig)
            .afterClosed()
            .pipe(
                filter(result => !!result),
                mergeMap(() => this.latsService.getLATSAngabenGemeindeContainer().pipe(first())),
                mergeMap(container => this.latsService.latsGemeindeAntragGeprueft(container))
            ).subscribe(container => {
                // tslint:disable-next-line:early-exit
                if (container.isInZweitPruefung()) {
                    const dialogConfigInfo = new MatDialogConfig();
                    dialogConfigInfo.data = {
                        title: this.translate.instant('LATS_INFO_SELECTED_FOR_ZWEITPRUEFUNG')
                    };
                    this.dialog.open(DvNgOkDialogComponent, dialogConfigInfo);
                }
            },
            () => this.errorService.addMesageAsError(this.translate.instant('ERROR_UNEXPECTED')));
    }

    public async zurueckAnGemeinde(): Promise<void> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: this.translate.instant('ZURUECK_AN_GEMEINDE_GEBEN'),
        };
        if (!await (this.dialog.open(DvNgConfirmDialogComponent, dialogConfig))
            .afterClosed()
            .toPromise()) {
            return;
        }
        this.latsService.zurueckAnGemeinde(this.container)
            .subscribe(() => this.$state.go('LASTENAUSGLEICH_TAGESSCHULEN.ANGABEN_GEMEINDE', {id: this.container.id}),
                () => this.errorService.addMesageAsError(this.translate.instant('ERROR_UNEXPECTED')));
    }

    public isInPruefungKanton(): Observable<boolean> {
        return this.latsService.getLATSAngabenGemeindeContainer().pipe(
            map(latsContainer => latsContainer.status ===
                TSLastenausgleichTagesschuleAngabenGemeindeStatus.IN_PRUEFUNG_KANTON ||
                latsContainer.status === TSLastenausgleichTagesschuleAngabenGemeindeStatus.ZWEITPRUEFUNG),
        );

    }

    public isReadyForGeprueft(): boolean {
        return this.container?.isInBearbeitungKanton() && this.container?.angabenKorrektur.isAbgeschlossen();
    }

    public isGeprueft(): Observable<boolean> {
        return this.latsService.getLATSAngabenGemeindeContainer().pipe(
            map(container => container.isGeprueft())
        );
    }

    public async zurueckInPruefung(): Promise<void> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: this.translate.instant('ZURUECK_IN_PRUEFUNG'),
        };
        if (!await (this.dialog.open(DvNgConfirmDialogComponent, dialogConfig))
            .afterClosed()
            .toPromise()) {
            return;
        }
        this.latsService.zurueckInPruefung(this.container)
            .subscribe(() => {},
                () => this.errorService.addMesageAsError(this.translate.instant('ERROR_UNEXPECTED')));
    }
}
