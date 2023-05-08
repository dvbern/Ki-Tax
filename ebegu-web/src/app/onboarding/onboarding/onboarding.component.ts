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

import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';
import {TranslateService} from '@ngx-translate/core';
import {BehaviorSubject, combineLatest, from, Observable, Subject} from 'rxjs';
import {map, takeUntil} from 'rxjs/operators';
import {YoutubeLinkVisitor} from '../../core/constants/YoutubeLinkVisitor';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {MandantService} from '../../shared/services/mandant.service';
import {OnboardingHelpDialogComponent} from '../onboarding-help-dialog/onboarding-help-dialog.component';
import {OnboardingPlaceholderService} from '../service/onboarding-placeholder.service';

@Component({
    selector: 'dv-onboarding',
    templateUrl: './onboarding.component.html',
    styleUrls: ['./onboarding.component.less', '../onboarding.less']
})
export class OnboardingComponent implements OnInit, OnDestroy {

    @Input() public showLogin: boolean = true;

    private readonly description1: string = 'ONBOARDING_MAIN_DESC1';
    private readonly description2: string = 'ONBOARDING_MAIN_DESC2';
    private readonly description3: string = 'ONBOARDING_MAIN_DESC3';
    private readonly description4: string = 'ONBOARDING_MAIN_DESC4';
    public isDummyMode$: Observable<boolean>;
    public currentLangDe$: BehaviorSubject<boolean>;
    public isMultimandantEnabled$: Observable<boolean>;
    public isLuzern$: Observable<boolean>;
    private readonly unsubscribe$ = new Subject<void>();
    public youtubeLink$: Observable<SafeResourceUrl>;

    public constructor(
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly onboardingPlaceholderService: OnboardingPlaceholderService,
        private readonly translate: TranslateService,
        private readonly dialog: MatDialog,
        private readonly mandantService: MandantService,
        private readonly sanitizer: DomSanitizer
    ) {
        this.isDummyMode$ = from(this.applicationPropertyRS.isDummyMode());
        this.isMultimandantEnabled$ = from(this.applicationPropertyRS.isMultimandantEnabled());
    }

    public ngOnInit(): void {
        this.onboardingPlaceholderService.setDescription1(this.translate.instant(this.description1));
        this.onboardingPlaceholderService.setDescription2(this.translate.instant(this.description2));
        this.onboardingPlaceholderService.setDescription3(this.translate.instant(this.description3));
        this.onboardingPlaceholderService.setDescription4(this.translate.instant(this.description4));

        this.currentLangDe$ = new BehaviorSubject(this.currLangIsGerman());
        this.translate.onLangChange.subscribe(() => {
            this.currentLangDe$.next(this.currLangIsGerman());
        }, (err: any) => {
            console.error(err);
        });
        this.initYoutubeLink();
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    private currLangIsGerman(): boolean {
        return this.translate.currentLang === 'de';
    }

    public isGerman$(): Observable<boolean> {
        return this.currentLangDe$.asObservable();
    }

    public openHelp($event: MouseEvent): void {
        $event.preventDefault();
        const dialogConfig = new MatDialogConfig();
        this.dialog.open(OnboardingHelpDialogComponent, dialogConfig);
    }

    private initYoutubeLink(): void {
        const mandant$ = this.mandantService.mandant$.pipe(takeUntil(this.unsubscribe$));
        const isGerman$ = this.isGerman$().pipe(takeUntil(this.unsubscribe$));
        this.youtubeLink$ = combineLatest([mandant$, isGerman$])
                .pipe(map(res => {
                    const url = new YoutubeLinkVisitor(res[1]).process(res[0]);
                    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
                }));
    }
}
