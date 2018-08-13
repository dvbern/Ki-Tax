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

import TSFinanzielleSituationContainer from './TSFinanzielleSituationContainer';
import TSErwerbspensumContainer from './TSErwerbspensumContainer';
import TSEinkommensverschlechterungContainer from './TSEinkommensverschlechterungContainer';
import TSAbstractEntity from './TSAbstractEntity';
import TSGesuchsteller from './TSGesuchsteller';
import TSAdresseContainer from './TSAdresseContainer';

export default class TSGesuchstellerContainer extends TSAbstractEntity {

    private _gesuchstellerGS: TSGesuchsteller;
    private _gesuchstellerJA: TSGesuchsteller;
    private _adressen: Array<TSAdresseContainer>;
    private _korrespondenzAdresse: TSAdresseContainer;
    private _rechnungsAdresse: TSAdresseContainer;
    private _finanzielleSituationContainer: TSFinanzielleSituationContainer;
    private _erwerbspensenContainer: Array<TSErwerbspensumContainer>;
    private _einkommensverschlechterungContainer: TSEinkommensverschlechterungContainer;
    private _showUmzug: boolean = false;

    constructor(gesuchstellerJA?: TSGesuchsteller, finanzielleSituation?: TSFinanzielleSituationContainer,
                erwerbspensen?: Array<TSErwerbspensumContainer>,
                einkommensverschlechterungContainer?: TSEinkommensverschlechterungContainer) {
        super();
        this._gesuchstellerJA = gesuchstellerJA;
        this._finanzielleSituationContainer = finanzielleSituation;
        this._erwerbspensenContainer = erwerbspensen ? erwerbspensen : [];
        this._einkommensverschlechterungContainer = einkommensverschlechterungContainer;
    }

    get gesuchstellerGS(): TSGesuchsteller {
        return this._gesuchstellerGS;
    }

    set gesuchstellerGS(value: TSGesuchsteller) {
        this._gesuchstellerGS = value;
    }

    get gesuchstellerJA(): TSGesuchsteller {
        return this._gesuchstellerJA;
    }

    set gesuchstellerJA(value: TSGesuchsteller) {
        this._gesuchstellerJA = value;
    }

    public get adressen(): Array<TSAdresseContainer> {
        return this._adressen;
    }

    public set adressen(adr: Array<TSAdresseContainer>) {
        this._adressen = adr;
    }

    public addAdresse(value: TSAdresseContainer) {
        if (this._adressen) {
            this._adressen.push(value);
        }
    }

    public get korrespondenzAdresse(): TSAdresseContainer {
        return this._korrespondenzAdresse;
    }

    public set korrespondenzAdresse(value: TSAdresseContainer) {
        this._korrespondenzAdresse = value;
    }

    public get rechnungsAdresse(): TSAdresseContainer {
        return this._rechnungsAdresse;
    }

    public set rechnungsAdresse(value: TSAdresseContainer) {
        this._rechnungsAdresse = value;
    }

    public get finanzielleSituationContainer(): TSFinanzielleSituationContainer {
        return this._finanzielleSituationContainer;
    }

    public set finanzielleSituationContainer(value: TSFinanzielleSituationContainer) {
        this._finanzielleSituationContainer = value;
    }

    get erwerbspensenContainer(): Array<TSErwerbspensumContainer> {
        return this._erwerbspensenContainer;
    }

    set erwerbspensenContainer(value: Array<TSErwerbspensumContainer>) {
        this._erwerbspensenContainer = value;
    }

    get einkommensverschlechterungContainer(): TSEinkommensverschlechterungContainer {
        return this._einkommensverschlechterungContainer;
    }

    set einkommensverschlechterungContainer(value: TSEinkommensverschlechterungContainer) {
        this._einkommensverschlechterungContainer = value;
    }

    get showUmzug(): boolean {
        return this._showUmzug;
    }

    set showUmzug(value: boolean) {
        this._showUmzug = value;
    }

    /**
     * Wir gehen davon aus dass die Liste von Adressen aus dem Server sortiert kommt.
     * Deshalb duerfen wir die erste Adresse der Liste als Wohnadresse nehmen
     */
    public getWohnAdresse(): TSAdresseContainer {
        if (this.adressen && this.adressen.length > 0) {
            return this.adressen[0];
        }
        return undefined;
    }

    /**
     * Hier wird eine Kopie der Adressen erstellt und die erste Adresse weggemacht. Damit haben wir nur
     * die Umzugsadressen
     */
    public getUmzugAdressen(): Array<TSAdresseContainer> {
        if (this.adressen && this.adressen.length > 0) {
            const adressenCopy: Array<TSAdresseContainer> = angular.copy(this.adressen);
            adressenCopy.splice(0, 1);
            return adressenCopy;
        }
        return [];
    }

    /**
     * Schaut ob der GS1 oder der GS2 mindestens eine umzugsadresse hat
     */
    public isThereAnyUmzug(): boolean {
        return this.getUmzugAdressen().length > 0;
    }

    public extractFullName(): string {
        if (this.gesuchstellerJA) {
            return this.gesuchstellerJA.getFullName();
        }
        return undefined;
    }

    public extractNachname(): string {
        if (this.gesuchstellerJA) {
            return this.gesuchstellerJA.nachname;
        }
        return undefined;
    }

    public extractMail(): string {
        if (this.gesuchstellerJA) {
            return this.gesuchstellerJA.mail;
        }
        return undefined;
    }

    public extractPhone(): string {
        if (this.gesuchstellerJA) {
            return this.gesuchstellerJA.getPhone();
        }
        return undefined;
    }
}

