/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {TSLastenausgleichTagesschuleAngabenGemeindeStatus} from '../enums/TSLastenausgleichTagesschuleAngabenGemeindeStatus';
import {TSAbstractEntity} from '../TSAbstractEntity';
import {TSGemeinde} from '../TSGemeinde';
import {TSGesuchsperiode} from '../TSGesuchsperiode';
import {TSLastenausgleichTagesschuleAngabenGemeinde} from './TSLastenausgleichTagesschuleAngabenGemeinde';
import {TSLastenausgleichTagesschuleAngabenInstitutionContainer} from './TSLastenausgleichTagesschuleAngabenInstitutionContainer';

export class TSLastenausgleichTagesschuleAngabenGemeindeContainer extends TSAbstractEntity {
    public status: TSLastenausgleichTagesschuleAngabenGemeindeStatus;
    public gemeinde: TSGemeinde;
    public gesuchsperiode: TSGesuchsperiode;
    public alleAngabenInKibonErfasst: boolean;
    public angabenDeklaration: TSLastenausgleichTagesschuleAngabenGemeinde;
    public angabenKorrektur: TSLastenausgleichTagesschuleAngabenGemeinde;
    public angabenInstitutionContainers: Array<TSLastenausgleichTagesschuleAngabenInstitutionContainer>;
}
