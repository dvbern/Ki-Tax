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

import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import * as moment from 'moment';
import {Observable} from 'rxjs';
import {DvNgConfirmDialogComponent} from '../../../app/core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {
    DvNgDisplayObjectDialogComponent,
} from '../../../app/core/component/dv-ng-display-object-dialog/dv-ng-display-object-dialog.component';
import {DvNgLinkDialogComponent} from '../../../app/core/component/dv-ng-link-dialog/dv-ng-link-dialog.component';
import {DvNgOkDialogComponent} from '../../../app/core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';
import {DvNgRemoveDialogComponent} from '../../../app/core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {BenutzerRSX} from '../../../app/core/service/benutzerRSX.rest';
import {GesuchsperiodeRS} from '../../../app/core/service/gesuchsperiodeRS.rest';
import {GemeindeAntragService} from '../../../app/gemeinde-antraege/services/gemeinde-antrag.service';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {GesuchRS} from '../../../gesuch/service/gesuchRS.rest';
import {TSPagination} from '../../../models/dto/TSPagination';
import {TSGemeindeAntragTyp} from '../../../models/enums/TSGemeindeAntragTyp';
import {TSKibonAnfrage} from '../../../models/neskovanp/TSKibonAnfrage';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSBenutzerNoDetails} from '../../../models/TSBenutzerNoDetails';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {TestFaelleRS} from '../../service/testFaelleRS.rest';

const LOG = LogFactory.createLog('TestdatenView');

@Component({
    selector: 'dv-testdaten-view',
    templateUrl: './testdatenView.component.html',
    styleUrls: ['./testdatenView.component.less'],
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

    // kiBonAnfrage Schnitstelle Test
    public antragId: string = '';
    public zpvNummer: number = 10099208;
    public gesuchsperiodeBeginnJahr: number = 2020;
    public geburtsdatum: moment.Moment = moment('1964-12-09', 'YYYY-MM-DD');
    public kibonAnfrageTestEnabled: boolean = false;

    public constructor(
        public readonly testFaelleRS: TestFaelleRS,
        private readonly benutzerRS: BenutzerRSX,
        private readonly errorService: ErrorService,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly gemeindeRS: GemeindeRS,
        private readonly dialog: MatDialog,
        private readonly gemeindeAntragRS: GemeindeAntragService,
        private readonly gesuchRS: GesuchRS
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
            this.gemeindeList = response;
            this.gemeindeList.sort((a, b) => a.name.localeCompare(b.name));
        });
        this.applicationPropertyRS.isEbeguKibonAnfrageTestGuiEnabled().then(response => {
            this.kibonAnfrageTestEnabled = response;
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
        verfuegen: boolean
    ): void {
        this.testFaelleRS.createTestFall(testFall, gesuchsperiodeId, gemeindeId, bestaetigt, verfuegen).subscribe(
            response => {
                this.createLinkDialog(response);
            }, err => LOG.error(err));
    }

    private createTestFallGS(
        testFall: string,
        gesuchsperiodeId: string,
        gemeindeId: string,
        bestaetigt: boolean,
        verfuegen: boolean,
        username: string
    ): void {
        this.testFaelleRS.createTestFallGS(testFall,
            gesuchsperiodeId,
            gemeindeId,
            bestaetigt,
            verfuegen,
            username).subscribe(response => {
            this.createLinkDialog(response);
        }, err => LOG.error(err));
    }

    public removeGesucheGS(): void {
        this.testFaelleRS.removeFaelleOfGS(this.selectedBesitzer.username).subscribe(() => {
            this.errorService.addMesageAsInfo(`Gesuche entfernt für ${this.selectedBesitzer.username}`);
        }, err => LOG.error(err));
    }

    public removeGesuchsperiode(): void {
        this.gesuchsperiodeRS.removeGesuchsperiode(this.selectedGesuchsperiode.id)
            .then(() => {
                const msg = `Gesuchsperiode entfernt ${this.selectedGesuchsperiode.gesuchsperiodeString}`;
                this.errorService.addMesageAsInfo(msg);
            });
    }

    public mutiereFallHeirat(): void {
        this.testFaelleRS.mutiereFallHeirat(this.dossierid,
            this.selectedGesuchsperiode.id,
            this.eingangsdatum,
            this.ereignisdatum)
            .subscribe(response => {
                this.createAndOpenOkDialog(response);
            }, error => LOG.error(error));
    }

    public testAllMails(): void {
        this.testFaelleRS.testAllMails(this.mailadresse).subscribe();
    }

    public mutiereFallScheidung(): void {
        this.testFaelleRS.mutiereFallScheidung(this.dossierid,
            this.selectedGesuchsperiode.id,
            this.eingangsdatum,
            this.ereignisdatum)
            .subscribe(response => {
                this.createAndOpenOkDialog(response);
            }, error => LOG.error(error));
    }

    public resetSchulungsdaten(): void {
        this.testFaelleRS.resetSchulungsdaten().subscribe(response => {
            this.createAndOpenOkDialog(response);
        }, error => LOG.error(error));
    }

    public deleteSchulungsdaten(): void {
        this.testFaelleRS.deleteSchulungsdaten().subscribe(response => {
            this.createAndOpenOkDialog(response);
        }, error => LOG.error(error));
    }

    public createTutorialdaten(): void {
        this.testFaelleRS.createTutorialdaten().subscribe(response => {
            this.createAndOpenOkDialog(response);
        }, error => LOG.error(error));
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
        const uuidPartOfString = response ? response.slice(uuidLength) : '';
        // nicht alle Parameter werden benoetigt, deswegen sind sie leer
        this.createAndOpenLinkDialog$(response, `#/gesuch/fall////${uuidPartOfString}//`);
    }

    private createAndOpenLinkDialog$(title: string, link: string): Observable<boolean> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title,
            link
        };
        return this.dialog.open(DvNgLinkDialogComponent, dialogConfig).afterClosed();
    }

    public async createGemeindeAntragTestDaten(): Promise<void> {

        if (this.latsSelected() && !this.gesuchsperiodeGemeindeAntrag) {
            this.errorService.addMesageAsError('Gesuchsperiode muss ausgewählt sein');
            return;
        }

        // eslint-disable-next-line
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
            this.gemeindeAntragStatus).subscribe(response => {
            this.errorService.clearAll();
            if (this.ferienbetreuungSelected()) {
                this.createAndOpenLinkDialog$(
                  `Ferienbetreuung für ${this.gemeindeForGemeindeAntrag.name} ${this.gesuchsperiodeGemeindeAntrag.gesuchsperiodeString} erstellt`,
                    `#/ferienbetreuung/${response}/stammdaten-gemeinde`);
            } else if (this.gemeindeForGemeindeAntrag) {
                this.createAndOpenLinkDialog$(
                    `LATS für ${this.gemeindeForGemeindeAntrag.name} ${this.gesuchsperiodeGemeindeAntrag.gesuchsperiodeString} erstellt`,
                    `#/lastenausgleich-ts/${response}/angaben-gemeinde`);
            } else {
                this.errorService.addMesageAsInfo('Anträge für Periode erstellt');
            }
        }, () => this.errorService.addMesageAsError('Anträge konnten nicht erstellt werden'));
    }

    public testKibonAnfrageResponse(): void {
        this.errorService.clearAll();
        this.gesuchRS.getSteuerdaten(new TSKibonAnfrage(this.antragId,
            this.zpvNummer,
            this.gesuchsperiodeBeginnJahr,
            this.geburtsdatum)).then(result => {
                console.log(result);
                this.dialog.open(DvNgDisplayObjectDialogComponent,
                    {data: {object: result}});
            }
        );
    }

    private async overwriteIfGemeindeAntragExists(): Promise<boolean> {
        const antraege = await this.gemeindeAntragRS.getGemeindeAntraege({
                antragTyp: this.gemeindeAntragTyp,
                gesuchsperiodeString: this.gesuchsperiodeGemeindeAntrag.gesuchsperiodeString,
                gemeinde: this.gemeindeForGemeindeAntrag.name
            },
            {},
            new TSPagination()
        ).toPromise();
        return antraege.resultList.length === 0 || this.confirmDialog(
            'Es existiert bereits ein Antrag für die gewählte Gemeinde und Periode. Fortfahren?'
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
                frage: text
            }
        }).afterClosed()
            .toPromise();
    }

    public getGesuchstellerDataTestValue(gs: TSBenutzerNoDetails): string {
        return `gesuchsteller.${gs.vorname}-${gs.nachname}`;
    }
}
