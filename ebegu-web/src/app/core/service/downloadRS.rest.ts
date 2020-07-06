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
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSGeneratedDokumentTyp} from '../../../models/enums/TSGeneratedDokumentTyp';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {TSMahnung} from '../../../models/TSMahnung';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import ITranslateService = angular.translate.ITranslateService;

export class DownloadRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', '$window', '$interval', '$translate'];

    public serviceURL: string;

    public constructor(
        public http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        public log: ILogService,
        private readonly $window: IWindowService,
        private readonly $interval: IIntervalService,
        private readonly $translate: ITranslateService,
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

    public getAccessTokenNotrechtvorlageOeffentlicheInstitutionen(
        language: string, angebotTyp: TSBetreuungsangebotTyp): IPromise<TSDownloadFile> {
        const languageEnc = encodeURIComponent(language);
        const angebotTypEnc = encodeURIComponent(angebotTyp);
        return this.http.get(`${this.serviceURL}/NOTRECHTVORLAGEOEFFENTLICH/${languageEnc}/${angebotTypEnc}`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data);
            });
    }

    public getAccessTokenNotrechtvorlagePrivateInstitutionen(
        language: string, angebotTyp: TSBetreuungsangebotTyp): IPromise<TSDownloadFile> {
        const languageEnc = encodeURIComponent(language);
        const angebotTypEnc = encodeURIComponent(angebotTyp);
        return this.http.get(`${this.serviceURL}/NOTRECHTVORLAGEPRIVAT/${languageEnc}/${angebotTypEnc}`)
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
        this.redirectWindowToDownloadWhenReady(myWindow, href, accessToken);

        // This would be the way to open file in new window (for now it's better to open in new tab)
        // this.$window.open(href, name, 'toolbar=0,location=0,menubar=0');
    }

    public prepareDownloadWindow(): Window {
        return this.$window.open(`assets/downloadWindow/downloadWindow.html?spinnerTextLoading=${encodeURIComponent(this.$translate.instant('DOWNLOAD_WINDOW_LOADING_TEXT'))}&spinnerTitle=${encodeURIComponent(this.$translate.instant('DOWNLOAD_WINDOW_TITLE'))}`,
            EbeguUtil.generateRandomName(5));
    }

    public redirectWindowToDownloadWhenReady(win: Window, href: string, _name: string): void {
        // wir pruefen den dokumentstatus alle 100ms, insgesamt maximal 300 mal
        const count = 3000;
        const readyTimer = this.$interval(() => {
            if (!win.document || win.document.readyState !== 'complete') {
                return;
            }
            this.$interval.cancel(readyTimer);
            // do stuff
            this.downloadFinished(win);
            win.open(href, win.name);
        }, 100, count);
    }

    private downloadFinished(win: Window): void {
        this.hideElement(win, 'spinnerCont');
        this.addTextToElement(win, 'spinnerText', 'DOWNLOAD_WINDOW_FINISHED_TEXT');
        this.showCloseButton(win);
    }

    public addTextToElement(win: Window, elementId: string, textToTranslate: string): void {
        const element = win.document.getElementById(elementId);
        if (!element) {
            this.log.error(`element not found, can not add text to element ${elementId}`);
            return;
        }

        // noinspection InnerHTMLJS
        element.innerHTML = this.$translate.instant(textToTranslate);
    }

    private showCloseButton(win: Window): void {
        const buttonElement = win.document.getElementById('closeButton');
        if (!buttonElement) {
            return;
        }
        buttonElement.style.display = 'block';
        this.addCloseButtonHandler(win, buttonElement);
        this.addTextToElement(win, 'buttonText', 'DOWNLOAD_WINDOW_CLOSE_TEXT');
    }

    public addCloseButtonHandler(win: Window, element: HTMLElement): void {
        element.addEventListener('click', () => win.close(), false);
    }

    /**
     * Es kann sein, dass das popup noch gar nicht fertig gerendert ist bevor wir das Element schon wieder verstecken
     * wollen in diesem fall warten wir noch bis das popup in den readyState 'complete' wechselt und verstecken das
     * Element dann
     */
    public hideElement(win: Window, elementId: string): void {
        const element = win.document.getElementById(elementId);
        if (element) {
            element.style.display = 'none';
        } else {
            this.log.error(`element not found, can not hide ${elementId}`);
        }
    }

    public getAccessTokenAnmeldebestaetigungGeneratedDokument(
        gesuchId: string,
        anmeldungId: string,
        forceCreation: boolean,
        mitTarif: boolean
    ): IPromise<TSDownloadFile> {
        const dokumentTypEnc = encodeURIComponent(TSGeneratedDokumentTyp[TSGeneratedDokumentTyp.ANMELDEBESTAETIGUNG]);
        const gesuchIdEnc = encodeURIComponent(gesuchId);
        const anmeldungIdEnc = encodeURIComponent(anmeldungId);
        const url = `${this.serviceURL}/${gesuchIdEnc}/${anmeldungIdEnc}/${dokumentTypEnc}/${forceCreation}/${mitTarif}/generated`;

        return this.http.get(url)
            .then((response: any) => {
                return this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data);
            });
    }

    public openDownload(blob: Blob, filename: string): void {
        // tslint:disable-next-line
        if (typeof this.$window.navigator.msSaveBlob !== 'undefined') {
            // IE workaround for "HTML7007: One or more blob URLs were revoked by closing the blob for which
            // they were created. These URLs will no longer resolve as the data backing the URL has been
            // freed."
            this.$window.navigator.msSaveBlob(blob, filename);
            return;
        }
        // @ts-ignore: webkitURL may exist in some browsers
        const url = this.$window.URL || this.$window.webkitURL;
        const downloadUrl = url.createObjectURL(blob);        // use HTML5 a[download] attribute to specify filename
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        this.$window.URL.revokeObjectURL(url);
        a.remove();
    }

    public getAccessTokenRueckforderungDokument(rueckForderungDokumentId: string): IPromise<TSDownloadFile> {
        return this.http.get(
            `${this.serviceURL}/${encodeURIComponent(rueckForderungDokumentId)}/rueckforderungDokument`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data);
            });
    }
}
