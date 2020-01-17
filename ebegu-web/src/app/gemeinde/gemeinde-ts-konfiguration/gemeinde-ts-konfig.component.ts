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
import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {Transition} from '@uirouter/core';
import {StateDeclaration} from '@uirouter/core/lib/state/interface';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {OkHtmlDialogController} from '../../../gesuch/dialog/OkHtmlDialogController';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSDokumentTyp} from '../../../models/enums/TSDokumentTyp';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSGemeindeStatus} from '../../../models/enums/TSGemeindeStatus';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import {TSSprache} from '../../../models/enums/TSSprache';
import {TSGemeindeKonfiguration} from '../../../models/TSGemeindeKonfiguration';
import {CONSTANTS, MAX_FILE_SIZE} from '../../core/constants/CONSTANTS';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {DownloadRS} from '../../core/service/downloadRS.rest';
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
    @Input() private readonly gemeindeId: string;

    private navigationDest: StateDeclaration;

    public constructor(
        private readonly $transition$: Transition,
        private readonly errorService: ErrorService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly downloadRS: DownloadRS,
        private readonly uploadRS: UploadRS,
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
            .forEach(property => { property.value = this.getTagesschuleAktivierungsdatumAsString(config); });
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
            .forEach(property => { property.value = this.getTagesschuleErsterSchultagAsString(config); });
    }

    private initProperties(): void {
        this.konfigurationsListe.forEach(config => {
            config.initProperties();
        });
    }

    public uploadGemeindeGesuchsperiodeDokument(gesuchsperiodeId: string, file: any[], sprache: TSSprache, dokumentTyp: TSDokumentTyp): void {
        if (file.length <= 0) {
            return;
        }
        const selectedFile = file[0];
        if (selectedFile.size > MAX_FILE_SIZE) {
            this.dvDialog.showDialog(okHtmlDialogTempl, OkHtmlDialogController, {
                title: this.$translate.instant('FILE_ZU_GROSS'),
            });
        } // warum hier keine unterbrechung wenn der File ist zu gross ???

        this.uploadRS.uploadGemeindeGesuchsperiodeDokument(selectedFile, sprache, this.gemeindeId, gesuchsperiodeId, dokumentTyp)
            .then(() => {
                if (dokumentTyp === TSDokumentTyp.VORLAGE_MERKBLATT_TS) {
                    this.setVorlageMerkblattTSBoolean(true, sprache);
                }
            });
    }

    public removeGemeindeGesuchsperiodeDokument(gesuchsperiodeId: string, sprache: TSSprache, dokumentTyp: TSDokumentTyp): void {
        this.gemeindeRS.removeGemeindeGesuchsperiodeDokument(this.gemeindeId ,gesuchsperiodeId, sprache, dokumentTyp)
            .then(() => {
              if (dokumentTyp === TSDokumentTyp.VORLAGE_MERKBLATT_TS) {
                    this.setVorlageMerkblattTSBoolean(false, sprache);
                }
            });
    }

    public downloadGemeindeGesuchsperiodeDokument(gesuchsperiodeId: string, sprache: TSSprache, dokumentTyp: TSDokumentTyp): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.gemeindeRS.downloadGemeindeGesuchsperiodeDokument(this.gemeindeId, gesuchsperiodeId, sprache, dokumentTyp).then(
            response => {
                let file;
                 if (dokumentTyp === TSDokumentTyp.VORLAGE_MERKBLATT_TS) {
                    file = new Blob([response], {type: 'application/pdf'});
                }
                const fileURL = URL.createObjectURL(file);
                this.downloadRS.redirectWindowToDownloadWhenReady(win, fileURL, '');
            });
    }
}
