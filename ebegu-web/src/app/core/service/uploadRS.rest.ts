/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {IHttpService, ILogService, IPromise, IQService} from 'angular';
import TSDokumentGrund from '../../../models/TSDokumentGrund';
import EbeguRestUtil from '../../../utils/EbeguRestUtil';

export class UploadRS {

    public static $inject = ['$http', 'REST_API', '$log', 'Upload', 'EbeguRestUtil', '$q'];
    public serviceURL: string;

    public constructor(public http: IHttpService,
                       REST_API: string,
                       public log: ILogService,
                       private readonly upload: any,
                       public ebeguRestUtil: EbeguRestUtil,
                       public q: IQService) {
        this.serviceURL = REST_API + 'upload';
    }

    public uploadFile(files: any, dokumentGrund: TSDokumentGrund, gesuchID: string): IPromise<TSDokumentGrund> {

        let restDokumentGrund = {};
        restDokumentGrund = this.ebeguRestUtil.dokumentGrundToRestObject(restDokumentGrund, dokumentGrund);
        const restDokumentString = this.upload.json(restDokumentGrund);

        const names: string [] = [];
        for (const file of files) {
            if (file) {
                const encodedFilename = btoa(file.name);
                names.push(encodedFilename);
            }
        }

        return this.upload.upload({
            url: this.serviceURL,
            method: 'POST',
            headers: {
                'x-filename': names.join(';'),
                'x-gesuchID': gesuchID
            },
            data: {
                file: files,
                dokumentGrund: restDokumentString
            }
        }).then((response: any) => {
            return this.ebeguRestUtil.parseDokumentGrund(new TSDokumentGrund(), response.data);
        }, (response: any) => {
            console.log('Upload File: NOT SUCCESS');
            return this.q.reject(response);
        }, (evt: any) => {
            const loaded: number = evt.loaded;
            const total: number = evt.total;
            const progressPercentage = 100 * loaded / total;
            console.log(`progress: ${progressPercentage}% `);
            return this.q.defer().notify();
        });
    }

    public getServiceName(): string {
        return 'UploadRS';
    }
}
