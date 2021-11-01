/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {GesuchModelManager} from '../service/gesuchModelManager';

export class AbstractGesuchViewX {

    public constructor(
        protected gesuchModelManager: GesuchModelManager
    ) {
    }

    public getBasisjahr(): number | undefined {
        if (this.gesuchModelManager && this.gesuchModelManager.getBasisjahr()) {
            return this.gesuchModelManager.getBasisjahr();
        }
        return undefined;
    }

    public getBasisjahrMinus1(): number | undefined {
        return this.getBasisjahrMinus(1);
    }

    public getBasisjahrMinus2(): number | undefined {
        return this.getBasisjahrMinus(2);
    }

    private getBasisjahrMinus(nbr: number): number | undefined {
        if (this.gesuchModelManager && this.gesuchModelManager.getBasisjahr()) {
            return this.gesuchModelManager.getBasisjahr() - nbr;
        }
        return undefined;
    }

    public getBasisjahrPlus1(): number | undefined {
        return this.getBasisjahrPlus(1);
    }

    public getBasisjahrPlus2(): number | undefined {
        return this.getBasisjahrPlus(2);
    }

    private getBasisjahrPlus(nbr: number): number | undefined {
        if (this.gesuchModelManager && this.gesuchModelManager.getBasisjahrPlus(nbr)) {
            return this.gesuchModelManager.getBasisjahrPlus(nbr);
        }
        return undefined;
    }
}
