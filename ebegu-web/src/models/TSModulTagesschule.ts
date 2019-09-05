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

import {TSDayOfWeek} from './enums/TSDayOfWeek';
import {TSModulTagesschuleIntervall} from './enums/TSModulTagesschuleIntervall';
import {TSModulTagesschuleName} from './enums/TSModulTagesschuleName';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';

export default class TSModulTagesschule extends TSAbstractMutableEntity {

    public gesuchsperiodeId: string;
    public wochentag: TSDayOfWeek;
    public modulTagesschuleName: TSModulTagesschuleName;
    public bezeichnung: string;
    public zeitVon: string;
    public zeitBis: string;
    public verpflegungskosten: number;
    public intervall: TSModulTagesschuleIntervall;
    public wirdPaedagogischBetreut: boolean;

    public angemeldet: boolean; // Transient, wird nicht auf Server synchronisiert, bzw. nur die mit angemeldet=true

    public constructor(
        wochentag?: TSDayOfWeek,
        modulTagesschuleName?: TSModulTagesschuleName,
        zeitVon?: string,
        zeitBis?: string,
    ) {
        super();
        this.wochentag = wochentag;
        this.modulTagesschuleName = modulTagesschuleName;
        this.zeitVon = zeitVon;
        this.zeitBis = zeitBis;
    }


    /**
     * Prueft ob beide Module gleich sind. Sie sind glech wenn wochentag und modulTagesschuleName gleich sind.
     * Die ZeitVon und ZeitBis spielt keine Rolle in diesem Fall, da so Module unterschiedlichen Institutionen
     * verglichen werden koennen.
     */
    public isSameModul(modulTagesschule: TSModulTagesschule): boolean {
        return modulTagesschule
            && this.wochentag === modulTagesschule.wochentag
            && this.modulTagesschuleName === modulTagesschule.modulTagesschuleName
            && this.bezeichnung === modulTagesschule.bezeichnung
            && this.wochentag === modulTagesschule.wochentag
            && this.gesuchsperiodeId === modulTagesschule.gesuchsperiodeId
    }

    public uniqueId(): string {
        return this.id + this.gesuchsperiodeId + this.modulTagesschuleName + this.wochentag;
    }
}
