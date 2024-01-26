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

export class UnknownKitaIdVisitor implements MandantVisitor<string> {

    public process(mandant: KiBonMandant): string {
        return mandant.accept(this);
    }

    public visitBern(): string {
        return '00000000-0000-0000-0000-000000000000';
    }

    public visitLuzern(): string {
        return '00000000-0000-0000-0000-000000000003';
    }

    public visitSolothurn(): string {
        return '00000000-0000-0000-0000-000000000006';
    }

    public visitAppenzellAusserrhoden(): string {
        return '00000000-0000-0000-0000-000000000009';
    }

    public visitSchwyz(): string {
        return '00000000-0000-0000-0000-000000000012';
    }

}
