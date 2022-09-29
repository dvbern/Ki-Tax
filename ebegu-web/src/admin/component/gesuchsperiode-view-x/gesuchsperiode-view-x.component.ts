import {ChangeDetectionStrategy, ChangeDetectorRef, Component, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {MatTableDataSource} from '@angular/material/table';
import {TranslateService} from '@ngx-translate/core';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import * as moment from 'moment';
import {DvNgOkDialogComponent} from '../../../app/core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';
import {DvNgRemoveDialogComponent} from '../../../app/core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {MAX_FILE_SIZE} from '../../../app/core/constants/CONSTANTS';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import {DownloadRS} from '../../../app/core/service/downloadRS.rest';
import {GesuchsperiodeRS} from '../../../app/core/service/gesuchsperiodeRS.rest';
import {UploadRS} from '../../../app/core/service/uploadRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GlobalCacheService} from '../../../gesuch/service/globalCacheService';
import {TSCacheTyp} from '../../../models/enums/TSCacheTyp';
import {TSDokumentTyp} from '../../../models/enums/TSDokumentTyp';
import {getTSGesuchsperiodeStatusValues, TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import {TSSprache} from '../../../models/enums/TSSprache';
import {TSEinstellung} from '../../../models/TSEinstellung';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {TSDateRange} from '../../../models/types/TSDateRange';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {AbstractAdminViewX} from '../../abstractAdminViewX';
import {EinstellungRS} from '../../service/einstellungRS.rest';

const LOG = LogFactory.createLog('GesuchsperiodeViewXComponent');

@Component({
  selector: 'dv-gesuchsperiode-view-x',
  templateUrl: './gesuchsperiode-view-x.component.html',
  styleUrls: ['./gesuchsperiode-view-x.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class GesuchsperiodeViewXComponent extends AbstractAdminViewX {

    private static readonly DOKUMENT_TYP_NOT_DEFINED = 'DokumentTyp not defined';

    @ViewChild(NgForm) public form: NgForm;

    public gesuchsperiode: TSGesuchsperiode;
    public einstellungenGesuchsperiode: MatTableDataSource<TSEinstellung>;

    public displayedColumns: string[] = ['key', 'value'];

    public initialStatus: TSGesuchsperiodeStatus;

    public isErlaeuterungDE: boolean = false;
    public isErlaeuterungFR: boolean = false;

    public isVorlageMerkblattDE: boolean = false;
    public isVorlageMerkblattFR: boolean = false;

    public isVorlageVerfuegungLatsDE: boolean = false;
    public isVorlageVerfuegungLatsFR: boolean = false;

    public isVorlageVerfuegungFerienbetreuungDE: boolean = false;
    public isVorlageVerfuegungFerienbetreuungFR: boolean = false;

    private readonly OFFICE_DOC_TYPE = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
    public readonly DEUTSCH: TSSprache = TSSprache.DEUTSCH;
    public readonly FRANZOESISCH: TSSprache = TSSprache.FRANZOESISCH;

    public readonly ERLAUTERUNG_ZUR_VERFUEGUNG: TSDokumentTyp = TSDokumentTyp.ERLAUTERUNG_ZUR_VERFUEGUNG;
    public readonly VORLAGE_MERKBLATT_TS: TSDokumentTyp = TSDokumentTyp.VORLAGE_MERKBLATT_TS;
    public readonly VORLAGE_VERFUEGUNG_LATS: TSDokumentTyp = TSDokumentTyp.VORLAGE_VERFUEGUNG_LATS;
    public readonly VORLAGE_VERFUEGUNG_FERIENBETREUUNG: TSDokumentTyp =
        TSDokumentTyp.VORLAGE_VERFUEGUNG_FERIENBETREUUNG;

    public constructor(
        private readonly einstellungenRS: EinstellungRS,
        private readonly dvDialog: MatDialog,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly $stateParams: UIRouterGlobals,
        private readonly $state: StateService,
        private readonly $translate: TranslateService,
        private readonly uploadRS: UploadRS,
        private readonly downloadRS: DownloadRS,
        private readonly globalCacheService: GlobalCacheService,
        authServiceRS: AuthServiceRS,
        private readonly cd: ChangeDetectorRef
    ) {
        super(authServiceRS);
    }

    public ngOnInit(): void {
        if (!this.$stateParams.params.gesuchsperiodeId) {
            this.createGesuchsperiode();

            return;
        }

        this.gesuchsperiodeRS.findGesuchsperiode(this.$stateParams.params.gesuchsperiodeId)
            .then((found: TSGesuchsperiode) => {
            this.setSelectedGesuchsperiode(found);
            this.initialStatus = this.gesuchsperiode.status;

            this.updateExistDokumenten(this.gesuchsperiode);
            this.cd.markForCheck();
        });
    }

    public getTSGesuchsperiodeStatusValues(): Array<TSGesuchsperiodeStatus> {
        return getTSGesuchsperiodeStatusValues();
    }

    private setSelectedGesuchsperiode(gesuchsperiode: any): void {
        this.gesuchsperiode = gesuchsperiode;
        this.readEinstellungenByGesuchsperiode();
    }

    private readEinstellungenByGesuchsperiode(): void {
        this.einstellungenRS.getAllEinstellungenBySystem(this.gesuchsperiode.id)
            .subscribe((response: TSEinstellung[]) => {
                response.sort((a, b) =>  this.$translate.instant(a.key.toString())
                        .localeCompare(this.$translate.instant(b.key.toString())));
                this.einstellungenGesuchsperiode = new MatTableDataSource<TSEinstellung>(response);
                this.cd.markForCheck();
            }, error => LOG.error(error));
    }

    public cancelGesuchsperiode(): void {
        this.$state.go('admin.gesuchsperioden');
    }

    public saveGesuchsperiode(): void {
        if (this.form.invalid || !this.statusHaveChanged()) {
            return;
        }
        if (!(this.gesuchsperiode.isNew()
            || this.initialStatus !== this.gesuchsperiode.status
            || this.gesuchsperiode.status === TSGesuchsperiodeStatus.AKTIV)) {
            return;
        }
        const dialogText = this.getGesuchsperiodeSaveDialogText(this.initialStatus !== this.gesuchsperiode.status);
        this.dvDialog.open(DvNgRemoveDialogComponent, {data: {
            title: 'GESUCHSPERIODE_DIALOG_TITLE',
            text: dialogText
        }}).afterClosed().subscribe(isOk => {
            if (isOk) {
                this.doSave();
            }
        }, error => LOG.error(error));
        return;
    }

    private doSave(): void {
        this.gesuchsperiodeRS.saveGesuchsperiode(this.gesuchsperiode).then((response: TSGesuchsperiode) => {
            this.gesuchsperiode = response;
            this.globalCacheService.getCache(TSCacheTyp.EBEGU_EINSTELLUNGEN).removeAll();
            // Die E-BEGU-Parameter für die neue Periode lesen bzw. erstellen, wenn noch nicht vorhanden
            this.readEinstellungenByGesuchsperiode();
            this.gesuchsperiodeRS.updateActiveGesuchsperiodenList(); // reset gesuchperioden in manager
            this.gesuchsperiodeRS.updateNichtAbgeschlosseneGesuchsperiodenList();
            this.initialStatus = this.gesuchsperiode.status;
            this.cd.markForCheck();
        });
    }

    public createGesuchsperiode(): void {
        this.gesuchsperiodeRS.getNewestGesuchsperiode().then(newestGesuchsperiode => {
            this.gesuchsperiode = new TSGesuchsperiode(TSGesuchsperiodeStatus.ENTWURF, new TSDateRange());
            this.initialStatus = undefined; // initialStatus ist undefined for new created Gesuchsperioden
            this.gesuchsperiode.gueltigkeit.gueltigAb =
                newestGesuchsperiode.gueltigkeit.gueltigAb.clone().add(1, 'years');
            this.gesuchsperiode.gueltigkeit.gueltigBis =
                newestGesuchsperiode.gueltigkeit.gueltigBis.clone().add(1, 'years');

            // we need to check for erlaeuterung already here, because the server wont send any information back and we
            // do not want to reloade the page
            this.updateExistDokumenten(newestGesuchsperiode);

        });
        this.gesuchsperiode = undefined;
    }

    public saveParameterByGesuchsperiode(): void {
        this.einstellungenGesuchsperiode.data.forEach(param => this.einstellungenRS.saveEinstellung(param)
            .subscribe(() => {}, error => LOG.error(error)));
        this.globalCacheService.getCache(TSCacheTyp.EBEGU_EINSTELLUNGEN).removeAll();
        this.gesuchsperiodeRS.updateActiveGesuchsperiodenList();
        this.gesuchsperiodeRS.updateNichtAbgeschlosseneGesuchsperiodenList();
    }

    private getGesuchsperiodeSaveDialogText(hasStatusChanged: boolean): string {
        if (!hasStatusChanged) {
            return ''; // if the status didn't change no message is required
        }
        if (this.gesuchsperiode.status === TSGesuchsperiodeStatus.ENTWURF) {
            return 'GESUCHSPERIODE_DIALOG_TEXT_ENTWURF';
        }
        if (this.gesuchsperiode.status === TSGesuchsperiodeStatus.AKTIV) {
            return 'GESUCHSPERIODE_DIALOG_TEXT_AKTIV';
        }
        if (this.gesuchsperiode.status === TSGesuchsperiodeStatus.INAKTIV) {
            return 'GESUCHSPERIODE_DIALOG_TEXT_INAKTIV';
        }
        if (this.gesuchsperiode.status === TSGesuchsperiodeStatus.GESCHLOSSEN) {
            return 'GESUCHSPERIODE_DIALOG_TEXT_GESCHLOSSEN';
        }
        LOG.warn('Achtung, Status unbekannt: ', this.gesuchsperiode.status);

        return null;
    }

    public periodenParamsEditable(): boolean {
        return this.periodenParamsEditableForPeriode(this.gesuchsperiode);
    }

    /**
     * Gibt true zurueck wenn der Status sich geaendert hat oder wenn der Status AKTIV ist, da in Status AKTIV, die
     * Parameter (Tagesschule) noch geaendert werden koennen.
     */
    private statusHaveChanged(): boolean {
        return this.initialStatus !== this.gesuchsperiode.status || this.gesuchsperiode.status === TSGesuchsperiodeStatus.AKTIV;
    }

    public getDatumFreischaltungMax(): moment.Moment {
        const gueltigAb = angular.copy(this.gesuchsperiode.gueltigkeit.gueltigAb);

        return gueltigAb.subtract(1, 'days');
    }

    public uploadGesuchsperiodeDokument(event: any, sprache: TSSprache, dokumentTyp: TSDokumentTyp): void {
        if (EbeguUtil.isNullOrUndefined(event?.target?.files?.length)) {
            return;
        }
        const files = event.target.files;
        const selectedFile = files[0];
        if (selectedFile.size > MAX_FILE_SIZE) {
            this.dvDialog.open(DvNgOkDialogComponent, {
                data: { title: this.$translate.instant('FILE_ZU_GROSS')}
            });
            return;
        }

        this.uploadRS.uploadGesuchsperiodeDokument(selectedFile, sprache, this.gesuchsperiode.id, dokumentTyp)
            .then(() => {
                switch (dokumentTyp) {
                    case TSDokumentTyp.ERLAUTERUNG_ZUR_VERFUEGUNG: {
                        this.setErlauterungBoolean(true, sprache);
                        break;
                    } case TSDokumentTyp.VORLAGE_MERKBLATT_TS: {
                        this.setVorlageMerkblattTSBoolean(true, sprache);
                        break;
                    } case TSDokumentTyp.VORLAGE_VERFUEGUNG_LATS: {
                        this.setVorlageVerfuegungLatsBoolean(true, sprache);
                        break;
                    } case TSDokumentTyp.VORLAGE_VERFUEGUNG_FERIENBETREUUNG: {
                        this.setVorlageVerfuegungFerienbetreuungBoolean(true, sprache);
                        break;
                    } default: {
                        throw new Error(GesuchsperiodeViewXComponent.DOKUMENT_TYP_NOT_DEFINED);
                    }
                }
                this.cd.markForCheck();
            });
    }

    public removeGesuchsperiodeDokument(sprache: TSSprache, dokumentTyp: TSDokumentTyp): void {
        this.gesuchsperiodeRS.removeGesuchsperiodeDokument(this.gesuchsperiode.id, sprache, dokumentTyp)
            .then(() => {
                switch (dokumentTyp) {
                    case TSDokumentTyp.ERLAUTERUNG_ZUR_VERFUEGUNG: {
                        this.setErlauterungBoolean(false, sprache);
                        break;
                    } case TSDokumentTyp.VORLAGE_MERKBLATT_TS: {
                        this.setVorlageMerkblattTSBoolean(false, sprache);
                        break;
                    } case TSDokumentTyp.VORLAGE_VERFUEGUNG_LATS: {
                        this.setVorlageVerfuegungLatsBoolean(false, sprache);
                        break;
                    }  case TSDokumentTyp.VORLAGE_VERFUEGUNG_FERIENBETREUUNG: {
                        this.setVorlageVerfuegungFerienbetreuungBoolean(false, sprache);
                        break;
                    } default: {
                        throw new Error(GesuchsperiodeViewXComponent.DOKUMENT_TYP_NOT_DEFINED);
                    }
                }
                this.cd.markForCheck();
            });
    }

    public  downloadGesuchsperiodeDokument(sprache: TSSprache, dokumentTyp: TSDokumentTyp): void {
        this.gesuchsperiodeRS.downloadGesuchsperiodeDokument(this.gesuchsperiode.id, sprache, dokumentTyp).then(
            response => {
                let file;
                let filename;
                switch (dokumentTyp) {
                    case TSDokumentTyp.ERLAUTERUNG_ZUR_VERFUEGUNG: {
                        file = new Blob([response], {type: 'application/pdf'});
                        filename = this.$translate.instant('ERLAUTERUNG_ZUR_VERFUEGUNG_DATEI_NAME');
                        break;
                    } case TSDokumentTyp.VORLAGE_MERKBLATT_TS: {
                        file = new Blob([response],
                            {type: this.OFFICE_DOC_TYPE});
                        filename = this.$translate.instant('VORLAGE_MERKBLATT_ANMELDUNG_TAGESSCHULE_DATEI_NAME');
                        break;
                    } case TSDokumentTyp.VORLAGE_VERFUEGUNG_LATS: {
                        file = new Blob([response],
                            {type: this.OFFICE_DOC_TYPE});
                        filename = this.$translate.instant('VORLAGE_VERFUEGUNG_LATS_DATEI_NAME');
                        break;
                    }  case TSDokumentTyp.VORLAGE_VERFUEGUNG_FERIENBETREUUNG: {
                        file = new Blob([response],
                            {type: this.OFFICE_DOC_TYPE});
                        filename = this.$translate.instant('VORLAGE_VERFUEGUNG_FERIENBETREUUNG_DATEI_NAME');
                        break;
                    } default: {
                        throw new Error(GesuchsperiodeViewXComponent.DOKUMENT_TYP_NOT_DEFINED);
                    }
                }
                this.downloadRS.openDownload(file, filename);
            });
    }

    private setErlauterungBoolean(value: boolean, sprache: TSSprache): void {
        switch (sprache) {
            case TSSprache.FRANZOESISCH:
                this.isErlaeuterungFR = value;
                break;
            case TSSprache.DEUTSCH:
                this.isErlaeuterungDE = value;
                break;
            default:
                return;
        }
    }

    private updateExistDokumenten(gesuchsperiode: TSGesuchsperiode): void {
        this.gesuchsperiodeRS.existDokument(
            gesuchsperiode.id, TSSprache.DEUTSCH, TSDokumentTyp.ERLAUTERUNG_ZUR_VERFUEGUNG).then(
            result => {
                this.isErlaeuterungDE = !!result;
                this.cd.markForCheck();
            });
        this.gesuchsperiodeRS.existDokument(
            gesuchsperiode.id, TSSprache.FRANZOESISCH, TSDokumentTyp.ERLAUTERUNG_ZUR_VERFUEGUNG).then(
            result => {
                this.isErlaeuterungFR = !!result;
                this.cd.markForCheck();
            });
        this.gesuchsperiodeRS.existDokument(
            gesuchsperiode.id, TSSprache.DEUTSCH, TSDokumentTyp.VORLAGE_MERKBLATT_TS).then(
            result => {
                this.isVorlageMerkblattDE = !!result;
                this.cd.markForCheck();
            });
        this.gesuchsperiodeRS.existDokument(
            gesuchsperiode.id, TSSprache.FRANZOESISCH, TSDokumentTyp.VORLAGE_MERKBLATT_TS).then(
            result => {
                this.isVorlageMerkblattFR = !!result;
                this.cd.markForCheck();
            });
        this.gesuchsperiodeRS.existDokument(
            gesuchsperiode.id, TSSprache.DEUTSCH, TSDokumentTyp.VORLAGE_VERFUEGUNG_LATS).then(
            result => {
                this.isVorlageVerfuegungLatsDE = !!result;
                this.cd.markForCheck();
            });
        this.gesuchsperiodeRS.existDokument(
            gesuchsperiode.id, TSSprache.FRANZOESISCH, TSDokumentTyp.VORLAGE_VERFUEGUNG_LATS).then(
            result => {
                this.isVorlageVerfuegungLatsFR = !!result;
                this.cd.markForCheck();
            });
        this.gesuchsperiodeRS.existDokument(
            gesuchsperiode.id, TSSprache.DEUTSCH, TSDokumentTyp.VORLAGE_VERFUEGUNG_FERIENBETREUUNG).then(
            result => {
                this.isVorlageVerfuegungFerienbetreuungDE = !!result;
                this.cd.markForCheck();
            });
        this.gesuchsperiodeRS.existDokument(
            gesuchsperiode.id, TSSprache.FRANZOESISCH, TSDokumentTyp.VORLAGE_VERFUEGUNG_FERIENBETREUUNG).then(
            result => {
                this.isVorlageVerfuegungFerienbetreuungFR = !!result;
                this.cd.markForCheck();
            });
    }

    private setVorlageMerkblattTSBoolean(value: boolean, sprache: TSSprache): void {
        switch (sprache) {
            case TSSprache.FRANZOESISCH:
                this.isVorlageMerkblattFR = value;
                break;
            case TSSprache.DEUTSCH:
                this.isVorlageMerkblattDE = value;
                break;
            default:
                return;
        }
    }

    private setVorlageVerfuegungLatsBoolean(value: boolean, sprache: TSSprache): void {
        switch (sprache) {
            case TSSprache.FRANZOESISCH:
                this.isVorlageVerfuegungLatsFR = value;
                break;
            case TSSprache.DEUTSCH:
                this.isVorlageVerfuegungLatsDE = value;
                break;
            default:
                return;
        }
    }

    private setVorlageVerfuegungFerienbetreuungBoolean(value: boolean, sprache: TSSprache): void {
        switch (sprache) {
            case TSSprache.FRANZOESISCH:
                this.isVorlageVerfuegungFerienbetreuungFR = value;
                break;
            case TSSprache.DEUTSCH:
                this.isVorlageVerfuegungFerienbetreuungDE = value;
                break;
            default:
                return;
        }
    }

    public doFilter(value: string): void {
        this.einstellungenGesuchsperiode.filter = value.trim().toLocaleLowerCase();
    }
}
