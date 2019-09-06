/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {TSModulTagesschuleIntervall} from './enums/TSModulTagesschuleIntervall';
import {TSModulTagesschuleName} from './enums/TSModulTagesschuleName';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import TSModulTagesschule from './TSModulTagesschule';

export default class TSModulTagesschuleGroup extends TSAbstractMutableEntity {

    public gesuchsperiodeId: string;
    public modulTagesschuleName: TSModulTagesschuleName;
    public bezeichnung: string;
    public zeitVon: string;
    public zeitBis: string;
    public verpflegungskosten: number;
    public intervall: TSModulTagesschuleIntervall;
    public wirdPaedagogischBetreut: boolean;
    public reihenfolge: number;
    public module: Array<TSModulTagesschule>;

    public constructor(
        modulTagesschuleName?: TSModulTagesschuleName,
        zeitVon?: string,
        zeitBis?: string,
    ) {
        super();
        this.modulTagesschuleName = modulTagesschuleName;
        this.zeitVon = zeitVon;
        this.zeitBis = zeitBis;
    }

    /**
     * Prueft ob beide Module gleich sind. Sie sind glech wenn wochentag und modulTagesschuleName gleich sind.
     * Die ZeitVon und ZeitBis spielt keine Rolle in diesem Fall, da so Module unterschiedlichen Institutionen
     * verglichen werden koennen.
     */
    public isSameModul(modulTagesschule: TSModulTagesschuleGroup): boolean {
        return modulTagesschule
            && this.modulTagesschuleName === modulTagesschule.modulTagesschuleName
            && this.bezeichnung === modulTagesschule.bezeichnung
            && this.gesuchsperiodeId === modulTagesschule.gesuchsperiodeId;
    }

    public uniqueId(): string {
        return this.id + this.gesuchsperiodeId + this.modulTagesschuleName;
    }
}
