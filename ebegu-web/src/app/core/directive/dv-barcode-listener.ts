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

import {IController, IDirective, IDirectiveFactory, IDocumentService, ILogService, ITimeoutService} from 'angular';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {AuthLifeCycleService} from '../../../authentication/service/authLifeCycle.service';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {FreigabeController} from '../../../gesuch/dialog/FreigabeController';
import {GesuchRS} from '../../../gesuch/service/gesuchRS.rest';
import {TSAuthEvent} from '../../../models/enums/TSAuthEvent';
import {TSAntragDTO} from '../../../models/TSAntragDTO';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {ErrorService} from '../errors/service/ErrorService';
import {LogFactory} from '../logging/LogFactory';
import {DvDialog} from './dv-dialog/dv-dialog';
import ITranslateService = angular.translate.ITranslateService;

const FREIGEBEN_DIALOG_TEMPLATE = require('../../../gesuch/dialog/freigabe.html');
const LOG = LogFactory.createLog('DVBarcodeListener');

export class DVBarcodeListener implements IDirective {
    public restrict = 'A';
    public controller = DVBarcodeController;
    public controllerAs = 'vm';

    public static factory(): IDirectiveFactory {
        const directive = () => new DVBarcodeListener();
        // @ts-ignore
        directive.$inject = [];
        return directive;
    }
}

/**
 * This binds a listener for a certain keypress sequence to the document. If this keypress sequence (escaped with §)
 * is found then we open the dialog
 * The format of an expected barcode sequence is §FREIGABE|OPEN|cd85e001-403f-407f-8eb8-102c402342b6§
 */
export class DVBarcodeController implements IController {

    public static $inject: ReadonlyArray<string> = [
        '$document',
        '$timeout',
        'DvDialog',
        'AuthServiceRS',
        'ErrorService',
        '$log',
        'AuthLifeCycleService',
        'GesuchRS',
        '$translate'
    ];

    private readonly unsubscribe$ = new Subject<void>();
    private barcodeReading: boolean = false;
    private barcodeBuffer: string[] = [];
    private barcodeReadtimeout: any = null;

    public constructor(
        private readonly $document: IDocumentService,
        private readonly $timeout: ITimeoutService,
        private readonly dVDialog: DvDialog,
        private readonly authService: AuthServiceRS,
        private readonly errorService: ErrorService,
        private readonly $log: ILogService,
        private readonly authLifeCycleService: AuthLifeCycleService,
        private readonly gesuchRS: GesuchRS,
        private readonly $translate: ITranslateService
    ) {
    }

    public $onInit(): void {
        const keypressEvent = (e: any) => {
            this.barcodeOnKeyPressed(e);
        };

        this.authLifeCycleService.get$(TSAuthEvent.LOGIN_SUCCESS)
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(
                () => this.handleLoginSuccessEvent(keypressEvent),
                err => this.$log.error(err));

        this.authLifeCycleService.get$(TSAuthEvent.LOGOUT_SUCCESS)
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(
                () => this.handleLogoutSuccessEvent(keypressEvent),
                err => this.$log.error(err));

    }

    public $onDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    private handleLoginSuccessEvent(keypressEvent: any): void {
        this.$document.unbind('keypress', keypressEvent);
        if (this.authService.isOneOfRoles(TSRoleUtil.getAdministratorJugendamtSchulamtRoles())) {
            this.$document.bind('keypress', keypressEvent);
        }
    }

    private handleLogoutSuccessEvent(keypressEvent: any): void {
        this.$document.unbind('keypress', keypressEvent);
    }

    // eslint-disable-next-line
    public barcodeOnKeyPressed(e: any): void {
        const key = e.keyCode || e.which || 0;
        const keyPressChar = String.fromCharCode(key);

        if (this.barcodeReading) {
            e.preventDefault();
            if (keyPressChar !== '§') {
                this.barcodeBuffer.push(keyPressChar);
                this.$log.debug(`Current buffer: ${  this.barcodeBuffer.join('')}`);
            }
        }

        if (keyPressChar !== '§') {
            return;
        }
        e.preventDefault();
        if (this.barcodeReading) {
            this.$log.debug('End Barcode read');

            let barcodeRead = this.barcodeBuffer.join('');
            this.$log.debug(`Barcode read:${  barcodeRead}`);
            barcodeRead = barcodeRead.replace('§', '');

            const barcodeParts = barcodeRead.split('|');

            if (barcodeParts.length === 3 || barcodeParts.length === 4) {
                const barcodeDocType = barcodeParts[0];
                const barcodeDocFunction = barcodeParts[1];
                const barcodeDocID = barcodeParts[2];
                const barcodeDocAnzahlZurueckgezogen = barcodeParts[3] || '0';

                this.$log.debug(`Barcode Doc Type: ${  barcodeDocType}`);
                this.$log.debug(`Barcode Doc Function: ${  barcodeDocFunction}`);
                this.$log.debug(`Barcode Doc ID: ${  barcodeDocID}`);
                this.$log.debug(`Barcode Doc Anzahl Zurueckgezogen: ${  barcodeDocAnzahlZurueckgezogen}`);

                this.barcodeBuffer = [];
                this.$timeout.cancel(this.barcodeReadtimeout);

                this.gesuchRS.findGesuchForFreigabe(barcodeDocID, barcodeDocAnzahlZurueckgezogen)
                    .then((response: TSAntragDTO) => {
                        let message;
                        if (!response) {
                            message = this.$translate.instant('FREIGABE_GESUCH_NOT_FOUND');
                        }
                        if (!response.canBeFreigegeben()) {
                            message = this.$translate.instant('FREIGABE_GESUCH_ALREADY_FREIGEGEBEN');
                        }
                        this.dVDialog.showDialogFullscreen(FREIGEBEN_DIALOG_TEMPLATE, FreigabeController, {
                            docID: barcodeDocID,
                            errorMessage: message,
                            gesuch: response
                        });
                    }).catch(error => {
                    this.errorService.addMesageAsError(this.$translate.instant('FREIGABE_GESUCH_NICHT_MOEGLICH'));
                    LOG.warn('Gesuch konnte nicht freigegeben werden!', error);
                });
            } else {
                this.errorService.addMesageAsError(this.$translate.instant('BARCODE_FALSCHES_FORMAT') + barcodeRead);
            }
        } else {
            this.$log.debug('Begin Barcode read');

            this.barcodeReadtimeout = this.$timeout(() => {
                this.barcodeReading = false;
                this.$log.debug('End Barcode read');
                this.$log.debug(`Clearing buffer: ${  this.barcodeBuffer.join('')}`);
                this.barcodeBuffer = [];
                // eslint-disable-next-line no-magic-numbers
            }, 2000);
        }
        this.barcodeReading = !this.barcodeReading;
    }
}
