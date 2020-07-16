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
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {DossierRS} from '../../../gesuch/service/dossierRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSDossier} from '../../../models/TSDossier';
import {TSGemeindeRegistrierung} from '../../../models/TSGemeindeRegistrierung';
import {EbeguUtil} from '../../../utils/EbeguUtil';
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
    public gemeindenAndVerbund$: Observable<TSGemeindeRegistrierung[]>;

    private readonly gemeindenTSIds: string; // Parameter aus URL
    private readonly gemeindeBGId: string;

    public constructor(
        private readonly transition: Transition,
        public readonly authServiceRS: AuthServiceRS,
        public readonly gemeindeRS: GemeindeRS,
        private readonly stateService: StateService,
        private readonly dossierRS: DossierRS,
        private readonly onboardingPlaceholderService: OnboardingPlaceholderService,
    ) {
        this.gemeindenTSIds = this.transition.params().gemeindenId;
        this.gemeindeBGId = this.transition.params().gemeindeBGId;
    }

    public ngOnInit(): void {
        const gemeindenTSIdList = this.gemeindenTSIds.split(',');
        this.gemeindenAndVerbund$ = from(this.gemeindeRS
            .getGemeindenRegistrierung(this.gemeindeBGId, gemeindenTSIdList));
        this.user$ = this.authServiceRS.principal$;

        if (this.stateService.transition) {
            this.onboardingPlaceholderService.setSplittedScreen(true);
        } else {
            this.onboardingPlaceholderService.setSplittedScreen(false);
        }
    }

    public createDossier(form: NgForm, gemList: TSGemeindeRegistrierung[]): void {
        if (!form.valid) {
            return;
        }
        // Die erste Gemeinde muss speziell behandelt werden: Fuer diese muss sichergestellt werden, dass das
        // Dossier und der Fall erstellt werden, bevor die weiteren Gemeinden asynchron und parallel erstellt werden
        const gemeindenAdded: string[] = [];
        const firstGemeinde = gemList.pop();
        let firstGemeindeId: string;
        if (EbeguUtil.isNotNullOrUndefined(firstGemeinde)) {
            firstGemeindeId = firstGemeinde.verbundId === null ? firstGemeinde.id : firstGemeinde.verbundId;
        }
        gemeindenAdded.push(firstGemeindeId);
        if (EbeguUtil.isNullOrUndefined(firstGemeindeId)) {
            // Gemaess Sentry 36245 ist hier die ID ab und zu undefined -> herausfinden welche und warum
            LOG.error('firstGemeinde ID undefined', firstGemeinde);
        }
        this.dossierRS.getOrCreateDossierAndFallForCurrentUserAsBesitzer(firstGemeindeId).then((dossier: TSDossier) => {
            gemList.forEach(tsGemeindeRegistrierung => {
                // Das Dossier wird für den Verbund erstellt, falls einer vorhanden ist, sonst für die Gemeinde
                const gemeindeIdForDossier = EbeguUtil.isNullOrUndefined(tsGemeindeRegistrierung.verbundId) ?
                    tsGemeindeRegistrierung.id : tsGemeindeRegistrierung.verbundId;
                // In der Liste sind jetzt immer noch Duplikate, im Sinne von
                // Gemeinde A (Verbund 1), Gemeinde B (Verbund 1) => für diese Konstellation soll nur
                // 1 Dossier (für Verbund 1) erstellt werden
                if (gemeindenAdded.indexOf(gemeindeIdForDossier) !== -1) {
                    return;
                }
                if (EbeguUtil.isNullOrUndefined(gemeindeIdForDossier)) {
                    // Gemaess Sentry 36245 ist hier die ID ab und zu undefined -> herausfinden welche und warum
                    LOG.error('tsGemeindeRegistrierung ID undefined', tsGemeindeRegistrierung);
                }
                this.dossierRS.getOrCreateDossierAndFallForCurrentUserAsBesitzer(gemeindeIdForDossier);
                gemeindenAdded.push(gemeindeIdForDossier);
            });
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
