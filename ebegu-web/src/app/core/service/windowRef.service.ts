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

import {isPlatformBrowser} from '@angular/common';
import {Inject, Injectable, PLATFORM_ID} from '@angular/core';

export function getWindowObject(): Window {
    // return the global native browser window object
    return window;
}
function getMockWindow(): any {
    return {
        innerWidth: 0,
        innerHeight: 0,
        scrollY: 0,
        scrollX: 0,
        pageYOffset: 0,
        pageXOffset: 0,
        scroll: () => {},
        scrollTo: () => {},
        addEventListener: () => {},
        removeEventListener: () => {},
    };
}

@Injectable()
export class WindowRef {

    private readonly isBrowser: boolean = false;

    public get nativeWindow(): Window {
        if (this.isBrowser) {
            return getWindowObject();
        }
        return getMockWindow();
    }

    public get nativeLocalStorage(): Storage {
        return getWindowObject().localStorage;
    }

    public constructor(@Inject(PLATFORM_ID) platformId: any) {
        this.isBrowser = isPlatformBrowser(platformId);
    }
}
