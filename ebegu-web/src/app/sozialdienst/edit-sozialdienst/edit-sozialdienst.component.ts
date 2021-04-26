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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, QueryList, ViewChildren} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {StateService, Transition} from '@uirouter/core';
import {Observable} from 'rxjs';
import {tap} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import {TSSozialdienstStammdaten} from '../../../models/sozialdienst/TSSozialdienstStammdaten';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {SozialdienstRS} from '../../core/service/SozialdienstRS.rest';

@Component({
    selector: 'dv-edit-sozialdienst',
    templateUrl: './edit-sozialdienst.component.html',
    styleUrls: ['./edit-sozialdienst.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditSozialdienstComponent implements OnInit {
    public readonly CONSTANTS: any = CONSTANTS;

    @ViewChildren(NgForm) public forms: QueryList<NgForm>;

    public stammdaten$: Observable<TSSozialdienstStammdaten>;
    public sozialdienstId: string;
    public editMode: boolean;
    public ebeguUtil = EbeguUtil;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly sozialdienstRS: SozialdienstRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly translate: TranslateService,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly errorService: ErrorService,
    ) {
    }

    public ngOnInit(): void {
        this.sozialdienstId = this.$transition$.params().sozialdienstId;
        if (!this.sozialdienstId) {
            return;
        }
        this.loadStammdaten();
        this.editMode = false;
    }

    public onSubmit(stammdaten: TSSozialdienstStammdaten): void {
        if (this.editMode) {
            this.persistStammdaten(stammdaten);
            return;
        }
        this.editMode = true;
    }

    public navigateBack(): void {
        this.$state.go('sozialdienst.list');
    }

    public isStammdatenEditable(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_SOZIALDIENST,
        ]);
    }

    private loadStammdaten(): void {
        this.stammdaten$ = this.sozialdienstRS.getSozialdienstStammdaten(this.sozialdienstId);
    }

    private persistStammdaten(stammdaten: TSSozialdienstStammdaten): void {
        let valid = true;
        this.forms.forEach(form => {
            if (!form.valid) {
                valid = false;
            }
        });

        if (!valid) {
            EbeguUtil.selectFirstInvalid();
            return;
        }
        this.stammdaten$ = this.sozialdienstRS.saveSozialdienstStammdaten(stammdaten)
            .pipe(tap(() => {
                this.editMode = false;
            }));
        this.changeDetectorRef.markForCheck();
    }

    public submitButtonLabel(): string {
        if (this.editMode) {
            return this.translate.instant('SOZIALDIENST_SPEICHERN');
        }
        return this.translate.instant('SOZIALDIENST_EDIT');
    }
}
