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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

// Corresponds with URL
import {MandantVisitor} from './MandantVisitor';

export abstract class MANDANTS {
    public static readonly BERN: KiBonMandant = {
        accept: visitor => visitor.visitBern(),
        hostname: 'be',
        fullName: 'Kanton Bern'
    };

    public static readonly LUZERN: KiBonMandant = {
        accept: visitor => visitor.visitLuzern(),
        hostname: 'stadtluzern',
        fullName: 'Stadt Luzern'
    };

    public static readonly SOLOTHURN: KiBonMandant = {
        accept: visitor => visitor.visitSolothurn(),
        hostname: 'so',
        fullName: 'Kanton Solothurn'
    };
    public static readonly APPENZELL_AUSSERRHODEN: KiBonMandant = {
        accept: visitor => visitor.visitAppenzellAusserrhoden(),
        hostname: 'ar',
        fullName: 'Appenzell Ausserrhoden'
    };
    public static readonly SCHWYZ: KiBonMandant = {
        accept: visitor => visitor.visitSchwyz(),
        hostname: 'sz',
        fullName: 'Kanton Schwyz'
    };
    public static readonly NONE: KiBonMandant = {
        accept: () => {
            throw new Error('Should never be called for Mandant NONE, make sure mandant is set');
        },
        hostname: null,
        fullName: null
    };

}

export interface KiBonMandant {
    hostname: string;
    fullName: string;
    accept<T>(visitor: MandantVisitor<T>): T;
}
