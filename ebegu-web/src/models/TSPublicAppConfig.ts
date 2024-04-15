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

import * as moment from 'moment';

export class TSPublicAppConfig {
    public currentNode: string;
    public devmode: boolean;
    public whitelist: string;
    public dummyMode: boolean;
    public sentryEnvName: string;
    public backgroundColor: string = '#FFFFFF';
    public primaryColor: string;
    public primaryColorDark: string;
    public primaryColorLight: string;
    public logoFileName: string;
    public logoFileNameWhite: string;
    public zahlungentestmode: boolean;
    public personenSucheDisabled: boolean;
    public kitaxHost: string;
    public kitaxEndpoint: string;
    public notverordnungDefaultEinreichefristOeffentlich: string;
    public notverordnungDefaultEinreichefristPrivat: string;
    public ferienbetreuungAktiv: boolean;
    public lastenausgleichAktiv: boolean;
    public lastenausgleichTagesschulenAktiv: boolean;
    public gemeindeKennzahlenAktiv: any;
    public mulitmandantAktiv: any;
    public angebotTSActivated: boolean;
    public angebotFIActivated: boolean;
    public angebotTFOActivated: boolean;
    public angebotMittagstischActivated: boolean;
    public infomaZahlungen: boolean;
    public frenchEnabled: boolean;
    public geresEnabledForMandant: boolean;
    public ebeguKibonAnfrageTestGuiEnabled: boolean;
    public steuerschnittstelleAktivAb: moment.Moment;
    public zusatzinformationenInstitution: boolean;
    public activatedDemoFeatures: string;
    public checkboxAuszahlungInZukunft: boolean;
    public institutionenDurchGemeindenEinladen: boolean;
    public erlaubenInstitutionenZuWaehlen: boolean;
    public auszahlungAnEltern: boolean;
    public abweichnungenEnabled: boolean;
}
