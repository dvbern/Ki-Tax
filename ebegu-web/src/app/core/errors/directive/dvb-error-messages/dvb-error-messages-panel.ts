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

import IComponentOptions = angular.IComponentOptions;
import IScope = angular.IScope;
import {IController, ILogService, IOnInit} from 'angular';
import {RemoveDialogController} from '../../../../../gesuch/dialog/RemoveDialogController';
import {GesuchRS} from '../../../../../gesuch/service/gesuchRS.rest';
import {WizardStepManager} from '../../../../../gesuch/service/wizardStepManager';
import {TSErrorAction} from '../../../../../models/enums/TSErrorAction';
import {TSMessageEvent} from '../../../../../models/enums/TSErrorEvent';
import {TSErrorLevel} from '../../../../../models/enums/TSErrorLevel';
import {TSExceptionReport} from '../../../../../models/TSExceptionReport';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {DvDialog} from '../../../directive/dv-dialog/dv-dialog';
import {BroadcastService} from '../../../service/broadcast.service';
import {ErrorService} from '../../service/ErrorService';
import {ErrorServiceX} from '../../service/ErrorServiceX';

const removeDialogTemplate = require('../../../../../gesuch/dialog/removeDialogTemplate.html');

export class DvErrorMessagesPanelComponentConfig implements IComponentOptions {
    public scope = {};
    public template = require('./dvb-error-messages-panel.html');
    public controller = DvErrorMessagesPanelComponent;
    public controllerAs = 'vm';
}

/**
 * component that can display error messages
 */
export class DvErrorMessagesPanelComponent implements IController, IOnInit {

    public static $inject: string[] = [
        '$scope',
        'ErrorService',
        'DvDialog',
        'GesuchRS',
        'WizardStepManager',
        'BroadcastService',
        '$log',
        'ErrorServiceX'
    ];

    public errors: Array<TSExceptionReport> = [];
    public readonly TSRoleUtil = TSRoleUtil;

    public constructor(
        private readonly $scope: IScope,
        private readonly errorService: ErrorService,
        private readonly dvDialog: DvDialog,
        private readonly gesuchRS: GesuchRS,
        private readonly wizardStepManager: WizardStepManager,
        private readonly broadcastService: BroadcastService,
        private readonly $log: ILogService,
        private readonly errorServiceX: ErrorServiceX
    ) {
    }

    public $onInit(): void {
        this.$scope.$on(TSMessageEvent[TSMessageEvent.ERROR_UPDATE], this.displayMessages);
        this.$scope.$on(TSMessageEvent[TSMessageEvent.INFO_UPDATE], this.displayMessages);
        this.$scope.$on(TSMessageEvent[TSMessageEvent.CLEAR], () => {
            this.errors = [];
            this.hide();
        });

        this.broadcastService.on$(TSMessageEvent[TSMessageEvent.ERROR_UPDATE])
            .subscribe(message => this.displayMessagesX(message),
            error => this.$log.error(error));
        this.broadcastService.on$(TSMessageEvent[TSMessageEvent.INFO_UPDATE])
            .subscribe(message => this.displayMessagesX(message),
            error => this.$log.error(error));
        this.broadcastService.on$(TSMessageEvent[TSMessageEvent.CLEAR]).subscribe(() => {
            this.errors = [];
            this.hide();
        }, error => this.$log.error(error));
    }

    public displayMessages = (_event: any, errors: Array<TSExceptionReport>) => {
        this.resetTransitionInProgressFlag();

        this.errors = errors;
        this.show();
    };

    public displayMessagesX = (report: {type: any, payload: any}) => {
        this.resetTransitionInProgressFlag();
        // TODO: remove once migrated to angularX
        setTimeout(() => {
            this.errors = report.payload;
        }, 0);
        this.show();
    };

    /**
     * if an error comes from client we know that the transition is not in progress any more
     */
    private resetTransitionInProgressFlag(): void {
        if (this.wizardStepManager) {
            this.wizardStepManager.isTransitionInProgress = false;
        }
    }

    public executeAction(error: TSExceptionReport): void {
        if (error.action) {
            if (error.action === TSErrorAction.REMOVE_ONLINE_MUTATION && error.argumentList.length > 0) {
                this.removeOnlineMutation(error.objectId, error.argumentList[0]);

            } else if (error.action === TSErrorAction.REMOVE_ONLINE_ERNEUERUNGSGESUCH
                && error.argumentList.length > 0) {
                this.removeOnlineErneuerungsgesuch(error.objectId, error.argumentList[0]);
            }
        }
        this.clear();
    }

    private removeOnlineMutation(objectId: string, gesuchsperiodeId: string): void {
        this.dvDialog.showRemoveDialog(removeDialogTemplate, undefined, RemoveDialogController, {
            title: 'REMOVE_ONLINE_MUTATION_CONFIRMATION',
            deleteText: 'REMOVE_ONLINE_MUTATION_BESCHREIBUNG',
            parentController: undefined,
            elementID: undefined,
        }).then(() => {   // User confirmed removal
            this.gesuchRS.removeOnlineMutation(objectId, gesuchsperiodeId);
        });
    }

    private removeOnlineErneuerungsgesuch(objectId: string, gesuchsperiodeId: string): void {
        this.dvDialog.showRemoveDialog(removeDialogTemplate, undefined, RemoveDialogController, {
            title: 'REMOVE_ONLINE_ERNEUERUNGSGESUCH_CONFIRMATION',
            deleteText: 'REMOVE_ONLINE_ERNEUERUNGSGESUCH_BESCHREIBUNG',
            parentController: undefined,
            elementID: undefined,
        }).then(() => {   // User confirmed removal
            this.gesuchRS.removeOnlineFolgegesuch(objectId, gesuchsperiodeId);
        });
    }

    public isActionDefined(error: TSExceptionReport): boolean {
        return error.action !== undefined && error.action !== null;
    }

    public show(): void {
        angular.element('dvb-error-messages-panel').show();     // besser als $element injection fuer tests
    }

    public hide(): void {
        angular.element('dvb-error-messages-panel').hide();
    }

    public clear(): void {
        this.errorService.clearAll();
        this.errorServiceX.clearAll();
    }

    public messageStyle(): string {
        for (const error of this.errors) {
            if (error.severity !== TSErrorLevel.INFO) {
                return '';
            }
        }
        return 'info';
    }

}
