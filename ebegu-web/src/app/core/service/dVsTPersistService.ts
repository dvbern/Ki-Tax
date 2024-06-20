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

import {AuthLifeCycleService} from '../../../authentication/service/authLifeCycle.service';
import {TSAuthEvent} from '../../../models/enums/TSAuthEvent';
import {LogFactory} from '../logging/LogFactory';

const LOG = LogFactory.createLog('DVsTPersistService');

/**
 * This service stores an array of TSSTPersistObject.
 * The namespace cannot be repeated which means that if a new configuration is saved for an
 * existing namespace, this configuration will overwrite the existing one.
 */
export class DVsTPersistService {
    public static $inject: any = ['AuthLifeCycleService'];

    public persistedData: Map<string, string> = new Map<string, string>();

    public constructor(
        private readonly authLifeCycleService: AuthLifeCycleService
    ) {
        this.clearAll();

        this.authLifeCycleService.get$(TSAuthEvent.LOGIN_SUCCESS).subscribe(
            () => this.clearAll(),
            err => LOG.error(err)
        );
    }

    private clearAll(): void {
        this.persistedData = new Map<string, string>();
    }

    public saveData(namespace: string, data: any): void {
        this.persistedData.set(namespace, JSON.stringify(data));
    }

    public loadData(namespace: string): any {
        if (this.persistedData.has(namespace)) {
            return JSON.parse(this.persistedData.get(namespace));
        }
        return undefined;
    }

    /**
     * Deletes the given namespace from the list if it exists and returns true.
     * If it doesn't exist it returns false
     */
    public deleteData(namespace: string): boolean {
        if (this.persistedData.has(namespace)) {
            this.persistedData.delete(namespace);
            return true;
        }
        return false;
    }
}
