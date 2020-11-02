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

import {StateService} from '@uirouter/core';
import {IComponentOptions, IController} from 'angular';
import {of} from 'rxjs';
import {map, switchMap} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSZahlungsstatus} from '../../../models/enums/TSZahlungsstatus';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {TSZahlung} from '../../../models/TSZahlung';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {LogFactory} from '../../core/logging/LogFactory';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {ReportRS} from '../../core/service/reportRS.rest';
import {ZahlungRS} from '../../core/service/zahlungRS.rest';

const LOG = LogFactory.createLog('ZahlungViewController');

export class ZahlungViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./zahlungView.html');
    public controller = ZahlungViewController;
    public controllerAs = 'vm';
}

export class ZahlungViewController implements IController {

    public static $inject: string[] = [
        'ZahlungRS',
        '$state',
        'DownloadRS',
        'ReportRS',
        'AuthServiceRS',
    ];

    private zahlungen: TSZahlung[] = [];
    private isMahlzeitenzahlungen: boolean = false;

    public itemsByPage: number = 20;

    public constructor(
        private readonly zahlungRS: ZahlungRS,
        private readonly $state: StateService,
        private readonly downloadRS: DownloadRS,
        private readonly reportRS: ReportRS,
        private readonly authServiceRS: AuthServiceRS,
    ) {
    }

    public $onInit(): void {
        if (!this.$state.params.zahlungsauftragId) {
            return;
        }
        if (this.$state.params.isMahlzeitenzahlungen) {
            this.isMahlzeitenzahlungen = true;
        }

        this.authServiceRS.principal$
            .pipe(
                switchMap(principal => {
                    if (principal) {
                        const zahlungsauftragId = this.$state.params.zahlungsauftragId;
                        if (this.$state.params.zahlungsauftragId) {
                            return this.zahlungRS.getZahlungsauftragForRole$(
                                principal.getCurrentRole(), zahlungsauftragId);
                        }
                    }

                    return of(null);
                }),
                map(zahlungsauftrag => zahlungsauftrag ? zahlungsauftrag.zahlungen : []),
            )
            .subscribe(
                zahlungen => {
                    this.zahlungen = zahlungen;
                },
                err => LOG.error(err),
            );
    }

    public gotToUebersicht(): void {
        this.$state.go('zahlungsauftrag.view', {
            isMahlzeitenzahlungen: this.isMahlzeitenzahlungen,
        });
    }

    public downloadDetails(zahlung: TSZahlung): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.reportRS.getZahlungReportExcel(zahlung.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch(() => {
                win.close();
            });
    }

    public bestaetigen(zahlung: TSZahlung): void {
        console.log('bestaetigen');
        this.zahlungRS.zahlungBestaetigen(zahlung.id).then((response: TSZahlung) => {
            const index = EbeguUtil.getIndexOfElementwithID(response, this.zahlungen);
            if (index > -1) {
                this.zahlungen[index] = response;
            }
            EbeguUtil.handleSmarttablesUpdateBug(this.zahlungen);
        });
    }

    // noinspection JSMethodCanBeStatic
    public isBestaetigt(zahlungstatus: TSZahlungsstatus): boolean {
        return zahlungstatus === TSZahlungsstatus.BESTAETIGT;
    }
}
