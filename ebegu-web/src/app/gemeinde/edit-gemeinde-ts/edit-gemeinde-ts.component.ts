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
import {MatCheckboxChange} from '@angular/material/checkbox';
import {StateService} from '@uirouter/core';
import {Moment} from 'moment';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSExternalClientAssignment} from '../../../models/TSExternalClientAssignment';
import {TSGemeindeStammdaten} from '../../../models/TSGemeindeStammdaten';
import {TSInstitutionListDTO} from '../../../models/TSInstitutionListDTO';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {LogFactory} from '../../core/logging/LogFactory';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';

const LOG = LogFactory.createLog('EditGemeidneComponentTS');

@Component({
    selector: 'dv-edit-gemeinde-ts',
    templateUrl: './edit-gemeinde-ts.component.html',
    styleUrls: ['./edit-gemeinde-ts.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class EditGemeindeTSComponent implements OnInit {

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
    @Input() public altLogoImageUrl: string;
    @Output() public readonly altLogoImageChange: EventEmitter<File> = new EventEmitter();

    public readonly CONSTANTS = CONSTANTS;
    private _tagesschulen: TSInstitutionListDTO[];
    public showTSList: boolean = false;
    public altLogoImageUrl$: Observable<string>;
    private fileToUpload: File;

    public constructor(
        private readonly $state: StateService,
        private readonly institutionRS: InstitutionRS,
        private readonly gemeindeRS: GemeindeRS,
    ) {
    }

    public ngOnInit(): void {
        if (!this.gemeindeId) {
            return;
        }
        this.updateInstitutionenList();
        this.altLogoImageUrl$ = of(this.altLogoImageUrl);
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
        this.institutionRS.getInstitutionenForGemeinde(this.gemeindeId).subscribe(
            result => {
                this._tagesschulen = result;
                this._tagesschulen.sort((a, b) => a.name.localeCompare(b.name));
            }, error => LOG.error(error)
        );
    }

    public get tagesschulen(): TSInstitutionListDTO[] {
        return this._tagesschulen;
    }

    public showListTS(): void {
        this.showTSList = true;
    }

    public hideTSList(): void {
        this.showTSList = false;
    }

    public isNotNurLats(): Observable<boolean> {
        return this.stammdaten$.pipe(map(stammdaten => stammdaten.gemeinde.nurLats));
    }

    public srcChange(files: FileList): void {
        this.fileToUpload = files[0];
        this.gemeindeRS.isSupportedImage(this.fileToUpload).then(() => {
            const tmpFileReader = new FileReader();
            tmpFileReader.readAsDataURL(this.fileToUpload);
            tmpFileReader.onload = (event: any): void => {
                const result: string = event.target.result;
                this.altLogoImageUrl$ = of(result);
                this.emitLogoChange();
            };
        }).catch(() => {
            this.fileToUpload = null;
            this.altLogoImageUrl$ = null;
            this.altLogoImageUrl = null;
            this.emitLogoChange();
        });
    }

    private emitLogoChange(): void {
        if (this.altLogoImageChange) {
            this.altLogoImageChange.emit(this.fileToUpload);
        }
    }

    public async onAlternativeLogoChange(event: MatCheckboxChange): Promise<void> {
        if (!event.checked) {
            await this.gemeindeRS.deleteAlternativeLogoTagesschule(this.gemeindeId);
            this.altLogoImageUrl = null;
            this.altLogoImageUrl$ = null;
            this.fileToUpload = null;
            this.emitLogoChange();
        }
    }
}
