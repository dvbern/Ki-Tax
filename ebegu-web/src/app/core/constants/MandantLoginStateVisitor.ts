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

import {KiBonMandant} from './MANDANTS';
import {MandantVisitor} from './MandantVisitor';

export class MandantLoginStateVisitor implements MandantVisitor<string> {

    public process(mandant: KiBonMandant): string {
        return mandant.accept(this);
    }

    public visitAppenzellAusserrhoden(): string {
        return 'authentication.login';
    }

    public visitBern(): string {
        return 'authentication.login';
    }

    public visitLuzern(): string {
        return 'authentication.login';
    }

    public visitSolothurn(): string {
        return 'authentication.login';
    }

    public visitSchwyz(): string {
        return this.visitSolothurn();
    }

}
