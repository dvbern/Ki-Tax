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

import {Component, OnInit} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {IPromise} from 'angular';
import * as moment from 'moment';
import {Observable} from 'rxjs';
import {DvNgConfirmDialogComponent} from '../../../app/core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {DvNgLinkDialogComponent} from '../../../app/core/component/dv-ng-link-dialog/dv-ng-link-dialog.component';
import {DvNgOkDialogComponent} from '../../../app/core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';
import {DvNgRemoveDialogComponent} from '../../../app/core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {BenutzerRS} from '../../../app/core/service/benutzerRS.rest';
import {GesuchsperiodeRS} from '../../../app/core/service/gesuchsperiodeRS.rest';
import {GemeindeAntragService} from '../../../app/gemeinde-antraege/services/gemeinde-antrag.service';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSGemeindeAntragTyp} from '../../../models/enums/TSGemeindeAntragTyp';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSBenutzerNoDetails} from '../../../models/TSBenutzerNoDetails';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {TestFaelleRS} from '../../service/testFaelleRS.rest';

@Component({
    selector: 'dv-testdaten-view',
    templateUrl: './testdatenView.html',
    styleUrls: ['./testdatenView.less'],
})
export class TestdatenViewComponent implements OnInit {

    public dossierid: string;
    public eingangsdatum: moment.Moment;
    public ereignisdatum: moment.Moment;

    public creationType: string = 'verfuegt';
    public selectedBesitzer: TSBenutzer;
    public gesuchstellerList: Array<TSBenutzerNoDetails>;

    public selectedGesuchsperiode: TSGesuchsperiode;
    public gesuchsperiodeList: Array<TSGesuchsperiode>;

    public selectedGemeinde: TSGemeinde;
    public gemeindeList: Array<TSGemeinde>;

    public mailadresse: string;

    public devMode: boolean;

    public gesuchsperiodeGemeindeAntrag: TSGesuchsperiode;
    public gemeindeForGemeindeAntrag: TSGemeinde;
    public gemeindeAntragStatus: string = 'IN_BEARBEITUNG_GEMEINDE';
    public gemeindeAntragTyp: TSGemeindeAntragTyp;
    public gemeindeAntragTypeList: TSGemeindeAntragTyp[];

    public constructor(
        public readonly testFaelleRS: TestFaelleRS,
        private readonly benutzerRS: BenutzerRS,
        private readonly errorService: ErrorService,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly gemeindeRS: GemeindeRS,
        private readonly dialog: MatDialog,
        private readonly gemeindeAntragRS: GemeindeAntragService,
    ) {
    }

    public ngOnInit(): void {
        this.benutzerRS.getAllGesuchsteller().then(result => {
            this.gesuchstellerList = result;
        });
        this.gesuchsperiodeRS.getAllGesuchsperioden().then(result => {
            this.gesuchsperiodeList = result;
        });
        this.applicationPropertyRS.isDevMode().then(response => {
            this.devMode = response;
        });
        this.gemeindeRS.getAktiveGemeinden().then(response => {
            this.gemeindeList = angular.copy(response);
            this.gemeindeList.sort((a, b) => a.name.localeCompare(b.name));
        });
        this.initGemeindeAntragTypes();
    }

    public createTestFallType(testFall: string): void {
        if (!this.selectedGesuchsperiode ||
            !this.selectedGemeinde) {
            this.errorService.addMesageAsError('Gemeinde und Gesuchsperiode müssen ausgewählt sein');
            return;
        }
        let bestaetigt = false;
        let verfuegen = false;
        switch (this.creationType) {
            case 'warten':
                bestaetigt = false;
                verfuegen = false;
                break;
            case 'bestaetigt':
                bestaetigt = true;
                verfuegen = false;
                break;
            case 'verfuegt':
                bestaetigt = true;
                verfuegen = true;
                break;
            default:
                throw new Error(`not implemented for creationType ${this.creationType}`);
        }
        if (this.selectedBesitzer) {
            this.createTestFallGS(testFall,
                this.selectedGesuchsperiode.id,
                this.selectedGemeinde.id,
                bestaetigt,
                verfuegen,
                this.selectedBesitzer.username);
        } else {
            this.createTestFall(testFall,
                this.selectedGesuchsperiode.id,
                this.selectedGemeinde.id,
                bestaetigt,
                verfuegen);
        }
    }

    private createTestFall(
        testFall: string,
        gesuchsperiodeId: string,
        gemeindeId: string,
        bestaetigt: boolean,
        verfuegen: boolean,
    ): void {
        this.testFaelleRS.createTestFall(testFall, gesuchsperiodeId, gemeindeId, bestaetigt, verfuegen).then(
            response => {
                this.createLinkDialog(response);
            });
    }

    private createTestFallGS(
        testFall: string,
        gesuchsperiodeId: string,
        gemeindeId: string,
        bestaetigt: boolean,
        verfuegen: boolean,
        username: string,
    ): void {
        this.testFaelleRS.createTestFallGS(testFall,
            gesuchsperiodeId,
            gemeindeId,
            bestaetigt,
            verfuegen,
            username).then(response => {
            this.createLinkDialog(response);
        });
    }

    public removeGesucheGS(): void {
        this.testFaelleRS.removeFaelleOfGS(this.selectedBesitzer.username).then(() => {
            this.errorService.addMesageAsInfo(`Gesuche entfernt fuer ${this.selectedBesitzer.username}`);
        });
    }

    public removeGesuchsperiode(): void {
        this.gesuchsperiodeRS.removeGesuchsperiode(this.selectedGesuchsperiode.id)
            .then(() => {
                const msg = `Gesuchsperiode entfernt ${this.selectedGesuchsperiode.gesuchsperiodeString}`;
                this.errorService.addMesageAsInfo(msg);
            });
    }

    public mutiereFallHeirat(): IPromise<any> {
        return this.testFaelleRS.mutiereFallHeirat(this.dossierid,
            this.selectedGesuchsperiode.id,
            this.eingangsdatum,
            this.ereignisdatum)
            .then(response => {
                this.createAndOpenOkDialog(response.data);
            });
    }

    public testAllMails(): IPromise<any> {
        return this.testFaelleRS.testAllMails(this.mailadresse);
    }

    public mutiereFallScheidung(): IPromise<any> {
        return this.testFaelleRS.mutiereFallScheidung(this.dossierid,
            this.selectedGesuchsperiode.id,
            this.eingangsdatum,
            this.ereignisdatum)
            .then(response => {
                this.createAndOpenOkDialog(response.data);
            });
    }

    public resetSchulungsdaten(): IPromise<any> {
        return this.testFaelleRS.resetSchulungsdaten().then(response => {
            this.createAndOpenOkDialog(response.data);
        });
    }

    public deleteSchulungsdaten(): IPromise<any> {
        return this.testFaelleRS.deleteSchulungsdaten().then(response => {
            this.createAndOpenOkDialog(response.data);
        });
    }

    public createTutorialdaten(): IPromise<any> {
        return this.testFaelleRS.createTutorialdaten().then(response => {
            this.createAndOpenOkDialog(response.data);
        });
    }

    private createAndOpenOkDialog(title: string): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {title};

        this.dialog.open(DvNgOkDialogComponent, dialogConfig).afterClosed();
    }

    private createAndOpenRemoveDialog$(title: string, text: string): Observable<boolean> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {title, text};

        return this.dialog.open(DvNgRemoveDialogComponent, dialogConfig).afterClosed();
    }

    private createLinkDialog(response: any): void {
        // einfach die letzten 36 zeichen der response als uuid betrachten, hacky ist aber nur fuer uns intern
        const uuidLength = -36;
        const uuidPartOfString = response.data ? response.data.slice(uuidLength) : '';
        // nicht alle Parameter werden benoetigt, deswegen sind sie leer
        this.createAndOpenLinkDialog$(response.data, `#/gesuch/fall////${uuidPartOfString}//`);
    }

    private createAndOpenLinkDialog$(title: string, link: string): Observable<boolean> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title,
            link,
        };
        return this.dialog.open(DvNgLinkDialogComponent, dialogConfig).afterClosed();
    }

    public async createGemeindeAntragTestDaten(): Promise<void> {

        if (this.latsSelected() && !this.gesuchsperiodeGemeindeAntrag) {
            this.errorService.addMesageAsError('Gesuchsperiode muss ausgewählt sein');
            return;
        }

        // tslint:disable-next-line:no-collapsible-if
        if (this.latsSelected() && !this.gemeindeForGemeindeAntrag) {
            if (!await this.confirmDialog(
                'Ohne ausgewählte Gemeinde werden die LATS Formular für ALLE Gemeinden erstellt/überschrieben. Fortfahren?')) {
                return;
            }
        }

        if (!this.gesuchsperiodeGemeindeAntrag ||
            this.ferienbetreuungSelected() && !this.gemeindeForGemeindeAntrag) {
            this.errorService.addMesageAsError('Gemeinde und Gesuchsperiode müssen ausgewählt sein');
            return;
        }

        if (this.gemeindeForGemeindeAntrag && !await this.overwriteIfGemeindeAntragExists()) {
            return;
        }

        this.testFaelleRS.createGemeindeAntragTestDaten(this.gemeindeAntragTyp,
            this.gesuchsperiodeGemeindeAntrag,
            this.gemeindeForGemeindeAntrag,
            this.gemeindeAntragStatus).then(() => {
            this.errorService.clearAll();
            this.errorService.addMesageAsInfo('Gemeindeanträge erstellt');
        }, () => this.errorService.addMesageAsError('Anträge konnten nicht erstellt werden'));
    }

    private async overwriteIfGemeindeAntragExists(): Promise<boolean> {
        const antraege = await this.gemeindeAntragRS.getGemeindeAntraege({
            antragTyp: this.gemeindeAntragTyp,
            gesuchsperiodeString: this.gesuchsperiodeGemeindeAntrag.gesuchsperiodeString,
            gemeinde: this.gemeindeForGemeindeAntrag.name,
        }, {}).toPromise();
        return antraege.length === 0 || this.confirmDialog(
            'Es existiert bereits ein Antrag für die gewählte Gemeinde und Periode. Fortfahren?',
        );
    }

    private initGemeindeAntragTypes(): void {
        this.applicationPropertyRS.getPublicPropertiesCached()
            .then(configs => {
                this.gemeindeAntragTypeList = [];
                if (configs.ferienbetreuungAktiv) {
                    this.gemeindeAntragTypeList.push(TSGemeindeAntragTyp.FERIENBETREUUNG);
                }
                if (configs.lastenausgleichTagesschulenAktiv) {
                    this.gemeindeAntragTypeList.push(TSGemeindeAntragTyp.LASTENAUSGLEICH_TAGESSCHULEN);
                }
                this.gemeindeAntragTyp = this.gemeindeAntragTypeList[0];
            }, error => {
                console.error(error);
            });
    }

    private ferienbetreuungSelected(): boolean {
        return this.gemeindeAntragTyp === TSGemeindeAntragTyp.FERIENBETREUUNG;
    }

    private latsSelected(): boolean {
        return this.gemeindeAntragTyp === TSGemeindeAntragTyp.LASTENAUSGLEICH_TAGESSCHULEN;
    }

    private async confirmDialog(text: string): Promise<boolean> {
        return this.dialog.open(DvNgConfirmDialogComponent, {
            data: {
                frage: text,
            },
        }).afterClosed()
            .toPromise();
    }
}
