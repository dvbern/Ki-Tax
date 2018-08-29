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

/**
 * Enum for the actions that can be triggered to create new Fall, Dossier and Gesuch
 */
import EbeguUtil from '../../utils/EbeguUtil';

export enum TSCreationAction {
    CREATE_NEW_FALL = 'CREATE_NEW_FALL',
    CREATE_NEW_DOSSIER = 'CREATE_NEW_DOSSIER',
    CREATE_NEW_GESUCH = 'CREATE_NEW_GESUCH',
    CREATE_NEW_MUTATION = 'CREATE_NEW_MUTATION',
    CREATE_NEW_FOLGEGESUCH = 'CREATE_NEW_FOLGEGESUCH',
    // Wenn leer: Bestehendes Gesuch oeffnen!
}

export function isNewDossierNeeded(creationAction: TSCreationAction): boolean {
    return EbeguUtil.isNotNullOrUndefined(creationAction)
        && (creationAction === TSCreationAction.CREATE_NEW_DOSSIER || creationAction === TSCreationAction.CREATE_NEW_FALL);
}

export function isNewFallNeeded(creationAction: TSCreationAction): boolean {
    return EbeguUtil.isNotNullOrUndefined(creationAction)
        && creationAction === TSCreationAction.CREATE_NEW_FALL;
}
