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

import {
    AfterViewChecked,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnInit,
    ViewChild
} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {Transition} from '@uirouter/core';
import * as moment from 'moment';
import {from, Observable} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSBetreuungsangebotTyp} from '../../../models/enums/betreuung/TSBetreuungsangebotTyp';
import {TSRole} from '../../../models/enums/TSRole';
import {TSRueckforderungDokumentTyp} from '../../../models/enums/TSRueckforderungDokumentTyp';
import {TSRueckforderungInstitutionTyp} from '../../../models/enums/TSRueckforderungInstitutionTyp';
import {
    isAnyOfVerfuegtOrPruefungKantonStufe2,
    isBereitZumVerfuegenOderVerfuegt,
    isNeuOrEingeladenStatus,
    isStatusRelevantForFrist,
    TSRueckforderungStatus
} from '../../../models/enums/TSRueckforderungStatus';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {TSRueckforderungDokument} from '../../../models/TSRueckforderungDokument';
import {TSRueckforderungFormular} from '../../../models/TSRueckforderungFormular';
import {TSRueckforderungZahlung} from '../../../models/TSRueckforderungZahlung';
import {DateUtil} from '../../../utils/DateUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvNgOkDialogComponent} from '../../core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';
import {DvNgRemoveDialogComponent} from '../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {MAX_FILE_SIZE} from '../../core/constants/CONSTANTS';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {NotrechtRS} from '../../core/service/notrechtRS.rest';
import {UploadRS} from '../../core/service/uploadRS.rest';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {RueckforderungVerlaengerungDialogComponent} from './rueckforderung-verlaengerung-dialog/rueckforderung-verlaengerung-dialog.component';

@Component({
    selector: 'dv-rueckforderung-formular',
    templateUrl: './rueckforderung-formular.component.html',
    styleUrls: ['./rueckforderung-formular.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class RueckforderungFormularComponent
    implements OnInit, AfterViewChecked
{
    public get rueckforderungZahlungenList(): TSRueckforderungZahlung[] {
        return this._rueckforderungZahlungenList;
    }

    @ViewChild(NgForm, {static: false}) private readonly form: NgForm;
    @ViewChild(NgForm, {static: false}) private readonly beschwerdeForm: NgForm;

    private einreicheFristPrivatDefault: moment.Moment;
    private einreicheFristOeffentlich: moment.Moment;

    public rueckforderungFormular$: Observable<TSRueckforderungFormular>;

    public readOnly: boolean;
    public readOnlyDocument: boolean;
    public showBeschwerde: boolean;
    public beschwerdeReadOnly: boolean = true;
    public beschwerdeAlreadyExist: boolean;

    // Checkbox for Institution Stufe 1:
    public betreuungKorrektAusgewiesen: boolean;
    public gutscheinPlaetzenReduziert: boolean;
    public erstattungGemaessKanton: boolean;
    public mahlzeitenBGSubventionenGebuehrensystem: boolean;
    public belegeEinreichenBetrageKantonZurueckfordern: boolean;
    // Checkbox for Institution Stufe 2:
    public elternbeitraegeNichtInRechnung: boolean;
    public verpflegungskostenKorrektAbgezogen: boolean;
    public notwendigenInformationenLiefern: boolean;

    public showMessageFehlendeDokumenteAngaben: boolean = false;
    public showMessageFehlendeDokumenteKommunikation: boolean = false;
    public showMessageFehlendeDokumenteEinsatzplaene: boolean = false;
    public showMessageFehlendeDokumenteKurzarbeit: boolean = false;
    public showMessageFehlendeDokumenteErwerbsersatz: boolean = false;
    public showMessageFehlendeVerfuegungBetrag: boolean = false;
    public showMessageFehlendeBemerkungen: boolean = false;
    public showMessageFehlendeDokumenteBeschwerde: boolean = false;

    private _rueckforderungZahlungenList: TSRueckforderungZahlung[];
    private _provisorischerBetrag: number;

    public rueckforderungAngabenDokumente?: TSRueckforderungDokument[];
    public rueckforderungKommunikationDokumente?: TSRueckforderungDokument[];
    public rueckforderungEinsatzplaeneDokumente?: TSRueckforderungDokument[];
    public rueckforderungKurzarbeitDokumente?: TSRueckforderungDokument[];
    public rueckforderungErwerbsersatzDokumente?: TSRueckforderungDokument[];
    public rueckforderungBeschwerdeDokumente?: TSRueckforderungDokument[];

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
        private readonly applicationPropertyService: ApplicationPropertyRS
    ) {}

    public ngAfterViewChecked(): void {
        const anchor: string = this.$transition$.params().anchor;

        if (EbeguUtil.isNullOrUndefined(anchor)) {
            return;
        }

        const el = document.getElementById(anchor);
        if (el) {
            el.scrollIntoView();
            el.setAttribute('id', 'scrolled');
        }
    }

    public ngOnInit(): void {
        const rueckforederungFormId: string =
            this.$transition$.params().rueckforderungId;
        this.applicationPropertyService
            .getNotverordnungDefaultEinreichefristPrivat()
            .then(res => {
                this.einreicheFristPrivatDefault =
                    DateUtil.localDateToMoment(res).endOf('day');
            });
        this.applicationPropertyService
            .getNotverordnungDefaultEinreichefristOeffentlich()
            .then(res => {
                this.einreicheFristOeffentlich =
                    DateUtil.localDateToMoment(res).endOf('day');
            });

        if (!rueckforederungFormId) {
            return;
        }
        this.rueckforderungFormular$ = from(
            this.notrechtRS
                .findRueckforderungFormular(rueckforederungFormId)
                .then((response: TSRueckforderungFormular) => {
                    this.readOnly = this.initReadOnly(response);
                    this.readOnlyDocument = this.initReadOnlyDocument(response);
                    this.initRueckforderungZahlungen(response);
                    this.initDokumente(response);
                    this.calculateProvBetrag(response);
                    this.showBeschwerde = this.initBeschwerde(response);
                    this.beschwerdeAlreadyExist = this.showBeschwerde;
                    return response;
                })
        );
    }

    public saveRueckforderungFormular(
        rueckforderungFormular: TSRueckforderungFormular
    ): void {
        this.showMessageFehlendeDokumenteAngaben = false;
        this.showMessageFehlendeDokumenteKommunikation = false;
        this.showMessageFehlendeDokumenteEinsatzplaene = false;
        this.showMessageFehlendeDokumenteKurzarbeit = false;
        this.showMessageFehlendeDokumenteErwerbsersatz = false;
        this.showMessageFehlendeVerfuegungBetrag = false;
        this.showMessageFehlendeBemerkungen = false;
        if (!this.form.valid) {
            EbeguUtil.selectFirstInvalid();
            return;
        }
        if (isNeuOrEingeladenStatus(rueckforderungFormular.status)) {
            return;
        }
        if (
            this.isInstitutionStufe2(rueckforderungFormular) &&
            !this.validateDokumente(rueckforderungFormular)
        ) {
            return;
        }
        if (
            rueckforderungFormular.status ===
                TSRueckforderungStatus.VERFUEGT_PROVISORISCH ||
            rueckforderungFormular.status ===
                TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2
        ) {
            if (
                EbeguUtil.isNullOrUndefined(
                    rueckforderungFormular.stufe2VerfuegungBetrag
                )
            ) {
                this.showMessageFehlendeVerfuegungBetrag = true;
                document
                    .getElementById('gewaehrte_ausfallentschaedigung_stufe2_id')
                    .focus();
                return;
            }
            if (
                rueckforderungFormular.stufe2VerfuegungBetrag !==
                    this.provisorischerBetrag &&
                EbeguUtil.isNullOrUndefined(
                    rueckforderungFormular.bemerkungFuerVerfuegung
                )
            ) {
                this.showMessageFehlendeBemerkungen = true;
                document.getElementById('bemerkungZurVerfuegung_id').focus();
                return;
            }
        }

        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: 'RUECKFORDERUNGFORMULAR_CONFIRMATION_TITLE',
            text: ''
        };
        this.dialog
            .open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed()
            .subscribe(
                answer => {
                    if (answer !== true) {
                        return;
                    }
                    this.doSave(rueckforderungFormular, true);
                    this.changeDetectorRef.markForCheck();
                },
                () => {}
            );
    }

    private doSave(
        rueckforderungFormular: TSRueckforderungFormular,
        doSaveStatusChange: boolean
    ): void {
        // Den Status sollte sicherheitshalber im Backend geprueft und gesetzt werden
        this.rueckforderungFormular$ = from(
            this.notrechtRS
                .saveRueckforderungFormular(
                    rueckforderungFormular,
                    doSaveStatusChange
                )
                .then((response: TSRueckforderungFormular) => {
                    this.readOnly = this.initReadOnly(response);
                    this.readOnlyDocument = this.initReadOnlyDocument(response);
                    this.initRueckforderungZahlungen(response);
                    return response;
                })
        );
    }

    public rueckforderungAbschliessen(
        rueckforderungFormular: TSRueckforderungFormular
    ): void {
        if (
            rueckforderungFormular.status ===
                TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_1 &&
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
            )
        ) {
            rueckforderungFormular.status =
                TSRueckforderungStatus.ABGESCHLOSSEN_OHNE_GESUCH;
        } else {
            // ERROR transition not accepted
            return;
        }

        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: 'RUECKFORDERUNGFORMULAR_CONFIRMATION_TITLE',
            text: ''
        };
        this.dialog
            .open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed()
            .subscribe(
                answer => {
                    if (answer !== true) {
                        rueckforderungFormular.status =
                            TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_1;
                        this.changeDetectorRef.markForCheck();
                        return;
                    }
                    this.doSave(rueckforderungFormular, true);
                },
                () => {}
            );
    }

    public initRueckforderungZahlungen(
        rueckfordeungFormular: TSRueckforderungFormular
    ): void {
        this._rueckforderungZahlungenList = [];
        if (
            EbeguUtil.isNotNullOrUndefined(
                rueckfordeungFormular.stufe1FreigabeBetrag
            )
        ) {
            const rueckforderungZahlungStufe1 = new TSRueckforderungZahlung();
            rueckforderungZahlungStufe1.betrag =
                rueckfordeungFormular.stufe1FreigabeBetrag;
            rueckforderungZahlungStufe1.datumErstellt =
                rueckfordeungFormular.stufe1FreigabeDatum;
            rueckforderungZahlungStufe1.stufe =
                'RUECKFORDERUNG_ZAHLUNGEN_STUFE_1';
            rueckforderungZahlungStufe1.ausgeloest =
                EbeguUtil.isNotNullOrUndefined(
                    rueckfordeungFormular.stufe1FreigabeAusbezahltAm
                );
            this.rueckforderungZahlungenList.push(rueckforderungZahlungStufe1);
        }
        if (
            EbeguUtil.isNullOrUndefined(
                rueckfordeungFormular.stufe2VerfuegungDatum
            )
        ) {
            return;
        }
        const rueckforderungZahlungStufe2 = new TSRueckforderungZahlung();
        rueckforderungZahlungStufe2.betrag =
            rueckfordeungFormular.stufe2VerfuegungBetrag;
        rueckforderungZahlungStufe2.datumErstellt =
            rueckfordeungFormular.stufe2VerfuegungDatum;
        rueckforderungZahlungStufe2.stufe = 'RUECKFORDERUNG_ZAHLUNGEN_STUFE_2';
        rueckforderungZahlungStufe2.ausgeloest = EbeguUtil.isNotNullOrUndefined(
            rueckfordeungFormular.stufe2VerfuegungAusbezahltAm
        );
        this.rueckforderungZahlungenList.push(rueckforderungZahlungStufe2);
        if (
            EbeguUtil.isNullOrUndefined(rueckfordeungFormular.beschwerdeBetrag)
        ) {
            return;
        }
        const beschwerdeZahlung = new TSRueckforderungZahlung();
        beschwerdeZahlung.betrag = rueckfordeungFormular.beschwerdeBetrag;
        beschwerdeZahlung.stufe = 'RUECKFORDERUNG_ZAHLUNGEN_BESCHWERDE';
        beschwerdeZahlung.ausgeloest = EbeguUtil.isNotNullOrUndefined(
            rueckfordeungFormular.beschwerdeAusbezahltAm
        );
        this.rueckforderungZahlungenList.push(beschwerdeZahlung);
    }

    public enableRueckforderungAbsenden(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        if (this.isInstitutionStufe1(rueckforderungFormular)) {
            return (
                this.betreuungKorrektAusgewiesen &&
                this.gutscheinPlaetzenReduziert &&
                this.erstattungGemaessKanton &&
                this.mahlzeitenBGSubventionenGebuehrensystem &&
                this.belegeEinreichenBetrageKantonZurueckfordern
            );
        }
        if (
            rueckforderungFormular.institutionTyp ===
            this.getRueckforderungInstitutionTypPrivat()
        ) {
            return (
                this.elternbeitraegeNichtInRechnung &&
                this.notwendigenInformationenLiefern &&
                this.verpflegungskostenKorrektAbgezogen
            );
        }
        return (
            this.elternbeitraegeNichtInRechnung &&
            this.notwendigenInformationenLiefern
        );
    }

    public isInstitutionStufe1(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        if (
            rueckforderungFormular.status ===
                TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_1 &&
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
            )
        ) {
            return true;
        }
        return false;
    }

    public isInstitutionStufe2(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        if (
            rueckforderungFormular.status ===
                TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_2 &&
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
            )
        ) {
            return true;
        }
        return false;
    }

    public isPruefungKantonStufe1(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        if (
            rueckforderungFormular.status ===
                TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_1 &&
            this.authServiceRS.isOneOfRoles([
                TSRole.SUPER_ADMIN,
                TSRole.ADMIN_MANDANT,
                TSRole.SACHBEARBEITER_MANDANT
            ])
        ) {
            return true;
        }
        return false;
    }

    public isPruefungKantonStufe2(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        if (
            rueckforderungFormular.status ===
                TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2 &&
            this.authServiceRS.isOneOfRoles([
                TSRole.SUPER_ADMIN,
                TSRole.ADMIN_MANDANT,
                TSRole.SACHBEARBEITER_MANDANT
            ])
        ) {
            return true;
        }
        return false;
    }

    public isProvVerfuegenPossible(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        return (
            this.isPruefungKantonStufe2(rueckforderungFormular) &&
            rueckforderungFormular.institutionTyp ===
                TSRueckforderungInstitutionTyp.PRIVAT
        );
    }

    public isProvisorischVerfuegtStufe2(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        if (
            rueckforderungFormular.status ===
                TSRueckforderungStatus.VERFUEGT_PROVISORISCH &&
            this.authServiceRS.isOneOfRoles([
                TSRole.SUPER_ADMIN,
                TSRole.ADMIN_MANDANT,
                TSRole.SACHBEARBEITER_MANDANT
            ])
        ) {
            return true;
        }
        return false;
    }

    public isGeprueftKantonStufe1(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        if (
            rueckforderungFormular.status ===
                TSRueckforderungStatus.GEPRUEFT_STUFE_1 &&
            this.authServiceRS.isOneOfRoles(TSRoleUtil.getAllRolesForNotrecht())
        ) {
            return true;
        }
        return false;
    }

    public isKantonBenutzer(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }

    public getTextConfirmationAfterInBearbeitungInstitutionStufe2(
        rueckforderungFormular: TSRueckforderungFormular
    ): string {
        switch (rueckforderungFormular.institutionTyp) {
            case TSRueckforderungInstitutionTyp.OEFFENTLICH:
                return this.translate.instant(
                    'CONFIRMATON_AFTER_IN_BEARBEITUNG_INSTITUTION_STUFE_2_OEFFENTLICH'
                );
            case TSRueckforderungInstitutionTyp.PRIVAT:
                if (
                    rueckforderungFormular.isKurzarbeitProzessBeendet() &&
                    rueckforderungFormular.isCoronaErwerbsersatzProzessBeendet()
                ) {
                    return this.translate.instant(
                        'CONFIRMATON_AFTER_IN_BEARBEITUNG_INSTITUTION_STUFE_2_PRIVAT_VOLLSTAENDIG'
                    );
                }
                return this.translate.instant(
                    'CONFIRMATON_AFTER_IN_BEARBEITUNG_INSTITUTION_STUFE_2_PRIVAT_UNVOLLSTAENDIG'
                );
            default:
                return '';
        }
    }

    public translateStatus(status: string): string {
        return this.translate.instant(`RUECKFORDERUNG_STATUS_${status}`);
    }

    public isKitaAngebot(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        return (
            rueckforderungFormular.institutionStammdaten
                .betreuungsangebotTyp === TSBetreuungsangebotTyp.KITA
        );
    }

    public get provisorischerBetrag(): number {
        return this._provisorischerBetrag;
    }

    public set provisorischerBetrag(value: number) {
        this._provisorischerBetrag = value;
    }

    public calculateProvBetrag(
        rueckforderungFormular: TSRueckforderungFormular
    ): void {
        this.cleanInputData(rueckforderungFormular);
        this._provisorischerBetrag =
            rueckforderungFormular.calculateProvisorischerBetrag();
    }

    private cleanInputData(
        rueckforderungFormular: TSRueckforderungFormular
    ): void {
        if (
            EbeguUtil.isNullOrFalse(rueckforderungFormular.kurzarbeitBeantragt)
        ) {
            rueckforderungFormular.kurzarbeitBetrag = null;
            rueckforderungFormular.kurzarbeitDefinitivVerfuegt = null;
        }
        if (
            EbeguUtil.isNullOrFalse(
                rueckforderungFormular.coronaErwerbsersatzBeantragt
            )
        ) {
            rueckforderungFormular.coronaErwerbsersatzBetrag = null;
            rueckforderungFormular.coronaErwerbsersatzDefinitivVerfuegt = null;
        }
    }

    public showVorlageOeffentlicheInstitutionen(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        // Wenn noch nicht ausgefuellt, werden beide angezeigt
        return (
            EbeguUtil.isNullOrUndefined(
                rueckforderungFormular.institutionTyp
            ) ||
            rueckforderungFormular.institutionTyp ===
                TSRueckforderungInstitutionTyp.OEFFENTLICH
        );
    }

    public downloadVorlageOeffentlicheInstitutionen(
        rueckforderungFormular: TSRueckforderungFormular
    ): void {
        const win = this.downloadRS.prepareDownloadWindow();
        const language = this.i18nServiceRS.currentLanguage();
        const angebotTyp =
            rueckforderungFormular.institutionStammdaten.betreuungsangebotTyp;
        this.downloadRS
            .getAccessTokenNotrechtvorlageOeffentlicheInstitutionen(
                language,
                angebotTyp
            )
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(
                    downloadFile.accessToken,
                    downloadFile.filename,
                    true,
                    win
                );
            })
            .catch(() => {
                win.close();
            });
    }

    public showVorlagePrivateInstitutionen(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        // Wenn noch nicht ausgefuellt, werden beide angezeigt
        return (
            EbeguUtil.isNullOrUndefined(
                rueckforderungFormular.institutionTyp
            ) ||
            rueckforderungFormular.institutionTyp ===
                TSRueckforderungInstitutionTyp.PRIVAT
        );
    }

    public downloadVorlagePrivateInstitutionen(
        rueckforderungFormular: TSRueckforderungFormular
    ): void {
        const win = this.downloadRS.prepareDownloadWindow();
        const language = this.i18nServiceRS.currentLanguage();
        const angebotTyp =
            rueckforderungFormular.institutionStammdaten.betreuungsangebotTyp;
        this.downloadRS
            .getAccessTokenNotrechtvorlagePrivateInstitutionen(
                language,
                angebotTyp
            )
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(
                    downloadFile.accessToken,
                    downloadFile.filename,
                    true,
                    win
                );
            })
            .catch(() => {
                win.close();
            });
    }

    public get rueckforderungDokumentTyp(): typeof TSRueckforderungDokumentTyp {
        return TSRueckforderungDokumentTyp;
    }

    public uploadRuckforderungDokumente(
        event: any,
        rueckforderungFormularId: string,
        tsRueckforderungDokumentTyp: TSRueckforderungDokumentTyp
    ): void {
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
        this.uploadRS
            .uploadRueckforderungsDokumente(
                filesOk,
                rueckforderungFormularId,
                tsRueckforderungDokumentTyp
            )
            .then(rueckforderungDokumente => {
                switch (tsRueckforderungDokumentTyp) {
                    case TSRueckforderungDokumentTyp.ANGABEN_DOKUMENTE:
                        rueckforderungDokumente.forEach(dokument =>
                            this.rueckforderungAngabenDokumente.push(dokument)
                        );
                        this.rueckforderungAngabenDokumente = [].concat(
                            this.rueckforderungAngabenDokumente
                        );
                        this.cdr.markForCheck();
                        break;
                    case TSRueckforderungDokumentTyp.KOMMUNIKATION_DOKUMENTE:
                        rueckforderungDokumente.forEach(dokument =>
                            this.rueckforderungKommunikationDokumente.push(
                                dokument
                            )
                        );
                        this.rueckforderungKommunikationDokumente = [].concat(
                            this.rueckforderungKommunikationDokumente
                        );
                        this.cdr.markForCheck();
                        break;
                    case TSRueckforderungDokumentTyp.EINSATZPLAENE_DOKUMENTE:
                        rueckforderungDokumente.forEach(dokument =>
                            this.rueckforderungEinsatzplaeneDokumente.push(
                                dokument
                            )
                        );
                        this.rueckforderungEinsatzplaeneDokumente = [].concat(
                            this.rueckforderungEinsatzplaeneDokumente
                        );
                        this.cdr.markForCheck();
                        break;
                    case TSRueckforderungDokumentTyp.KURZARBEIT_DOKUMENTE:
                        rueckforderungDokumente.forEach(dokument =>
                            this.rueckforderungKurzarbeitDokumente.push(
                                dokument
                            )
                        );
                        this.rueckforderungKurzarbeitDokumente = [].concat(
                            this.rueckforderungKurzarbeitDokumente
                        );
                        this.cdr.markForCheck();
                        break;
                    case TSRueckforderungDokumentTyp.ERWERBSERSATZ_DOKUMENTE:
                        rueckforderungDokumente.forEach(dokument =>
                            this.rueckforderungErwerbsersatzDokumente.push(
                                dokument
                            )
                        );
                        this.rueckforderungErwerbsersatzDokumente = [].concat(
                            this.rueckforderungErwerbsersatzDokumente
                        );
                        this.cdr.markForCheck();
                        break;
                    case TSRueckforderungDokumentTyp.BESCHWERDE_DOKUMENTE:
                        rueckforderungDokumente.forEach(dokument =>
                            this.rueckforderungBeschwerdeDokumente.push(
                                dokument
                            )
                        );
                        this.rueckforderungBeschwerdeDokumente = [].concat(
                            this.rueckforderungBeschwerdeDokumente
                        );
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
        this.dialog.open(DvNgOkDialogComponent, dialogConfig);
    }

    public initDokumente(
        rueckforderungFormular: TSRueckforderungFormular
    ): void {
        this.notrechtRS
            .getRueckforderungDokumente(rueckforderungFormular.id)
            .then(rueckforderungDokumente => {
                this.rueckforderungAngabenDokumente =
                    rueckforderungDokumente.filter(
                        dokument =>
                            dokument.rueckforderungDokumentTyp ===
                            TSRueckforderungDokumentTyp.ANGABEN_DOKUMENTE
                    );
                this.rueckforderungAngabenDokumente = [].concat(
                    this.rueckforderungAngabenDokumente
                );

                this.rueckforderungKommunikationDokumente =
                    rueckforderungDokumente.filter(
                        dokument =>
                            dokument.rueckforderungDokumentTyp ===
                            TSRueckforderungDokumentTyp.KOMMUNIKATION_DOKUMENTE
                    );
                this.rueckforderungKommunikationDokumente = [].concat(
                    this.rueckforderungKommunikationDokumente
                );

                this.rueckforderungEinsatzplaeneDokumente =
                    rueckforderungDokumente.filter(
                        dokument =>
                            dokument.rueckforderungDokumentTyp ===
                            TSRueckforderungDokumentTyp.EINSATZPLAENE_DOKUMENTE
                    );
                this.rueckforderungEinsatzplaeneDokumente = [].concat(
                    this.rueckforderungEinsatzplaeneDokumente
                );

                this.rueckforderungKurzarbeitDokumente =
                    rueckforderungDokumente.filter(
                        dokument =>
                            dokument.rueckforderungDokumentTyp ===
                            TSRueckforderungDokumentTyp.KURZARBEIT_DOKUMENTE
                    );
                this.rueckforderungKurzarbeitDokumente = [].concat(
                    this.rueckforderungKurzarbeitDokumente
                );

                this.rueckforderungErwerbsersatzDokumente =
                    rueckforderungDokumente.filter(
                        dokument =>
                            dokument.rueckforderungDokumentTyp ===
                            TSRueckforderungDokumentTyp.ERWERBSERSATZ_DOKUMENTE
                    );
                this.rueckforderungErwerbsersatzDokumente = [].concat(
                    this.rueckforderungErwerbsersatzDokumente
                );

                this.rueckforderungBeschwerdeDokumente =
                    rueckforderungDokumente.filter(
                        dokument =>
                            dokument.rueckforderungDokumentTyp ===
                            TSRueckforderungDokumentTyp.BESCHWERDE_DOKUMENTE
                    );
                this.rueckforderungBeschwerdeDokumente = [].concat(
                    this.rueckforderungBeschwerdeDokumente
                );
                this.cdr.markForCheck();
            });
    }

    public delete(dokument: TSRueckforderungDokument): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: this.translate.instant('LOESCHEN_DIALOG_TITLE'),
            text: ''
        };
        this.dialog
            .open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed()
            .subscribe(
                userAccepted => {
                    if (!userAccepted) {
                        return;
                    }
                    this.notrechtRS
                        .deleteRueckforderungDokument(dokument.id)
                        .then(() => {
                            switch (dokument.rueckforderungDokumentTyp) {
                                case TSRueckforderungDokumentTyp.ANGABEN_DOKUMENTE:
                                    this.removeFromList(
                                        dokument,
                                        this.rueckforderungAngabenDokumente
                                    );
                                    this.rueckforderungAngabenDokumente =
                                        [].concat(
                                            this.rueckforderungAngabenDokumente
                                        );
                                    this.cdr.markForCheck();
                                    break;
                                case TSRueckforderungDokumentTyp.KOMMUNIKATION_DOKUMENTE:
                                    this.removeFromList(
                                        dokument,
                                        this
                                            .rueckforderungKommunikationDokumente
                                    );
                                    this.rueckforderungKommunikationDokumente =
                                        [].concat(
                                            this
                                                .rueckforderungKommunikationDokumente
                                        );
                                    this.cdr.markForCheck();
                                    break;
                                case TSRueckforderungDokumentTyp.EINSATZPLAENE_DOKUMENTE:
                                    this.removeFromList(
                                        dokument,
                                        this
                                            .rueckforderungEinsatzplaeneDokumente
                                    );
                                    this.rueckforderungEinsatzplaeneDokumente =
                                        [].concat(
                                            this
                                                .rueckforderungEinsatzplaeneDokumente
                                        );
                                    this.cdr.markForCheck();
                                    break;
                                case TSRueckforderungDokumentTyp.KURZARBEIT_DOKUMENTE:
                                    this.removeFromList(
                                        dokument,
                                        this.rueckforderungKurzarbeitDokumente
                                    );
                                    this.rueckforderungKurzarbeitDokumente =
                                        [].concat(
                                            this
                                                .rueckforderungKurzarbeitDokumente
                                        );
                                    this.cdr.markForCheck();
                                    break;
                                case TSRueckforderungDokumentTyp.ERWERBSERSATZ_DOKUMENTE:
                                    this.removeFromList(
                                        dokument,
                                        this
                                            .rueckforderungErwerbsersatzDokumente
                                    );
                                    this.rueckforderungErwerbsersatzDokumente =
                                        [].concat(
                                            this
                                                .rueckforderungErwerbsersatzDokumente
                                        );
                                    this.cdr.markForCheck();
                                    break;
                                case TSRueckforderungDokumentTyp.BESCHWERDE_DOKUMENTE:
                                    this.removeFromList(
                                        dokument,
                                        this.rueckforderungBeschwerdeDokumente
                                    );
                                    this.rueckforderungBeschwerdeDokumente =
                                        [].concat(
                                            this
                                                .rueckforderungBeschwerdeDokumente
                                        );
                                    this.cdr.markForCheck();
                                    break;
                                default:
                                    return;
                            }
                        });
                },
                () => {}
            );
    }

    private removeFromList(
        dokument: TSRueckforderungDokument,
        rueckforderungDokumente: TSRueckforderungDokument[]
    ): void {
        const idx = EbeguUtil.getIndexOfElementwithID(
            dokument,
            rueckforderungDokumente
        );
        if (idx > -1) {
            rueckforderungDokumente.splice(idx, 1);
        }
    }

    public download(
        dokument: TSRueckforderungDokument,
        attachment: boolean
    ): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.downloadRS
            .getAccessTokenRueckforderungDokument(dokument.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(
                    downloadFile.accessToken,
                    downloadFile.filename,
                    attachment,
                    win
                );
            })
            .catch(() => {
                win.close();
            });
    }

    public isStufe2(rueckforderungFormular: TSRueckforderungFormular): boolean {
        // Dokumente sollen erst ab Stufe zwei hochgeladen werden
        return (
            rueckforderungFormular.status ===
                TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_2 ||
            rueckforderungFormular.status ===
                TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2 ||
            rueckforderungFormular.status ===
                TSRueckforderungStatus.VERFUEGT_PROVISORISCH ||
            rueckforderungFormular.status ===
                TSRueckforderungStatus.BEREIT_ZUM_VERFUEGEN ||
            rueckforderungFormular.status === TSRueckforderungStatus.VERFUEGT
        );
    }

    public getRueckforderungInstitutionTypOffentlich(): TSRueckforderungInstitutionTyp {
        return TSRueckforderungInstitutionTyp.OEFFENTLICH;
    }

    public getRueckforderungInstitutionTypPrivat(): TSRueckforderungInstitutionTyp {
        return TSRueckforderungInstitutionTyp.PRIVAT;
    }

    public showFristAbgelaufen(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        if (isStatusRelevantForFrist(rueckforderungFormular.status)) {
            return this.fristSchonErreicht(rueckforderungFormular);
        }
        return false;
    }

    public fristSchonErreicht(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        const currentDate = moment();
        let fristabgelaufen = false;
        const frist =
            this.getRueckforderungInstitutionTypPrivat() ===
            rueckforderungFormular.institutionTyp
                ? this.einreicheFristPrivatDefault
                : this.einreicheFristOeffentlich;
        if (
            rueckforderungFormular.institutionTyp ===
                this.getRueckforderungInstitutionTypPrivat() ||
            (rueckforderungFormular.status ===
                TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_2 &&
                rueckforderungFormular.institutionTyp ===
                    this.getRueckforderungInstitutionTypOffentlich())
        ) {
            fristabgelaufen = EbeguUtil.isNotNullOrUndefined(
                rueckforderungFormular.extendedEinreichefrist
            )
                ? !currentDate.isBefore(
                      rueckforderungFormular.extendedEinreichefrist.endOf('day')
                  )
                : !currentDate.isBefore(frist);
        } else {
            fristabgelaufen = !currentDate.isBefore(
                this.einreicheFristOeffentlich
            );
        }
        return fristabgelaufen;
    }

    public fristVerlaengern(
        rueckforderungFormular: TSRueckforderungFormular
    ): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {rueckforderungFormular};
        this.rueckforderungFormular$ = from(
            this.dialog
                .open(RueckforderungVerlaengerungDialogComponent, dialogConfig)
                .afterClosed()
                .toPromise()
                .then((result: TSRueckforderungFormular) => {
                    if (EbeguUtil.isNotNullOrUndefined(result)) {
                        return result;
                    }
                    return rueckforderungFormular;
                })
        );
    }

    public getFristBis(
        rueckforderungFormular: TSRueckforderungFormular
    ): string {
        const fristVerlaengert = EbeguUtil.isNotNullOrUndefined(
            rueckforderungFormular.extendedEinreichefrist
        );
        const relevantFristPrivat = fristVerlaengert
            ? rueckforderungFormular.extendedEinreichefrist
            : this.einreicheFristPrivatDefault;
        const relevanteFristOeffentlich =
            fristVerlaengert &&
            rueckforderungFormular.status ===
                TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_2
                ? rueckforderungFormular.extendedEinreichefrist
                : this.einreicheFristOeffentlich;
        const privatRelevantText = DateUtil.momentToLocalDateFormat(
            relevantFristPrivat,
            'DD.MM.YYYY'
        );
        const oeffentlichText = DateUtil.momentToLocalDateFormat(
            relevanteFristOeffentlich,
            'DD.MM.YYYY'
        );
        if (
            EbeguUtil.isNullOrUndefined(rueckforderungFormular.institutionTyp)
        ) {
            // Wir wissen noch nicht, ob privat oder oeffentlich
            return this.translate.instant(
                'RUECKFORDERUNGSFORMULARE_INFO_FRIST_BEIDE',
                {
                    oeffentlich: oeffentlichText,
                    private: privatRelevantText
                }
            );
        }
        const isPrivat =
            rueckforderungFormular.institutionTyp ===
            TSRueckforderungInstitutionTyp.PRIVAT;
        const relevantText = isPrivat ? privatRelevantText : oeffentlichText;
        if (fristVerlaengert) {
            return this.translate.instant(
                'RUECKFORDERUNGSFORMULARE_INFO_FRIST_VERLAENGERT',
                {
                    frist: relevantText
                }
            );
        }
        return this.translate.instant(
            'RUECKFORDERUNGSFORMULARE_INFO_FRIST_STANDARD',
            {
                frist: relevantText
            }
        );
    }

    private validateDokumente(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
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
        if (
            rueckforderungFormular.institutionTyp !==
            this.getRueckforderungInstitutionTypOffentlich()
        ) {
            if (
                rueckforderungFormular.coronaErwerbsersatzDefinitivVerfuegt &&
                this.rueckforderungErwerbsersatzDokumente.length === 0
            ) {
                this.showMessageFehlendeDokumenteErwerbsersatz = true;
                valid = false;
            }
            if (
                rueckforderungFormular.kurzarbeitDefinitivVerfuegt &&
                this.rueckforderungKurzarbeitDokumente.length === 0
            ) {
                this.showMessageFehlendeDokumenteKurzarbeit = true;
                valid = false;
            }
        }
        return valid;
    }

    public getDokumenteKommunikationTitle(
        rueckforderungFormular: TSRueckforderungFormular
    ): string {
        if (
            rueckforderungFormular.institutionTyp ===
            TSRueckforderungInstitutionTyp.PRIVAT
        ) {
            return 'RUECKOFORDERUNG_DOKUMENTE_KOMMUNIKATION_PRIVAT';
        }
        return 'RUECKOFORDERUNG_DOKUMENTE_KOMMUNIKATION_OEFFENTLICH';
    }

    // ist nicht identisch => return von anderem string
    public getDokumenteEinsatzplaeneTitle(
        rueckforderungFormular: TSRueckforderungFormular
    ): string {
        if (
            rueckforderungFormular.institutionTyp ===
            TSRueckforderungInstitutionTyp.PRIVAT
        ) {
            return 'RUECKOFORDERUNG_DOKUMENTE_EINSATZPLAENE_PRIVAT';
        }
        return 'RUECKOFORDERUNG_DOKUMENTE_EINSATZPLAENE_OEFFENTLICH';
    }

    // ist nicht identisch => return von anderem string
    public getDokumenteAngabenTitle(
        rueckforderungFormular: TSRueckforderungFormular
    ): string {
        if (
            rueckforderungFormular.institutionTyp ===
            TSRueckforderungInstitutionTyp.PRIVAT
        ) {
            return 'RUECKOFORDERUNG_DOKUMENTE_ANGABEN_PRIVAT';
        }
        return 'RUECKOFORDERUNG_DOKUMENTE_ANGABEN_OEFFENTLICH';
    }

    private initReadOnly(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        switch (rueckforderungFormular.status) {
            case TSRueckforderungStatus.GEPRUEFT_STUFE_1:
            case TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2:
            case TSRueckforderungStatus.VERFUEGT_PROVISORISCH:
            case TSRueckforderungStatus.BEREIT_ZUM_VERFUEGEN:
            case TSRueckforderungStatus.VERFUEGT:
                return true;
            case TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_1:
            case TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_2:
                return this.authServiceRS.isOneOfRoles([
                    TSRole.SUPER_ADMIN,
                    TSRole.ADMIN_MANDANT,
                    TSRole.SACHBEARBEITER_MANDANT
                ]);
            case TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_1:
                return this.authServiceRS.isOneOfRoles(
                    TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
                );
            default:
                return false;
        }
    }

    private initReadOnlyDocument(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        // Alles ausser BEREIT_ZUM_VERRUEGEN und VERFUEGT
        if (isBereitZumVerfuegenOderVerfuegt(rueckforderungFormular.status)) {
            // falls schon Verfügt kann Mandant und Superadmin immer noch hochladen
            return !this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getMandantRoles()
            );
        }
        return false;
    }

    private initBeschwerde(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        if (
            EbeguUtil.isNotNullOrUndefined(
                rueckforderungFormular.beschwerdeBetrag
            )
        ) {
            return true;
        }
        return false;
    }

    public isInstitutionStufe1ReadOnly(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        if (
            rueckforderungFormular.status ===
                TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_1 &&
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
            )
        ) {
            return true;
        }
        return false;
    }

    public isInstitutionStufe2ReadOnly(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        if (
            (rueckforderungFormular.status ===
                TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2 ||
                rueckforderungFormular.status ===
                    TSRueckforderungStatus.VERFUEGT_PROVISORISCH ||
                rueckforderungFormular.status ===
                    TSRueckforderungStatus.BEREIT_ZUM_VERFUEGEN ||
                rueckforderungFormular.status ===
                    TSRueckforderungStatus.VERFUEGT) &&
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
            )
        ) {
            return true;
        }
        return false;
    }

    public isInstitutionStufe2ForKantonReadOnly(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        if (
            rueckforderungFormular.status ===
                TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_2 &&
            EbeguUtil.isNullOrUndefined(
                rueckforderungFormular.institutionTyp
            ) &&
            this.isKantonBenutzer()
        ) {
            return true;
        }
        return false;
    }

    public isKantonStufe2ReadOnly(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        if (
            (rueckforderungFormular.status ===
                TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_2 ||
                rueckforderungFormular.status ===
                    TSRueckforderungStatus.VERFUEGT_PROVISORISCH ||
                rueckforderungFormular.status ===
                    TSRueckforderungStatus.BEREIT_ZUM_VERFUEGEN ||
                rueckforderungFormular.status ===
                    TSRueckforderungStatus.VERFUEGT) &&
            EbeguUtil.isNotNullOrUndefined(
                rueckforderungFormular.institutionTyp
            ) &&
            this.isKantonBenutzer()
        ) {
            return true;
        }
        return false;
    }

    public resetStatus(rueckforderungFormular: TSRueckforderungFormular): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: 'RUECKFORDERUNGSFORMULAR_RESET_CONFIRMATION_TITLE',
            text: 'RUECKFORDERUNGSFORMULAR_RESET_CONFIRMATION_TEXT'
        };
        this.dialog
            .open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed()
            .subscribe(
                answer => {
                    if (answer !== true) {
                        return;
                    }
                    this.rueckforderungFormular$ = from(
                        this.notrechtRS
                            .resetStatus(rueckforderungFormular)
                            .then((response: TSRueckforderungFormular) => {
                                this.changeDetectorRef.markForCheck();
                                this.readOnly = this.initReadOnly(response);
                                this.readOnlyDocument =
                                    this.initReadOnlyDocument(response);
                                return response;
                            })
                    );
                },
                () => {}
            );
    }

    public formularZurueckholen(
        rueckforderungFormular: TSRueckforderungFormular
    ): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: 'RUECKFORDERUNGSFORMULAR_ZURUECKHOLEN_CONFIRMATION_TITLE',
            text: 'RUECKFORDERUNGSFORMULAR_ZURUECKHOLEN_CONFIRMATION_TEXT'
        };
        this.dialog
            .open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed()
            .subscribe(
                answer => {
                    if (answer !== true) {
                        return;
                    }
                    this.rueckforderungFormular$ = from(
                        this.notrechtRS
                            .formularZurueckholen(rueckforderungFormular)
                            .then((response: TSRueckforderungFormular) => {
                                this.readOnly = this.initReadOnly(response);
                                this.readOnlyDocument =
                                    this.initReadOnlyDocument(response);
                                this.changeDetectorRef.markForCheck();
                                return response;
                            })
                    );
                },
                () => {}
            );
    }

    public showButtonZurueckholen(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        const fristAbgelaufen = this.fristSchonErreicht(rueckforderungFormular);
        const roleMandant = this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getMandantOnlyRoles()
        );
        const statusInstitution2 =
            rueckforderungFormular.status ===
            TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_2;
        const datenErfasst = EbeguUtil.isNotNullOrUndefined(
            rueckforderungFormular.institutionTyp
        );
        return (
            fristAbgelaufen && roleMandant && statusInstitution2 && datenErfasst
        );
    }

    public getTextWarnungFormularKannNichtFreigegebenWerden(
        rueckforderungFormular: TSRueckforderungFormular
    ): string {
        // Wir zeigen dies grundsaetzlich nur bei Institutionszustaenden an
        if (this.isInstitutionStufe1(rueckforderungFormular)) {
            // (1) Die Frist ist abgelaufen
            if (this.fristSchonErreicht(rueckforderungFormular)) {
                return 'FREIGABE_DISABLED_FRIST_ABGELAUFEN';
            }
            // (2) Die Checkboxen wurden nicht bestaetigt
            if (!this.enableRueckforderungAbsenden(rueckforderungFormular)) {
                return 'FREIGABE_DISABLED_CHECKBOXEN_BESTAETIGEN';
            }
        }
        if (this.isInstitutionStufe2(rueckforderungFormular)) {
            // (1) Die Frist ist abgelaufen
            if (this.fristSchonErreicht(rueckforderungFormular)) {
                return 'FREIGABE_DISABLED_FRIST_ABGELAUFEN';
            }
            // (2) Die Checkboxen wurden nicht bestaetigt
            if (!this.enableRueckforderungAbsenden(rueckforderungFormular)) {
                return 'FREIGABE_DISABLED_CHECKBOXEN_BESTAETIGEN';
            }
        }
        return null;
    }

    public showWarnungFormularKannNichtFreigegebenWerden(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        return !EbeguUtil.isEmptyStringNullOrUndefined(
            this.getTextWarnungFormularKannNichtFreigegebenWerden(
                rueckforderungFormular
            )
        );
    }

    public showKantonStufe2Felder(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        if (
            (rueckforderungFormular.status ===
                TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2 ||
                rueckforderungFormular.status ===
                    TSRueckforderungStatus.VERFUEGT_PROVISORISCH ||
                rueckforderungFormular.status ===
                    TSRueckforderungStatus.VERFUEGT ||
                rueckforderungFormular.status ===
                    TSRueckforderungStatus.BEREIT_ZUM_VERFUEGEN) &&
            this.authServiceRS.isOneOfRoles([
                TSRole.SUPER_ADMIN,
                TSRole.ADMIN_MANDANT,
                TSRole.SACHBEARBEITER_MANDANT
            ])
        ) {
            return true;
        }
        return false;
    }

    public canEditKantonStufe2Felder(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        if (
            (rueckforderungFormular.status ===
                TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2 ||
                rueckforderungFormular.status ===
                    TSRueckforderungStatus.VERFUEGT_PROVISORISCH) &&
            this.authServiceRS.isOneOfRoles([
                TSRole.SUPER_ADMIN,
                TSRole.ADMIN_MANDANT,
                TSRole.SACHBEARBEITER_MANDANT
            ])
        ) {
            return true;
        }
        return false;
    }

    public provisorischVerfugen(
        rueckforderungFormular: TSRueckforderungFormular
    ): void {
        if (!this.isPruefungKantonStufe2(rueckforderungFormular)) {
            return;
        }
        this.rueckforderungFormular$ = from(
            this.notrechtRS
                .verfuegtProvisorischRueckforderungFormular(
                    rueckforderungFormular
                )
                .then((response: TSRueckforderungFormular) => {
                    this.initRueckforderungZahlungen(response);
                    this.readOnly = this.initReadOnly(response);
                    this.readOnlyDocument = this.initReadOnlyDocument(response);
                    return response;
                })
        );
    }

    public openProvisorischeVerfuegung(
        rueckforderungFormular: TSRueckforderungFormular
    ): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.downloadRS
            .getAccessTokenProvisoricheVerfuegungDokument(
                rueckforderungFormular.id
            )
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(
                    downloadFile.accessToken,
                    downloadFile.filename,
                    false,
                    win
                );
            })
            .catch(() => {
                win.close();
            });
    }

    public openDefinitiveVerfuegung(
        rueckforderungFormular: TSRueckforderungFormular
    ): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.downloadRS
            .getAccessTokenDefinitiveVerfuegungDokument(
                rueckforderungFormular.id
            )
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(
                    downloadFile.accessToken,
                    downloadFile.filename,
                    false,
                    win
                );
            })
            .catch(() => {
                win.close();
            });
    }

    public setCurrentUserAsVerantwortlicher(
        rueckforderungFormular: TSRueckforderungFormular
    ): void {
        this.rueckforderungFormular$ = from(
            this.notrechtRS.setVerantwortlicher(
                rueckforderungFormular.id,
                this.authServiceRS.getPrincipal().username
            )
        );
    }

    public setDokumenteGeprueft(
        rueckforderungFormular: TSRueckforderungFormular
    ): void {
        this.rueckforderungFormular$ = from(
            this.notrechtRS.setDokumenteGeprueft(rueckforderungFormular.id)
        );
    }

    public canDeleteDocuments(formular: TSRueckforderungFormular): boolean {
        return !isAnyOfVerfuegtOrPruefungKantonStufe2(formular.status);
    }

    public beschwerdeBearbeiten(): void {
        this.showBeschwerde = true;
        this.beschwerdeReadOnly = false;
    }

    public saveBeschwerde(
        rueckforderungFormular: TSRueckforderungFormular
    ): void {
        this.showMessageFehlendeDokumenteBeschwerde = false;
        if (this.rueckforderungBeschwerdeDokumente.length === 0) {
            this.showMessageFehlendeDokumenteBeschwerde = true;
        }
        if (
            EbeguUtil.isNullOrUndefined(
                rueckforderungFormular.beschwerdeBetrag
            ) ||
            this.showMessageFehlendeDokumenteBeschwerde
        ) {
            EbeguUtil.selectFirstInvalid();
            return;
        }
        this.rueckforderungFormular$ = from(
            this.notrechtRS
                .saveBeschwerde(rueckforderungFormular)
                .then((response: TSRueckforderungFormular) => {
                    this.updateBeschwerdeView(response);
                    return response;
                })
        );
    }

    public isKantonBenutzerUndBeschwerdeNichtausgeloest(
        rueckforderungFormular: TSRueckforderungFormular
    ): boolean {
        return (
            this.isKantonBenutzer() &&
            EbeguUtil.isNullOrUndefined(
                rueckforderungFormular.beschwerdeAusbezahltAm
            )
        );
    }

    public abbrechenBeschwerde(
        rueckforderungFormular: TSRueckforderungFormular
    ): void {
        this.showMessageFehlendeDokumenteBeschwerde = false;
        this.rueckforderungFormular$ = from(
            this.notrechtRS
                .findRueckforderungFormular(rueckforderungFormular.id)
                .then((response: TSRueckforderungFormular) => {
                    this.updateBeschwerdeView(response);
                    return response;
                })
        );
    }

    private updateBeschwerdeView(
        rueckforderungFormular: TSRueckforderungFormular
    ): void {
        this.showBeschwerde = this.initBeschwerde(rueckforderungFormular);
        this.beschwerdeAlreadyExist = this.showBeschwerde;
        this.beschwerdeReadOnly = true;
    }
}
