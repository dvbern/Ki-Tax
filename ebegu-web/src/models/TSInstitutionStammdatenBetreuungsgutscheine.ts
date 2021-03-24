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

import {TSAbstractEntity} from './TSAbstractEntity';
import {TSAdresse} from './TSAdresse';
import {TSBetreuungsstandort} from './TSBetreuungsstandort';
import {TSOeffnungstageInstitution} from './TSOeffnungstageInstitution';

export class TSInstitutionStammdatenBetreuungsgutscheine extends TSAbstractEntity {

    public iban: string = undefined;
    public kontoinhaber: string = undefined;
    public adresseKontoinhaber: TSAdresse = undefined;
    public alterskategorieBaby: boolean = undefined;
    public alterskategorieVorschule: boolean = undefined;
    public alterskategorieKindergarten: boolean = undefined;
    public alterskategorieSchule: boolean = undefined;
    public subventioniertePlaetze: boolean = undefined;
    public anzahlPlaetze: number = undefined;
    public anzahlPlaetzeFirmen: number = undefined;
    public tarifProHauptmahlzeit: number = undefined;
    public tarifProNebenmahlzeit: number = undefined;
    public oeffnungstage: TSOeffnungstageInstitution = new TSOeffnungstageInstitution();
    public offenVon: string = undefined;
    public offenBis: string = undefined;
    public oeffnungsAbweichungen: string;
    public alternativeEmailFamilienportal: string = undefined;
    public betreuungsstandorte: TSBetreuungsstandort[] = [];

    public constructor() {
        super();
    }
}
