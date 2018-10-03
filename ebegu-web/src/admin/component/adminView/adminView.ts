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

import {IComponentOptions, IHttpPromise} from 'angular';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import TSApplicationProperty from '../../../models/TSApplicationProperty';
import EbeguRestUtil from '../../../utils/EbeguRestUtil';
import AbstractAdminViewController from '../../abstractAdminView';
import {ReindexRS} from '../../service/reindexRS.rest';

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
    public static $inject = ['ApplicationPropertyRS', 'EbeguRestUtil', 'ReindexRS', 'AuthServiceRS'];

    public applicationProperty: TSApplicationProperty;
    public applicationPropertyRS: ApplicationPropertyRS;
    public applicationProperties: TSApplicationProperty[];
    public ebeguRestUtil: EbeguRestUtil;

    public constructor(
        applicationPropertyRS: ApplicationPropertyRS,
        ebeguRestUtil: EbeguRestUtil,
        private readonly reindexRS: ReindexRS,
        authServiceRS: AuthServiceRS,
    ) {
        super(authServiceRS);
        this.applicationProperty = undefined;
        this.applicationPropertyRS = applicationPropertyRS;
        this.ebeguRestUtil = ebeguRestUtil;
    }

    public submit(): void {
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
