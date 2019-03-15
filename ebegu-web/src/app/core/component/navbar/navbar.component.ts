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

import {
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnDestroy
} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material';
import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {GuidedTourService, Orientation} from 'ngx-guided-tour';
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
import {KiBonGuidedTour, KiBonTourStep} from '../../../kibonTour/KiBonGuidedTour';
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
        private guidedTourService: GuidedTourService,
        private readonly state: StateService,
        public readonly translate: TranslateService
    ) {

        // navbar depends on the principal. trigger change detection when the principal changes
        this.authServiceRS.principal$
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(
                () => this.changeDetectorRef.markForCheck(),
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

        // TODO: Text content in DE
        // TODO: FR Translations
        // TODO: Styling
        // TODO: Welcome dialog analog invision design
        // TODO: End dialog analog invision design
        // TODO: Link to start tour again in help dialog
        // TODO: Restrict tour startup based on role and cookie value "AlreadyViewedTour"
        // TODO: Consider replacing CSS selectors like "a[uisref="pendenzen.list-view"]" with ids

        this.guidedTourService.startTour(new KiBonGuidedTour('kibon-tour',
            [new KiBonTourStep(this.translate.instant('TOUR_START_TITLE'), this.translate.instant('TOUR_START_CONTENT'),
                '', Orientation.Center, this.state, 'pendenzen.list-view'),

                new KiBonTourStep(this.translate.instant('TOUR_STEP_1_TITLE'), this.translate.instant('TOUR_STEP_1_CONTENT'),
                    'a[uisref="pendenzen.list-view"]', Orientation.Bottom, this.state, 'faelle.list'),

                new KiBonTourStep(this.translate.instant('TOUR_STEP_2_TITLE'), this.translate.instant('TOUR_STEP_2_CONTENT'),
                    'a[uisref="faelle.list"]', Orientation.Bottom, this.state, 'zahlungsauftrag.view'),

                new KiBonTourStep(this.translate.instant('TOUR_STEP_3_TITLE'), this.translate.instant('TOUR_STEP_3_CONTENT'),
                    'a[uisref="zahlungsauftrag.view"]', Orientation.Bottom, this.state, 'statistik.view'),

                new KiBonTourStep(this.translate.instant('TOUR_STEP_4_TITLE'), this.translate.instant('TOUR_STEP_4_CONTENT'),
                    'a[uisref="statistik.view"]', Orientation.Bottom, this.state, 'posteingang.view'),

                new KiBonTourStep(this.translate.instant('TOUR_STEP_5_TITLE'), this.translate.instant('TOUR_STEP_5_CONTENT'),
                    'dv-posteingang[uisref="posteingang.view"]', Orientation.Bottom),

                new KiBonTourStep(this.translate.instant('TOUR_END_TITLE'), this.translate.instant('TOUR_END_CONTENT'),
                    '[class~="dv-helpmenu-question"]', Orientation.Left)],
            false
        ));
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
