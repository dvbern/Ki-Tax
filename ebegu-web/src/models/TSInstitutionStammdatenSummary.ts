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

import {TSBetreuungsangebotTyp} from './enums/TSBetreuungsangebotTyp';
import {TSAbstractDateRangedEntity} from './TSAbstractDateRangedEntity';
import TSAdresse from './TSAdresse';
import TSInstitution from './TSInstitution';
import TSInstitutionStammdatenFerieninsel from './TSInstitutionStammdatenFerieninsel';
import TSInstitutionStammdatenTagesschule from './TSInstitutionStammdatenTagesschule';

export default class TSInstitutionStammdatenSummary extends TSAbstractDateRangedEntity {

    public betreuungsangebotTyp: TSBetreuungsangebotTyp = undefined;
    public institution: TSInstitution = undefined;
    public adresse: TSAdresse = undefined;
    public mail: string = undefined;
    public telefon: string = undefined;
    public webseite: string = undefined;
    public oeffnungszeiten: string = undefined;
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
    public institutionStammdatenTagesschule: TSInstitutionStammdatenTagesschule = undefined;
    public institutionStammdatenFerieninsel: TSInstitutionStammdatenFerieninsel = undefined;
    public sendMailWennOffenePendenzen: boolean = true;

    public constructor() {
        super();
    }
}
