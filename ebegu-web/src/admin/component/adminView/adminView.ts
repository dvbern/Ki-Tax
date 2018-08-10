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

import {IComponentOptions} from 'angular';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import TSApplicationProperty from '../../../models/TSApplicationProperty';
import EbeguRestUtil from '../../../utils/EbeguRestUtil';
import AbstractAdminViewController from '../../abstractAdminView';
import {ReindexRS} from '../../service/reindexRS.rest';

export class AdminViewComponentConfig implements IComponentOptions {
    transclude = false;
    bindings = {
        applicationProperties: '<'
    };
    template = require('./adminView.html');
    controller = AdminViewController;
    controllerAs = 'vm';
}

export class AdminViewController extends AbstractAdminViewController {
    static $inject = ['ApplicationPropertyRS', 'EbeguRestUtil', 'ReindexRS', 'AuthServiceRS'];

    applicationProperty: TSApplicationProperty;
    applicationPropertyRS: ApplicationPropertyRS;
    applicationProperties: TSApplicationProperty[];
    ebeguRestUtil: EbeguRestUtil;

    constructor(applicationPropertyRS: ApplicationPropertyRS, ebeguRestUtil: EbeguRestUtil,
                private readonly reindexRS: ReindexRS, authServiceRS: AuthServiceRS) {
        super(authServiceRS);
        this.applicationProperty = undefined;
        this.applicationPropertyRS = applicationPropertyRS;
        this.ebeguRestUtil = ebeguRestUtil;
    }

    submit(): void {
        //testen ob aktuelles property schon gespeichert ist
        if (this.applicationProperty.isNew()) {
            this.applicationPropertyRS.update(this.applicationProperty.name, this.applicationProperty.value);

        } else {
            this.applicationPropertyRS.create(this.applicationProperty.name, this.applicationProperty.value);
        }
        this.applicationProperty = undefined;
    }

    createItem(): void {
        this.applicationProperty = new TSApplicationProperty('', '');
    }

    editRow(row: TSApplicationProperty): void {
        this.applicationProperty = row;
    }

    resetForm(): void {
        this.applicationProperty = undefined;
        this.applicationPropertyRS.getAllApplicationProperties().then(response => {
            this.applicationProperties = response;
        });
    }

    private getIndexOfElementwithID(prop: TSApplicationProperty) {
        const idToSearch = prop.id;
        for (let i = 0; i < this.applicationProperties.length; i++) {
            if (this.applicationProperties[i].id === idToSearch) {
                return i;
            }
        }
        return -1;
    }

    public startReindex() {
        return this.reindexRS.reindex();
    }
}
