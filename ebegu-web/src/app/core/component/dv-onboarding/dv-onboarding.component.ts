import {ChangeDetectionStrategy, Component} from '@angular/core';
import {from, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import GemeindeRS from '../../../../gesuch/service/gemeindeRS.rest';
import TSGemeinde from '../../../../models/TSGemeinde';

@Component({
    selector: 'dv-onboarding',
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './dv-onboarding.component.html',
    styleUrls: ['./dv-onboarding.component.less'],
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
