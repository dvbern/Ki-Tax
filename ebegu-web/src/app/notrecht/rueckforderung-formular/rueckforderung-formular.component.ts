/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material';
import {TranslateService} from '@ngx-translate/core';
import {Transition} from '@uirouter/core';
import {from, Observable} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSRole} from '../../../models/enums/TSRole';
import {TSRueckforderungDokumentTyp} from '../../../models/enums/TSRueckforderungDokumentTyp';
import {TSRueckforderungInstitutionTyp} from '../../../models/enums/TSRueckforderungInstitutionTyp';
import {isNeuOrEingeladenStatus, TSRueckforderungStatus} from '../../../models/enums/TSRueckforderungStatus';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {TSRueckforderungDokument} from '../../../models/TSRueckforderungDokument';
import {TSRueckforderungFormular} from '../../../models/TSRueckforderungFormular';
import {TSRueckforderungZahlung} from '../../../models/TSRueckforderungZahlung';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvNgOkDialogComponent} from '../../core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';
import {DvNgRemoveDialogComponent} from '../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {MAX_FILE_SIZE} from '../../core/constants/CONSTANTS';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {NotrechtRS} from '../../core/service/notrechtRS.rest';
import {UploadRS} from '../../core/service/uploadRS.rest';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';

@Component({
    selector: 'dv-rueckforderung-formular',
    templateUrl: './rueckforderung-formular.component.html',
    styleUrls: ['./rueckforderung-formular.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class RueckforderungFormularComponent implements OnInit {
    public get rueckforderungZahlungenList(): TSRueckforderungZahlung[] {
        return this._rueckforderungZahlungenList;
    }

    @ViewChild(NgForm) private readonly form: NgForm;

    public rueckforderungFormular$: Observable<TSRueckforderungFormular>;

    // Checkbox for Institution Stufe 1:
    public betreuungKorrektAusgewiesen: boolean;
    public gutscheinPlaetzenReduziert: boolean;
    public erstattungGemaessKanton: boolean;
    public mahlzeitenBGSubventionenGebuehrensystem: boolean;
    public belegeEinreichenBetrageKantonZurueckfordern: boolean;
    // Checkbox for Institution Stufe 2:
    public elternbeitraegeNichtInRechnung: boolean;
    public notwendigenInformationenLiefern: boolean;

    public showMessageFehlendeDokumenteAngaben: boolean = false;
    public showMessageFehlendeDokumenteKommunikation: boolean = false;
    public showMessageFehlendeDokumenteEinsatzplaene: boolean = false;
    public showMessageFehlendeDokumenteKurzarbeit: boolean = false;
    public showMessageFehlendeDokumenteErwerbsersatz: boolean = false;

    private _rueckforderungZahlungenList: TSRueckforderungZahlung[];
    private _stufe1ProvBetrag: number;
    private _stufe2ProvBetrag: number;

    public rueckforderungAngabenDokumente?: TSRueckforderungDokument[];
    public rueckforderungKommunikationDokumente?: TSRueckforderungDokument[];
    public rueckforderungEinsatzplaeneDokumente?: TSRueckforderungDokument[];
    public rueckforderungKurzarbeitDokumente?: TSRueckforderungDokument[];
    public rueckforderungErwerbsersatzDokumente?: TSRueckforderungDokument[];

    public constructor(
        private readonly $transition$: Transition,
        private readonly translate: TranslateService,
        private readonly notrechtRS: NotrechtRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly downloadRS: DownloadRS,
        private readonly dialog: MatDialog,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly i18nServiceRS: I18nServiceRSRest,
        private readonly uploadRS: UploadRS,
        private readonly cdr: ChangeDetectorRef,
    ) {
    }

    public ngOnInit(): void {
        const rueckforederungFormId: string = this.$transition$.params().rueckforderungId;

        if (!rueckforederungFormId) {
            return;
        }
        this.rueckforderungFormular$ = from(
            this.notrechtRS.findRueckforderungFormular(rueckforederungFormId).then(
                (response: TSRueckforderungFormular) => {
                    this.initRueckforderungZahlungen(response);
                    this.initDokumente(response);
                    if (this.isPruefungKantonStufe1(response)) {
                        this.calculateKantonProvBetrag(response, true);
                    }
                    if (this.isInstitutionStufe2(response)) {
                        this.calculateInstiProvBetrag(response, false);
                    }
                    return response;
                }));
    }

    public saveRueckforderungFormular(rueckforderungFormular: TSRueckforderungFormular): void {
        this.showMessageFehlendeDokumenteAngaben = false;
        this.showMessageFehlendeDokumenteKommunikation = false;
        this.showMessageFehlendeDokumenteEinsatzplaene = false;
        this.showMessageFehlendeDokumenteKurzarbeit = false;
        this.showMessageFehlendeDokumenteErwerbsersatz = false;
        if (!this.form.valid) {
            EbeguUtil.selectFirstInvalid();
            return;
        }
        if (isNeuOrEingeladenStatus(rueckforderungFormular.status)) {
            return;
        }
        if (this.isInstitutionStufe2(rueckforderungFormular) && !this.validateDokumente(rueckforderungFormular)) {
            return;
        }

        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: 'RUECKFORDERUNGFORMULAR_CONFIRMATION_TITLE',
            text: '',
        };
        this.dialog.open(DvNgRemoveDialogComponent, dialogConfig).afterClosed()
            .subscribe(answer => {
                    if (answer !== true) {
                        return;
                    }
                    this.doSave(rueckforderungFormular);
                    this.changeDetectorRef.markForCheck();
                },
                () => {
                });
    }

    private doSave(rueckforderungFormular: TSRueckforderungFormular): void {
        // Den Status sollte sicherheitshalber im Backend geprueft und gesetzt werden
        this.rueckforderungFormular$ = from(this.notrechtRS.saveRueckforderungFormular(rueckforderungFormular)
            .then((response: TSRueckforderungFormular) => {
                this.initRueckforderungZahlungen(response);
                return response;
            }));
    }

    public rueckforderungAbschliessen(rueckforderungFormular: TSRueckforderungFormular): void {
        if (rueckforderungFormular.status === TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_1
            && this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
            rueckforderungFormular.status = TSRueckforderungStatus.ABGESCHLOSSEN_OHNE_GESUCH;
        } else {
            // ERROR transition not accepted
            return;
        }

        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: 'RUECKFORDERUNGFORMULAR_CONFIRMATION_TITLE',
            text: '',
        };
        this.dialog.open(DvNgRemoveDialogComponent, dialogConfig).afterClosed()
            .subscribe(answer => {
                    if (answer !== true) {
                        rueckforderungFormular.status = TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_1;
                        this.changeDetectorRef.markForCheck();
                        return;
                    }
                    this.doSave(rueckforderungFormular);
                },
                () => {
                });
    }

    public initRueckforderungZahlungen(rueckfordeungFormular: TSRueckforderungFormular): void {
        this._rueckforderungZahlungenList = [];
        if (EbeguUtil.isNotNullOrUndefined(rueckfordeungFormular.stufe1FreigabeBetrag)) {
            const rueckforderungZahlungStufe1 = new TSRueckforderungZahlung();
            rueckforderungZahlungStufe1.betrag = rueckfordeungFormular.stufe1FreigabeBetrag;
            rueckforderungZahlungStufe1.datumErstellt = rueckfordeungFormular.stufe1FreigabeDatum;
            rueckforderungZahlungStufe1.stufe = 'RUECKFORDERUNG_ZAHLUNGEN_STUFE_1';
            rueckforderungZahlungStufe1.ausgeloest =
                EbeguUtil.isNotNullOrUndefined(rueckfordeungFormular.stufe1FreigabeAusbezahltAm);
            this.rueckforderungZahlungenList.push(rueckforderungZahlungStufe1);
        }
        if (EbeguUtil.isNullOrUndefined(rueckfordeungFormular.stufe2VerfuegungBetrag)) {
            return;
        }
        const rueckforderungZahlungStufe2 = new TSRueckforderungZahlung();
        rueckforderungZahlungStufe2.betrag = rueckfordeungFormular.stufe2VerfuegungBetrag;
        rueckforderungZahlungStufe2.datumErstellt = rueckfordeungFormular.stufe2VerfuegungDatum;
        rueckforderungZahlungStufe2.stufe = 'RUECKFORDERUNG_ZAHLUNGEN_STUFE_2';
        rueckforderungZahlungStufe2.ausgeloest =
            EbeguUtil.isNotNullOrUndefined(rueckfordeungFormular.stufe2VerfuegungAusbezahltAm);
        this.rueckforderungZahlungenList.push(rueckforderungZahlungStufe2);

    }

    public enableRueckforderungAbsenden(rueckforderungFormular: TSRueckforderungFormular): boolean {
        if (this.isInstitutionStufe1(rueckforderungFormular)) {
            return this.betreuungKorrektAusgewiesen
                && this.gutscheinPlaetzenReduziert
                && this.erstattungGemaessKanton
                && this.mahlzeitenBGSubventionenGebuehrensystem
                && this.belegeEinreichenBetrageKantonZurueckfordern;
        }
        return this.elternbeitraegeNichtInRechnung && this.notwendigenInformationenLiefern;
    }

    public enableRueckforderungDefinitivAbsenden(rueckforderungFormular: TSRueckforderungFormular): boolean {
        return this.isInstitutionStufe2Definitiv(rueckforderungFormular)
            && rueckforderungFormular.isKurzarbeitProzessBeendet()
            && rueckforderungFormular.isCoronaErwerbsersatzProzessBeendet();
    }

    public isInstitutionStufe1(rueckforderungFormular: TSRueckforderungFormular): boolean {
        if (rueckforderungFormular.status === TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_1
            && this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
            return true;
        }
        return false;
    }

    public isInstitutionStufe2(rueckforderungFormular: TSRueckforderungFormular): boolean {
        if (rueckforderungFormular.status === TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_2
            && this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
            return true;
        }
        return false;
    }

    public isInstitutionStufe2Definitiv(rueckforderungFormular: TSRueckforderungFormular): boolean {
        if (rueckforderungFormular.status === TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_2_DEFINITIV
            && this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
            return true;
        }
        return false;
    }

    public isPruefungKantonStufe1(rueckforderungFormular: TSRueckforderungFormular): boolean {
        if (rueckforderungFormular.status === TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_1
            && this.authServiceRS.isOneOfRoles(
                [TSRole.SUPER_ADMIN, TSRole.ADMIN_MANDANT, TSRole.SACHBEARBEITER_MANDANT])) {
            return true;
        }
        return false;
    }

    public isPruefungKantonStufe2Provisorisch(rueckforderungFormular: TSRueckforderungFormular): boolean {
        if (rueckforderungFormular.status === TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2_PROVISORISCH
            && this.authServiceRS.isOneOfRoles(
                [TSRole.SUPER_ADMIN, TSRole.ADMIN_MANDANT, TSRole.SACHBEARBEITER_MANDANT])) {
            return true;
        }
        return false;
    }

    public isPruefungKantonStufe2(rueckforderungFormular: TSRueckforderungFormular): boolean {
        if (rueckforderungFormular.status === TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2
            && this.authServiceRS.isOneOfRoles(
                [TSRole.SUPER_ADMIN, TSRole.ADMIN_MANDANT, TSRole.SACHBEARBEITER_MANDANT])) {
            return true;
        }
        return false;
    }

    public isGeprueftKantonStufe1(rueckforderungFormular: TSRueckforderungFormular): boolean {
        if (rueckforderungFormular.status === TSRueckforderungStatus.GEPRUEFT_STUFE_1
            && this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getAllRolesForNotrecht())) {
            return true;
        }
        return false;
    }

    public showConfirmationAfterInBearbeitungInstitutionStufe1(rueckforderungFormular: TSRueckforderungFormular): boolean {
        if (rueckforderungFormular.status === TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_1
            && this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
            return true;
        }
        return false;
    }

    public showConfirmationAfterInBearbeitungInstitutionStufe2(rueckforderungFormular: TSRueckforderungFormular): boolean {
        if ((rueckforderungFormular.status === TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2
            || rueckforderungFormular.status === TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2_PROVISORISCH)
            && this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
            return true;
        }
        return false;
    }

    public getTextConfirmationAfterInBearbeitungInstitutionStufe2(rueckforderungFormular: TSRueckforderungFormular): string {
        switch (rueckforderungFormular.institutionTyp) {
            case TSRueckforderungInstitutionTyp.OEFFENTLICH:
                return this.translate.instant('CONFIRMATON_AFTER_IN_BEARBEITUNG_INSTITUTION_STUFE_2_OEFFENTLICH');
            case TSRueckforderungInstitutionTyp.PRIVAT:
                if (rueckforderungFormular.isKurzarbeitProzessBeendet() && rueckforderungFormular.isCoronaErwerbsersatzProzessBeendet()) {
                    if (EbeguUtil.isNotNullAndTrue(rueckforderungFormular.hasBeenSentBackToInstitution)) {
                        return this.translate.instant('CONFIRMATON_AFTER_IN_BEARBEITUNG_INSTITUTION_STUFE_2_DEFINITIV');
                    }
                    return this.translate.instant('CONFIRMATON_AFTER_IN_BEARBEITUNG_INSTITUTION_STUFE_2_PRIVAT_VOLLSTAENDIG');
                }
                return this.translate.instant('CONFIRMATON_AFTER_IN_BEARBEITUNG_INSTITUTION_STUFE_2_PRIVAT_UNVOLLSTAENDIG');
            default:
                return '';
        }
    }

    public translateStatus(status: string): string {
        return this.translate.instant(`RUECKFORDERUNG_STATUS_${status}`);
    }

    public isKitaAngebot(rueckforderungFormular: TSRueckforderungFormular): boolean {
        return rueckforderungFormular.institutionStammdaten.betreuungsangebotTyp === TSBetreuungsangebotTyp.KITA;
    }

    public calculateInstiProvBetrag(rueckforderungFormular: TSRueckforderungFormular, isStufe1: boolean): void {
        this.stufe1ProvBetrag = undefined;
        this.stufe2ProvBetrag = undefined;
        const kostenuebernahmeBetreuung = isStufe1 ? rueckforderungFormular.stufe1InstitutionKostenuebernahmeBetreuung
            : rueckforderungFormular.stufe2InstitutionKostenuebernahmeBetreuung;
        if (EbeguUtil.isNullOrUndefined(kostenuebernahmeBetreuung)) {
            return;
        }
        const kostenuebernahmeAnzahlTage = isStufe1 ? rueckforderungFormular.stufe1InstitutionKostenuebernahmeAnzahlTage
            : rueckforderungFormular.stufe2InstitutionKostenuebernahmeAnzahlTage;

        if (this.isKitaAngebot(rueckforderungFormular)
            && EbeguUtil.isNotNullOrUndefined(kostenuebernahmeAnzahlTage)) {
            if (isStufe1) {
                this.stufe1ProvBetrag = kostenuebernahmeAnzahlTage + kostenuebernahmeBetreuung;
                return;
            }
            this.stufe2ProvBetrag = kostenuebernahmeAnzahlTage + kostenuebernahmeBetreuung;
            return;
        }
        const kostenuebernahmeAnzahlStunden = isStufe1 ? rueckforderungFormular.stufe1InstitutionKostenuebernahmeAnzahlStunden
            : rueckforderungFormular.stufe2InstitutionKostenuebernahmeAnzahlStunden;
        if (EbeguUtil.isNullOrUndefined(kostenuebernahmeAnzahlStunden)) {
            return;
        }
        if (isStufe1) {
            this.stufe1ProvBetrag = kostenuebernahmeAnzahlStunden + kostenuebernahmeBetreuung;
            return;
        }
        this.stufe2ProvBetrag = kostenuebernahmeAnzahlStunden + kostenuebernahmeBetreuung;
        return;
    }

    public calculateKantonProvBetrag(rueckforderungFormular: TSRueckforderungFormular, isStufe1: boolean): void {
        this.stufe1ProvBetrag = undefined;
        this.stufe2ProvBetrag = undefined;
        const kostenuebernahmeBetreuung = isStufe1 ? rueckforderungFormular.stufe1KantonKostenuebernahmeBetreuung
            : rueckforderungFormular.stufe2KantonKostenuebernahmeBetreuung;
        if (EbeguUtil.isNullOrUndefined(kostenuebernahmeBetreuung)) {
            return;
        }
        const kostenuebernahmeAnzahlTage = isStufe1 ? rueckforderungFormular.stufe1KantonKostenuebernahmeAnzahlTage
            : rueckforderungFormular.stufe2KantonKostenuebernahmeAnzahlTage;

        if (this.isKitaAngebot(rueckforderungFormular)
            && EbeguUtil.isNotNullOrUndefined(kostenuebernahmeAnzahlTage)) {
            if (isStufe1) {
                this.stufe1ProvBetrag = kostenuebernahmeAnzahlTage + kostenuebernahmeBetreuung;
                return;
            }
            this.stufe2ProvBetrag = kostenuebernahmeAnzahlTage + kostenuebernahmeBetreuung;
            return;
        }
        const kostenuebernahmeAnzahlStunden = isStufe1
            ? rueckforderungFormular.stufe1KantonKostenuebernahmeAnzahlStunden
            : rueckforderungFormular.stufe2KantonKostenuebernahmeAnzahlStunden;
        if (EbeguUtil.isNullOrUndefined(kostenuebernahmeAnzahlStunden)) {
            return;
        }
        if (isStufe1) {
            this.stufe1ProvBetrag = kostenuebernahmeAnzahlStunden + kostenuebernahmeBetreuung;
            return;
        }
        this.stufe2ProvBetrag = kostenuebernahmeAnzahlStunden + kostenuebernahmeBetreuung;
        return;
    }

    public get stufe1ProvBetrag(): number {
        return this._stufe1ProvBetrag;
    }

    public set stufe1ProvBetrag(stufe1ProvBetrag: number) {
        this._stufe1ProvBetrag = stufe1ProvBetrag;
    }

    public get stufe2ProvBetrag(): number {
        return this._stufe2ProvBetrag;
    }

    public set stufe2ProvBetrag(stufe2ProvBetrag: number) {
        this._stufe2ProvBetrag = stufe2ProvBetrag;
    }

    public showVorlageOeffentlicheInstitutionen(rueckforderungFormular: TSRueckforderungFormular): boolean {
        // Wenn noch nicht ausgefuellt, werden beide angezeigt
        return EbeguUtil.isNullOrUndefined(rueckforderungFormular.institutionTyp)
            || rueckforderungFormular.institutionTyp === TSRueckforderungInstitutionTyp.OEFFENTLICH;
    }

    public downloadVorlageOeffentlicheInstitutionen(rueckforderungFormular: TSRueckforderungFormular): void {
        const win = this.downloadRS.prepareDownloadWindow();
        const language = this.i18nServiceRS.currentLanguage();
        const angebotTyp = rueckforderungFormular.institutionStammdaten.betreuungsangebotTyp;
        this.downloadRS.getAccessTokenNotrechtvorlageOeffentlicheInstitutionen(language, angebotTyp)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, true, win);
            })
            .catch(() => {
                win.close();
            });
    }

    public showVorlagePrivateInstitutionen(rueckforderungFormular: TSRueckforderungFormular): boolean {
        // Wenn noch nicht ausgefuellt, werden beide angezeigt
        return EbeguUtil.isNullOrUndefined(rueckforderungFormular.institutionTyp)
            || rueckforderungFormular.institutionTyp === TSRueckforderungInstitutionTyp.PRIVAT;
    }

    public downloadVorlagePrivateInstitutionen(rueckforderungFormular: TSRueckforderungFormular): void {
        const win = this.downloadRS.prepareDownloadWindow();
        const language = this.i18nServiceRS.currentLanguage();
        const angebotTyp = rueckforderungFormular.institutionStammdaten.betreuungsangebotTyp;
        this.downloadRS.getAccessTokenNotrechtvorlagePrivateInstitutionen(language, angebotTyp)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, true, win);
            })
            .catch(() => {
                win.close();
            });
    }

    public get rueckforderungDokumentTyp(): typeof TSRueckforderungDokumentTyp {
        return TSRueckforderungDokumentTyp;
    }

    public uploadRuckforderungDokumente(event: any, rueckforderungFormularId: string,
                                        tsRueckforderungDokumentTyp: TSRueckforderungDokumentTyp): void {
        const files = event.target.files;
        const filesTooBig: any[] = [];
        const filesOk: any[] = [];
        for (const file of files) {
            if (file.size > MAX_FILE_SIZE) {
                filesTooBig.push(file);
            } else {
                filesOk.push(file);
            }
        }
        if (filesTooBig.length > 0) {
            // DialogBox anzeigen für Files, welche zu gross sind!

            let fileListString = '<ul>';
            for (const file of filesTooBig) {
                fileListString += '<li>';
                fileListString += file.name;
                fileListString += '</li>';
            }
            fileListString += '</ul>';
            this.showFileTooBigDialog(fileListString);
            return;
        }
        if (filesOk.length <= 0) {
            return;
        }
        this.uploadRS.uploadRueckforderungsDokumente(filesOk, rueckforderungFormularId, tsRueckforderungDokumentTyp)
            .then(rueckforderungDokumente => {
                switch (tsRueckforderungDokumentTyp) {
                    case TSRueckforderungDokumentTyp.ANGABEN_DOKUMENTE:
                        rueckforderungDokumente.forEach(dokument =>
                            this.rueckforderungAngabenDokumente.push(dokument));
                        this.rueckforderungAngabenDokumente = [].concat(this.rueckforderungAngabenDokumente);
                        this.cdr.markForCheck();
                        break;
                    case TSRueckforderungDokumentTyp.KOMMUNIKATION_DOKUMENTE:
                        rueckforderungDokumente.forEach(dokument =>
                            this.rueckforderungKommunikationDokumente.push(dokument));
                        this.rueckforderungKommunikationDokumente =
                            [].concat(this.rueckforderungKommunikationDokumente);
                        this.cdr.markForCheck();
                        break;
                    case TSRueckforderungDokumentTyp.EINSATZPLAENE_DOKUMENTE:
                        rueckforderungDokumente.forEach(dokument =>
                            this.rueckforderungEinsatzplaeneDokumente.push(dokument));
                        this.rueckforderungEinsatzplaeneDokumente =
                            [].concat(this.rueckforderungEinsatzplaeneDokumente);
                        this.cdr.markForCheck();
                        break;
                    case TSRueckforderungDokumentTyp.KURZARBEIT_DOKUMENTE:
                        rueckforderungDokumente.forEach(dokument =>
                            this.rueckforderungKurzarbeitDokumente.push(dokument));
                        this.rueckforderungKurzarbeitDokumente =
                            [].concat(this.rueckforderungKurzarbeitDokumente);
                        this.cdr.markForCheck();
                        break;
                    case TSRueckforderungDokumentTyp.ERWERBSERSATZ_DOKUMENTE:
                        rueckforderungDokumente.forEach(dokument =>
                            this.rueckforderungErwerbsersatzDokumente.push(dokument));
                        this.rueckforderungErwerbsersatzDokumente =
                            [].concat(this.rueckforderungErwerbsersatzDokumente);
                        this.cdr.markForCheck();
                        break;
                    default:
                        return;
                }
            });
    }

    private showFileTooBigDialog(text: string): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: this.translate.instant('FILE_ZU_GROSS'),
            text: `${text}`
        };
        this.dialog
            .open(DvNgOkDialogComponent, dialogConfig);
    }

    public initDokumente(rueckforderungFormular: TSRueckforderungFormular): void {
        this.notrechtRS.getRueckforderungDokumente(
            rueckforderungFormular.id)
            .then(rueckforderungDokumente => {
                this.rueckforderungAngabenDokumente = rueckforderungDokumente.filter(
                    dokument =>
                        dokument.rueckforderungDokumentTyp === TSRueckforderungDokumentTyp.ANGABEN_DOKUMENTE);
                this.rueckforderungAngabenDokumente = [].concat(this.rueckforderungAngabenDokumente);

                this.rueckforderungKommunikationDokumente = rueckforderungDokumente.filter(
                    dokument =>
                        dokument.rueckforderungDokumentTyp === TSRueckforderungDokumentTyp.KOMMUNIKATION_DOKUMENTE);
                this.rueckforderungKommunikationDokumente = [].concat(this.rueckforderungKommunikationDokumente);

                this.rueckforderungEinsatzplaeneDokumente = rueckforderungDokumente.filter(
                    dokument =>
                        dokument.rueckforderungDokumentTyp === TSRueckforderungDokumentTyp.EINSATZPLAENE_DOKUMENTE);
                this.rueckforderungEinsatzplaeneDokumente = [].concat(this.rueckforderungEinsatzplaeneDokumente);

                this.rueckforderungKurzarbeitDokumente = rueckforderungDokumente.filter(
                    dokument =>
                        dokument.rueckforderungDokumentTyp === TSRueckforderungDokumentTyp.KURZARBEIT_DOKUMENTE);
                this.rueckforderungKurzarbeitDokumente = [].concat(this.rueckforderungKurzarbeitDokumente);

                this.rueckforderungErwerbsersatzDokumente = rueckforderungDokumente.filter(
                    dokument =>
                        dokument.rueckforderungDokumentTyp === TSRueckforderungDokumentTyp.ERWERBSERSATZ_DOKUMENTE);
                this.rueckforderungErwerbsersatzDokumente = [].concat(this.rueckforderungErwerbsersatzDokumente);
                this.cdr.markForCheck();
            });
    }

    public delete(dokument: TSRueckforderungDokument): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: this.translate.instant('LOESCHEN_DIALOG_TITLE'),
            text: ''
        };
        this.dialog.open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed()
            .subscribe(
                userAccepted => {
                    if (!userAccepted) {
                        return;
                    }
                    this.notrechtRS.deleteRueckforderungDokument(dokument.id).then(() => {
                        switch (dokument.rueckforderungDokumentTyp) {
                            case TSRueckforderungDokumentTyp.ANGABEN_DOKUMENTE:
                                this.removeFromList(dokument, this.rueckforderungAngabenDokumente);
                                this.rueckforderungAngabenDokumente = [].concat(this.rueckforderungAngabenDokumente);
                                this.cdr.markForCheck();
                                break;
                            case TSRueckforderungDokumentTyp.KOMMUNIKATION_DOKUMENTE:
                                this.removeFromList(dokument, this.rueckforderungKommunikationDokumente);
                                this.rueckforderungKommunikationDokumente =
                                    [].concat(this.rueckforderungKommunikationDokumente);
                                this.cdr.markForCheck();
                                break;
                            case TSRueckforderungDokumentTyp.EINSATZPLAENE_DOKUMENTE:
                                this.removeFromList(dokument, this.rueckforderungEinsatzplaeneDokumente);
                                this.rueckforderungEinsatzplaeneDokumente =
                                    [].concat(this.rueckforderungEinsatzplaeneDokumente);
                                this.cdr.markForCheck();
                                break;
                            case TSRueckforderungDokumentTyp.KURZARBEIT_DOKUMENTE:
                                this.removeFromList(dokument, this.rueckforderungKurzarbeitDokumente);
                                this.rueckforderungKurzarbeitDokumente =
                                    [].concat(this.rueckforderungKurzarbeitDokumente);
                                this.cdr.markForCheck();
                                break;
                            case TSRueckforderungDokumentTyp.ERWERBSERSATZ_DOKUMENTE:
                                this.removeFromList(dokument, this.rueckforderungErwerbsersatzDokumente);
                                this.rueckforderungErwerbsersatzDokumente =
                                    [].concat(this.rueckforderungErwerbsersatzDokumente);
                                this.cdr.markForCheck();
                                break;
                            default:
                                return;
                        }

                    });
                },
                () => {
                }
            );
    }

    private removeFromList(dokument: TSRueckforderungDokument,
                           rueckforderungDokumente: TSRueckforderungDokument[]): void {
        const idx = EbeguUtil.getIndexOfElementwithID(dokument, rueckforderungDokumente);
        if (idx > -1) {
            rueckforderungDokumente.splice(idx, 1);
        }
    }

    public download(dokument: TSRueckforderungDokument, attachment: boolean): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getAccessTokenRueckforderungDokument(dokument.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, attachment, win);
            })
            .catch(() => {
                win.close();
            });
    }

    public showDokumentenUpload(): boolean {
        return true;
    }

    public getRueckforderungInstitutionTypOffentlich(): TSRueckforderungInstitutionTyp {
        return TSRueckforderungInstitutionTyp.OEFFENTLICH;
    }

    public getRueckforderungInstitutionTypPrivat(): TSRueckforderungInstitutionTyp {
        return TSRueckforderungInstitutionTyp.PRIVAT;
    }

    private validateDokumente(rueckforderungFormular: TSRueckforderungFormular): boolean {
        let valid = true;
        if (this.rueckforderungAngabenDokumente.length === 0) {
            this.showMessageFehlendeDokumenteAngaben = true;
            valid = false;
        }
        if (this.rueckforderungEinsatzplaeneDokumente.length === 0) {
            this.showMessageFehlendeDokumenteEinsatzplaene = true;
            valid = false;
        }
        if (this.rueckforderungKommunikationDokumente.length === 0) {
            this.showMessageFehlendeDokumenteKommunikation = true;
            valid = false;
        }
        if (rueckforderungFormular.institutionTyp !== this.getRueckforderungInstitutionTypOffentlich()) {
            if (rueckforderungFormular.coronaErwerbsersatzDefinitivVerfuegt && this.rueckforderungErwerbsersatzDokumente.length === 0) {
                this.showMessageFehlendeDokumenteErwerbsersatz = true;
                valid = false;
            }
            if (rueckforderungFormular.kurzarbeitDefinitivVerfuegt && this.rueckforderungKurzarbeitDokumente.length === 0) {
                this.showMessageFehlendeDokumenteKurzarbeit = true;
                valid = false;
            }
        }
        return valid;
    }
}
