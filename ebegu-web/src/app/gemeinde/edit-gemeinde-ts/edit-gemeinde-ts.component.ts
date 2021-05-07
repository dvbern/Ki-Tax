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

import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {StateService} from '@uirouter/core';
import {Moment} from 'moment';
import {Observable} from 'rxjs';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSExternalClientAssignment} from '../../../models/TSExternalClientAssignment';
import {TSGemeindeStammdaten} from '../../../models/TSGemeindeStammdaten';
import {TSInstitutionListDTO} from '../../../models/TSInstitutionListDTO';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {InstitutionRS} from '../../core/service/institutionRS.rest';

@Component({
    selector: 'dv-edit-gemeinde-ts',
    templateUrl: './edit-gemeinde-ts.component.html',
    styleUrls: ['./edit-gemeinde-ts.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
})
export class EditGemeindeComponentTS implements OnInit {

    @Input() public stammdaten$: Observable<TSGemeindeStammdaten>;
    @Input() private readonly gemeindeId: string;
    @Input() public editMode: boolean;
    @Input() public altTSAdresse: boolean;
    @Input() public tsAnmeldungenStartDatum: Moment;
    @Input() public tsAnmeldungenStartStr: string;
    @Input() public externalClients: TSExternalClientAssignment;
    @Input() public isSuperAdmin: boolean;
    @Input() public usernameScolaris: string;

    @Output() public readonly altTSAdresseChange: EventEmitter<boolean> = new EventEmitter();
    @Output() public readonly usernameScolarisChange: EventEmitter<string> = new EventEmitter();

    public readonly CONSTANTS = CONSTANTS;
    private _tagesschulen: TSInstitutionListDTO[];
    public showTSList: boolean = false;

    public constructor(
        private readonly $state: StateService,
        private readonly institutionRS: InstitutionRS,
    ) {
    }

    public ngOnInit(): void {
        if (!this.gemeindeId) {
            return;
        }
        this.updateInstitutionenList();
    }

    public compareBenutzer(b1: TSBenutzer, b2: TSBenutzer): boolean {
        return b1 && b2 ? b1.username === b2.username : b1 === b2;
    }

    public altTSAdresseHasChange(newVal: boolean): void {
        this.altTSAdresseChange.emit(newVal);
    }

    public usernameScolarisHasChange(newVal: string): void {
        this.usernameScolarisChange.emit(newVal);
    }

    public isUsernameScolarisNotNullOrUndefined(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.usernameScolaris);
    }

    public updateInstitutionenList(): void {
        this.institutionRS.getInstitutionenForGemeinde(this.gemeindeId).then(
            result => {
                this._tagesschulen = result.sort((a, b) => a.name.localeCompare(b.name));
            },
        );
    }

    public get tagesschulen(): TSInstitutionListDTO[] {
        return this._tagesschulen;
    }

    public gotoTagesschule(id: string): void {
        this.$state.go('institution.edit', {
            institutionId: id,
            editMode: false,
        });
    }

    public showListTS(): void {
        this.showTSList = true;
    }

    public hideTSList(): void {
        this.showTSList = false;
    }
}
