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

import {ChangeDetectionStrategy, Component} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSBenutzerStatus} from '../../../models/enums/TSBenutzerStatus';
import {TSRole} from '../../../models/enums/TSRole';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {AngularXBenutzerRS} from '../../core/service/angularXBenutzerRS.rest';
import {ErrorService} from '../../core/errors/service/ErrorService';

@Component({
    selector: 'dv-benutzer-einladen',
    templateUrl: './benutzer-einladen.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BenutzerEinladenComponent {

    public readonly benutzer = new TSBenutzer();
    public readonly excludedRoles: ReadonlyArray<TSRole> = [TSRole.GESUCHSTELLER];
    public readonly CONSTANTS = CONSTANTS;

    public constructor(
        private readonly benutzerRS: AngularXBenutzerRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly stateService: StateService,
        private readonly errorService: ErrorService,
        private readonly translate: TranslateService,
    ) {
    }

    public onSubmit(form: NgForm): void {
        if (!form.valid) {
            return;
        }
        this.benutzer.email = this.benutzer.email.trim();
        this.benutzer.status = TSBenutzerStatus.EINGELADEN;
        this.benutzer.username = this.benutzer.email;
        this.benutzer.mandant = this.authServiceRS.getPrincipal().mandant;

        this.benutzer.berechtigungen.forEach(berechtigung => berechtigung.prepareForSave());

        this.benutzerRS.einladen(this.benutzer)
            .then(() => {
                this.stateService.go('admin.benutzerlist').then(() => {
                    this.errorService.addMesageAsInfo(this.translate.instant(
                        'BENUTZER_INVITED_MESSAGE',
                        {fullName: this.benutzer.getFullName()}
                    ));
                });
            });
    }

}
