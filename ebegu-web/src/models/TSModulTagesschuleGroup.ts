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

import EbeguUtil from '../utils/EbeguUtil';
import {TSDayOfWeek} from './enums/TSDayOfWeek';
import {TSModulTagesschuleIntervall} from './enums/TSModulTagesschuleIntervall';
import {TSModulTagesschuleName} from './enums/TSModulTagesschuleName';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import TSModulTagesschule from './TSModulTagesschule';

export default class TSModulTagesschuleGroup extends TSAbstractMutableEntity {

    public modulTagesschuleName: TSModulTagesschuleName;
    public identifier: string;
    public bezeichnung: string;
    public zeitVon: string;
    public zeitBis: string;
    public verpflegungskosten: number;
    public intervall: TSModulTagesschuleIntervall;
    public wirdPaedagogischBetreut: boolean;
    public reihenfolge: number;
    public module: Array<TSModulTagesschule>;

    // Zum einfacheren Handling: Pro Tag ein fixes Modul erstellen
    public tempModulMonday: TSModulTagesschule;
    public tempModulTuesday: TSModulTagesschule;
    public tempModulWednesday: TSModulTagesschule;
    public tempModulThursday: TSModulTagesschule;
    public tempModulFriday: TSModulTagesschule;

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
            && this.identifier === modulTagesschule.identifier;
    }

    public uniqueId(): string {
        return this.modulTagesschuleName + this.identifier;
    }

    public getZeitraumString(): string {
        if (this.zeitVon && this.zeitBis) {
            return this.zeitVon + ' - ' + this.zeitBis;
        }
        return '';
    }

    public initializeTempModule(): void {
        // Alle die aktuell gesetzt sind, werden als angeboten initialisiert
        if (EbeguUtil.isNotNullOrUndefined(this.module)) {
            for (const modul of this.module) {
                if (TSDayOfWeek.MONDAY === modul.wochentag) {
                    this.tempModulMonday = modul;
                    this.tempModulMonday.angeboten = true;
                }
                if (TSDayOfWeek.TUESDAY === modul.wochentag) {
                    this.tempModulTuesday = modul;
                    this.tempModulTuesday.angeboten = true;
                }
                if (TSDayOfWeek.WEDNESDAY === modul.wochentag) {
                    this.tempModulWednesday = modul;
                    this.tempModulWednesday.angeboten = true;
                }
                if (TSDayOfWeek.THURSDAY === modul.wochentag) {
                    this.tempModulThursday = modul;
                    this.tempModulThursday.angeboten = true;
                }
                if (TSDayOfWeek.FRIDAY === modul.wochentag) {
                    this.tempModulFriday = modul;
                    this.tempModulFriday.angeboten = true;
                }
            }
        }
        // Alle die jetzt noch nicht gesetzt sind, müssen neu erstellt werden (nicht angeboten)
        if (EbeguUtil.isNullOrUndefined(this.tempModulMonday)){
            this.tempModulMonday = TSModulTagesschule.create(TSDayOfWeek.MONDAY);
        }
        if (EbeguUtil.isNullOrUndefined(this.tempModulTuesday)){
            this.tempModulTuesday = TSModulTagesschule.create(TSDayOfWeek.TUESDAY);
        }
        if (EbeguUtil.isNullOrUndefined(this.tempModulWednesday)){
            this.tempModulWednesday = TSModulTagesschule.create(TSDayOfWeek.WEDNESDAY);
        }
        if (EbeguUtil.isNullOrUndefined(this.tempModulThursday)){
            this.tempModulThursday = TSModulTagesschule.create(TSDayOfWeek.THURSDAY);
        }
        if (EbeguUtil.isNullOrUndefined(this.tempModulFriday)){
            this.tempModulFriday = TSModulTagesschule.create(TSDayOfWeek.FRIDAY);
        }
        console.error('module initialized');
    }

    public applyTempModule(): void {
        this.module = [];
        this.applyModulIfAngeboten(this.tempModulMonday);
        this.applyModulIfAngeboten(this.tempModulTuesday);
        this.applyModulIfAngeboten(this.tempModulWednesday);
        this.applyModulIfAngeboten(this.tempModulThursday);
        this.applyModulIfAngeboten(this.tempModulFriday);
    }

    private applyModulIfAngeboten(modulToEvaluate: TSModulTagesschule): void {
        if (modulToEvaluate.angeboten) {
            this.module.push(modulToEvaluate);
        }
    }
}
