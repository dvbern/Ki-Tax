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

    public gemeinde: TSGemeinde;
    public user: TSUser;

    public user$: Observable<TSUser>;

    constructor(public readonly authServiceRS: AuthServiceRS) {
    }

    ngOnInit() {
        this.user$ = this.authServiceRS.principal$;
    }
}
