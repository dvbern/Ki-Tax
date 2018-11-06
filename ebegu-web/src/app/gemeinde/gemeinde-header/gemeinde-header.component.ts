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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {StateService, Transition} from '@uirouter/core';
import {StateDeclaration} from '@uirouter/core/lib/state/interface';
import {Observable, of} from 'rxjs';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import TSGemeinde from '../../../models/TSGemeinde';
import ErrorService from '../../core/errors/service/ErrorService';

@Component({
    selector: 'dv-gemeinde-header',
    templateUrl: './gemeinde-header.component.html',
    styleUrls: ['./gemeinde-header.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GemeindeHeaderComponent implements OnInit {
    @ViewChild(NgForm) public form: NgForm;
    @Input() public gemeinde: TSGemeinde;
    @Input() public administratoren: string;
    @Input() public sachbearbeiter: string;

    private fileToUpload: File;
    private navigationDest: StateDeclaration;
    public logoImageUrl$: Observable<string>;

    public constructor(
        private readonly $transition$: Transition,
        private readonly gemeindeRS: GemeindeRS,
        private readonly translate: TranslateService,
        private readonly errorService: ErrorService,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly $state: StateService,
    ) {
    }

    public ngOnInit(): void {
        this.navigationDest = this.$transition$.to();
        this.logoImageUrl$ = of(this.gemeindeRS.getLogoUrl(this.gemeinde.id));
    }

    public mitarbeiterBearbeiten(): void {
        this.$state.go('admin.benutzerlist');
    }

    public getGemeindeTitel(): string {
        return this.translate.instant('GEMEINDE_NAME',
            {name: this.gemeinde.name, bfs: this.gemeinde.bfsNummer});
    }

    /**
     * If we are in edition mode we should be able to edit everything. All users that open 'gemeinde.edit'
     * must be allowed to edit the gemeinde. For this reason the role doesn't need to be checked here again.
     */
    public isEditionMode(): boolean {
        return 'gemeinde.edit' === this.navigationDest.name;
    }

    public srcChange(files: FileList): void {
        this.fileToUpload = files[0];
        const tmpFileReader = new FileReader();
        tmpFileReader.readAsDataURL(this.fileToUpload);
        tmpFileReader.onload = (event: any): void => {
            if (!(this.fileToUpload && this.fileToUpload.type.includes('image/'))) {
                return; // upload only images
            }
            this.gemeindeRS.uploadLogoImage(this.gemeinde.id, this.fileToUpload).then(() => {
                const result: string = event.target.result;
                this.logoImageUrl$ = of(result);
                // markForCheck needed for the image to refresh
                this.changeDetectorRef.markForCheck();
            }, () => {
                this.errorService.clearAll();
                this.errorService.addMesageAsError(this.translate.instant('GEMEINDE_LOGO_ZU_GROSS'));
            });
        };

    }

}
