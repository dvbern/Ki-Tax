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
	public final Boolean getLehrbetrieb() {
		return isLehrbetrieb;
	}

	public final void setLehrbetrieb(@Nonnull Boolean lehrbetrieb) {
		isLehrbetrieb = lehrbetrieb;
	}

	@Nonnull
	public final BigDecimal getAnzahlEingeschriebeneKinder() {
		return anzahlEingeschriebeneKinder;
	}

	public final void setAnzahlEingeschriebeneKinder(@Nonnull BigDecimal anzahlEingeschriebeneKinder) {
		this.anzahlEingeschriebeneKinder = anzahlEingeschriebeneKinder;
	}

	@Nonnull
	public final BigDecimal getAnzahlEingeschriebeneKinderKindergarten() {
		return anzahlEingeschriebeneKinderKindergarten;
	}

	public final void setAnzahlEingeschriebeneKinderKindergarten(@Nonnull BigDecimal anzahlEingeschriebeneKinderKindergarten) {
		this.anzahlEingeschriebeneKinderKindergarten = anzahlEingeschriebeneKinderKindergarten;
	}

	@Nonnull
	public final BigDecimal getAnzahlEingeschriebeneKinderBasisstufe() {
		return anzahlEingeschriebeneKinderBasisstufe;
	}

	public final void setAnzahlEingeschriebeneKinderBasisstufe(@Nonnull BigDecimal anzahlEingeschriebeneKinderBasisstufe) {
		this.anzahlEingeschriebeneKinderBasisstufe = anzahlEingeschriebeneKinderBasisstufe;
	}

	@Nonnull
	public final BigDecimal getAnzahlEingeschriebeneKinderPrimarstufe() {
		return anzahlEingeschriebeneKinderPrimarstufe;
	}

	public final void setAnzahlEingeschriebeneKinderPrimarstufe(@Nonnull BigDecimal anzahlEingeschriebeneKinderPrimarstufe) {
		this.anzahlEingeschriebeneKinderPrimarstufe = anzahlEingeschriebeneKinderPrimarstufe;
	}

	@Nonnull
	public final BigDecimal getAnzahlEingeschriebeneKinderMitBesonderenBeduerfnissen() {
		return anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen;
	}

	public final void setAnzahlEingeschriebeneKinderMitBesonderenBeduerfnissen(@Nonnull BigDecimal anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen) {
		this.anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen = anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen;
	}

	@Nonnull
	public final BigDecimal getDurchschnittKinderProTagFruehbetreuung() {
		return durchschnittKinderProTagFruehbetreuung;
	}

	public final void setDurchschnittKinderProTagFruehbetreuung(@Nonnull BigDecimal durchschnittKinderProTagFruehbetreuung) {
		this.durchschnittKinderProTagFruehbetreuung = durchschnittKinderProTagFruehbetreuung;
	}

	@Nonnull
	public final BigDecimal getDurchschnittKinderProTagMittag() {
		return durchschnittKinderProTagMittag;
	}

	public final void setDurchschnittKinderProTagMittag(@Nonnull BigDecimal durchschnittKinderProTagMittag) {
		this.durchschnittKinderProTagMittag = durchschnittKinderProTagMittag;
	}

	@Nonnull
	public final BigDecimal getDurchschnittKinderProTagNachmittag1() {
		return durchschnittKinderProTagNachmittag1;
	}

	public final void setDurchschnittKinderProTagNachmittag1(@Nonnull BigDecimal durchschnittKinderProTagNachmittag1) {
		this.durchschnittKinderProTagNachmittag1 = durchschnittKinderProTagNachmittag1;
	}

	@Nonnull
	public final BigDecimal getDurchschnittKinderProTagNachmittag2() {
		return durchschnittKinderProTagNachmittag2;
	}

	public final void setDurchschnittKinderProTagNachmittag2(@Nonnull BigDecimal durchschnittKinderProTagNachmittag2) {
		this.durchschnittKinderProTagNachmittag2 = durchschnittKinderProTagNachmittag2;
	}

	@Nonnull
	public final Boolean getSchuleAufBasisOrganisatorischesKonzept() {
		return schuleAufBasisOrganisatorischesKonzept;
	}

	public final void setSchuleAufBasisOrganisatorischesKonzept(@Nonnull Boolean schuleAufBasisOrganisatorischesKonzept) {
		this.schuleAufBasisOrganisatorischesKonzept = schuleAufBasisOrganisatorischesKonzept;
	}

	@Nonnull
	public final Boolean getSchuleAufBasisPaedagogischesKonzept() {
		return schuleAufBasisPaedagogischesKonzept;
	}

	public final void setSchuleAufBasisPaedagogischesKonzept(@Nonnull Boolean schuleAufBasisPaedagogischesKonzept) {
		this.schuleAufBasisPaedagogischesKonzept = schuleAufBasisPaedagogischesKonzept;
	}

	@Nonnull
	public final Boolean getRaeumlicheVoraussetzungenEingehalten() {
		return raeumlicheVoraussetzungenEingehalten;
	}

	public final void setRaeumlicheVoraussetzungenEingehalten(@Nonnull Boolean raeumlicheVoraussetzungenEingehalten) {
		this.raeumlicheVoraussetzungenEingehalten = raeumlicheVoraussetzungenEingehalten;
	}

	@Nonnull
	public final Boolean getBetreuungsverhaeltnisEingehalten() {
		return betreuungsverhaeltnisEingehalten;
	}

	public final void setBetreuungsverhaeltnisEingehalten(@Nonnull Boolean betreuungsverhaeltnisEingehalten) {
		this.betreuungsverhaeltnisEingehalten = betreuungsverhaeltnisEingehalten;
	}

	@Nonnull
	public final Boolean getErnaehrungsGrundsaetzeEingehalten() {
		return ernaehrungsGrundsaetzeEingehalten;
	}

	public final void setErnaehrungsGrundsaetzeEingehalten(@Nonnull Boolean ernaehrungsGrundsaetzeEingehalten) {
		this.ernaehrungsGrundsaetzeEingehalten = ernaehrungsGrundsaetzeEingehalten;
	}

	@Nullable
	public final String getBemerkungen() {
		return bemerkungen;
	}

	public final void setBemerkungen(@Nullable String bemerkungen) {
		this.bemerkungen = bemerkungen;
	}

	@Override
	public final boolean isSame(AbstractEntity other) {
		return getId().equals(other.getId());
	}
}
