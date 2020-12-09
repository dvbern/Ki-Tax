/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {ChangeDetectionStrategy, Component, Input, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {Transition} from '@uirouter/core';
import {StateDeclaration} from '@uirouter/core/lib/state/interface';
import {Moment} from 'moment';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSDokumentTyp} from '../../../models/enums/TSDokumentTyp';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSGemeindeStatus} from '../../../models/enums/TSGemeindeStatus';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import {TSRole} from '../../../models/enums/TSRole';
import {TSSprache} from '../../../models/enums/TSSprache';
import {TSGemeindeKonfiguration} from '../../../models/TSGemeindeKonfiguration';
import {DvNgOkDialogComponent} from '../../core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';
import {CONSTANTS, MAX_FILE_SIZE} from '../../core/constants/CONSTANTS';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {GesuchsperiodeRS} from '../../core/service/gesuchsperiodeRS.rest';
import {UploadRS} from '../../core/service/uploadRS.rest';

@Component({
    selector: 'dv-gemeinde-ts-konfiguration',
    templateUrl: './gemeinde-ts-konfig.component.html',
    styleUrls: ['./gemeinde-ts-konfig.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GemeindeTsKonfigComponent implements OnInit {
    @ViewChild(NgForm) public form: NgForm;
    @Input() public konfigurationsListe: TSGemeindeKonfiguration[];
    @Input() public gemeindeStatus: TSGemeindeStatus;
    @Input() public editMode: boolean = false;
    @Input() public tsAnmeldungenStartDatum: Moment;
    @Input() public gemeindeId: string;
    @Input() public korrespondenzspracheDe: boolean;
    @Input() public korrespondenzspracheFr: boolean;

    private navigationDest: StateDeclaration;
    private readonly _merkblattAnmeldungTSDE: { [key: string]: boolean } = {};
    private readonly _merkblattAnmeldungTSFR: { [key: string]: boolean } = {};
    private readonly _vorlageMerkblattAnmeldungTSDE: { [key: string]: boolean } = {};
    private readonly _vorlageMerkblattAnmeldungTSFR: { [key: string]: boolean } = {};

    public constructor(
        private readonly $transition$: Transition,
        private readonly errorService: ErrorService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly downloadRS: DownloadRS,
        private readonly uploadRS: UploadRS,
        private readonly dialog: MatDialog,
        private readonly translate: TranslateService,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly authServiceRS: AuthServiceRS,
    ) {
    }

    public ngOnInit(): void {
        this.navigationDest = this.$transition$.to();
        this.initProperties();
    }

    public isKonfigurationEditable(gk: TSGemeindeKonfiguration): boolean {
        return 'gemeinde.edit' === this.navigationDest.name
            && this.editMode
            && (TSGemeindeStatus.EINGELADEN === this.gemeindeStatus
                || (gk.gesuchsperiode && gk.gesuchsperiode.status &&
                    TSGesuchsperiodeStatus.GESCHLOSSEN !== gk.gesuchsperiode.status));
    }

    public getTagesschuleAktivierungsdatumAsString(konfiguration: TSGemeindeKonfiguration): string {
        const datum = konfiguration.konfigTagesschuleAktivierungsdatum;
        if (datum && datum.isValid()) {
            return datum.format(CONSTANTS.DATE_FORMAT);
        }
        return '';
    }

    public tagesschuleAktivierungsdatumChanged(config: TSGemeindeKonfiguration): void {
        config.konfigurationen
            .filter(property => TSEinstellungKey.GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB === property.key)
            .forEach(property => {
                property.value = this.getTagesschuleAktivierungsdatumAsString(config);
            });
    }

    public getTagesschuleErsterSchultagAsString(konfiguration: TSGemeindeKonfiguration): string {
        const datum = konfiguration.konfigTagesschuleErsterSchultag;
        if (datum && datum.isValid()) {
            return datum.format(CONSTANTS.DATE_FORMAT);
        }
        return '';
    }

    public tagesschuleErsterSchultagChanged(config: TSGemeindeKonfiguration): void {
        config.konfigurationen
            .filter(property => TSEinstellungKey.GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG === property.key)
            .forEach(property => {
                property.value = this.getTagesschuleErsterSchultagAsString(config);
            });
    }

    private initProperties(): void {
        this.konfigurationsListe.forEach(config => {
            config.initProperties();
            this.existMerkblattAnmeldungTS(config.gesuchsperiode.id, TSSprache.DEUTSCH);
            this.existMerkblattAnmeldungTS(config.gesuchsperiode.id, TSSprache.FRANZOESISCH);
            this.existVorlageMerkblattAnmeldungTS(config.gesuchsperiode.id, TSSprache.DEUTSCH);
            this.existVorlageMerkblattAnmeldungTS(config.gesuchsperiode.id, TSSprache.FRANZOESISCH);
        });
    }

    public uploadGemeindeGesuchsperiodeDokument(gesuchsperiodeId: string, event: any, sprache: TSSprache,
                                                dokumentTyp: TSDokumentTyp): void {
        if (event.target.files.length <= 0) {
            return;
        }
        const selectedFile = event.target.files[0];
        if (selectedFile.size > MAX_FILE_SIZE) {
            this.showFileTooBigDialog();
            return;
        }

        this.uploadRS.uploadGemeindeGesuchsperiodeDokument(selectedFile, sprache,
            this.gemeindeId, gesuchsperiodeId, dokumentTyp)
            .then(() => {
                if (dokumentTyp === TSDokumentTyp.MERKBLATT_ANMELDUNG_TS) {
                    this.setMerkblattAnmeldungTSBoolean(gesuchsperiodeId, true, sprache);
                }
            });
    }

    public removeGemeindeGesuchsperiodeDokument(gesuchsperiodeId: string, sprache: TSSprache,
                                                dokumentTyp: TSDokumentTyp): void {
        this.gemeindeRS.removeGemeindeGesuchsperiodeDokument(this.gemeindeId, gesuchsperiodeId, sprache, dokumentTyp)
            .then(() => {
                if (dokumentTyp === TSDokumentTyp.MERKBLATT_ANMELDUNG_TS) {
                    this.setMerkblattAnmeldungTSBoolean(gesuchsperiodeId, false, sprache);
                }
            });
    }

    public downloadGemeindeGesuchsperiodeDokument(gesuchsperiodeId: string, sprache: TSSprache,
                                                  dokumentTyp: TSDokumentTyp): void {
        this.gemeindeRS.downloadGemeindeGesuchsperiodeDokument(this.gemeindeId, gesuchsperiodeId,
            sprache, dokumentTyp).then(
            response => {
                let file;
                if (dokumentTyp === TSDokumentTyp.MERKBLATT_ANMELDUNG_TS) {
                    file = new Blob([response], {type: 'application/pdf'});
                }
                const filename = this.translate.instant('MERKBLATT_ANMELDUNG_TAGESSCHULE_DATEI_NAME');
                this.downloadRS.openDownload(file, filename);
            });
    }

    private existMerkblattAnmeldungTS(gesuchsperiodeId: string, sprache: TSSprache): void {
        this.gemeindeRS.existGemeindeGesuchsperiodeDokument(this.gemeindeId, gesuchsperiodeId,
            sprache, TSDokumentTyp.MERKBLATT_ANMELDUNG_TS).then(
            result => {
                this.setMerkblattAnmeldungTSBoolean(gesuchsperiodeId, result, sprache);
            });
    }

    private existVorlageMerkblattAnmeldungTS(gesuchsperiodeId: string, sprache: TSSprache): void {
        this.gesuchsperiodeRS.existDokument(
            gesuchsperiodeId, sprache, TSDokumentTyp.VORLAGE_MERKBLATT_TS).then(
            result => {
                this.setVorlageMerkblattAnmeldungTSBoolean(gesuchsperiodeId, result, sprache);
            });
    }

    private setMerkblattAnmeldungTSBoolean(gesuchsperiodeId: string, value: boolean, sprache: TSSprache): void {
        switch (sprache) {
            case TSSprache.FRANZOESISCH:
                this._merkblattAnmeldungTSFR[gesuchsperiodeId] = value;
                break;
            case TSSprache.DEUTSCH:
                this._merkblattAnmeldungTSDE[gesuchsperiodeId] = value;
                break;
            default:
                return;
        }
        this.reloadButton(gesuchsperiodeId);
    }

    private setVorlageMerkblattAnmeldungTSBoolean(gesuchsperiodeId: string, value: boolean, sprache: TSSprache): void {
        switch (sprache) {
            case TSSprache.FRANZOESISCH:
                this._vorlageMerkblattAnmeldungTSFR[gesuchsperiodeId] = value;
                break;
            case TSSprache.DEUTSCH:
                this._vorlageMerkblattAnmeldungTSDE[gesuchsperiodeId] = value;
                break;
            default:
                return;
        }
        this.reloadButton(gesuchsperiodeId);
    }

    private showFileTooBigDialog(): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: this.translate.instant('FILE_ZU_GROSS')
        };
        this.dialog
            .open(DvNgOkDialogComponent, dialogConfig);
    }

    public get sprache(): typeof TSSprache {
        return TSSprache;
    }

    public get dokumentTyp(): typeof TSDokumentTyp {
        return TSDokumentTyp;
    }

    public get merkblattAnmeldungTSDE(): { [key: string]: boolean } {
        return this._merkblattAnmeldungTSDE;
    }

    public get merkblattAnmeldungTSFR(): { [key: string]: boolean } {
        return this._merkblattAnmeldungTSFR;
    }

    public get vorlageMerkblattAnmeldungTSDE(): { [key: string]: boolean } {
        return this._vorlageMerkblattAnmeldungTSDE;
    }

    public get vorlageMerkblattAnmeldungTSFR(): { [key: string]: boolean } {
        return this._vorlageMerkblattAnmeldungTSFR;
    }

    private reloadButton(gesuchsperiodeId: string): void {
        const element2 = document.getElementById('accordion-tab-' + gesuchsperiodeId);
        element2.click();
    }

    public reload(): void {
        // force Angular to update the form
    }

    public downloadGesuchsperiodeDokument(gesuchsperiodeId: string, sprache: TSSprache,
                                          dokumentTyp: TSDokumentTyp): void {
        this.gesuchsperiodeRS.downloadGesuchsperiodeDokument(gesuchsperiodeId, sprache, dokumentTyp).then(
            response => {
                let file;
                if (dokumentTyp === TSDokumentTyp.VORLAGE_MERKBLATT_TS) {
                    file = new Blob([response],
                        {type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'});
                }
                this.downloadRS.openDownload(file,
                    this.translate.instant('VORLAGE_MERKBLATT_ANMELDUNG_TAGESSCHULE_DATEI_NAME'));
            });
    }

    public changeKonfigTagesschuleTagisEnabled(config: TSGemeindeKonfiguration): void {
        config.konfigurationen
            .filter(property => TSEinstellungKey.GEMEINDE_TAGESSCHULE_TAGIS_ENABLED === property.key)
            .forEach(property => {
                property.value = String(config.konfigTagesschuleTagisEnabled);
            });
    }

    public getKonfigTagesschuleTagisEnabledString(): string {
        return this.translate.instant('TAGESSCHULE_TAGIS_ENABLED');
    }

    public isSuperAdmin(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }
}
