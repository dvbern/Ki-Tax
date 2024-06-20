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

import {GesuchModelManager} from '../../service/gesuchModelManager';

export class EKVViewUtil {
    protected constructor() {}

    public static getGemeinsameFullname(
        gesuchModelManager: GesuchModelManager
    ): string {
        return `${gesuchModelManager.getGesuch().gesuchsteller1?.extractFullName()}
         + ${gesuchModelManager.getGesuch().gesuchsteller2?.extractFullName()}`;
    }

    public static getAntragsteller1Name(
        gesuchModelManager: GesuchModelManager
    ): string {
        return gesuchModelManager.getGesuch().gesuchsteller1.extractFullName();
    }

    public static getAntragsteller2Name(
        gesuchModelManager: GesuchModelManager
    ): string {
        return gesuchModelManager.getGesuch().gesuchsteller2.extractFullName();
    }
}
