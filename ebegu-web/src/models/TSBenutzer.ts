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

import {DateUtil} from '../utils/DateUtil';
import {EbeguUtil} from '../utils/EbeguUtil';
import {TSBenutzerStatus} from './enums/TSBenutzerStatus';
import {TSGemeindeStatus} from './enums/TSGemeindeStatus';
import {rolePrefix, TSRole} from './enums/TSRole';
import {TSSozialdienst} from './sozialdienst/TSSozialdienst';
import {TSBenutzerNoDetails} from './TSBenutzerNoDetails';
import {TSBerechtigung} from './TSBerechtigung';
import {TSGemeinde} from './TSGemeinde';
import {TSInstitution} from './TSInstitution';
import {TSMandant} from './TSMandant';
import {TSTraegerschaft} from './TSTraegerschaft';

export class TSBenutzer {

    private _nachname: string;
    private _vorname: string;
    private _username: string;
    private _externalUUID: string;
    private _password: string;
    private _email: string;
    private _mandant: TSMandant;
    private _status: TSBenutzerStatus;

    private _currentBerechtigung: TSBerechtigung;
    private _berechtigungen: Array<TSBerechtigung> = [];

    public constructor(
        vorname?: string,
        nachname?: string,
        username?: string,
        password?: string,
        email?: string,
        mandant?: TSMandant,
        role?: TSRole,
        traegerschaft?: TSTraegerschaft,
        institution?: TSInstitution,
        gemeinde?: TSGemeinde[],
        sozialdienst?: TSSozialdienst,
        status: TSBenutzerStatus = TSBenutzerStatus.AKTIV,
        externalUUID?: string,
    ) {
        this._vorname = vorname;
        this._nachname = nachname;
        this._username = username;
        this._externalUUID = externalUUID;
        this._password = password;
        this._email = email;
        this._mandant = mandant;
        this._status = status;
        // Berechtigung
        this._currentBerechtigung = new TSBerechtigung();
        this._currentBerechtigung.role = role;
        this._currentBerechtigung.institution = institution;
        this._currentBerechtigung.traegerschaft = traegerschaft;
        this._currentBerechtigung.sozialdienst = sozialdienst;
        if (gemeinde) {
            this._currentBerechtigung.gemeindeList = gemeinde;
        }
        this._berechtigungen.push(this._currentBerechtigung);
    }

    public get nachname(): string {
        return this._nachname;
    }

    public set nachname(value: string) {
        this._nachname = value;
    }

    public get vorname(): string {
        return this._vorname;
    }

    public set vorname(value: string) {
        this._vorname = value;
    }

    public get username(): string {
        return this._username;
    }

    public set username(value: string) {
        this._username = value;
    }

    public get externalUUID(): string {
        return this._externalUUID;
    }

    public set externalUUID(value: string) {
        this._externalUUID = value;
    }

    public get password(): string {
        return this._password;
    }

    public set password(value: string) {
        this._password = value;
    }

    public get email(): string {
        return this._email;
    }

    public set email(value: string) {
        this._email = value;
    }

    public get mandant(): TSMandant {
        return this._mandant;
    }

    public set mandant(value: TSMandant) {
        this._mandant = value;
    }

    public get status(): TSBenutzerStatus {
        return this._status;
    }

    public set status(value: TSBenutzerStatus) {
        this._status = value;
    }

    public get berechtigungen(): Array<TSBerechtigung> {
        return this._berechtigungen;
    }

    public set berechtigungen(value: Array<TSBerechtigung>) {
        this._berechtigungen = value;
    }

    public isActive(): boolean {
        return this._status === TSBenutzerStatus.AKTIV;
    }

    public isGesperrt(): boolean {
        return this._status === TSBenutzerStatus.GESPERRT;
    }

    public get currentBerechtigung(): TSBerechtigung {
        if (EbeguUtil.isNullOrUndefined(this._currentBerechtigung)) {
            for (const obj of this.berechtigungen) {
                if (obj.gueltigkeit.isInDateRange(DateUtil.now())) {
                    this._currentBerechtigung = obj;
                }
            }
        }
        if (!this._currentBerechtigung) {
            console.log('ERROR - Benutzer hat keine Berechtigung!', this.username);
        }
        return this._currentBerechtigung;
    }

    public set currentBerechtigung(value: TSBerechtigung) {
        this._currentBerechtigung = value;
    }

    /**
     * Returns the currentGemeinde for users with only 1 Gemeinde.
     * For a user with more than 1 Gemeinde undefined is returned
     */
    public extractCurrentGemeindeId(): string | undefined {
        if (this.hasJustOneGemeinde()) {
            return this.extractCurrentAktiveGemeinden()[0].id;
        }
        return undefined;
    }

    public extractCurrentGemeinden(): TSGemeinde[] {
        return this.currentBerechtigung.gemeindeList;
    }

    public extractCurrentAktiveGemeinden(): TSGemeinde[] {
        return this.currentBerechtigung.gemeindeList.filter(gmde => gmde.status === TSGemeindeStatus.AKTIV);
    }

    public getFullName(): string {
        return `${this.vorname ? this.vorname : ''} ${this.nachname ? this.nachname : ''}`;
    }

    public getRoleKey(): string {
        return rolePrefix() + this.currentBerechtigung.role;
    }

    public getCurrentRole(): TSRole | undefined {
        if (this.currentBerechtigung) {
            return this.currentBerechtigung.role;
        }
        return undefined;
    }

    public hasJustOneGemeinde(): boolean {
        return this.extractCurrentAktiveGemeinden().length === 1;
    }

    public hasRole(role: TSRole): boolean {
        return this.getCurrentRole() === role;
    }

    public hasOneOfRoles(roles: ReadonlyArray<TSRole>): boolean {
        const principalRole = this.getCurrentRole();
        return roles.some(role => role === principalRole);
    }

    public toBenutzerNoDetails(): TSBenutzerNoDetails {
        const noDetails = new TSBenutzerNoDetails();
        noDetails.nachname = this.nachname;
        noDetails.vorname = this.vorname;
        noDetails.username = this.username;
        noDetails.gemeindeIds = new Array<string>();
        for (const tsBerechtigung of this.berechtigungen) {
            for (const tsGemeinde of tsBerechtigung.gemeindeList) {
                noDetails.gemeindeIds.push(tsGemeinde.id);
            }
        }
        return noDetails;
    }
}
