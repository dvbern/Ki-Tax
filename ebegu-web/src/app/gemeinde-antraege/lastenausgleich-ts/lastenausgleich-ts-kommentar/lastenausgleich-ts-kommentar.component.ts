/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {HttpErrorResponse} from '@angular/common/http';
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {ReplaySubject, Subscription} from 'rxjs';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {TSBenutzerNoDetails} from '../../../../models/TSBenutzerNoDetails';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {BenutzerRSX} from '../../../core/service/benutzerRSX.rest';
import {LastenausgleichTSService} from '../services/lastenausgleich-ts.service';

const LOG = LogFactory.createLog('LastenausgleichTsKommentarComponent');

@Component({
    selector: 'dv-lastenausgleich-ts-kommentar',
    templateUrl: './lastenausgleich-ts-kommentar.component.html',
    styleUrls: ['./lastenausgleich-ts-kommentar.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class LastenausgleichTsKommentarComponent implements OnInit, OnDestroy {

    public lATSAngabenGemeindeContainer: TSLastenausgleichTagesschuleAngabenGemeindeContainer;
    public form: FormGroup;
    private kommentarControl: FormControl;
    public saving$ = new ReplaySubject(1);
    private subscription: Subscription;

    public userList: Array<TSBenutzerNoDetails>;

    public constructor(
        private readonly lastenausgleichTSService: LastenausgleichTSService,
        private readonly ref: ChangeDetectorRef,
        private readonly errorService: ErrorService,
        private readonly translate: TranslateService,
        private readonly $state: StateService,
        private readonly benutzerRS: BenutzerRSX,
    ) {
    }

    public ngOnInit(): void {
        this.subscription = this.lastenausgleichTSService.getLATSAngabenGemeindeContainer()
            .subscribe(container => {
                this.lATSAngabenGemeindeContainer = container;
                this.initForm();
                this.ref.markForCheck();
            }, err => LOG.error(err));
    }

    public ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    public saveKommentar(): void {
        if (!this.kommentarControl.valid) {
            return;
        }
        this.saving$.next(true);
        this.lastenausgleichTSService.saveLATSKommentar(
            this.lATSAngabenGemeindeContainer.id,
            this.kommentarControl.value
        ).subscribe(() => {
            this.saving$.next(false);
        }, (error: HttpErrorResponse) => this.handleErrorOnSave(error, 'ERROR_LATS_KOMMENTAR_SAVE'));
    }

    private initForm(): void {
        this.kommentarControl = new FormControl(
            this.lATSAngabenGemeindeContainer?.internerKommentar,
        );
        this.form = new FormGroup({
            kommentar: this.kommentarControl
        });
        if (this.lATSAngabenGemeindeContainer?.isAbgeschlossen()) {
            this.form.disable();
        }
        this.loadUserList();
        this.ref.detectChanges();
    }

    public showVerlauf(): void {
        this.$state.go('LASTENAUSGLEICH_TAGESSCHULEN.VERLAUF');
    }

    public getVerantwortlicherFullName(): string {
        if (this.lATSAngabenGemeindeContainer?.verantwortlicher) {
            return this.lATSAngabenGemeindeContainer.verantwortlicher.getFullName();
        }

        return this.translate.instant('NO_VERANTWORTLICHER_SELECTED');
    }

    public saveVerantwortlicher(): void {
        this.lastenausgleichTSService.saveLATSVerantworlicher(this.lATSAngabenGemeindeContainer.id,
            this.lATSAngabenGemeindeContainer.verantwortlicher?.username)
            .subscribe(
                () => {},
                (error: HttpErrorResponse) => this.handleErrorOnSave(error, 'ERROR_VERANTWORTLICHER_SAVE'));
    }

    private handleErrorOnSave(error: HttpErrorResponse, errorMsgKey: string): void {
        LOG.error(error);
        const translated = this.translate.instant(errorMsgKey);
        this.errorService.addMesageAsError(translated);
    }

    private loadUserList(): void {
        this.benutzerRS.getAllBenutzerMandant()
            .then(response => {
                this.userList = response;
            });
    }

}
