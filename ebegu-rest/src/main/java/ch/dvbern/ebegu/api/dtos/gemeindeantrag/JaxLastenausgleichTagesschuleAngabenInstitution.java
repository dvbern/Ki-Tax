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

package ch.dvbern.ebegu.api.dtos.gemeindeantrag;

import java.math.BigDecimal;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;

public class JaxLastenausgleichTagesschuleAngabenInstitution extends JaxAbstractDTO {

	private static final long serialVersionUID = -3807424667612896592L;

	// A: Informationen zur Tagesschule

	@Nullable
	private Boolean isLehrbetrieb;

	// B: Quantitative Angaben

	@Nullable
	private BigDecimal anzahlEingeschriebeneKinder;

	@Nullable
	private BigDecimal anzahlEingeschriebeneKinderKindergarten;

	@Nullable
	private BigDecimal anzahlEingeschriebeneKinderBasisstufe;

	@Nullable
	private BigDecimal anzahlEingeschriebeneKinderPrimarstufe;

	@Nullable
	private BigDecimal anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen;

	@Nullable
	private BigDecimal durchschnittKinderProTagFruehbetreuung;

	@Nullable
	private BigDecimal durchschnittKinderProTagMittag;

	@Nullable
	private BigDecimal durchschnittKinderProTagNachmittag1;

	@Nullable
	private BigDecimal durchschnittKinderProTagNachmittag2;

	// C: Qualitative Vorgaben der Tagesschuleverordnung

	@Nullable
	private Boolean schuleAufBasisOrganisatorischesKonzept;

	@Nullable
	private Boolean schuleAufBasisPaedagogischesKonzept;

	@Nullable
	private Boolean raeumlicheVoraussetzungenEingehalten;

	@Nullable
	private Boolean betreuungsverhaeltnisEingehalten;

	@Nullable
	private Boolean ernaehrungsGrundsaetzeEingehalten;

	// Bemerkungen

	@Nullable
	private String bemerkungen;

	@Nullable
	public Boolean getLehrbetrieb() {
		return isLehrbetrieb;
	}

	public void setLehrbetrieb(@Nullable Boolean lehrbetrieb) {
		isLehrbetrieb = lehrbetrieb;
	}

	@Nullable
	public BigDecimal getAnzahlEingeschriebeneKinder() {
		return anzahlEingeschriebeneKinder;
	}

	public void setAnzahlEingeschriebeneKinder(@Nullable BigDecimal anzahlEingeschriebeneKinder) {
		this.anzahlEingeschriebeneKinder = anzahlEingeschriebeneKinder;
	}

	@Nullable
	public BigDecimal getAnzahlEingeschriebeneKinderKindergarten() {
		return anzahlEingeschriebeneKinderKindergarten;
	}

	public void setAnzahlEingeschriebeneKinderKindergarten(@Nullable BigDecimal anzahlEingeschriebeneKinderKindergarten) {
		this.anzahlEingeschriebeneKinderKindergarten = anzahlEingeschriebeneKinderKindergarten;
	}

	@Nullable
	public BigDecimal getAnzahlEingeschriebeneKinderBasisstufe() {
		return anzahlEingeschriebeneKinderBasisstufe;
	}

	public void setAnzahlEingeschriebeneKinderBasisstufe(@Nullable BigDecimal anzahlEingeschriebeneKinderBasisstufe) {
		this.anzahlEingeschriebeneKinderBasisstufe = anzahlEingeschriebeneKinderBasisstufe;
	}

	@Nullable
	public BigDecimal getAnzahlEingeschriebeneKinderPrimarstufe() {
		return anzahlEingeschriebeneKinderPrimarstufe;
	}

	public void setAnzahlEingeschriebeneKinderPrimarstufe(@Nullable BigDecimal anzahlEingeschriebeneKinderPrimarstufe) {
		this.anzahlEingeschriebeneKinderPrimarstufe = anzahlEingeschriebeneKinderPrimarstufe;
	}

	@Nullable
	public BigDecimal getAnzahlEingeschriebeneKinderMitBesonderenBeduerfnissen() {
		return anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen;
	}

	public void setAnzahlEingeschriebeneKinderMitBesonderenBeduerfnissen(@Nullable BigDecimal anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen) {
		this.anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen = anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen;
	}

	@Nullable
	public BigDecimal getDurchschnittKinderProTagFruehbetreuung() {
		return durchschnittKinderProTagFruehbetreuung;
	}

	public void setDurchschnittKinderProTagFruehbetreuung(@Nullable BigDecimal durchschnittKinderProTagFruehbetreuung) {
		this.durchschnittKinderProTagFruehbetreuung = durchschnittKinderProTagFruehbetreuung;
	}

	@Nullable
	public BigDecimal getDurchschnittKinderProTagMittag() {
		return durchschnittKinderProTagMittag;
	}

	public void setDurchschnittKinderProTagMittag(@Nullable BigDecimal durchschnittKinderProTagMittag) {
		this.durchschnittKinderProTagMittag = durchschnittKinderProTagMittag;
	}

	@Nullable
	public BigDecimal getDurchschnittKinderProTagNachmittag1() {
		return durchschnittKinderProTagNachmittag1;
	}

	public void setDurchschnittKinderProTagNachmittag1(@Nullable BigDecimal durchschnittKinderProTagNachmittag1) {
		this.durchschnittKinderProTagNachmittag1 = durchschnittKinderProTagNachmittag1;
	}

	@Nullable
	public BigDecimal getDurchschnittKinderProTagNachmittag2() {
		return durchschnittKinderProTagNachmittag2;
	}

	public void setDurchschnittKinderProTagNachmittag2(@Nullable BigDecimal durchschnittKinderProTagNachmittag2) {
		this.durchschnittKinderProTagNachmittag2 = durchschnittKinderProTagNachmittag2;
	}

	@Nullable
	public Boolean getSchuleAufBasisOrganisatorischesKonzept() {
		return schuleAufBasisOrganisatorischesKonzept;
	}

	public void setSchuleAufBasisOrganisatorischesKonzept(@Nullable Boolean schuleAufBasisOrganisatorischesKonzept) {
		this.schuleAufBasisOrganisatorischesKonzept = schuleAufBasisOrganisatorischesKonzept;
	}

	@Nullable
	public Boolean getSchuleAufBasisPaedagogischesKonzept() {
		return schuleAufBasisPaedagogischesKonzept;
	}

	public void setSchuleAufBasisPaedagogischesKonzept(@Nullable Boolean schuleAufBasisPaedagogischesKonzept) {
		this.schuleAufBasisPaedagogischesKonzept = schuleAufBasisPaedagogischesKonzept;
	}

	@Nullable
	public Boolean getRaeumlicheVoraussetzungenEingehalten() {
		return raeumlicheVoraussetzungenEingehalten;
	}

	public void setRaeumlicheVoraussetzungenEingehalten(@Nullable Boolean raeumlicheVoraussetzungenEingehalten) {
		this.raeumlicheVoraussetzungenEingehalten = raeumlicheVoraussetzungenEingehalten;
	}

	@Nullable
	public Boolean getBetreuungsverhaeltnisEingehalten() {
		return betreuungsverhaeltnisEingehalten;
	}

	public void setBetreuungsverhaeltnisEingehalten(@Nullable Boolean betreuungsverhaeltnisEingehalten) {
		this.betreuungsverhaeltnisEingehalten = betreuungsverhaeltnisEingehalten;
	}

	@Nullable
	public Boolean getErnaehrungsGrundsaetzeEingehalten() {
		return ernaehrungsGrundsaetzeEingehalten;
	}

	public void setErnaehrungsGrundsaetzeEingehalten(@Nullable Boolean ernaehrungsGrundsaetzeEingehalten) {
		this.ernaehrungsGrundsaetzeEingehalten = ernaehrungsGrundsaetzeEingehalten;
	}

	@Nullable
	public String getBemerkungen() {
		return bemerkungen;
	}

	public void setBemerkungen(@Nullable String bemerkungen) {
		this.bemerkungen = bemerkungen;
	}
}
