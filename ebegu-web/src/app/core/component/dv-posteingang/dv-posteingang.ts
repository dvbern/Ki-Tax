/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

import {ChangeDetectionStrategy, Component, OnDestroy} from '@angular/core';
import {from, merge, Observable, of, Subject, timer} from 'rxjs';
import {switchMap, takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSPostEingangEvent} from '../../../../models/enums/TSPostEingangEvent';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {PosteingangService} from '../../../posteingang/service/posteingang.service';
import {Log, LogFactory} from '../../logging/LogFactory';
import {MitteilungRS} from '../../service/mitteilungRS.rest';

@Component({
    selector: 'dv-posteingang',
    templateUrl: './dv-posteingang.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DvPosteingangComponent implements OnDestroy {

    private readonly log: Log = LogFactory.createLog('DvPosteingangComponent');

    private readonly unsubscribe$ = new Subject<void>();
    public mitteilungenCount$: Observable<number>;

    public constructor(
        private readonly mitteilungRS: MitteilungRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly posteingangService: PosteingangService,
    ) {

        const posteingangeChanged$ = this.posteingangService.get$(TSPostEingangEvent.POSTEINGANG_MIGHT_HAVE_CHANGED)
            .pipe(switchMap(() => this.getMitteilungenCount$()));

        this.mitteilungenCount$ = merge(posteingangeChanged$, this.getCountForPrincipal$())
            .pipe(takeUntil(this.unsubscribe$));
    }

    private getCountForPrincipal$(): Observable<number> {
        return this.authServiceRS.principal$
            .pipe(
                switchMap(principal => {
                    if (!principal) {
                        return of(0);
                    }

                    // not for GS
                    if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorOrAmtRole())
                        || principal.hasOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionRoles())
                        || principal.hasOneOfRoles(TSRoleUtil.getSozialdienstRolle())) {
                        const fiveMin = 300000;
                        return timer(0, fiveMin)
                            .pipe(takeUntil(this.unsubscribe$))
                            .pipe(switchMap(() => this.getMitteilungenCount$()));
                    }

                    if (principal.hasOneOfRoles(TSRoleUtil.getGesuchstellerOnlyRoles())) {
                        return this.getMitteilungenCount$();
                    }

                    return of(0);
                }),
            );
    }

    private getMitteilungenCount$(): Observable<number> {
        return from(this.mitteilungRS.getAmountMitteilungenForCurrentBenutzer()
            .then(response => !response || isNaN(response) ? 0 : response)
            .catch(() => {
                // Fehler bei deisem request (notokenrefresh )werden bis hier ohne Behandlung
                // (unerwarteter Fehler anzeige, redirect etc.) weitergeschlauft
                this.log.debug('received error message while reading posteingang. Ignoring ...');
                return 0;
            }));
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }
}
