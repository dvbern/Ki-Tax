/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {from, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {DvNgCancelDialogComponent} from '../../core/component/dv-ng-confirm-dialog/dv-ng-cancel-dialog.component';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {OnboardingHelpDialogComponent} from '../onboarding-help-dialog/onboarding-help-dialog.component';

@Component({
    selector: 'dv-onboarding-neu-benutzer',
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './onboarding-neu-benutzer.component.html',
    styleUrls: ['./onboarding-neu-benutzer.component.less', '../onboarding.less']
})
export class OnboardingNeuBenutzerComponent {

    @Input() public nextState: string = 'onboarding.be-login';
    public isTSAngebotEnabled: boolean;

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
        private readonly cd: ChangeDetectorRef,
        private readonly dialog: MatDialog,
        private readonly translate: TranslateService
    ) {
        this.gemeinden$ = from(this.gemeindeRS.getAktiveUndVonSchulverbundGemeinden())
            .pipe(map(gemeinden => {
                gemeinden.sort(EbeguUtil.compareByName);
                return gemeinden;
            }));
        this.gemeindenBG$ = from(this.gemeinden$).pipe(map(gemeinden => gemeinden.filter(
            gemeinde => gemeinde.angebotBG)));
        this.gemeindenTS$ = from(this.gemeinden$).pipe(map(gemeinden => gemeinden.filter(
            gemeinde => gemeinde.angebotTS && !gemeinde.besondereVolksschule && !gemeinde.nurLats)));
        this.besondereVolksschulen$ = from(this.gemeinden$).pipe(map(gemeinden => gemeinden.filter(
            gemeinde => gemeinde.besondereVolksschule)));
        this.applicationPropertyRS.getPublicPropertiesCached().then(properties => {
            this.isTSAngebotEnabled = properties.angebotTSActivated;
            this.cd.markForCheck();
        });
    }

    public async onSubmit(form: NgForm): Promise<void> {
        if (!form.valid) {
            return;
        }
        const confirmed = await this.showPopupAfterRegistrierenIfNecessary();
        if (!confirmed) {
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

    public showPopupAfterRegistrierenIfNecessary(): Promise<boolean> {
        const popupText = this.translate.instant('POPUPTEXT_AFTER_REGISTRIEREN');
        // dieses popup haben wir nicht bei allen Mandanten. Wir zeigen es nur, falls ein Text dafür in den Translation
        // files existiert.
        if (popupText.length === 0) {
            return Promise.resolve(true);
        }
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: popupText
        };
        return this.dialog.open(DvNgCancelDialogComponent, dialogConfig)
            .afterClosed()
            .toPromise();
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

    public isNotNullAndNotEmpty(tsGemeindes: TSGemeinde[]): boolean {
        return tsGemeindes !== null && tsGemeindes.length > 0;
    }

    public openHelp($event: MouseEvent): void {
        $event.preventDefault();
        const dialogConfig = new MatDialogConfig();
        this.dialog.open(OnboardingHelpDialogComponent, dialogConfig);
    }
}
