import {ChangeDetectionStrategy, Component} from '@angular/core';
import {from, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import TSGemeinde from '../../../models/TSGemeinde';
import {LogFactory} from '../../core/logging/LogFactory';

const LOG = LogFactory.createLog('OnboardingComponent');

@Component({
    selector: 'dv-onboarding',
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './onboarding.component.html',
    styleUrls: ['./onboarding.component.less'],
})
export class OnboardingComponent {
    public gemeinden$: Observable<TSGemeinde[]>;
    public gemeinde?: TSGemeinde;

    constructor(private readonly gemeindeRs: GemeindeRS) {
        this.gemeinden$ = from(this.gemeindeRs.getAllGemeinden())
            .pipe(map(gemeinden => gemeinden.sort((a, b) => a.name.localeCompare(b.name))));
    }

    public onSubmit(): void {
        LOG.info('submitted', this.gemeinde);
    }
}
