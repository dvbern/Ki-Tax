/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnDestroy,
    OnInit
} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatTableDataSource} from '@angular/material/table';
import {TranslateService} from '@ngx-translate/core';
import * as moment from 'moment';
import {Observable} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSBetreuungsangebotTyp} from '../../../models/enums/betreuung/TSBetreuungsangebotTyp';
import {TSInstitutionStatus} from '../../../models/enums/TSInstitutionStatus';
import {TSRole} from '../../../models/enums/TSRole';
import {TSStatistikParameterType} from '../../../models/enums/TSStatistikParameterType';
import {TSBatchJobInformation} from '../../../models/TSBatchJobInformation';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {TSInstitution} from '../../../models/TSInstitution';
import {TSInstitutionStammdaten} from '../../../models/TSInstitutionStammdaten';
import {TSStatistikParameter} from '../../../models/TSStatistikParameter';
import {TSWorkJob} from '../../../models/TSWorkJob';
import {DateUtil} from '../../../utils/DateUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvNgRemoveDialogComponent} from '../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {TSDemoFeature} from '../../core/directive/dv-hide-feature/TSDemoFeature';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {LogFactory} from '../../core/logging/LogFactory';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {BatchJobRS} from '../../core/service/batchRS.rest';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {GesuchsperiodeRS} from '../../core/service/gesuchsperiodeRS.rest';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {InstitutionStammdatenRS} from '../../core/service/institutionStammdatenRS.rest';
import {ReportAsyncRS} from '../../core/service/reportAsyncRS.rest';
import {LastenausgleichRS} from '../../lastenausgleich/services/lastenausgleichRS.rest';

const LOG = LogFactory.createLog('StatistikComponent');

@Component({
    selector: 'dv-statistik',
    templateUrl: './statistik.component.html',
    styleUrls: ['./statistik.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class StatistikComponent implements OnInit, OnDestroy {
    public readonly TSStatistikParameterType = TSStatistikParameterType;
    public readonly TSRole = TSRole;
    public readonly TSRoleUtil = TSRoleUtil;
    public readonly demoFeature = TSDemoFeature.ZAHLUNGEN_STATISTIK;

    private polling: NodeJS.Timeout;
    public statistikParameter: TSStatistikParameter;
    public gesuchsperioden: Array<TSGesuchsperiode>;
    private readonly DATE_PARAM_FORMAT: string = 'YYYY-MM-DD';
    // Statistiken sind nur moeglich ab Beginn der fruehesten Periode bis Ende der letzten Periode
    public maxDate: moment.Moment;
    public minDate: moment.Moment;
    public userjobs: MatTableDataSource<TSWorkJob>;
    public columndefs: string[] = [
        'typ',
        'erstellt',
        'gestartet',
        'beendet',
        'status',
        'icon'
    ];
    public allJobs: Array<TSBatchJobInformation>;
    public years: number[];
    public tagesschulenStammdatenList: TSInstitutionStammdaten[];
    public bgInstitutionen: TSInstitution[];
    public gemeinden: TSGemeinde[];
    public gemeindenMahlzeitenverguenstigungen: TSGemeinde[];
    public flagShowErrorNoGesuchSelected: boolean = false;
    public showKantonStatistik: boolean = false;
    public ferienbetreuungActive: boolean = false;
    public lastenausgleichActive: boolean = false;
    public lastenausgleichTagesschulenActive: boolean = false;
    public tagesschulenActive = false;
    public lastenausgleichYears: number[] = [];

    public constructor(
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly institutionStammdatenRS: InstitutionStammdatenRS,
        private readonly institutionRS: InstitutionRS,
        private readonly reportAsyncRS: ReportAsyncRS,
        private readonly downloadRS: DownloadRS,
        private readonly batchJobRS: BatchJobRS,
        private readonly errorService: ErrorService,
        private readonly translate: TranslateService,
        private readonly dialog: MatDialog,
        private readonly authServiceRS: AuthServiceRS,
        private readonly gemeindeRS: GemeindeRS,
        private readonly cd: ChangeDetectorRef,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly lastenausgleichRS: LastenausgleichRS
    ) {}

    private static sortInstitutions(
        stammdaten: TSInstitutionStammdaten[]
    ): TSInstitutionStammdaten[] {
        return stammdaten.sort((a, b) =>
            a.institution.name.localeCompare(b.institution.name)
        );
    }

    private static handleError(err: Error): void {
        LOG.error(err);
    }

    public ngOnInit(): void {
        this.statistikParameter = new TSStatistikParameter();
        this.gesuchsperiodeRS.getAllGesuchsperioden().then((response: any) => {
            this.gesuchsperioden = response;
            if (this.gesuchsperioden.length > 0) {
                this.maxDate = this.gesuchsperioden[0].gueltigkeit.gueltigBis;
                this.minDate = DateUtil.localDateToMoment('2017-01-01');
            }
            this.calculateYears();
            this.cd.markForCheck();
        });

        this.institutionStammdatenRS
            .getAllTagesschulenForCurrentBenutzer()
            .then((tagesschulenStammdatenList: TSInstitutionStammdaten[]) => {
                this.tagesschulenStammdatenList = tagesschulenStammdatenList
                    .filter(
                        t =>
                            t.institution.status !==
                            TSInstitutionStatus.NUR_LATS
                    )
                    .filter(
                        t =>
                            t.institutionStammdatenTagesschule
                                ?.einstellungenTagesschule
                    );
                this.tagesschulenStammdatenList =
                    StatistikComponent.sortInstitutions(
                        this.tagesschulenStammdatenList
                    );
                this.cd.markForCheck();
            });

        this.loadBGInstitutionen();

        this.gemeindeRS.getGemeindenForPrincipal$().subscribe(gemeinden => {
            this.gemeinden = gemeinden;
            this.cd.markForCheck();
        });

        if (this.showLastenausgleichBGStatistikAllowedForRole()) {
            this.lastenausgleichRS.getAllLastenausgleiche().subscribe(
                lastenausgleiche => {
                    this.lastenausgleichYears = lastenausgleiche
                        .map(l => l.jahr)
                        .filter(
                            y =>
                                y >=
                                CONSTANTS.FIRST_YEAR_LASTENAUSGLEICH_WITHOUT_SELBSTBEHALT
                        )
                        .sort((a, b) => a - b);
                    this.cd.markForCheck();
                },
                err => {
                    LOG.error(err);
                }
            );
        }

        this.updateShowMahlzeitenStatistik();
        this.refreshUserJobs();
        this.initBatchJobPolling();

        this.applicationPropertyRS.getPublicPropertiesCached().then(res => {
            this.ferienbetreuungActive = res.ferienbetreuungAktiv;
            this.lastenausgleichActive = res.lastenausgleichAktiv;
            this.lastenausgleichTagesschulenActive =
                res.lastenausgleichTagesschulenAktiv;
            this.updateShowKantonStatistik();
            this.tagesschulenActive = res.angebotTSActivated;
        });
    }

    public ngOnDestroy(): void {
        if (this.polling) {
            clearInterval(this.polling);
            LOG.debug('canceled job polling');
        }
    }

    private initBatchJobPolling(): void {
        // check all 8 seconds for the state
        const delay = 12000;
        this.polling = setInterval(() => this.refreshUserJobs(), delay);
    }

    private refreshUserJobs(): void {
        this.batchJobRS
            .getBatchJobsOfUser()
            .subscribe((response: TSWorkJob[]) => {
                this.userjobs = new MatTableDataSource(response);
                this.cd.markForCheck();
            }, StatistikComponent.handleError);
    }

    public generateStatistik(form: NgForm, type?: string): void {
        if (!form.valid) {
            return;
        }
        const stichtag = this.statistikParameter.stichtag
            ? this.statistikParameter.stichtag.format(this.DATE_PARAM_FORMAT)
            : undefined;
        switch (type) {
            case TSStatistikParameterType.GESUCH_STICHTAG:
                this.reportAsyncRS
                    .getGesuchStichtagReportExcel(
                        stichtag,
                        this.statistikParameter.gesuchsperiode
                            ? this.statistikParameter.gesuchsperiode
                            : null
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            case TSStatistikParameterType.GESUCH_ZEITRAUM:
                this.reportAsyncRS
                    .getGesuchZeitraumReportExcel(
                        this.statistikParameter.von.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.bis.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.gesuchZeitraumDatumTyp,
                        this.statistikParameter.gesuchsperiode
                            ? this.statistikParameter.gesuchsperiode
                            : null
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            case TSStatistikParameterType.KINDER:
                this.reportAsyncRS
                    .getKinderReportExcel(
                        this.statistikParameter.von.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.bis.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.gesuchsperiode
                            ? this.statistikParameter.gesuchsperiode
                            : null
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                break;
            case TSStatistikParameterType.GESUCHSTELLER:
                this.reportAsyncRS
                    .getGesuchstellerReportExcel(stichtag)
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            case TSStatistikParameterType.KANTON:
                this.reportAsyncRS
                    .getKantonReportExcel(
                        this.statistikParameter.von.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.bis.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.kantonSelbstbehalt
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                break;
            case TSStatistikParameterType.MITARBEITERINNEN:
                this.reportAsyncRS
                    .getMitarbeiterinnenReportExcel(
                        this.statistikParameter.von.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.bis.format(
                            this.DATE_PARAM_FORMAT
                        )
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            case TSStatistikParameterType.BENUTZER:
                this.reportAsyncRS
                    .getBenutzerReportExcel()
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                break;
            case TSStatistikParameterType.GESUCHSTELLER_KINDER_BETREUUNG:
                this.reportAsyncRS
                    .getGesuchstellerKinderBetreuungReportExcel(
                        this.statistikParameter.von.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.bis.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.gesuchsperiode
                            ? this.statistikParameter.gesuchsperiode
                            : null
                    )
                    .subscribe(
                        (res: {workjobId: string}) => {
                            this.informReportGenerationStarted(res);
                        },
                        () => {
                            LOG.error(
                                'An error occurred downloading the document, closing download window.'
                            );
                        }
                    );
                return;
            case TSStatistikParameterType.ZAHLUNGEN_PERIODE:
                if (this.statistikParameter.gesuchsperiode) {
                    this.reportAsyncRS
                        .getZahlungPeriodeReportExcel(
                            this.statistikParameter.gesuchsperiode
                        )
                        .subscribe((res: {workjobId: string}) => {
                            this.informReportGenerationStarted(res);
                            const startmsg =
                                this.translate.instant('STARTED_GENERATION');
                            this.errorService.addMesageAsInfo(startmsg);
                        }, StatistikComponent.handleError);
                } else {
                    LOG.warn('gesuchsperiode muss gewählt sein');
                }
                return;
            case TSStatistikParameterType.MASSENVERSAND:
                if (!this.isMassenversandValid()) {
                    return;
                }
                if (this.statistikParameter.text) {
                    this.openRemoveDialog$().subscribe(
                        () => {
                            this.createMassenversand();
                        },
                        err => LOG.error(err)
                    );
                } else {
                    this.createMassenversand();
                }
                return;
            case TSStatistikParameterType.INSTITUTIONEN:
                this.reportAsyncRS
                    .getInstitutionenReportExcel()
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            case TSStatistikParameterType.VERRECHNUNG_KIBON:
                this.reportAsyncRS
                    .getVerrechnungKibonReportExcel(
                        this.statistikParameter.doSave,
                        this.statistikParameter.betragProKind
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                break;
            case TSStatistikParameterType.TAGESSCHULE_ANMELDUNGEN:
                this.reportAsyncRS
                    .getTagesschuleAnmeldungenReportExcel(
                        this.statistikParameter.tagesschuleAnmeldungen.id,
                        this.statistikParameter.gesuchsperiode
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                break;
            case TSStatistikParameterType.TAGESSCHULE_RECHNUNGSSTELLUNG:
                this.reportAsyncRS
                    .getTagesschuleRechnungsstellungReportExcel(
                        this.statistikParameter.gesuchsperiode
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                break;
            case TSStatistikParameterType.NOTRECHT:
                this.reportAsyncRS
                    .getNotrechtReportExcel(this.statistikParameter.doSave)
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                break;
            case TSStatistikParameterType.MAHLZEITENVERGUENSTIGUNG:
                this.reportAsyncRS
                    .getMahlzeitenverguenstigungReportExcel(
                        this.statistikParameter.von.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.bis.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter
                            .gemeindeMahlzeitenverguenstigungen
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            case TSStatistikParameterType.GEMEINDEN:
                this.reportAsyncRS
                    .getGemeindenReportExcel()
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            case TSStatistikParameterType.FERIENBETREUUNG:
                this.reportAsyncRS
                    .getFerienbetreuungReportExcel()
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            case TSStatistikParameterType.LASTENAUSGLEICH_TAGESSCHULEN:
                this.reportAsyncRS
                    .getLastenausgleichTagesschulenReportExcel(
                        this.statistikParameter.gesuchsperiode
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            case TSStatistikParameterType.LASTENAUSGLEICH_BG:
                this.reportAsyncRS
                    .getLastenausgleichBGReportExcel(
                        this.statistikParameter.gemeinde,
                        this.statistikParameter.jahr,
                        this.statistikParameter.von?.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.bis?.format(
                            this.DATE_PARAM_FORMAT
                        )
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            case TSStatistikParameterType.ZAHLUNGEN:
                // falls der eingeloggte benutzer eine institution ist, wird das Dropdown mit den Institutinen
                // nicht gezeigt. Wir setzen die BG Institution, weil es in diesem Fall immer nur eine in der Liste
                // hat
                if (
                    this.authServiceRS.isOneOfRoles(
                        TSRoleUtil.getInstitutionOnlyRoles()
                    )
                ) {
                    this.statistikParameter.institution =
                        this.bgInstitutionen[0];
                }
                this.reportAsyncRS
                    .getZahlungenReportExcel(
                        this.statistikParameter.gesuchsperiode,
                        this.statistikParameter.gemeinde,
                        this.statistikParameter.institution
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            default:
                throw new Error(`unknown TSStatistikParameterType: ${type}`);
        }
    }

    private openRemoveDialog$(): Observable<boolean> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: this.translate.instant(
                'MASSENVERSAND_ERSTELLEN_CONFIRM_TITLE'
            ),
            text: this.translate.instant('MASSENVERSAND_ERSTELLEN_CONFIRM_INFO')
        };
        return this.dialog
            .open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed();
    }

    private createMassenversand(): void {
        LOG.info('Erstelle Massenversand');
        this.reportAsyncRS
            .getMassenversandReportExcel(
                this.statistikParameter.von
                    ? this.statistikParameter.von.format(this.DATE_PARAM_FORMAT)
                    : null,
                this.statistikParameter.bis.format(this.DATE_PARAM_FORMAT),
                this.statistikParameter.gesuchsperiode,
                this.statistikParameter.bgGesuche,
                this.statistikParameter.mischGesuche,
                this.statistikParameter.tsGesuche,
                this.statistikParameter.ohneFolgegesuche,
                this.statistikParameter.text
            )
            .subscribe(
                (res: {workjobId: string}) => {
                    this.informReportGenerationStarted(res);
                },
                () => {
                    LOG.error(
                        'An error occurred downloading the document, closing download window.'
                    );
                }
            );
    }

    private informReportGenerationStarted(res: {workjobId: string}): void {
        LOG.debug(`executionID: ${res.workjobId}`);
        const startmsg = this.translate.instant('STARTED_GENERATION');
        this.errorService.addMesageAsInfo(startmsg);
        this.refreshUserJobs();
    }

    private loadBGInstitutionen(): void {
        // bei trägerschaften und Institutionen laden wir nur die Institutionen, für die sie berechtigt sind.
        if (
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
            )
        ) {
            this.institutionRS
                .getInstitutionenEditableForCurrentBenutzer()
                .subscribe(institutionen => {
                    this.bgInstitutionen = institutionen;
                });
            return;
        }
        // mandanten und gemeinden sollen grundsätzlich alle Institutionen sehen.
        this.institutionRS
            .getInstitutionenReadableForCurrentBenutzer()
            .subscribe(institutionen => {
                this.bgInstitutionen = institutionen;
            });
    }

    public downloadStatistik(row: TSWorkJob): void {
        if (
            EbeguUtil.isNullOrUndefined(row) ||
            EbeguUtil.isNullOrUndefined(row.execution)
        ) {
            return;
        }

        if (
            EbeguUtil.isNullOrUndefined(row.execution.batchStatus) ||
            row.execution.batchStatus !== 'COMPLETED'
        ) {
            LOG.info('batch-job is not yet finnished');
            return;
        }

        const win = this.downloadRS.prepareDownloadWindow();
        LOG.debug(`accessToken: ${row.resultData}`);
        this.downloadRS.startDownload(
            row.resultData,
            'report.xlsx',
            false,
            win
        );
    }

    /**
     * helper methode die es dem Admin erlaubt alle jobs zu sehen
     */
    public showAllJobs(): void {
        this.batchJobRS.getAllJobs().subscribe((result: TSWorkJob[]) => {
            let res: TSBatchJobInformation[] = [];
            res = res.concat(result.map(value => value.execution || undefined));
            this.allJobs = res;
            this.cd.markForCheck();
        }, StatistikComponent.handleError);
    }

    /**
     * Takes all years of all Gesuchsperioden and saves them as a string into an array
     */
    private calculateYears(): void {
        this.years = [];
        this.gesuchsperioden.forEach(periode => {
            if (this.years.indexOf(periode.getBasisJahrPlus1()) < 0) {
                this.years.push(periode.getBasisJahrPlus1());
            }
            if (this.years.indexOf(periode.getBasisJahrPlus2()) < 0) {
                this.years.push(periode.getBasisJahrPlus2());
            }
        });

        this.years.sort();
    }

    public getGesuchsperiodenForTagesschule(
        stammdaten: TSInstitutionStammdaten
    ): TSGesuchsperiode[] {
        return stammdaten.institutionStammdatenTagesschule.einstellungenTagesschule
            .map(d => d.gesuchsperiode)
            .sort((a, b) =>
                b.gesuchsperiodeString.localeCompare(a.gesuchsperiodeString)
            );
    }

    public showMahlzeitenverguenstigungStatistik(): boolean {
        return (
            this.gemeindenMahlzeitenverguenstigungen &&
            this.gemeindenMahlzeitenverguenstigungen.length > 0 &&
            this.authServiceRS.isOneOfRoles([
                TSRole.SACHBEARBEITER_BG,
                TSRole.ADMIN_BG,
                TSRole.ADMIN_GEMEINDE,
                TSRole.SACHBEARBEITER_GEMEINDE,
                TSRole.SUPER_ADMIN,
                TSRole.ADMIN_TS,
                TSRole.SACHBEARBEITER_TS
            ])
        );
    }

    private updateShowMahlzeitenStatistik(): void {
        // Grundsaetzliche nur fuer Superadmin und Gemeinde-Mitarbeiter
        if (
            !this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getAdministratorOrAmtRole()
            )
        ) {
            return;
        }
        // Abfragen, welche meiner berechtigten Gemeinden Mahlzeitenverguenstigung haben
        this.gemeindeRS
            .getGemeindenWithMahlzeitenverguenstigungForBenutzer()
            .then(value => {
                // falls es nur eine Gemeinde gibt, wird dropdown nicht angezeigt
                if (value.length === 1) {
                    this.statistikParameter.gemeindeMahlzeitenverguenstigungen =
                        value[0];
                }
                this.gemeindenMahlzeitenverguenstigungen = value;
                this.cd.markForCheck();
            });
    }

    private isMassenversandValid(): boolean {
        // simulate a click in the checkboxes of Verantwortlichkeit
        this.gesuchTypeClicked();
        return !this.flagShowErrorNoGesuchSelected;
    }

    public gesuchTypeClicked(): void {
        this.flagShowErrorNoGesuchSelected =
            !this.statistikParameter.bgGesuche &&
            !this.statistikParameter.mischGesuche &&
            !this.statistikParameter.tsGesuche;
    }

    public updateShowKantonStatistik(): void {
        this.showKantonStatistik = false;
        if (
            this.authServiceRS.isOneOfRoles([
                TSRole.ADMIN_TS,
                TSRole.SACHBEARBEITER_TS
            ])
        ) {
            return;
        }
        if (!this.lastenausgleichActive) {
            return;
        }

        if (
            !this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
            )
        ) {
            this.showKantonStatistik = true;
            return;
        }
        this.institutionStammdatenRS
            .getBetreuungsangeboteForInstitutionenOfCurrentBenutzer()
            .then(response => {
                response.forEach(angebottyp => {
                    if (angebottyp !== TSBetreuungsangebotTyp.TAGESSCHULE) {
                        this.showKantonStatistik = true;
                    }
                });
                this.cd.markForCheck();
            });
    }

    public showGesucheNachStichtag(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SUPER_ADMIN,
            TSRole.REVISOR,
            TSRole.ADMIN_TS,
            TSRole.SACHBEARBEITER_TS,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT
        ]);
    }

    public showAllJobsVisible(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public showGesucheNachZeitraum(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_BG,
            TSRole.SUPER_ADMIN,
            TSRole.REVISOR,
            TSRole.ADMIN_TS,
            TSRole.SACHBEARBEITER_TS,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT
        ]);
    }

    public showZahlungenNachPeriode(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_BG,
            TSRole.SUPER_ADMIN,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE
        ]);
    }

    public showStatistikForRoles(roles: TSRole[]): boolean {
        return this.authServiceRS.isOneOfRoles(roles);
    }

    public showKinderStatistik(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_BG,
            TSRole.SUPER_ADMIN,
            TSRole.REVISOR,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
            TSRole.ADMIN_INSTITUTION,
            TSRole.SACHBEARBEITER_INSTITUTION,
            TSRole.ADMIN_TRAEGERSCHAFT,
            TSRole.SACHBEARBEITER_TRAEGERSCHAFT
        ]);
    }

    public showGesuchstellerStatistik(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_BG,
            TSRole.SUPER_ADMIN,
            TSRole.REVISOR,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT
        ]);
    }

    public showMitarbeiterStatistik(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_BG,
            TSRole.SUPER_ADMIN,
            TSRole.REVISOR,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE
        ]);
    }

    public showBenutzerStatistik(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.ADMIN_BG,
            TSRole.ADMIN_TS,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SUPER_ADMIN,
            TSRole.REVISOR,
            TSRole.ADMIN_TRAEGERSCHAFT,
            TSRole.ADMIN_INSTITUTION,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT
        ]);
    }

    public showGesuchstellerKinderBetreuungStatistik(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_BG,
            TSRole.SUPER_ADMIN,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE
        ]);
    }

    public showStatistikMassenversand(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.ADMIN_TS,
            TSRole.ADMIN_GEMEINDE
        ]);
    }

    public showInstitutionenStatistik(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_TS,
            TSRole.SACHBEARBEITER_TS
        ]);
    }

    public isSuperadmin(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public showTagesschuleAnmeldungenStatistik(): boolean {
        return (
            this.tagesschulenStammdatenList?.length &&
            this.authServiceRS.isOneOfRoles([
                TSRole.SUPER_ADMIN,
                TSRole.ADMIN_MANDANT,
                TSRole.SACHBEARBEITER_MANDANT,
                TSRole.ADMIN_GEMEINDE,
                TSRole.SACHBEARBEITER_GEMEINDE,
                TSRole.ADMIN_TS,
                TSRole.SACHBEARBEITER_TS,
                TSRole.ADMIN_INSTITUTION,
                TSRole.SACHBEARBEITER_INSTITUTION,
                TSRole.ADMIN_TRAEGERSCHAFT,
                TSRole.SACHBEARBEITER_TRAEGERSCHAFT
            ]) &&
            this.tagesschulenActive
        );
    }

    public showRechnungsstellungStatistik(): boolean {
        return (
            this.authServiceRS.isOneOfRoles([
                TSRole.SUPER_ADMIN,
                TSRole.ADMIN_MANDANT,
                TSRole.SACHBEARBEITER_MANDANT,
                TSRole.ADMIN_GEMEINDE,
                TSRole.SACHBEARBEITER_GEMEINDE,
                TSRole.ADMIN_TS,
                TSRole.SACHBEARBEITER_TS
            ]) && this.tagesschulenActive
        );
    }

    public showNotrechtStatistik(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }

    public showMandantStatistik(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }

    public showFerienbetreuungStatistik(): boolean {
        return (
            this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles()) &&
            this.ferienbetreuungActive
        );
    }

    public showLastenausgleichTagesschulenStatistik(): boolean {
        return (
            this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles()) &&
            this.lastenausgleichTagesschulenActive
        );
    }

    public showLastenausgleichBGStatistikAllowedForRole() {
        return this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getGemeindeOrBGRoles().concat(
                TSRoleUtil.getMandantRoles()
            )
        );
    }

    public showZahlungenStatistik(): boolean {
        // die Statistik wird nur gezeigt, falls der User für mindestens eine BG Institution berechtigt ist.
        // ansonsten handelt es sich allenfalls um einen TS Institution User
        return (
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getGemeindeOrBGRoles()
                    .concat(TSRoleUtil.getMandantRoles())
                    .concat(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())
            ) && this.bgInstitutionen?.length > 0
        );
    }

    public gemeindenVisibleZahlungenStatistik(): boolean {
        return !this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
        );
    }

    public institutionenVisibleZahlungenStatistik(): boolean {
        return !this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getInstitutionOnlyRoles()
        );
    }

    public requiredIfAlleGemeinden(): boolean {
        return EbeguUtil.isNullOrUndefined(this.statistikParameter.gemeinde);
    }

    public requiredIfAlleInstitutionen(): boolean {
        return EbeguUtil.isNullOrUndefined(this.statistikParameter.institution);
    }

    public getActiveGesuchsperioden(): TSGesuchsperiode[] {
        return this.gesuchsperioden?.filter(gp => gp.isAktiv());
    }
}
