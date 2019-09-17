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
import {TSRole} from '../../../models/enums/TSRole';
import TSBenutzer from '../../../models/TSBenutzer';
import TSDossier from '../../../models/TSDossier';
import TSGemeinde from '../../../models/TSGemeinde';
import {LogFactory} from '../../core/logging/LogFactory';
import {OnboardingPlaceholderService} from '../service/onboarding-placeholder.service';

const LOG = LogFactory.createLog('OnboardingGsAbschliessenComponent');

@Component({
    selector: 'dv-onboarding-gs-abschliessen',
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './onboarding-gs-abschliessen.component.html',
    styleUrls: ['./onboarding-gs-abschliessen.component.less', '../onboarding.less'],
})
export class OnboardingGsAbschliessenComponent implements OnInit {

    public user$: Observable<TSBenutzer>;
    public gemeinde$: Observable<TSGemeinde>;

    private readonly gemeindeId: string; // Parameter aus URL

    public constructor(
        private readonly transition: Transition,
        public readonly authServiceRS: AuthServiceRS,
        public readonly gemeindeRS: GemeindeRS,
        private readonly stateService: StateService,
        private readonly dossierRS: DossierRS,
        private readonly onboardingPlaceholderService: OnboardingPlaceholderService,
    ) {
        this.gemeindeId = this.transition.params().gemeindeId;
    }

    public ngOnInit(): void {
        this.gemeinde$ = from(this.gemeindeRS.findGemeinde(this.gemeindeId));
        this.user$ = this.authServiceRS.principal$;

        if (this.stateService.transition) {
            this.onboardingPlaceholderService.setSplittedScreen(true);
        } else {
            this.onboardingPlaceholderService.setSplittedScreen(false);
        }
    }

    public onSubmit(form: NgForm): void {
        if (!form.valid) {
            return;
        }
        this.dossierRS.getOrCreateDossierAndFallForCurrentUserAsBesitzer(this.gemeindeId).then((dossier: TSDossier) => {
            this.stateService.go('gesuchsteller.dashboard', {
                dossierId: dossier.id,
            });
        });
    }

    public changeGemeinde(): void {
        switch (this.authServiceRS.getPrincipalRole()) {
            case TSRole.GESUCHSTELLER:
                this.onboardingPlaceholderService.setSplittedScreen(true);
                this.stateService.go('onboarding.gesuchsteller.registration-incomplete');
                break;
            case TSRole.ANONYMOUS:
                this.onboardingPlaceholderService.setSplittedScreen(true);
                this.stateService.go('onboarding.start');
                break;
            default:
                LOG.error('User has no possible onboarding page for role');
                break;
        }
    }
}
