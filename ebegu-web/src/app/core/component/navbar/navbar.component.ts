/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import {AfterViewInit, ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material';
import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {GuidedTourService} from 'ngx-guided-tour';
import {from as fromPromise, Observable, of, Subject} from 'rxjs';
import {filter, map, switchMap, take, takeUntil} from 'rxjs/operators';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import {INewFallStateParams} from '../../../../gesuch/gesuch.route';
import GemeindeRS from '../../../../gesuch/service/gemeindeRS.rest';
import {TSCreationAction} from '../../../../models/enums/TSCreationAction';
import {TSEingangsart} from '../../../../models/enums/TSEingangsart';
import {TSRole} from '../../../../models/enums/TSRole';
import TSGemeinde from '../../../../models/TSGemeinde';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {KiBonGuidedTourService} from '../../../kibonTour/service/KiBonGuidedTourService';
import {
    AdminInstitutionGuidedTour,
    GemeindeGuidedTour,
    InstitutionGuidedTour
} from '../../../kibonTour/shared/KiBonGuidedTour';
import {LogFactory} from '../../logging/LogFactory';
import {DvNgGemeindeDialogComponent} from '../dv-ng-gemeinde-dialog/dv-ng-gemeinde-dialog.component';

const LOG = LogFactory.createLog('NavbarComponent');

@Component({
    selector: 'dv-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NavbarComponent implements OnDestroy, AfterViewInit {

    public readonly TSRoleUtil = TSRoleUtil;

    private readonly unsubscribe$ = new Subject<void>();

    public constructor(
        private readonly authServiceRS: AuthServiceRS,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly dialog: MatDialog,
        private readonly $state: StateService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly guidedTourService: GuidedTourService,
        private readonly translate: TranslateService,
        private readonly kibonGuidedTourService: KiBonGuidedTourService
    ) {

        // navbar depends on the principal. trigger change detection when the principal changes

        this.authServiceRS.principal$
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(
                () => {
                    this.changeDetectorRef.markForCheck();
                },
                err => LOG.error(err),
            );
        this.kibonGuidedTourService.guidedTour$.pipe(takeUntil(this.unsubscribe$))
            .subscribe(
                next => {
                    this.tourStart(next);
                    this.changeDetectorRef.markForCheck();
                },
                err => LOG.error(err),
            );
    }

    public createNewFall(): void {
        this.getGemeindeIDFromUser$()
            .pipe(
                take(1),
                filter(gemeindeId => !!gemeindeId),
            )
            .subscribe(
                gemeindeId => {
                    const params: INewFallStateParams = {
                        gesuchsperiodeId: null,
                        creationAction: TSCreationAction.CREATE_NEW_FALL,
                        gesuchId: null,
                        dossierId: null,
                        gemeindeId,
                        eingangsart: TSEingangsart.PAPIER,
                    };
                    this.$state.go('gesuch.fallcreation', params);
                }
                ,
                err => LOG.error(err),
            );
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    public ngAfterViewInit(): void {
        this.tourStart(false);
    }

    public tourStart(start: boolean): void {

        if (!start) {
            return;
        }
        const roleLoggedIn = this.authServiceRS.getPrincipalRole();

        switch (roleLoggedIn) {
            case TSRole.ADMIN_TRAEGERSCHAFT:
                this.guidedTourService.startTour(new AdminInstitutionGuidedTour(this.$state, this.translate));
                break;
            case TSRole.SACHBEARBEITER_TRAEGERSCHAFT:
            case TSRole.JURIST:
            case TSRole.REVISOR:
            case TSRole.STEUERAMT:
            case TSRole.ADMIN_MANDANT:
            case TSRole.SACHBEARBEITER_MANDANT:
            case TSRole.ANONYMOUS:
            case TSRole.GESUCHSTELLER:
            case TSRole.SACHBEARBEITER_BG:
            case TSRole.SACHBEARBEITER_GEMEINDE:
                this.guidedTourService.startTour(new GemeindeGuidedTour(this.$state, this.translate));
                break;
            case TSRole.SACHBEARBEITER_INSTITUTION:
                this.guidedTourService.startTour(new InstitutionGuidedTour(this.$state, this.translate));
                break;
            case TSRole.ADMIN_INSTITUTION:
                this.guidedTourService.startTour(new AdminInstitutionGuidedTour(this.$state, this.translate));
                break;
            case TSRole.SACHBEARBEITER_TS:
            case TSRole.ADMIN_BG:
                this.guidedTourService.startTour(new GemeindeGuidedTour(this.$state, this.translate));
                break;
            case TSRole.ADMIN_GEMEINDE:
                this.guidedTourService.startTour(new GemeindeGuidedTour(this.$state, this.translate));
                break;
            case TSRole.SUPER_ADMIN:
            case TSRole.ADMIN_TS:
            default:
        }

    }

    private getGemeindeIDFromUser$(): Observable<string> {
        return this.authServiceRS.principal$
            .pipe(
                switchMap(principal => {
                    if (principal && principal.hasJustOneGemeinde()) {
                        return of(principal.extractCurrentGemeindeId());
                    }

                    return this.getListOfGemeinden$()
                        .pipe(
                            switchMap(gemeindeList => {
                                const dialogConfig = new MatDialogConfig();
                                dialogConfig.data = {gemeindeList};

                                return this.dialog.open(DvNgGemeindeDialogComponent, dialogConfig).afterClosed();
                            }),
                        );
                }),
            );
    }

    /**
     * Fuer den SUPER_ADMIN muessen wir die gesamte Liste von Gemeinden zurueckgeben, da er zu keiner Gemeinde gehoert
     * aber alles machen darf. Fuer andere Benutzer geben wir die Liste von Gemeinden zurueck, zu denen er gehoert.
     */
    private getListOfGemeinden$(): Observable<TSGemeinde[]> {
        if (this.authServiceRS.isRole(TSRole.SUPER_ADMIN)) {
            return fromPromise(this.gemeindeRS.getAktiveGemeinden());
        }

        return this.authServiceRS.principal$
            .pipe(map(p => p.extractCurrentAktiveGemeinden()));
    }
}
