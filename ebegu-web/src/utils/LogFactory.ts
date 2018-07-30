/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import {Log as LogImpl, Logger} from 'ng2-logger';
/*
 * Copyright © 2018 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

/**
 * Usage: private log = Log.createLog(MyObjectClass);
 */

/* tslint:disable:no-any */
export class Log {
    private readonly logger: Logger<any>;

    constructor(name: string) {
        // todo environments must be created for angular. While hybrid we don't do it
        // if (ENVIRONMENT.production) {
        //     LogImpl.setProductionMode();
        // }
        this.logger = LogImpl.create(name);
    }

    public static createLog(type: Function): Log {
        return new Log(type.name);
    }

    public info(message: string, ...args: any[]): void {
        this.logger.info('INFO', message, ...args);
    }

    public warn(message: string, ...args: any[]): void {
        this.logger.warn('WARN', message, ...args);
    }

    public error(message: string, ...args: any[]): void {
        this.logger.error('ERROR', message, ...args);
    }

    public debug(message: string, ...args: any[]): void {
        this.logger.data('DEBUG', message, ...args);
    }
}
