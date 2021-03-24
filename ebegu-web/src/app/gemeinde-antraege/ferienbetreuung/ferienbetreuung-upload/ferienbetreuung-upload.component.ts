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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSFerienbetreuungDokument} from '../../../../models/gemeindeantrag/TSFerienbetreuungDokument';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {MAX_FILE_SIZE} from '../../../core/constants/CONSTANTS';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {UploadRS} from '../../../core/service/uploadRS.rest';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

const LOG = LogFactory.createLog('FerienbetreuungUploadComponent');

@Component({
    selector: 'dv-ferienbetreuung-upload',
    templateUrl: './ferienbetreuung-upload.component.html',
    styleUrls: ['./ferienbetreuung-upload.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FerienbetreuungUploadComponent implements OnInit {

    public dokumente: TSFerienbetreuungDokument[];

    private container: TSFerienbetreuungAngabenContainer;
    private filesTooBig: File[];

    public constructor(
        private ferienbetreuungService: FerienbetreuungService,
        private uploadRS: UploadRS,
        private errorService: ErrorService
    ) {
    }

    public ngOnInit(): void {
        this.ferienbetreuungService.getFerienbetreuungContainer()
            .subscribe(container => {
                this.container = container;
            }, error => {
                LOG.error(error);
            });
    }

    public download(dokument: TSFerienbetreuungDokument, attachment: boolean): void {
        // TODO
    }

    public onDelete(dokument: TSFerienbetreuungDokument): void {
        // TODO
    }

    public onUpload(event: any): void {
        if (EbeguUtil.isNullOrUndefined(event?.target?.files?.length)) {
            return;
        }
        const files = event.target.files;
        if (this.checkFilesLength(files as File[])) {
            return;
        }
        this.uploadRS.uploadFerienbetreuungDokumente(files, this.container.id)
            .then(dokumente => {
                this.dokumente = dokumente;
            })
            .catch(err => {
                LOG.error(err);
                this.errorService.addMesageAsError(err);
            });

    }

    public isReadonly(): boolean {
        // TODO
        return false;
    }

    /**
     * checks if some files are too big and stores them in filesTooBig variable
     */
    private checkFilesLength(files: File[]): boolean {
        this.filesTooBig = [];
        for (const file of files) {
            if (file.size > MAX_FILE_SIZE) {
                this.filesTooBig.push(file);
            }
        }
        return this.filesTooBig.length > 0;
    }
}
