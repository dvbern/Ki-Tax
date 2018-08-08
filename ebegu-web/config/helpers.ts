/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

import * as path from 'path';

// Helper functions
const _root = path.resolve(__dirname, '..');

export function hasProcessFlag(flag) {
    return process.argv.join('').indexOf(flag) > -1;
}

export function root(...args) {
    args = Array.prototype.slice.call(arguments, 0);
    return path.join.apply(path, [_root].concat(args));
}

export function rootNode(args) {
    args = Array.prototype.slice.call(arguments, 0);
    return root.apply(path, ['node_modules'].concat(args));
}

export function prependExt(extensions, args) {
    args = args || [];
    if (!Array.isArray(args)) {
        args = [args];
    }
    return extensions.reduce((memo, val) => memo.concat(val, args.map(prefix => prefix + val)), ['']);
}

export function packageSort(packages) {
    // packages = ['polyfills', 'vendor', 'main']
    const len = packages.length - 1;
    const first = packages[0];
    const last = packages[len];
    return (a, b) => {
        // polyfills always first
        if (a.names[0] === first) {
            return -1;
        }
        // main always last
        if (a.names[0] === last) {
            return 1;
        }
        // vendor before app
        if (a.names[0] !== first && b.names[0] === last) {
            return -1;
        } else {
            return 1;
        }
    };
}

const entryPoints = ['inline', 'polyfills', 'sw-register', 'vendor', 'main'];

export function chunksSort(left: { names: string[] }, right: { names: string[] }): number {
    const leftIndex = entryPoints.indexOf(left.names[0]);
    const rightindex = entryPoints.indexOf(right.names[0]);

    if (leftIndex > rightindex) {
        return 1;
    }

    if (leftIndex < rightindex) {
        return -1;
    }

    return 0;
}

export function reverse(arr) {
    return arr.reverse();
}
