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
import {TSDokumentTyp} from '../../../models/enums/TSDokumentTyp';
import {TSRueckforderungDokumentTyp} from '../../../models/enums/TSRueckforderungDokumentTyp';
import {TSSprache} from '../../../models/enums/TSSprache';
import {TSFerienbetreuungDokument} from '../../../models/gemeindeantrag/TSFerienbetreuungDokument';
import {TSDokumentGrund} from '../../../models/TSDokumentGrund';
import {TSGesuchstellerAusweisDokument} from '../../../models/TSGesuchstellerAusweisDokument';
import {TSRueckforderungDokument} from '../../../models/TSRueckforderungDokument';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class UploadRS {

    public static $inject = ['$http', 'REST_API', '$log', 'Upload', 'EbeguRestUtil', '$q', 'base64'];
    public serviceURL: string;
    private readonly NOT_SUCCESS = 'Upload File: NOT SUCCESS';

    public constructor(
        public http: IHttpService,
        REST_API: string,
        public log: ILogService,
        private readonly upload: any,
        public ebeguRestUtil: EbeguRestUtil,
        public q: IQService,
        private readonly base64: any,
    ) {
        this.serviceURL = REST_API + 'upload';
    }

    public uploadFile(files: any, dokumentGrund: TSDokumentGrund, gesuchID: string): IPromise<TSDokumentGrund> {

        let restDokumentGrund = {};
        restDokumentGrund = this.ebeguRestUtil.dokumentGrundToRestObject(restDokumentGrund, dokumentGrund);
        const restDokumentString = this.upload.json(restDokumentGrund);

        const names: string [] = [];
        for (const file of files) {
            if (file) {
                const encodedFilename = this.base64.encode(file.name);
                names.push(encodedFilename);
            }
        }

        return this.upload.upload({
            url: this.serviceURL,
            method: 'POST',
            headers: {
                // tslint:disable-next-line:no-duplicate-string
                'x-filename': names.join(';'),
                'x-gesuchID': gesuchID,
            },
            data: {
                file: files,
                dokumentGrund: restDokumentString,
            },
        }).then((response: any) => {
            return this.ebeguRestUtil.parseDokumentGrund(new TSDokumentGrund(), response.data);
        }, (response: any) => {
            console.log(this.NOT_SUCCESS);
            return this.q.reject(response);
        }, (evt: any) => {
            this.notifyCallbackByUpload(evt);
        });
    }

    public uploadRueckforderungsDokumente(files: any, rueckforderungFormularId: string,
                                          rueckforderungDokumentTyp: TSRueckforderungDokumentTyp,
    ): IPromise<TSRueckforderungDokument[]> {
        const names = this.encodeFileNames(files);
        return this.upload.upload({
            url: `${this.serviceURL}/uploadRueckforderungsDokument/${encodeURIComponent(rueckforderungFormularId)}/${rueckforderungDokumentTyp}`,
            method: 'POST',
            headers: {
                'x-filename': names.join(';'),
            },
            data: {
                file: files,
            },
        }).then((response: any) => {
            return this.ebeguRestUtil.parseRueckforderungDokumente(response.data);
        }, (response: any) => {
            console.log(this.NOT_SUCCESS);
            return this.q.reject(response);
        }, (evt: any) => {
            this.notifyCallbackByUpload(evt);
        });
    }

    public uploadFerienbetreuungDokumente(files: any, ferienbetreuungContainerId: string):
        IPromise<TSFerienbetreuungDokument[]> {
        const names = this.encodeFileNames(files);
        return this.upload.upload({
            url: `${this.serviceURL}/ferienbetreuungDokumente/${encodeURIComponent(ferienbetreuungContainerId)}`,
            method: 'POST',
            headers: {
                'x-filename': names.join(';'),
            },
            data: {
                file: files,
            },
        }).then((response: any) => {
            return this.ebeguRestUtil.parseFerienbetreuungDokumente(response.data);
        }, (response: any) => {
            console.log(this.NOT_SUCCESS);
            return this.q.reject(response);
        }, (evt: any) => {
            this.notifyCallbackByUpload(evt);
        });
    }

    public uploadGesuchstellerAusweisDokumente(files: any, gesuchstellerContainerId: string):
        IPromise<TSGesuchstellerAusweisDokument[]> {
        const names = this.encodeFileNames(files);
        return this.upload.upload({
            url: `${this.serviceURL}/gesuchstellerausweis/${encodeURIComponent(gesuchstellerContainerId)}`,
            method: 'POST',
            headers: {
                'x-filename': names.join(';'),
            },
            data: {
                file: files,
            },
        }).then((response: any) => {
            return this.ebeguRestUtil.parseGesuchstellerAusweisDokumente(response.data);
        }, (response: any) => {
            console.log(this.NOT_SUCCESS);
            return this.q.reject(response);
        }, (evt: any) => {
            this.notifyCallbackByUpload(evt);
        });
    }

    private encodeFileNames(files: any): string[] {
        const names: string [] = [];
        for (const file of files) {
            if (file) {
                const encodedFilename = this.base64.encode(file.name);
                names.push(encodedFilename);
            }
        }
        return names;
    }

    public uploadZemisExcel(file: File): IPromise<void> {
        return this.upload.upload({
            url: `${this.serviceURL}/zemisExcel`,
            method: 'POST',
            headers: {
                'x-filename': this.base64.encode(file.name),
            },
            data: {
                file,
            },
        }).then((response: any) => {
            return response.data;
        }, (response: any) => {
            console.log(this.NOT_SUCCESS);
            return this.q.reject(response);
        });
    }

    public uploadGesuchsperiodeDokument(file: any, sprache: TSSprache, periodeID: string,
                                        dokumentTyp: TSDokumentTyp,
    ): IPromise<any> {
        return this.upload.upload({
            url: `${this.serviceURL}/gesuchsperiodeDokument/${sprache}/${periodeID}/${dokumentTyp}`,
            method: 'POST',
            data: {
                file,
            },
        }).then((response: any) => {
            return response.data;
        }, (response: any) => {
            console.log(this.NOT_SUCCESS);
            return this.q.reject(response);
        });
    }

    public uploadGemeindeGesuchsperiodeDokument(file: any, sprache: TSSprache, gemeindeId: string, periodeID: string,
                                                dokumentTyp: TSDokumentTyp,
    ): IPromise<any> {
        return this.upload.upload({
            // tslint:disable-next-line:max-line-length
            url: `${this.serviceURL}/gemeindeGesuchsperiodeDoku/${encodeURIComponent(gemeindeId)}/${encodeURIComponent(
                periodeID)}/${sprache}/${dokumentTyp}`,
            method: 'POST',
            data: {
                file,
            },
        }).then((response: any) => {
            return response.data;
        }, (response: any) => {
            console.log('Upload Gesuchsperiode Gemeinde File: NOT SUCCESS');
            return this.q.reject(response);
        });
    }

    public getServiceName(): string {
        return 'UploadRS';
    }

    public uploadVollmachtDokument(vollmacht: any, fallId: string): IPromise<any> {
        const encodedFilename = this.base64.encode(vollmacht.name);
        return this.upload.upload({
            // tslint:disable-next-line:max-line-length
            url: `${this.serviceURL}/uploadSozialdienstFallsDokument/${encodeURIComponent(fallId)}`,
            headers: {
                'x-filename': encodedFilename,
            },
            data: {
                file: vollmacht,
            },
        }).then((response: any) => {
            return this.ebeguRestUtil.parseSozialdienstFallDokumente(response.data);
        }, (response: any) => {
            console.log('Upload Vollmacht File: NOT SUCCESS');
            return this.q.reject(response);
        });
    }

    private notifyCallbackByUpload(evt: any): void {
        const loaded: number = evt.loaded;
        const total: number = evt.total;
        const progressPercentage = 100 * loaded / total;
        console.log(`progress: ${progressPercentage}% `);
        this.q.defer().notify();
    }
}
