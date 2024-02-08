/*
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

import {Component, Inject, OnInit} from '@angular/core';
import {MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef} from '@angular/material/legacy-dialog';
import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {GesuchstellerRS} from '../../../../../app/core/service/gesuchstellerRS.rest';
import {TSSprache} from '../../../../../models/enums/TSSprache';
import {TSGesuchstellerContainer} from '../../../../../models/TSGesuchstellerContainer';
import {CONSTANTS} from '../../../../../app/core/constants/CONSTANTS';
import {NgForm} from '@angular/forms';

@Component({
    selector: 'dv-ng-zpv-nummmer-verknuepfen-dialog',
    templateUrl: './dialog-init-zpv-nummer-verknpuefen.template.html'
})
export class DialogInitZPVNummerVerknuepfenComponent implements OnInit {

    private readonly gs: TSGesuchstellerContainer;
    private readonly korrespondenzSprache: TSSprache;
    public email: string;
    public readonly CONSTANTS = CONSTANTS;

    public constructor(
        private readonly dialogRef: MatDialogRef<DialogInitZPVNummerVerknuepfenComponent>,
        private readonly gesuchstellerRS: GesuchstellerRS,
        private readonly languageService: TranslateService,
        private readonly $state: StateService,
        @Inject(MAT_DIALOG_DATA) private readonly data: any
    ) {
        this.gs = data.gs;
        this.korrespondenzSprache = data.korrespondenzSprache;
    }

    public ngOnInit(): void {
    }

    public save(form: NgForm): void {
        if (!form.valid) {
            return;
        }
        const target = this.$state.target('onboarding.zpvgssuccess');
        const relayPath = this.$state.href(target.$state(), {gesuchstellerId: this.gs.id}, {absolute: true});
        this.gesuchstellerRS.initGS2ZPVNr(this.email, this.gs, this.korrespondenzSprache, relayPath)
            .then(() => this.dialogRef.close());
    }

    public close(): void {
        this.dialogRef.close();
    }
}
