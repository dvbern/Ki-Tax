import {ChangeDetectionStrategy, Component} from '@angular/core';
import {Observable} from 'rxjs/index';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
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

    constructor(public readonly authServiceRS: AuthServiceRS) {
    }

    ngOnInit() {
        this.user$ = this.authServiceRS.principal$;
        //TODO: Das Dossier muss zu diesem Zeitpunkt schon bestehen! Muss (wenn principal da ist) vom Server gelesen werden
        this.gemeinde = new TSGemeinde();
        this.gemeinde.name = 'Bern';
    }
}
