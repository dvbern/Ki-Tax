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

export enum TSFinanzielleSituationSubStepName {
    // general
    KEIN_WEITERER_SUBSTEP = 'KEIN_WEITERER_SUBSTEP',

    // bern
    BERN_START = 'BERN_START',
    BERN_GS1 = 'BERN_GS1',
    BERN_GS2 = 'BERN_GS2',
    BERN_RESULTATE = 'BERN_RESULTATE',
    BERN_SOZIALHILFE = 'BERN_SOZIALHILFE',
    BERN_SOZIALHILFE_DETAIL = 'BERN_SOZIALHILFE_DETAIL',

    // luzern
    LUZERN_START = 'LUZERN_START',
    LUZERN_GS2 = 'LUZERN_GS2',
    LUZERN_RESULTATE = 'LUZERN_RESULTATE',

    // solothurn
    SOLOTHURN_START = 'SOLOTHURN_START',
    SOLOTHURN_GS1 = 'SOLOTHURN_GS1',
    SOLOTHURN_GS2 = 'SOLOTHURN_GS2',

    // appenzell
    APPENZELL_START = 'APPENZELL_START',
    APPENZELL_GS2 = 'APPENZELL_GS2',

    // schwyz
    SCHWYZ_START = 'SCHWYZ_START',
    SCHWYZ_GS1 = 'SCHWYZ_GS1',
    SCHWYZ_GS2 = 'SCHWYZ_GS2'

}
