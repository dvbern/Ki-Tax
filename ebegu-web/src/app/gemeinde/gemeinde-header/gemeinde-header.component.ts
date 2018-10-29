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

import {ChangeDetectionStrategy, Component, Input, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {Transition} from '@uirouter/core';
import {StateDeclaration} from '@uirouter/core/lib/state/interface';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import TSGemeinde from '../../../models/TSGemeinde';

@Component({
  selector: 'dv-gemeinde-header',
  templateUrl: './gemeinde-header.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class GemeindeHeaderComponent implements OnInit {
    @ViewChild(NgForm) public form: NgForm;
    @Input() public gemeinde: TSGemeinde;
    @Input() public administratoren: string;
    @Input() public sachbearbeiter: string;

    private fileToUpload!: File;
    private navigationDest: StateDeclaration;
    public logoImageUrl: string = '#';

    public constructor(
        private readonly $transition$: Transition,
        private readonly gemeindeRS: GemeindeRS,
        private readonly translate: TranslateService,
    ) {
    }

    public ngOnInit(): void {
        this.navigationDest = this.$transition$.to();
        this.logoImageUrl = this.gemeindeRS.getLogoUrl(this.gemeinde.id);
    }

    public canUploadLogo(): boolean {
        return 'gemeinde.edit' === this.navigationDest.name;
    }

    public mitarbeiterBearbeiten(): void {
        // TODO: Implement Mitarbeiter Bearbeiten Button Action
    }

    public handleLogoUpload(files: FileList): void {
        this.fileToUpload = files[0];
        const tmpFileReader = new FileReader();
        tmpFileReader.onload = (event: any): void => {
            this.logoImageUrl = event.target.result;
        };
        tmpFileReader.readAsDataURL(this.fileToUpload);

        if (!(this.fileToUpload && this.fileToUpload.type.includes('image/'))) {
            return;
        }
        this.gemeindeRS.uploadLogoImage(this.gemeinde.id, this.fileToUpload);
    }

}
