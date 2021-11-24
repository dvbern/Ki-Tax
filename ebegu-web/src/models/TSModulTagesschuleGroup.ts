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

import {CONSTANTS} from '../app/core/constants/CONSTANTS';
import {EbeguUtil} from '../utils/EbeguUtil';
import {MAP_SORTED_BY_DAY_OF_WEEK, TSDayOfWeek} from './enums/TSDayOfWeek';
import {TSModulTagesschuleIntervall} from './enums/TSModulTagesschuleIntervall';
import {TSModulTagesschuleName} from './enums/TSModulTagesschuleName';
import {TSAbstractEntity} from './TSAbstractEntity';
import {TSExternalClient} from './TSExternalClient';
import {TSModulTagesschule} from './TSModulTagesschule';
import {TSTextRessource} from './TSTextRessource';

export class TSModulTagesschuleClientId extends TSAbstractEntity {
    private _client: TSExternalClient;
    private _identifier: string;

    public constructor(client: TSExternalClient, identifier: string) {
        super();
        this.client = client;
        this.identifier = identifier;
    }

    public set identifier(value: string) {
        this._identifier = value;
    }

    public get identifier(): string {
        return this._identifier;
    }

    public get client(): TSExternalClient {
        return this._client;
    }

    public set client(value: TSExternalClient) {
        this._client = value;
    }

    public copy(): TSModulTagesschuleClientId {
        const copy = new TSModulTagesschuleClientId(this.client, this.identifier);
        copy.identifier = this.identifier;

        return copy;
    }
}

export class TSModulTagesschuleGroup extends TSAbstractEntity {

    public modulTagesschuleName: TSModulTagesschuleName;
    public identifier: string;
    public bezeichnung: TSTextRessource = new TSTextRessource();
    public zeitVon: string;
    public zeitBis: string;
    public verpflegungskosten: number;
    public intervall: TSModulTagesschuleIntervall;
    public wirdPaedagogischBetreut: boolean;
    public reihenfolge: number;
    public module: Array<TSModulTagesschule>;
    public clientModuleIds: TSModulTagesschuleClientId[];

    // Zum einfacheren Handling: Pro Tag ein fixes Modul erstellen
    // Dies wird nicht zum Server synchronisiert
    public tempModulMonday: TSModulTagesschule;
    public tempModulTuesday: TSModulTagesschule;
    public tempModulWednesday: TSModulTagesschule;
    public tempModulThursday: TSModulTagesschule;
    public tempModulFriday: TSModulTagesschule;
    public validated = false;

    public constructor(
        modulTagesschuleName?: TSModulTagesschuleName,
        zeitVon?: string,
        zeitBis?: string,
    ) {
        super();
        this.modulTagesschuleName = modulTagesschuleName;
        this.zeitVon = zeitVon;
        this.zeitBis = zeitBis;
        this.identifier = EbeguUtil.generateRandomName(CONSTANTS.ID_LENGTH);
        this.clientModuleIds = [];
    }

    public getZeitraumString(): string {
        if (this.zeitVon && this.zeitBis) {
            // tslint:disable-next-line:prefer-template
            return this.zeitVon + ' - ' + this.zeitBis;
        }
        return '';
    }

    public initializeTempModule(): void {
        // Alle die aktuell gesetzt sind, werden als angeboten initialisiert
        if (EbeguUtil.isNotNullOrUndefined(this.module)) {
            this.initializeTempModuleIfAngeboten();
        } else {
            this.intervall = TSModulTagesschuleIntervall.WOECHENTLICH;
            this.wirdPaedagogischBetreut = true;
        }
        // Alle die jetzt noch nicht gesetzt sind, müssen neu erstellt werden (nicht angeboten)
        this.initializeTempModuleIfNichtAngeboten();
    }

    private initializeTempModuleIfAngeboten(): void {
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

    private initializeTempModuleIfNichtAngeboten(): void {
        if (EbeguUtil.isNullOrUndefined(this.tempModulMonday)) {
            this.tempModulMonday = TSModulTagesschule.create(TSDayOfWeek.MONDAY);
        }
        if (EbeguUtil.isNullOrUndefined(this.tempModulTuesday)) {
            this.tempModulTuesday = TSModulTagesschule.create(TSDayOfWeek.TUESDAY);
        }
        if (EbeguUtil.isNullOrUndefined(this.tempModulWednesday)) {
            this.tempModulWednesday = TSModulTagesschule.create(TSDayOfWeek.WEDNESDAY);
        }
        if (EbeguUtil.isNullOrUndefined(this.tempModulThursday)) {
            this.tempModulThursday = TSModulTagesschule.create(TSDayOfWeek.THURSDAY);
        }
        if (EbeguUtil.isNullOrUndefined(this.tempModulFriday)) {
            this.tempModulFriday = TSModulTagesschule.create(TSDayOfWeek.FRIDAY);
        }
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

    public isValid(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.modulTagesschuleName)
            && EbeguUtil.isNotNullOrUndefined(this.identifier)
            && EbeguUtil.isNotNullOrUndefined(this.bezeichnung)
            && EbeguUtil.isNotNullOrUndefined(this.zeitVon)
            && EbeguUtil.isNotNullOrUndefined(this.zeitBis)
            && EbeguUtil.isNotNullOrUndefined(this.intervall)
            && this.module.length > 0;
    }

    /**
     * Sortiert die Module dieser Group nach Wochentag und gibt sie zurück.
     */
    public getModuleOrdered(): TSModulTagesschule[] {
        const sorted = this.module;
        sorted.sort((a, b) => {
            const indexOfA = MAP_SORTED_BY_DAY_OF_WEEK.get(a.wochentag);
            const indexOfB = MAP_SORTED_BY_DAY_OF_WEEK.get(b.wochentag);
            return indexOfA.toString().localeCompare(indexOfB.toString());
        });
        this.module = sorted;
        return this.module;
    }

    /**
     * Liefert eine Kopie der Werte dieses Moduls
     */
    public getCopy(): TSModulTagesschuleGroup {
        const copy = new TSModulTagesschuleGroup(this.modulTagesschuleName, this.zeitVon, this.zeitBis);
        copy.bezeichnung = new TSTextRessource();
        copy.bezeichnung.textDeutsch = this.bezeichnung.textDeutsch;
        copy.bezeichnung.textFranzoesisch = this.bezeichnung.textFranzoesisch;
        copy.verpflegungskosten = this.verpflegungskosten;
        copy.intervall = this.intervall;
        copy.reihenfolge = this.reihenfolge;
        copy.wirdPaedagogischBetreut = this.wirdPaedagogischBetreut;
        copy.clientModuleIds = this.clientModuleIds.map(clientModuleId => clientModuleId.copy());
        copy.module = this.module.map(m => TSModulTagesschule.create(m.wochentag));
        return copy;
    }
}
