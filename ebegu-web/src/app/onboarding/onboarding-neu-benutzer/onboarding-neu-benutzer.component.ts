/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input} from '@angular/core';
import {NgForm} from '@angular/forms';
import {StateService} from '@uirouter/core';
import {from, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';

@Component({
    selector: 'dv-onboarding-neu-benutzer',
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './onboarding-neu-benutzer.component.html',
    styleUrls: ['./onboarding-neu-benutzer.component.less', '../onboarding.less'],
})
export class OnboardingNeuBenutzerComponent {

    @Input() public nextState: string = 'onboarding.be-login';
    @Input() public isTSAngebotEnabled: boolean;

    public gemeinden$: Observable<TSGemeinde[]>;
    public gemeindenBG$: Observable<TSGemeinde[]>;
    public gemeindenTS$: Observable<TSGemeinde[]>;
    public besondereVolksschulen$: Observable<TSGemeinde[]>;
    public gemeinde?: TSGemeinde;
    public besondereVolksschuleGemeinde?: TSGemeinde;
    private _gemeindeList: Array<TSGemeinde> = [];

    public betreuungsgutscheinBeantragen: boolean;
    public tsBeantragen: boolean;
    public besondereVolksschuleBeantragen: boolean;

    public constructor(
        private readonly gemeindeRS: GemeindeRS,
        private readonly stateService: StateService,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly cd: ChangeDetectorRef
    ) {
        this.gemeinden$ = from(this.gemeindeRS.getAktiveUndVonSchulverbundGemeinden())
            .pipe(map(gemeinden => {
                gemeinden.sort(EbeguUtil.compareByName);
                return gemeinden;
            }));
        this.gemeindenBG$ = from(this.gemeinden$).pipe(map(gemeinden => gemeinden.filter(
            gemeinde => gemeinde.angebotBG)));
        this.gemeindenTS$ = from(this.gemeinden$).pipe(map(gemeinden => gemeinden.filter(
            gemeinde => gemeinde.angebotTS && !gemeinde.besondereVolksschule)));
        this.besondereVolksschulen$ = from(this.gemeinden$).pipe(map(gemeinden => gemeinden.filter(
            gemeinde => gemeinde.besondereVolksschule)));
        this.applicationPropertyRS.getPublicPropertiesCached().then(properties => {
            this.isTSAngebotEnabled = properties.angebotTSActivated;
            this.cd.markForCheck();
        });
    }

    public onSubmit(form: NgForm): void {
        if (!form.valid) {
            return;
        }
        const listIds: string[] = [];
        if (this.besondereVolksschuleBeantragen) {
            listIds.push(this.besondereVolksschuleGemeinde.id);
        }
        this._gemeindeList.forEach(gemeinde => {
            if (listIds.indexOf(gemeinde.key) === -1) {
                listIds.push(gemeinde.key);
            }
        });
        this.stateService.go(this.nextState, {
            gemeindeBGId: this.gemeinde !== undefined ? this.gemeinde.id : null,
            gemeindenId: listIds
        });
    }

    public set gemeindeList(value: Array<TSGemeinde>) {
        this._gemeindeList = value;
    }

    public get gemeindeList(): Array<TSGemeinde> {
        return this._gemeindeList;
    }

    public getTSGemeinden(): Observable<TSGemeinde[]> {
        return this.besondereVolksschuleBeantragen ? this.besondereVolksschulen$ : this.gemeindenTS$;
    }

    public resetGemeindeListe(): void {
        this.besondereVolksschuleGemeinde = undefined;
        this.gemeindeList = [];
    }

    public resetBgGemeinde(): void {
        this.gemeinde = undefined;
    }
}
