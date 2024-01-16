/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import {KiBonMandant} from './MANDANTS';
import {MandantVisitor} from './MandantVisitor';

export class MandantLogoNameVisitor implements MandantVisitor<string> {

    public process(mandant: KiBonMandant): string {
        return mandant.accept(this);
    }

    public visitAppenzellAusserrhoden(): string {
        return 'logo-kibon-ar.svg';
    }

    public visitBern(): string {
        return 'logo-kibon-bern.svg';
    }

    public visitLuzern(): string {
        return 'logo-kibon-luzern.svg';
    }

    public visitSolothurn(): string {
        return 'logo-kibon-solothurn.svg';
    }

    public visitSchwyz(): string {
        return 'logo-kibon-schwyz.sg';
    }

}
