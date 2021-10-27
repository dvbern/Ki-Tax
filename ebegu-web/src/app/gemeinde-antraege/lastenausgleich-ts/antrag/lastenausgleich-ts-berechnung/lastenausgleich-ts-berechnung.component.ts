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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {BehaviorSubject, combineLatest} from 'rxjs';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSSprache} from '../../../../../models/enums/TSSprache';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {TSBenutzer} from '../../../../../models/TSBenutzer';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {ErrorService} from '../../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../../core/logging/LogFactory';
import {DownloadRS} from '../../../../core/service/downloadRS.rest';
import {LastenausgleichTSService} from '../../services/lastenausgleich-ts.service';

const LOG = LogFactory.createLog('LastenausgleichTsBerechnungComponent');

@Component({
    selector: 'dv-lastenausgleich-ts-berechnung',
    templateUrl: './lastenausgleich-ts-berechnung.component.html',
    styleUrls: ['./lastenausgleich-ts-berechnung.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LastenausgleichTsBerechnungComponent implements OnInit {

    private static readonly FILENAME_DE = 'Verfügung Tagesschulen kiBon';
    private static readonly FILENAME_FR = 'Modèle Décisions EJC kibon';

    public canViewDokumentErstellenButton: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    public downloadingDeFile: BehaviorSubject<boolean> = new BehaviorSubject(false);
    public downloadingFrFile: BehaviorSubject<boolean> = new BehaviorSubject(false);

    public latsContainer: TSLastenausgleichTagesschuleAngabenGemeindeContainer;
    private principal: TSBenutzer | null;
    public betreuungsstundenPrognose: number;
    public betreuungsstundenPrognoseFromKiBon: number;

    public constructor(
        private readonly translate: TranslateService,
        private readonly errorService: ErrorService,
        private readonly latsService: LastenausgleichTSService,
        private readonly authService: AuthServiceRS,
        private readonly downloadRS: DownloadRS,
        private readonly cd: ChangeDetectorRef,
    ) {
    }

    public ngOnInit(): void {
        combineLatest([
            this.latsService.getLATSAngabenGemeindeContainer(),
            this.authService.principal$,
        ]).subscribe(values => {
            this.latsContainer = values[0];
            this.principal = values[1];
            this.canViewDokumentErstellenButton.next(this.principal.hasOneOfRoles(TSRoleUtil.getMandantRoles()));
            this.initErwarteteBetreuungsstundenFromKiBon();
        }, () => this.errorService.addMesageAsInfo(this.translate.instant('DATA_RETRIEVAL_ERROR')));
    }

    public createLatsDocumentDe(): void {
        this.downloadingDeFile.next(true);
        this.latsService.latsDocxErstellen(
            this.latsContainer,
            TSSprache.DEUTSCH,
            this.betreuungsstundenPrognose || this.betreuungsstundenPrognoseFromKiBon,
        ).subscribe(
            response => {
                this.createDownloadFile(response, TSSprache.DEUTSCH);
                this.downloadingDeFile.next(false);
            },
            async err => {
                LOG.error(err);
                this.errorService.addMesageAsError(err?.translatedMessage || this.translate.instant(
                    'ERROR_UNEXPECTED'));
                this.downloadingDeFile.next(false);
            });
    }

    public createLatsDocumentFr(): void {
        this.downloadingFrFile.next(true);
        this.latsService.latsDocxErstellen(
            this.latsContainer,
            TSSprache.FRANZOESISCH,
            this.betreuungsstundenPrognose || this.betreuungsstundenPrognoseFromKiBon,
        ).subscribe(
            response => {
                this.createDownloadFile(response, TSSprache.FRANZOESISCH);
                this.downloadingFrFile.next(false);
            },
            async err => {
                LOG.error(err);
                this.errorService.addMesageAsError(err?.translatedMessage || this.translate.instant(
                    'ERROR_UNEXPECTED'));
                this.downloadingFrFile.next(false);
            },
        );
    }

    private createDownloadFile(response: BlobPart, sprache: TSSprache): void {
        const file = new Blob([response],
            {type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'});
        const filename = this.getFilename(sprache);
        this.downloadRS.openDownload(file, filename);
    }

    private getFilename(sprache: TSSprache): string {
        let filename;
        (sprache === TSSprache.DEUTSCH)
            ? filename = LastenausgleichTsBerechnungComponent.FILENAME_DE
            : filename = LastenausgleichTsBerechnungComponent.FILENAME_FR;

        return `${filename} ${this.latsContainer.gesuchsperiode.gesuchsperiodeString} ${this.latsContainer.gemeinde.name}.docx`;
    }

    private initErwarteteBetreuungsstundenFromKiBon(): void {
        this.latsService.getErwarteteBetreuungsstunden(this.latsContainer)
            .subscribe(res => {
                this.betreuungsstundenPrognoseFromKiBon = res;
                this.cd.markForCheck();
            }, err => LOG.error(err));
    }

    public saveContainerWithPrognose(): void {
        this.latsService.saveLATSAngabenGemeindePrognose(this.latsContainer.id,
            this.latsContainer.betreuungsstundenPrognose);
    }
}
