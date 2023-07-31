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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {Injectable} from '@angular/core';
import {NgForm} from '@angular/forms';

// Bridge to pass the reference of an angular2 form to angularJS
@Injectable({
    providedIn: 'root',
})
export class HybridFormBridgeService {
    public get form(): NgForm {
        return this._form;
    }

    public set form(value: NgForm) {
        this._form = value;
    }

    private _form: NgForm;
}
