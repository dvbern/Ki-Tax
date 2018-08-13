import {ChangeDetectionStrategy, Component} from '@angular/core';
import {NgForm} from '@angular/forms';
import {IPromise} from 'angular';
import {Observable} from 'rxjs/index';
import {take} from 'rxjs/internal/operators';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import TSGemeinde from '../../../models/TSGemeinde';
import TSUser from '../../../models/TSUser';
import {Transition} from '@uirouter/core';
import {StateService} from '@uirouter/core';

@Component({
    selector: 'dv-onboarding-gs-abschliessen',
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './onboarding-gs-abschliessen.component.html',
    styleUrls: ['./onboarding-gs-abschliessen.component.less'],
})
export class OnboardingGsAbschliessenComponent {

    public user$: Observable<TSUser>;
    private user: TSUser;

    public gemeinde: TSGemeinde;
    private readonly gemeindeId: string;

    constructor(
        private readonly transition: Transition,
        public readonly authServiceRS: AuthServiceRS,
        public readonly gemeindeRS: GemeindeRS,
        private readonly stateService: StateService) {

        this.gemeindeId = this.transition.params().gemeindeId;
    }

    ngOnInit() {
        this.gemeindeRS.findGemeinde(this.gemeindeId).then(foundGemeinde => {
            this.gemeinde = foundGemeinde;
        });
        this.user$ = this.authServiceRS.principal$;
        this.user$.subscribe(value => this.user = value);
    }

    public onSubmit(form: NgForm): void {
        if (!form.valid) {
            return;
        }
        console.log('erstelle Dossier fuer', this.user, this.gemeinde);
        this.stateService.go('gesuchsteller.dashboard', {gemeindeId: this.gemeinde.id});
    }
}
