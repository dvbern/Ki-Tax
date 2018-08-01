import {ChangeDetectionStrategy, Component} from '@angular/core';
import {Observable, from} from 'rxjs';
import {map} from 'rxjs/operators';
import GemeindeRS from '../../../../gesuch/service/gemeindeRS.rest';
import TSGemeinde from '../../../../models/TSGemeinde';

require('./dv-onboarding.component.less');

@Component({
    selector: 'dv-onboarding',
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: require('./dv-onboarding.component.html'),
})
export class DvOnboardingComponent {
    public gemeinden$: Observable<TSGemeinde[]>;
    public gemeinde?: TSGemeinde;

    constructor(private readonly gemeindeRs: GemeindeRS) {
        console.log('tegege', this.gemeindeRs);
        this.gemeinden$ = from(this.gemeindeRs.getAllGemeinden())
            .pipe(map(gemeinden => gemeinden.sort((a, b) => a.name.localeCompare(b.name))));

        this.gemeinden$.subscribe(g => console.log('test', g), e => console.error(e));
    }
}
