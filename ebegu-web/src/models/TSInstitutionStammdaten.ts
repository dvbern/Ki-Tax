/*
 * AGPL File-Header
 *
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

export default class TSInstitutionStammdaten extends TSAbstractDateRangedEntity {
    public administratoren: string; // read only
    public sachbearbeiter: string; // read only
    public betreuungsangebotTyp: TSBetreuungsangebotTyp;
    public institution: TSInstitution;
    public adresse: TSAdresse;
    public mail: string;
    public telefon: string;
    public iban: string;
    public kontoinhaber: string;
    public adresseKontoinhaber: TSAdresse;
    public institutionStammdatenTagesschule: TSInstitutionStammdatenTagesschule;
    public institutionStammdatenFerieninsel: TSInstitutionStammdatenFerieninsel;
}
