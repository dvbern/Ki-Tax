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
import {TSInstitutionStatus} from './enums/TSInstitutionStatus';
import {TSAbstractDateRangedEntity} from './TSAbstractDateRangedEntity';
import {TSAdresse} from './TSAdresse';
import {TSInstitution} from './TSInstitution';
import {TSInstitutionStammdatenBetreuungsgutscheine} from './TSInstitutionStammdatenBetreuungsgutscheine';
import {TSInstitutionStammdatenFerieninsel} from './TSInstitutionStammdatenFerieninsel';
import {TSInstitutionStammdatenTagesschule} from './TSInstitutionStammdatenTagesschule';

export class TSInstitutionStammdatenSummary extends TSAbstractDateRangedEntity {

    public betreuungsangebotTyp: TSBetreuungsangebotTyp = undefined;
    public institution: TSInstitution = undefined;
    public adresse: TSAdresse = undefined;
    public mail: string = undefined;
    public telefon: string = undefined;
    public webseite: string = undefined;
    public institutionStammdatenBetreuungsgutscheine: TSInstitutionStammdatenBetreuungsgutscheine = undefined;
    public institutionStammdatenTagesschule: TSInstitutionStammdatenTagesschule = undefined;
    public institutionStammdatenFerieninsel: TSInstitutionStammdatenFerieninsel = undefined;
    public sendMailWennOffenePendenzen: boolean = true;

    public constructor() {
        super();
    }

    public getAutocompleteText(): string {
        let queryString = this.institution.name;

        // ist der Status der Institution AKTIV, hat sie zwingend eine Adresse
        if (this.institution.status === TSInstitutionStatus.AKTIV) {
            queryString += ` - ${this.adresse.strasse}`;

            // Hausnummer kann theoretisch undefined sein
            if (this.adresse.hausnummer) {
                queryString += ` ${this.adresse.hausnummer}`;
            }
            queryString += `, ${this.adresse.plz} ${this.adresse.ort}`;
        }

        return queryString;
    }
}
