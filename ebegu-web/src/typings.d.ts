/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

declare module 'randomcolor' {
    /* eslint-disable  */
    interface RandomColorOptions {
        hue?:
            | number
            | 'red'
            | 'orange'
            | 'yellow'
            | 'green'
            | 'blue'
            | 'purple'
            | 'pink'
            | 'monochrome'
            | 'random';
        luminosity?: 'bright' | 'light' | 'dark' | 'random';
        count?: number;
        seed?: number | string;
        format?:
            | 'hsvArray'
            | 'hslArray'
            | 'hsl'
            | 'hsla'
            | 'rgbArray'
            | 'rgb'
            | 'rgba'
            | 'hex';
    }

    /* eslint-enable */

    declare function randomColor(options?: RandomColorOptions): string;
}
