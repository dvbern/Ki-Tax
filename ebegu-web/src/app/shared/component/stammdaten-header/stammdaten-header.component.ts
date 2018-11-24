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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {StateService} from '@uirouter/core';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../../models/enums/TSRole';
import {Permission} from '../../../authorisation/Permission';
import {PERMISSIONS} from '../../../authorisation/Permissions';

@Component({
    selector: 'dv-stammdaten-header',
    templateUrl: './stammdaten-header.component.html',
    styleUrls: ['./stammdaten-header.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class StammdatenHeaderComponent implements OnInit {
    @ViewChild(NgForm) public form: NgForm;
    @Input() public titel: string;
    @Input() public administratoren: string;
    @Input() public sachbearbeiter: string;
    @Input() public logoImageUrl: string;
    @Input() public editMode: boolean;

    private fileToUpload: File;

    public constructor(
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly $state: StateService,
        private readonly authServiceRS: AuthServiceRS,
    ) {
    }

    public ngOnInit(): void {
    }

    public mitarbeiterBearbeiten(): void {
        this.$state.go('admin.benutzerlist');
    }

    public areMitarbeiterVisible(): boolean {
        const allowedRoles = PERMISSIONS[Permission.ROLE_GEMEINDE];
        allowedRoles.push(TSRole.SUPER_ADMIN);
        return this.authServiceRS.isOneOfRoles(allowedRoles);
    }

    public srcChange(files: FileList): void {
        this.fileToUpload = files[0];
        const tmpFileReader = new FileReader();
        tmpFileReader.readAsDataURL(this.fileToUpload);
        tmpFileReader.onload = (event: any): void => {
            if (!(this.fileToUpload && this.fileToUpload.type.includes('image/'))) {
                return; // upload only images
            }
            const result: string = event.target.result;
            this.logoImageUrl = result;
            // markForCheck needed for the image to refresh
            this.changeDetectorRef.markForCheck();
            // TODO upload upon save
        };
    }
}
