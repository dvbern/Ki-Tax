/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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
import {ReplaySubject, Subscription} from 'rxjs';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

const LOG = LogFactory.createLog('FerienbetreuungKommantarComponent');

@Component({
    selector: 'dv-ferienbetreuung-kommantar',
    templateUrl: './ferienbetreuung-kommantar.component.html',
    styleUrls: ['./ferienbetreuung-kommantar.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FerienbetreuungKommantarComponent implements OnInit, OnDestroy {

    public form: FormGroup;
    public saving$ = new ReplaySubject(1);
    private kommentarControl: FormControl;
    private subscription: Subscription;
    private ferienbetreuungContainer: TSFerienbetreuungAngabenContainer;

    public constructor(
        private readonly ferienbetreuungService: FerienbetreuungService,
        private readonly ref: ChangeDetectorRef,
        private readonly errorService: ErrorService,
        private readonly translate: TranslateService
    ) {}

    public ngOnInit(): void {
        this.subscription = this.ferienbetreuungService.getFerienbetreuungContainer()
            .subscribe(container => {
                this.ferienbetreuungContainer = container;
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
        this.ferienbetreuungService.saveKommentar(
            this.ferienbetreuungContainer.id,
            this.kommentarControl.value
        ).subscribe(() => {
            this.saving$.next(false);
        }, (error: HttpErrorResponse) => {
            LOG.error(error);
            const translated = this.translate.instant('ERROR_LATS_KOMMENTAR_SAVE');
            this.errorService.addMesageAsError(translated);
        });
    }

    private initForm(): void {
        this.kommentarControl = new FormControl(
            this.ferienbetreuungContainer?.internerKommentar,
        );
        this.form = new FormGroup({
            kommentar: this.kommentarControl
        });
    }
}
