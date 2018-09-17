/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {NgForm} from '@angular/forms';
import {StateService, Transition} from '@uirouter/core';
import {from, Observable} from 'rxjs';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import DossierRS from '../../../gesuch/service/dossierRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import TSDossier from '../../../models/TSDossier';
import TSGemeinde from '../../../models/TSGemeinde';
import TSBenutzer from '../../../models/TSBenutzer';

@Component({
    selector: 'dv-onboarding-gs-abschliessen',
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './onboarding-gs-abschliessen.component.html',
    styleUrls: ['../onboarding.less', './onboarding-gs-abschliessen.component.less'],
})
export class OnboardingGsAbschliessenComponent implements OnInit {

    public user$: Observable<TSBenutzer>;
    public gemeinde$: Observable<TSGemeinde>;

    private readonly gemeindeId: string; // Parameter aus URL

    constructor(
        private readonly transition: Transition,
        public readonly authServiceRS: AuthServiceRS,
        public readonly gemeindeRS: GemeindeRS,
        private readonly stateService: StateService,
        private readonly dossierRS: DossierRS) {

        this.gemeindeId = this.transition.params().gemeindeId;
    }

    ngOnInit() {
        this.gemeinde$ = from(this.gemeindeRS.findGemeinde(this.gemeindeId));
        this.user$ = this.authServiceRS.principal$;
    }

    public onSubmit(form: NgForm): void {
        if (!form.valid) {
            return;
        }
        this.dossierRS.getOrCreateDossierAndFallForCurrentUserAsBesitzer(this.gemeindeId).then((dossier: TSDossier) => {
            this.stateService.go('gesuchsteller.dashboard', {
                dossierId: dossier.id
            });
        });
    }
}
