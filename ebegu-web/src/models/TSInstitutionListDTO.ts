/*
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

import {TSBetreuungsangebotTyp} from './enums/betreuung/TSBetreuungsangebotTyp';
import {TSInstitutionStatus} from './enums/TSInstitutionStatus';
import {TSGemeinde} from './TSGemeinde';
import {TSInstitution} from './TSInstitution';
import {TSMandant} from './TSMandant';
import {TSTraegerschaft} from './TSTraegerschaft';

export class TSInstitutionListDTO extends TSInstitution {

    public betreuungsangebotTyp: TSBetreuungsangebotTyp;
    public gemeinde: TSGemeinde;

    public constructor(
        name?: string,
        tragerschaft?: TSTraegerschaft,
        mandant?: TSMandant,
        status?: TSInstitutionStatus,
        betreuungsangebotTyp?: TSBetreuungsangebotTyp,
        gemeinde?: TSGemeinde
    ) {
        super(name, tragerschaft, mandant, status);
        this.betreuungsangebotTyp = betreuungsangebotTyp;
        this.gemeinde = gemeinde;
    }
}
