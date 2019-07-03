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

import {TSEinstellungKey} from './enums/TSEinstellungKey';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import TSMandantKonfiguration from './TSMandantKonfiguration';

export class TSMandant extends TSAbstractMutableEntity {


    public name: string;
    public konfigurationsListe: TSMandantKonfiguration[];

    private tagesschuleEnabled: boolean;

    constructor() {
        super();
        this.initProperties();
    }

    private initProperties(): void {
        if (this.konfigurationsListe) {
            this.konfigurationsListe.forEach(config => {
                config.konfigurationen.forEach(property => {
                    if (TSEinstellungKey.TAGESSCHULE_ENABLED_FOR_MANDANT === property.key) {
                        // Sobald es in irgendeiner Gesuchsperiode aktiv ist, gilt es für alle!
                        if (property.value === 'true') {
                            this.tagesschuleEnabled = true;
                            return;
                        }
                    }
                });
            });
        }
    }

    public isTagesschuleEnabled(): boolean {
        return this.tagesschuleEnabled;
    }
}
