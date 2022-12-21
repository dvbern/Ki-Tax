/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import {LOCALLOGIN_DATA, LocalLoginDaten} from './LOCALLOGIN_DATA';
import {KiBonMandant} from './MANDANTS';
import {MandantVisitor} from './MandantVisitor';

export class MandantLocalLoginDatenVisitor implements MandantVisitor<LocalLoginDaten> {

    public process(mandant: KiBonMandant): LocalLoginDaten {
        return mandant.accept(this);
    }

    public visitAppenzellAusserrhoden(): LocalLoginDaten {
        return LOCALLOGIN_DATA.AR;
    }

    public visitBern(): LocalLoginDaten {
        return LOCALLOGIN_DATA.BE;
    }

    public visitLuzern(): LocalLoginDaten {
        return LOCALLOGIN_DATA.LU;
    }

    public visitSolothurn(): LocalLoginDaten {
        return LOCALLOGIN_DATA.SO;
    }

}
