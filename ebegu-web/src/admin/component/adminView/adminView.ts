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

import {IComponentOptions, IFormController, IHttpPromise} from 'angular';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {RemoveDialogController} from '../../../gesuch/dialog/RemoveDialogController';
import {TSApplicationProperty} from '../../../models/TSApplicationProperty';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {AbstractAdminViewController} from '../../abstractAdminView';
import {ReindexRS} from '../../service/reindexRS.rest';

const removeDialogTemplate = require('../../../gesuch/dialog/removeDialogTemplate.html');

export class AdminViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {
        applicationProperties: '<',
    };
    public template = require('./adminView.html');
    public controller = AdminViewController;
    public controllerAs = 'vm';
}

export class AdminViewController extends AbstractAdminViewController {
    public static $inject = [
        'ApplicationPropertyRS',
        'EbeguRestUtil',
        'ReindexRS',
        'AuthServiceRS',
        'DvDialog'];

    public form: IFormController;
    public applicationProperty: TSApplicationProperty;
    public applicationPropertyRS: ApplicationPropertyRS;
    public applicationProperties: TSApplicationProperty[];
    public ebeguRestUtil: EbeguRestUtil;

    public constructor(
        applicationPropertyRS: ApplicationPropertyRS,
        ebeguRestUtil: EbeguRestUtil,
        private readonly reindexRS: ReindexRS,
        authServiceRS: AuthServiceRS,
        private readonly dvDialog: DvDialog,
    ) {
        super(authServiceRS);
        this.applicationProperty = undefined;
        this.applicationPropertyRS = applicationPropertyRS;
        this.ebeguRestUtil = ebeguRestUtil;
    }

    public submit(): void {
        if (!this.form.$valid) {
            return;
        }
        // Bei STADT_BERN_ASIV_CONFIGURED eine Sicherheitsabfrage machen
        // tslint:disable-next-line:early-exit
        if (this.applicationProperty.name === 'STADT_BERN_ASIV_CONFIGURED'
                && this.applicationProperty.value === 'true') {
            this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
                title: 'CREATE_MASSENMUTATION_BERN_DIALOG_TITLE',
                deleteText: 'CREATE_MASSENMUTATION_BERN_DIALOG_TEXT',
                parentController: undefined,
                elementID: undefined,
            }).then(() => {
                this.doSave();
            });
        } else {
            this.doSave();
        }
    }

    private doSave(): void {
        // testen ob aktuelles property schon gespeichert ist
        if (this.applicationProperty.isNew()) {
            this.applicationPropertyRS.update(this.applicationProperty.name, this.applicationProperty.value);
        } else {
            this.applicationPropertyRS.create(this.applicationProperty.name, this.applicationProperty.value);
        }
        this.applicationProperty = undefined;
    }

    public createItem(): void {
        this.applicationProperty = new TSApplicationProperty('', '');
    }

    public editRow(row: TSApplicationProperty): void {
        this.applicationProperty = row;
    }

    public resetForm(): void {
        this.applicationProperty = undefined;
        this.applicationPropertyRS.getAllApplicationProperties().then(response => {
            this.applicationProperties = response;
        });
    }

    public getIndexOfElementwithID(prop: TSApplicationProperty): number {
        const idToSearch = prop.id;
        for (let i = 0; i < this.applicationProperties.length; i++) {
            if (this.applicationProperties[i].id === idToSearch) {
                return i;
            }
        }

        return -1;
    }

    public startReindex(): IHttpPromise<any> {
        return this.reindexRS.reindex();
    }
}
