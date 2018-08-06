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
import {IComponentOptions, IFormController} from 'angular';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {InstitutionRS} from '../../../app/core/service/institutionRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {RemoveDialogController} from '../../../gesuch/dialog/RemoveDialogController';
import TSInstitution from '../../../models/TSInstitution';
import EbeguUtil from '../../../utils/EbeguUtil';
import AbstractAdminViewController from '../../abstractAdminView';
import './institutionenListView.less';

const removeDialogTemplate = require('../../../gesuch/dialog/removeDialogTemplate.html');

export class InstitutionenListViewComponentConfig implements IComponentOptions {
    transclude = false;
    bindings = {
        institutionen: '<',
    };
    template = require('./institutionenListView.html');
    controller = InstitutionenListViewController;
    controllerAs = 'vm';
}

export class InstitutionenListViewController extends AbstractAdminViewController {

    static $inject: ReadonlyArray<string> = ['InstitutionRS', 'DvDialog', 'AuthServiceRS', '$state'];

    form: IFormController;
    institutionen: TSInstitution[];
    selectedInstitution: TSInstitution = undefined;

    constructor(private readonly institutionRS: InstitutionRS,
                private readonly dvDialog: DvDialog, authServiceRS: AuthServiceRS,
                private readonly $state: StateService) {
        super(authServiceRS);
    }

    getInstitutionenList(): TSInstitution[] {
        return this.institutionen;
    }

    removeInstitution(institution: any): void {
        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            deleteText: '',
            title: 'LOESCHEN_DIALOG_TITLE',
            parentController: undefined,
            elementID: undefined
        }).then(() => {   //User confirmed removal
            this.selectedInstitution = undefined;
            this.institutionRS.removeInstitution(institution.id).then((response) => {
                const index = EbeguUtil.getIndexOfElementwithID(institution, this.institutionen);
                if (index > -1) {
                    this.institutionen.splice(index, 1);
                }
            });
        });
    }

    createInstitution(): void {
        this.$state.go('admin.institution', {
            institutionId: undefined
        });
    }

    editInstitution(institution: TSInstitution) {
        this.$state.go('admin.institution', {
            institutionId: institution.id
        });
    }
}
