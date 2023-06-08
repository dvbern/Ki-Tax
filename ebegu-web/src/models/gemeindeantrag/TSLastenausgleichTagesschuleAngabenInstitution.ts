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

import {EbeguUtil} from '../../utils/EbeguUtil';
import {TSAbstractEntity} from '../TSAbstractEntity';
import {TSOeffnungszeitenTagesschule} from './TSOeffnungszeitenTagesschule';

export class TSLastenausgleichTagesschuleAngabenInstitution extends TSAbstractEntity {

    // A: Informationen zur Tagesschule
    public isLehrbetrieb: boolean;

    // B: Quantitative Angaben
    public anzahlEingeschriebeneKinder: number;
    public anzahlEingeschriebeneKinderKindergarten: number;
    public anzahlEingeschriebeneKinderSekundarstufe: number;
    public anzahlEingeschriebeneKinderPrimarstufe: number;
    public anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen: number;
    public anzahlEingeschriebeneKinderVolksschulangebot: number;
    public anzahlEingeschriebeneKinderBasisstufe: number;
    public durchschnittKinderProTagFruehbetreuung: number;
    public durchschnittKinderProTagMittag: number;
    public durchschnittKinderProTagNachmittag1: number;
    public durchschnittKinderProTagNachmittag2: number;
    public betreuungsstundenEinschliesslichBesondereBeduerfnisse: number;

    // C: Qualitative Vorgaben der Tagesschuleverordnung
    public schuleAufBasisOrganisatorischesKonzept: boolean;
    public schuleAufBasisPaedagogischesKonzept: boolean;
    public raeumlicheVoraussetzungenEingehalten: boolean;
    public betreuungsverhaeltnisEingehalten: boolean;
    public ernaehrungsGrundsaetzeEingehalten: boolean;

    // Bemerkungen
    public bemerkungen: string;

    public oeffnungszeiten: TSOeffnungszeitenTagesschule[];

    public areKontrollfragenAnswered(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.schuleAufBasisOrganisatorischesKonzept) &&
            EbeguUtil.isNotNullOrUndefined(this.schuleAufBasisPaedagogischesKonzept) &&
            EbeguUtil.isNotNullOrUndefined(this.raeumlicheVoraussetzungenEingehalten) &&
            EbeguUtil.isNotNullOrUndefined(this.betreuungsverhaeltnisEingehalten) &&
            EbeguUtil.isNotNullOrUndefined(this.ernaehrungsGrundsaetzeEingehalten);
    }

    public areKontrollfragenOk(): boolean {
        return EbeguUtil.isNotNullAndTrue(this.schuleAufBasisOrganisatorischesKonzept) &&
            EbeguUtil.isNotNullAndTrue(this.schuleAufBasisPaedagogischesKonzept) &&
            EbeguUtil.isNotNullAndTrue(this.raeumlicheVoraussetzungenEingehalten) &&
            EbeguUtil.isNotNullAndTrue(this.betreuungsverhaeltnisEingehalten) &&
            EbeguUtil.isNotNullAndTrue(this.ernaehrungsGrundsaetzeEingehalten);
    }
}
