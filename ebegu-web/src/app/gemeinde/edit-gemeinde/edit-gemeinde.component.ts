/*
 * AGPL File-Header
 *
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {StateService, Transition} from '@uirouter/core';
import {Observable} from 'rxjs';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import TSGemeindeStammdaten from '../../../models/TSGemeindeStammdaten';
import ErrorService from '../../core/errors/service/ErrorService';
import GesuchsperiodeRS from '../../core/service/gesuchsperiodeRS.rest';

@Component({
    selector: 'dv-edit-gemeinde',
    templateUrl: './edit-gemeinde.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditGemeindeComponent implements OnInit {

    @ViewChild(NgForm) public form: NgForm;

    public stammdaten: TSGemeindeStammdaten;
    public stammdaten$: Observable<TSGemeindeStammdaten>;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly errorService: ErrorService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly translate: TranslateService,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
    ) { }

    public ngOnInit(): void {
        const gemeindeId: string = this.$transition$.params().gemeindeId;
        if (!gemeindeId) {
            return;
        }
        this.gemeindeRS.getGemeindeStammdaten(gemeindeId).then(results => {
            this.stammdaten = results;
            // TODO: GemeindeStammdaten über ein Observable laden, so entfällt changeDetectorRef.markForCheck(), siehe onboarding.component
            this.changeDetectorRef.markForCheck();
        });
    }

    public cancel(): void {
        this.navigateBack();
    }

    public persistGemeindeStammdaten(): void {
        this.gemeindeRS.saveGemeindeStammdaten(this.stammdaten).then(stammdaten => {
            this.stammdaten = stammdaten;
            this.navigateBack();
        });
    }

    private navigateBack(): void {
        this.$state.go('gemeinde.list');
    }
}
