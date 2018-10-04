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

import {IHttpService, IIntervalService, ILogService, IPromise, IWindowService} from 'angular';
import {TSGeneratedDokumentTyp} from '../../../models/enums/TSGeneratedDokumentTyp';
import TSDownloadFile from '../../../models/TSDownloadFile';
import TSMahnung from '../../../models/TSMahnung';
import EbeguRestUtil from '../../../utils/EbeguRestUtil';
import EbeguUtil from '../../../utils/EbeguUtil';

export class DownloadRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', '$window', '$interval'];

    public serviceURL: string;

    public constructor(
        public http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        public log: ILogService,
        private readonly $window: IWindowService,
        private readonly $interval: IIntervalService,
    ) {
        this.serviceURL = `${REST_API}blobs/temp`;
    }

    public getAccessTokenDokument(dokumentID: string): IPromise<TSDownloadFile> {
        return this.http.get(`${this.serviceURL}/${encodeURIComponent(dokumentID)}/dokument`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data);
            });
    }

    public getAccessTokenVorlage(vorlageID: string): IPromise<TSDownloadFile> {
        return this.http.get(`${this.serviceURL}/${encodeURIComponent(vorlageID)}/vorlage`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data);
            });
    }

    public getFinSitDokumentAccessTokenGeneratedDokument(gesuchId: string): IPromise<TSDownloadFile> {
        return this.http.get(`${this.serviceURL}/${encodeURIComponent(gesuchId)}/FINANZIELLE_SITUATION/generated`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data);
            });
    }

    public getBegleitschreibenDokumentAccessTokenGeneratedDokument(gesuchId: string): IPromise<TSDownloadFile> {
        return this.http.get(`${this.serviceURL}/${encodeURIComponent(gesuchId)}/BEGLEITSCHREIBEN/generated`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data);
            });
    }

    public getKompletteKorrespondenzAccessTokenGeneratedDokument(gesuchId: string): IPromise<TSDownloadFile> {
        return this.http.get(`${this.serviceURL}/${encodeURIComponent(gesuchId)}/KOMPLETTEKORRESPONDENZ/generated`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data);
            });
    }

    public getFreigabequittungAccessTokenGeneratedDokument(
        gesuchId: string,
        forceCreation: boolean,
    ): IPromise<TSDownloadFile> {
        const dokumentTypEnc = encodeURIComponent(TSGeneratedDokumentTyp[TSGeneratedDokumentTyp.FREIGABEQUITTUNG]);
        const gesuchIdEnc = encodeURIComponent(gesuchId);
        const url = `${this.serviceURL}/${gesuchIdEnc}/${dokumentTypEnc}/${forceCreation}/generated`;

        return this.http.get(url)
            .then((response: any) => {
                return this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data);
            });
    }

    public getAccessTokenMahnungGeneratedDokument(mahnung: TSMahnung): IPromise<TSDownloadFile> {
        let restMahnung = {};
        restMahnung = this.ebeguRestUtil.mahnungToRestObject(restMahnung, mahnung);
        const dokumentTypEnc = encodeURIComponent(TSGeneratedDokumentTyp[TSGeneratedDokumentTyp.MAHNUNG]);

        return this.http.put(`${this.serviceURL}/${dokumentTypEnc}/generated`, restMahnung)
            .then((response: any) => {
                return this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data);
            });
    }

    public getAccessTokenVerfuegungGeneratedDokument(
        gesuchId: string,
        betreuungId: string,
        forceCreation: boolean,
        manuelleBemerkungen: string,
    ): IPromise<TSDownloadFile> {

        const dokumentTypEnc = encodeURIComponent(TSGeneratedDokumentTyp[TSGeneratedDokumentTyp.VERFUEGUNG]);
        const gesuchIdEnc = encodeURIComponent(gesuchId);
        const betreuungIdEnc = encodeURIComponent(betreuungId);
        const url = `${this.serviceURL}/${gesuchIdEnc}/${betreuungIdEnc}/${dokumentTypEnc}/${forceCreation}/generated`;

        return this.http.post(url, manuelleBemerkungen, {headers: {'Content-Type': 'text/plain'}})
            .then((response: any) => {
                return this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data);
            });
    }

    public getAccessTokenNichteintretenGeneratedDokument(
        betreuungId: string,
        forceCreation: boolean,
    ): IPromise<TSDownloadFile> {

        const dokumentTypEnc = encodeURIComponent(TSGeneratedDokumentTyp[TSGeneratedDokumentTyp.NICHTEINTRETEN]);
        const betreuungIdEnc = encodeURIComponent(betreuungId);
        const url = `${this.serviceURL}/${betreuungIdEnc}/${dokumentTypEnc}/${forceCreation}/generated`;

        return this.http.get(url)
            .then((response: any) => {
                return this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data);
            });
    }

    public getPain001AccessTokenGeneratedDokument(zahlungsauftragId: string): IPromise<TSDownloadFile> {
        const dokumentTypEnc = encodeURIComponent(TSGeneratedDokumentTyp[TSGeneratedDokumentTyp.PAIN001]);
        const url = `${this.serviceURL}/${encodeURIComponent(zahlungsauftragId)}/${dokumentTypEnc}/generated`;

        return this.http.get(url)
            .then((response: any) => {
                return this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data);
            });
    }

    public getDokumentAccessTokenVerfuegungExport(betreuungId: string): IPromise<TSDownloadFile> {
        return this.http.get(`${this.serviceURL}/${encodeURIComponent(betreuungId)}/EXPORT`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data);
            });
    }

    public getAccessTokenBenutzerhandbuch(): IPromise<TSDownloadFile> {
        return this.http.get(`${this.serviceURL}/BENUTZERHANDBUCH`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data);
            });
    }

    public getServiceName(): string {
        return 'DownloadRS';
    }

    /**
     * Das Window muss als Parameter mitgegeben werden, damit der Popup Blocker das Oeffnen dieses Fesnters nicht als
     * Popup identifiziert.
     */
    public startDownload(accessToken: string, _dokumentName: string, attachment: boolean, myWindow: Window): void {
        if (!myWindow) {
            this.log.error('Download popup window was not initialized');
            return;
        }

        let href = `${this.serviceURL}/blobdata/${accessToken}`;
        if (attachment) {
            // add MatrixParam for to download file instead of opening it inline
            href += ';attachment=true;';
        } else {
            myWindow.focus();
        }
        // as soon as the window is ready send it to the download
        this.addCloseButtonHandler(myWindow);
        this.redirectWindowToDownloadWhenReady(myWindow, href, accessToken);

        // This would be the way to open file in new window (for now it's better to open in new tab)
        // this.$window.open(href, name, 'toolbar=0,location=0,menubar=0');
    }

    public prepareDownloadWindow(): Window {
        return this.$window.open('assets/downloadWindow/downloadWindow.html', EbeguUtil.generateRandomName(5));
    }

    private redirectWindowToDownloadWhenReady(win: Window, href: string, _name: string): void {
        // wir pruefen den dokumentstatus alle 100ms, insgesamt maximal 300 mal
        const count = 300;
        const readyTimer = this.$interval(() => {
            if (win.document.readyState !== 'complete') {
                return;
            }
            this.$interval.cancel(readyTimer);
            // do stuff
            this.hideSpinner(win);
            win.open(href, win.name);
        }, 100, count);
    }

    /**
     * Es kann sein, dass das popup noch gar nicht fertig gerendert ist bevor wir den spinner schon wieder verstecken
     * wollen in diesem fall warten wir noch bis das popup in den readyState 'conplete' wechselt und verstecken den
     * spinner dann
     */
    public hideSpinner(win: Window): void {
        this.log.debug('hiding spinner');
        const element = win.document.getElementById('spinnerCont');
        if (element) {
            element.style.display = 'none';
        } else {
            console.log('element not found, can not hide spinner');
        }
        const buttonElement = win.document.getElementById('closeButton');
        if (buttonElement) {
            buttonElement.style.display = 'block';
            this.addCloseButtonHandler(win);
        }
    }

    public addCloseButtonHandler(win: Window): void {
        const element = win.document.getElementById('closeButton');
        if (!element) {
            console.log('element not found, can not attach window close handler spinner');
            return;
        }

        element.addEventListener('click', () => win.close(), false);
    }
}
