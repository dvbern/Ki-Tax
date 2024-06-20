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

export class YoutubeLinkVisitor implements MandantVisitor<string | null> {
    private readonly _isGerman: boolean;

    public constructor(isGerman: boolean) {
        this._isGerman = isGerman;
    }

    public process(mandant: KiBonMandant): string | null {
        return mandant.accept(this);
    }

    public visitBern(): string {
        if (this._isGerman) {
            return 'https://www.youtube.com/embed/9eMWHfLjdBk';
        }
        return 'https://www.youtube.com/embed/4Um--UCWaXs';
    }

    public visitAppenzellAusserrhoden(): string {
        return 'https://www.youtube.com/embed/oq-NRhJlvUQ';
    }

    public visitLuzern(): string {
        return 'https://www.youtube.com/embed/1isAN9sqdTs';
    }

    public visitSolothurn(): string {
        return this.visitBern();
    }

    public visitSchwyz(): string | null {
        return null;
    }
}
