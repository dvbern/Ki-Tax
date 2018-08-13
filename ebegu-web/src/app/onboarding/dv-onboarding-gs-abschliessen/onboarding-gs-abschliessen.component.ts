import {ChangeDetectionStrategy, Component} from '@angular/core';
import {NgForm} from '@angular/forms';
import {StateService, Transition} from '@uirouter/core';
import {from, Observable} from 'rxjs/index';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import DossierRS from '../../../gesuch/service/dossierRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import TSDossier from '../../../models/TSDossier';
import TSGemeinde from '../../../models/TSGemeinde';
import TSUser from '../../../models/TSUser';

@Component({
    selector: 'dv-onboarding-gs-abschliessen',
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './onboarding-gs-abschliessen.component.html',
    styleUrls: ['./onboarding-gs-abschliessen.component.less'],
})
export class OnboardingGsAbschliessenComponent {

    public user$: Observable<TSUser>;
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
                gesuchstellerDashboardStateParams: {dossierId: dossier.id}
            });
        });
    }
}
