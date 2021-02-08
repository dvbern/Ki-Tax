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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Subscription} from 'rxjs';
import {TSLastenausgleichTagesschuleAngabenInstitution} from '../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenInstitution';
import {TSLastenausgleichTagesschuleAngabenInstitutionContainer} from '../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenInstitutionContainer';
import {LastenausgleichTSService} from '../../services/lastenausgleich-ts.service';

@Component({
    selector: 'dv-tagesschulen-angaben',
    templateUrl: './tagesschulen-angaben.component.html',
    styleUrls: ['./tagesschulen-angaben.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TagesschulenAngabenComponent {

    @Input() public institutionContainerId: string;

    public form: FormGroup;

    private subscription: Subscription;
    public latsAngabenInstitutionContainer: TSLastenausgleichTagesschuleAngabenInstitutionContainer

    public constructor(
        private readonly lastenausgleichTSService: LastenausgleichTSService,
        private fb: FormBuilder,
    ) {
    }

    public ngOnInit(): void {
        this.subscription = this.lastenausgleichTSService.getLATSAngabenGemeindeContainer().subscribe(container => {
            this.latsAngabenInstitutionContainer = container.angabenInstitutionContainers.find(institutionContainer => {
                return institutionContainer.id === this.institutionContainerId;
            });
            this.form = this.setupForm(this.latsAngabenInstitutionContainer.angabenDeklaration);
        })
    }

    private setupForm(latsAngabenInstiution: TSLastenausgleichTagesschuleAngabenInstitution): FormGroup {
        return this.fb.group({
            // A
            isLehrbetrieb: latsAngabenInstiution.isLehrbetrieb,
            // B
            anzahlEingeschriebeneKinder: latsAngabenInstiution.anzahlEingeschriebeneKinder,
            anzahlEingeschriebeneKinderKindergarten: latsAngabenInstiution.anzahlEingeschriebeneKinderKindergarten,
            anzahlEingeschriebeneKinderBasisstufe: latsAngabenInstiution.anzahlEingeschriebeneKinderBasisstufe,
            anzahlEingeschriebeneKinderPrimarstufe: latsAngabenInstiution.anzahlEingeschriebeneKinderPrimarstufe,
            anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen: latsAngabenInstiution.anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen,
            durchschnittKinderProTagFruehbetreuung: latsAngabenInstiution.durchschnittKinderProTagFruehbetreuung,
            durchschnittKinderProTagMittag: latsAngabenInstiution.durchschnittKinderProTagMittag,
            durchschnittKinderProTagNachmittag1: latsAngabenInstiution.durchschnittKinderProTagNachmittag1,
            durchschnittKinderProTagNachmittag2: latsAngabenInstiution.durchschnittKinderProTagNachmittag2,
            // C
            schuleAufBasisOrganisatorischesKonzept: latsAngabenInstiution.schuleAufBasisOrganisatorischesKonzept,
            schuleAufBasisPaedagogischesKonzept: latsAngabenInstiution.schuleAufBasisPaedagogischesKonzept,
            raeumlicheVoraussetzungenEingehalten: latsAngabenInstiution.raeumlicheVoraussetzungenEingehalten,
            betreuungsverhaeltnisEingehalten: latsAngabenInstiution.betreuungsverhaeltnisEingehalten,
            ernaehrungsGrundsaetzeEingehalten: latsAngabenInstiution.ernaehrungsGrundsaetzeEingehalten,
            // Bemerkungen
            bemerkungen: latsAngabenInstiution.bemerkungen
        })
    }

    public onFormSubmit(): void {

    }
}
