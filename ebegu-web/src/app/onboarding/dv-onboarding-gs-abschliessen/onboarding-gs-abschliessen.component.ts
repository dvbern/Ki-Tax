import {ChangeDetectionStrategy, Component} from '@angular/core';
import {Observable} from 'rxjs/index';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
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
    public gemeinde: TSGemeinde;

    constructor(
        public readonly authServiceRS: AuthServiceRS,
        public readonly gemeindeRS: GemeindeRS) {
    }

    ngOnInit() {
        this.user$ = this.authServiceRS.principal$;
        //TODO: Das Dossier muss zu diesem Zeitpunkt schon bestehen! Muss (wenn principal da ist) vom Server gelesen werden
        //TODO: Vorerst verwenden wir fix die Gemeinde Ostermundigen. Wir hoffen dass wir die Gemeinde dann erst auf dieser Maske selektieren lassen können
        this.gemeindeRS.findGemeinde('80a8e496-b73c-4a4a-a163-a0b2caf76487').then(foundGemeinde => {
            this.gemeinde = foundGemeinde;
        });
    }
}
