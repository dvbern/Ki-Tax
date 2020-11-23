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

package ch.dvbern.ebegu.entities.gemeindeantrag;

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.entities.AbstractEntity;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

@Audited
@Entity
public class LastenausgleichTagesschuleAngabenInstitution extends AbstractEntity {

	private static final long serialVersionUID = 5750986272858969305L;

	// A: Informationen zur Tagesschule

	@NotNull @Nonnull
	@Column(nullable = false)
	private Boolean isLehrbetrieb;

	// B: Quantitative Angaben

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal anzahlEingeschriebeneKinder;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal anzahlEingeschriebeneKinderKindergarten;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal anzahlEingeschriebeneKinderBasisstufe;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal anzahlEingeschriebeneKinderPrimarstufe;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal durchschnittKinderProTagFruehbetreuung;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal durchschnittKinderProTagMittag;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal durchschnittKinderProTagNachmittag1;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal durchschnittKinderProTagNachmittag2;

	// C: Qualitative Vorgaben der Tagesschuleverordnung

	@NotNull @Nonnull
	@Column(nullable = false)
	private Boolean schuleAufBasisOrganisatorischesKonzept;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Boolean schuleAufBasisPaedagogischesKonzept;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Boolean raeumlicheVoraussetzungenEingehalten;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Boolean betreuungsverhaeltnisEingehalten;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Boolean ernaehrungsGrundsaetzeEingehalten;

	// Bemerkungen

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = true)
	private String bemerkungen;

	public LastenausgleichTagesschuleAngabenInstitution() {
	}

	public LastenausgleichTagesschuleAngabenInstitution(@Nonnull LastenausgleichTagesschuleAngabenInstitution source) {
		// A: Informationen zur Tagesschule
		this.setLehrbetrieb(source.getLehrbetrieb());
		// B: Quantitative Angaben
		this.setAnzahlEingeschriebeneKinder(source.getAnzahlEingeschriebeneKinder());
		this.setAnzahlEingeschriebeneKinderKindergarten(source.getAnzahlEingeschriebeneKinderKindergarten());
		this.setAnzahlEingeschriebeneKinderBasisstufe(source.getAnzahlEingeschriebeneKinderBasisstufe());
		this.setAnzahlEingeschriebeneKinderPrimarstufe(source.getAnzahlEingeschriebeneKinderPrimarstufe());
		this.setAnzahlEingeschriebeneKinderMitBesonderenBeduerfnissen(source.getAnzahlEingeschriebeneKinderMitBesonderenBeduerfnissen());
		this.setDurchschnittKinderProTagFruehbetreuung(source.getDurchschnittKinderProTagFruehbetreuung());
		this.setDurchschnittKinderProTagMittag(source.getDurchschnittKinderProTagMittag());
		this.setDurchschnittKinderProTagNachmittag1(source.getDurchschnittKinderProTagNachmittag1());
		this.setDurchschnittKinderProTagNachmittag2(source.getDurchschnittKinderProTagNachmittag2());
		// C: Qualitative Vorgaben der Tagesschuleverordnung
		this.setSchuleAufBasisOrganisatorischesKonzept(source.getSchuleAufBasisOrganisatorischesKonzept());
		this.setSchuleAufBasisPaedagogischesKonzept(source.getSchuleAufBasisPaedagogischesKonzept());
		this.setRaeumlicheVoraussetzungenEingehalten(source.getRaeumlicheVoraussetzungenEingehalten());
		this.setBetreuungsverhaeltnisEingehalten(source.getBetreuungsverhaeltnisEingehalten());
		this.setErnaehrungsGrundsaetzeEingehalten(source.getErnaehrungsGrundsaetzeEingehalten());
		// Bemerkungen
		this.setBemerkungen(source.getBemerkungen());
	}

	@Nonnull
	public Boolean getLehrbetrieb() {
		return isLehrbetrieb;
	}

	public void setLehrbetrieb(@Nonnull Boolean lehrbetrieb) {
		isLehrbetrieb = lehrbetrieb;
	}

	@Nonnull
	public BigDecimal getAnzahlEingeschriebeneKinder() {
		return anzahlEingeschriebeneKinder;
	}

	public void setAnzahlEingeschriebeneKinder(@Nonnull BigDecimal anzahlEingeschriebeneKinder) {
		this.anzahlEingeschriebeneKinder = anzahlEingeschriebeneKinder;
	}

	@Nonnull
	public BigDecimal getAnzahlEingeschriebeneKinderKindergarten() {
		return anzahlEingeschriebeneKinderKindergarten;
	}

	public void setAnzahlEingeschriebeneKinderKindergarten(@Nonnull BigDecimal anzahlEingeschriebeneKinderKindergarten) {
		this.anzahlEingeschriebeneKinderKindergarten = anzahlEingeschriebeneKinderKindergarten;
	}

	@Nonnull
	public BigDecimal getAnzahlEingeschriebeneKinderBasisstufe() {
		return anzahlEingeschriebeneKinderBasisstufe;
	}

	public void setAnzahlEingeschriebeneKinderBasisstufe(@Nonnull BigDecimal anzahlEingeschriebeneKinderBasisstufe) {
		this.anzahlEingeschriebeneKinderBasisstufe = anzahlEingeschriebeneKinderBasisstufe;
	}

	@Nonnull
	public BigDecimal getAnzahlEingeschriebeneKinderPrimarstufe() {
		return anzahlEingeschriebeneKinderPrimarstufe;
	}

	public void setAnzahlEingeschriebeneKinderPrimarstufe(@Nonnull BigDecimal anzahlEingeschriebeneKinderPrimarstufe) {
		this.anzahlEingeschriebeneKinderPrimarstufe = anzahlEingeschriebeneKinderPrimarstufe;
	}

	@Nonnull
	public BigDecimal getAnzahlEingeschriebeneKinderMitBesonderenBeduerfnissen() {
		return anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen;
	}

	public void setAnzahlEingeschriebeneKinderMitBesonderenBeduerfnissen(@Nonnull BigDecimal anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen) {
		this.anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen = anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen;
	}

	@Nonnull
	public BigDecimal getDurchschnittKinderProTagFruehbetreuung() {
		return durchschnittKinderProTagFruehbetreuung;
	}

	public void setDurchschnittKinderProTagFruehbetreuung(@Nonnull BigDecimal durchschnittKinderProTagFruehbetreuung) {
		this.durchschnittKinderProTagFruehbetreuung = durchschnittKinderProTagFruehbetreuung;
	}

	@Nonnull
	public BigDecimal getDurchschnittKinderProTagMittag() {
		return durchschnittKinderProTagMittag;
	}

	public void setDurchschnittKinderProTagMittag(@Nonnull BigDecimal durchschnittKinderProTagMittag) {
		this.durchschnittKinderProTagMittag = durchschnittKinderProTagMittag;
	}

	@Nonnull
	public BigDecimal getDurchschnittKinderProTagNachmittag1() {
		return durchschnittKinderProTagNachmittag1;
	}

	public void setDurchschnittKinderProTagNachmittag1(@Nonnull BigDecimal durchschnittKinderProTagNachmittag1) {
		this.durchschnittKinderProTagNachmittag1 = durchschnittKinderProTagNachmittag1;
	}

	@Nonnull
	public BigDecimal getDurchschnittKinderProTagNachmittag2() {
		return durchschnittKinderProTagNachmittag2;
	}

	public void setDurchschnittKinderProTagNachmittag2(@Nonnull BigDecimal durchschnittKinderProTagNachmittag2) {
		this.durchschnittKinderProTagNachmittag2 = durchschnittKinderProTagNachmittag2;
	}

	@Nonnull
	public Boolean getSchuleAufBasisOrganisatorischesKonzept() {
		return schuleAufBasisOrganisatorischesKonzept;
	}

	public void setSchuleAufBasisOrganisatorischesKonzept(@Nonnull Boolean schuleAufBasisOrganisatorischesKonzept) {
		this.schuleAufBasisOrganisatorischesKonzept = schuleAufBasisOrganisatorischesKonzept;
	}

	@Nonnull
	public Boolean getSchuleAufBasisPaedagogischesKonzept() {
		return schuleAufBasisPaedagogischesKonzept;
	}

	public void setSchuleAufBasisPaedagogischesKonzept(@Nonnull Boolean schuleAufBasisPaedagogischesKonzept) {
		this.schuleAufBasisPaedagogischesKonzept = schuleAufBasisPaedagogischesKonzept;
	}

	@Nonnull
	public Boolean getRaeumlicheVoraussetzungenEingehalten() {
		return raeumlicheVoraussetzungenEingehalten;
	}

	public void setRaeumlicheVoraussetzungenEingehalten(@Nonnull Boolean raeumlicheVoraussetzungenEingehalten) {
		this.raeumlicheVoraussetzungenEingehalten = raeumlicheVoraussetzungenEingehalten;
	}

	@Nonnull
	public Boolean getBetreuungsverhaeltnisEingehalten() {
		return betreuungsverhaeltnisEingehalten;
	}

	public void setBetreuungsverhaeltnisEingehalten(@Nonnull Boolean betreuungsverhaeltnisEingehalten) {
		this.betreuungsverhaeltnisEingehalten = betreuungsverhaeltnisEingehalten;
	}

	@Nonnull
	public Boolean getErnaehrungsGrundsaetzeEingehalten() {
		return ernaehrungsGrundsaetzeEingehalten;
	}

	public void setErnaehrungsGrundsaetzeEingehalten(@Nonnull Boolean ernaehrungsGrundsaetzeEingehalten) {
		this.ernaehrungsGrundsaetzeEingehalten = ernaehrungsGrundsaetzeEingehalten;
	}

	@Nullable
	public String getBemerkungen() {
		return bemerkungen;
	}

	public void setBemerkungen(@Nullable String bemerkungen) {
		this.bemerkungen = bemerkungen;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return getId().equals(other.getId());
	}
}
