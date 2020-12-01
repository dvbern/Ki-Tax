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

import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {StateService} from '@uirouter/core';
import {Observable, of} from 'rxjs';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../../models/enums/TSRole';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';

@Component({
    selector: 'dv-stammdaten-header',
    templateUrl: './stammdaten-header.component.html',
    styleUrls: ['./stammdaten-header.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class StammdatenHeaderComponent implements OnInit {
    @ViewChild(NgForm) public form: NgForm;
    @Input() public preTitel: string;
    @Input() public titel: string;
    @Input() public administratoren: string;
    @Input() public sachbearbeiter: string;
    @Input() public logoImageUrl: string;
    @Input() public editMode: boolean;
    @Input() public allowedRoles: TSRole[] = [TSRole.SUPER_ADMIN];
    @Output() public readonly logoImageChange: EventEmitter<File> = new EventEmitter();

    public logoImageUrl$: Observable<string>;
    private fileToUpload: File;

    public constructor(
        private readonly $state: StateService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly gemeindeRS: GemeindeRS,
    ) {
    }

    public ngOnInit(): void {
        this.logoImageUrl$ = of(this.logoImageUrl);
    }

    public mitarbeiterBearbeiten(): void {
        this.$state.go('admin.benutzerlist');
    }

    public areMitarbeiterVisible(): boolean {
        return this.authServiceRS.isOneOfRoles(this.allowedRoles);
    }

    public srcChange(files: FileList): void {
        this.fileToUpload = files[0];
        this.gemeindeRS.isSupportedImage(this.fileToUpload).then( () => {
            const tmpFileReader = new FileReader();
            tmpFileReader.readAsDataURL(this.fileToUpload);
            tmpFileReader.onload = (event: any): void => {
                const result: string = event.target.result;
                this.logoImageUrl$ = of(result);
                // emit logo change to upload image by parent view
                this.emitLogoChange();
            };
        }).catch(() => {
            this.fileToUpload = null;
            this.logoImageUrl$ = null;
            this.logoImageUrl = null;
            this.emitLogoChange();
        });
    }

    public $postLink(): void {
    }

    private emitLogoChange(): void {
        if (this.logoImageChange) {
            this.logoImageChange.emit(this.fileToUpload);
        }
    }
}
