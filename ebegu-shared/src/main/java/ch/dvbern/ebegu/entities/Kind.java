/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.validators.CheckKinderabzug;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.annotations.SortNatural;
import org.hibernate.envers.Audited;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Entity fuer Kinder.
 */
@Audited
@Entity
@Table(
	indexes = @Index(columnList = "geburtsdatum", name = "IX_kind_geburtsdatum")
)
@EntityListeners({ AlleFaelleKindListener.class })
@CheckKinderabzug
public class Kind extends AbstractPersonEntity {

	private static final long serialVersionUID = -9032257320578372570L;

	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	@Nullable
	private Geschlecht geschlecht;

	@Column(nullable = true)
	@Nullable
	@Enumerated(EnumType.STRING)
	private Kinderabzug kinderabzugErstesHalbjahr;

	@Column(nullable = true)
	@Nullable
	@Enumerated(EnumType.STRING)
	private Kinderabzug kinderabzugZweitesHalbjahr;

	@Column(nullable = false)
	@Nonnull
	private Boolean isPflegekind = false;

	@Column(nullable = true)
	@Nullable
	private Boolean pflegeEntschaedigungErhalten;

	@Column(nullable = true)
	@Nullable
	private Boolean obhutAlternierendAusueben;

	@Column(nullable = true)
	@Nullable
	private Boolean gemeinsamesGesuch;

	@Column(nullable = true)
	@Nullable
	private Boolean inErstausbildung;

	@Column(nullable = true)
	@Nullable
	private Boolean lebtKindAlternierend;

	@Column(nullable = true)
	@Nullable
	private Boolean alimenteErhalten;

	@Column(nullable = true)
	@Nullable
	private Boolean alimenteBezahlen;

	@Column(nullable = false)
	@NotNull
	private Boolean familienErgaenzendeBetreuung = false;

	@Column(nullable = true)
	@Nullable
	private Boolean sprichtAmtssprache;

	@Column(nullable = true)
	@Nullable
	@Enumerated(EnumType.STRING)
	private EinschulungTyp einschulungTyp;

	@Column(nullable = false)
	@Nonnull
	private Boolean keinPlatzInSchulhort = false;

	@Valid
	@Nonnull
	@OneToMany(mappedBy = "kind", cascade = CascadeType.ALL, orphanRemoval = true)
	@SortNatural
	private Set<PensumFachstelle> pensumFachstelle = new TreeSet<>();

	@Valid
	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_kind_pensum_ausserordentlicheranspruch_id"), nullable = true)
	private PensumAusserordentlicherAnspruch pensumAusserordentlicherAnspruch;

	@Column(nullable = true)
	@Nullable
	private Boolean ausAsylwesen;

	@Column(nullable = true)
	@Nullable
	@Pattern(regexp = Constants.REGEX_ZEMIS, message = "{validator.constraints.zemis.message}")
	private String zemisNummer;

	@Column(nullable = false)
	@NotNull
	private Boolean zukunftigeGeburtsdatum = false;

	@Column(nullable = false)
	@NotNull
	@Nonnull
	private Boolean inPruefung = false;

	@Column(nullable = true)
	@Nullable
	private Boolean unterhaltspflichtig;

	public Kind() {
	}

	@Override
	@Nullable
	public Geschlecht getGeschlecht() {
		return geschlecht;
	}

	@Override
	public void setGeschlecht(@Nullable Geschlecht geschlecht) {
		this.geschlecht = geschlecht;
	}

	@Nullable
	public Kinderabzug getKinderabzugErstesHalbjahr() {
		return kinderabzugErstesHalbjahr;
	}

	public void setKinderabzugErstesHalbjahr(@Nullable Kinderabzug kinderabzugErstesHalbjahr) {
		this.kinderabzugErstesHalbjahr = kinderabzugErstesHalbjahr;
	}

	@Nullable
	public Kinderabzug getKinderabzugZweitesHalbjahr() {
		return kinderabzugZweitesHalbjahr;
	}

	public void setKinderabzugZweitesHalbjahr(@Nullable Kinderabzug kinderabzugZweitesHalbjahr) {
		this.kinderabzugZweitesHalbjahr = kinderabzugZweitesHalbjahr;
	}

	@Nonnull
	public Boolean getPflegekind() {
		return isPflegekind;
	}

	public void setPflegekind(@Nonnull Boolean pflegekind) {
		isPflegekind = pflegekind;
	}

	@Nullable
	public Boolean getPflegeEntschaedigungErhalten() {
		return pflegeEntschaedigungErhalten;
	}

	public void setPflegeEntschaedigungErhalten(@Nullable Boolean pflegeEntschaedigungErhalten) {
		this.pflegeEntschaedigungErhalten = pflegeEntschaedigungErhalten;
	}

	@Nullable
	public Boolean getObhutAlternierendAusueben() {
		return obhutAlternierendAusueben;
	}

	public void setObhutAlternierendAusueben(@Nullable Boolean obhutAlternierendAusueben) {
		this.obhutAlternierendAusueben = obhutAlternierendAusueben;
	}

	@Nullable
	public Boolean getGemeinsamesGesuch() {
		return gemeinsamesGesuch;
	}

	public void setGemeinsamesGesuch(@Nullable Boolean gemeinsamesGesuch) {
		this.gemeinsamesGesuch = gemeinsamesGesuch;
	}

	@Nullable
	public Boolean getInErstausbildung() {
		return inErstausbildung;
	}

	public void setInErstausbildung(@Nullable Boolean inErstausbildung) {
		this.inErstausbildung = inErstausbildung;
	}

	@Nullable
	public Boolean getLebtKindAlternierend() {
		return lebtKindAlternierend;
	}

	public void setLebtKindAlternierend(@Nullable Boolean lebtKindAlternierend) {
		this.lebtKindAlternierend = lebtKindAlternierend;
	}

	@Nullable
	public Boolean getAlimenteErhalten() {
		return alimenteErhalten;
	}

	public void setAlimenteErhalten(@Nullable Boolean alimenteErhalten) {
		this.alimenteErhalten = alimenteErhalten;
	}

	@Nullable
	public Boolean getAlimenteBezahlen() {
		return alimenteBezahlen;
	}

	public void setAlimenteBezahlen(@Nullable Boolean alimenteBezahlen) {
		this.alimenteBezahlen = alimenteBezahlen;
	}

	public Boolean getFamilienErgaenzendeBetreuung() {
		return familienErgaenzendeBetreuung;
	}

	public void setFamilienErgaenzendeBetreuung(Boolean familienErgaenzendeBetreuung) {
		this.familienErgaenzendeBetreuung = familienErgaenzendeBetreuung;
	}

	@Nullable
	public Boolean getSprichtAmtssprache() {
		return sprichtAmtssprache;
	}

	public void setSprichtAmtssprache(@Nullable Boolean sprichtAmtssprache) {
		this.sprichtAmtssprache = sprichtAmtssprache;
	}

	@Nullable
	public EinschulungTyp getEinschulungTyp() {
		return einschulungTyp;
	}

	public void setEinschulungTyp(@Nullable EinschulungTyp einschulungTyp) {
		this.einschulungTyp = einschulungTyp;
	}

	@Nonnull
	public Set<PensumFachstelle> getPensumFachstelle() {
		return pensumFachstelle;
	}

	public void setPensumFachstelle(@Nonnull Set<PensumFachstelle> pensumFachstelle) {
		this.pensumFachstelle = pensumFachstelle;
	}

	public void addPensumFachstelle(@Nonnull PensumFachstelle pensumFachstelle) {
		this.pensumFachstelle.add(pensumFachstelle);
	}

	@Nullable
	public PensumAusserordentlicherAnspruch getPensumAusserordentlicherAnspruch() {
		return pensumAusserordentlicherAnspruch;
	}

	public void setPensumAusserordentlicherAnspruch(
		@Nullable PensumAusserordentlicherAnspruch pensumAusserordentlicherAnspruch) {

		this.pensumAusserordentlicherAnspruch = pensumAusserordentlicherAnspruch;
	}

	@Nullable
	public Boolean getAusAsylwesen() {
		return ausAsylwesen;
	}

	public void setAusAsylwesen(@Nullable Boolean ausAsylwesen) {
		this.ausAsylwesen = ausAsylwesen;
	}

	@Nullable
	public String getZemisNummer() {
		return zemisNummer;
	}

	public void setZemisNummer(@Nullable String zemisNummer) {
		this.zemisNummer = zemisNummer;
	}

	@Nonnull
	public Boolean getInPruefung() {
		return inPruefung;
	}

	public void setInPruefung(@Nonnull Boolean inPruefung) {
		this.inPruefung = inPruefung;
	}

	@Nonnull
	public Kind copyKind(
		@Nonnull Kind target,
		@Nonnull AntragCopyType copyType,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull LocalDate regelStartDatum) {
		super.copyAbstractPersonEntity(target, copyType);
		target.setFamilienErgaenzendeBetreuung(this.getFamilienErgaenzendeBetreuung());
		target.setSprichtAmtssprache(this.getSprichtAmtssprache());
		target.setAusAsylwesen(this.getAusAsylwesen());
		target.setZemisNummer(this.getZemisNummer());

		switch (copyType) {
		case MUTATION:
			target.setEinschulungTyp(this.getEinschulungTyp());
			target.setKinderabzugErstesHalbjahr(this.getKinderabzugErstesHalbjahr());
			target.setKinderabzugZweitesHalbjahr(this.getKinderabzugZweitesHalbjahr());
			target.setPflegekind(this.getPflegekind());
			target.setPflegeEntschaedigungErhalten(this.getPflegeEntschaedigungErhalten());
			target.setObhutAlternierendAusueben(this.getObhutAlternierendAusueben());
			target.setGemeinsamesGesuch(this.getGemeinsamesGesuch());
			target.setInErstausbildung(this.getInErstausbildung());
			target.setLebtKindAlternierend(this.getLebtKindAlternierend());
			target.setAlimenteErhalten(this.getAlimenteErhalten());
			target.setAlimenteBezahlen(this.getAlimenteBezahlen());
			target.setZukunftigeGeburtsdatum(target.getGeburtsdatum().isAfter(regelStartDatum) ? true : false);
			target.setKeinPlatzInSchulhort(this.getKeinPlatzInSchulhort());
			target.setUnterhaltspflichtig(this.getUnterhaltspflichtig());
			copyFachstelle(target, copyType);
			copyAusserordentlicherAnspruch(target, copyType);
			break;
		case MUTATION_NEUES_DOSSIER:
			target.setEinschulungTyp(this.getEinschulungTyp());
			target.setKinderabzugErstesHalbjahr(this.getKinderabzugErstesHalbjahr());
			target.setKinderabzugZweitesHalbjahr(this.getKinderabzugZweitesHalbjahr());
			target.setPflegekind(this.getPflegekind());
			target.setPflegeEntschaedigungErhalten(this.getPflegeEntschaedigungErhalten());
			target.setObhutAlternierendAusueben(this.getObhutAlternierendAusueben());
			target.setGemeinsamesGesuch(this.getGemeinsamesGesuch());
			target.setInErstausbildung(this.getInErstausbildung());
			target.setLebtKindAlternierend(this.getLebtKindAlternierend());
			target.setAlimenteErhalten(this.getAlimenteErhalten());
			target.setAlimenteBezahlen(this.getAlimenteBezahlen());
			target.setUnterhaltspflichtig(this.getUnterhaltspflichtig());
			target.setZukunftigeGeburtsdatum(target.getGeburtsdatum().isAfter(regelStartDatum) ? true : false);
			copyFachstelleIfStillValid(target, copyType, gesuchsperiode);
			// Ausserordentlicher Anspruch wird nicht kopiert, auch wenn er noch gueltig waere.
			// Dieser liegt ja in der Kompetenz der Gemeinde und kann nicht uebernommen werden
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_AR_2023:
		case ERNEUERUNG_NEUES_DOSSIER:
			target.inPruefung = true;
			// Ausserordentlicher Anspruch wird nicht kopiert, auch wenn er noch gueltig waere.
			// Dieser muss immer neu beantragt werden!
			break;
		}
		return target;
	}

	private void copyFachstelle(@Nonnull Kind target, @Nonnull AntragCopyType copyType) {
		for (PensumFachstelle pensumFachstelle1 : this.getPensumFachstelle()) {
			copyFachstellePensum(target, copyType, pensumFachstelle1);
		}
	}

	private void copyFachstelleIfStillValid(
		@Nonnull Kind target,
		@Nonnull AntragCopyType copyType,
		@Nonnull Gesuchsperiode gesuchsperiode) {
		// Fachstelle nur kopieren, wenn sie noch gueltig ist
		for (PensumFachstelle pensumFachstelle1 : this.getPensumFachstelle()) {
			if (pensumFachstelle1
				.getGueltigkeit()
				.endsBefore(gesuchsperiode.getGueltigkeit().getGueltigAb())) {
				copyFachstellePensum(target, copyType, pensumFachstelle1);
			}
		}

	}

	private static void copyFachstellePensum(
		@Nonnull Kind target,
		@Nonnull AntragCopyType copyType,
		PensumFachstelle pensumFachstelle1) {
		PensumFachstelle pensumFachstelleCopy = pensumFachstelle1.copyPensumFachstelle(new PensumFachstelle(), copyType);
		pensumFachstelleCopy.setKind(target);
		target.addPensumFachstelle(pensumFachstelleCopy);
	}

	private void copyAusserordentlicherAnspruch(@Nonnull Kind target, @Nonnull AntragCopyType copyType) {
		if (this.getPensumAusserordentlicherAnspruch() != null) {
			target.setPensumAusserordentlicherAnspruch(this.getPensumAusserordentlicherAnspruch()
				.copyPensumAusserordentlicherAnspruch(new PensumAusserordentlicherAnspruch(), copyType));
		}
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!super.isSame(other)) {
			return false;
		}
		final Kind otherKind = (Kind) other;
		boolean sameFachstellen = isSameFachstellen(otherKind);
		return getKinderabzugErstesHalbjahr() == otherKind.getKinderabzugErstesHalbjahr() &&
			getKinderabzugZweitesHalbjahr() == otherKind.getKinderabzugZweitesHalbjahr() &&
			Objects.equals(getFamilienErgaenzendeBetreuung(), otherKind.getFamilienErgaenzendeBetreuung()) &&
			Objects.equals(getSprichtAmtssprache(), otherKind.getSprichtAmtssprache()) &&
			Objects.equals(getUnterhaltspflichtig(), otherKind.getUnterhaltspflichtig()) &&
			getEinschulungTyp() == otherKind.getEinschulungTyp() &&
			sameFachstellen &&
			EbeguUtil.isSame(
				getPensumAusserordentlicherAnspruch(),
				otherKind.getPensumAusserordentlicherAnspruch());
	}

	private boolean isSameFachstellen(Kind otherKind) {
		boolean sameFachstellen = true;
		if (getPensumFachstelle().size() != otherKind.getPensumFachstelle().size()) {
			sameFachstellen = false;
		}
		for (PensumFachstelle pensumFachstelle1 : pensumFachstelle) {
			if (!otherKind.getPensumFachstelle().contains(pensumFachstelle1)) {
				sameFachstellen = false;
				break;
			}
		}
		return sameFachstellen;
	}

	public boolean isGeprueft() {
		return !inPruefung;
	}

	public Boolean getZukunftigeGeburtsdatum() {
		return zukunftigeGeburtsdatum;
	}

	public void setZukunftigeGeburtsdatum(Boolean zukunftigeGeburtsdatum) {
		this.zukunftigeGeburtsdatum = zukunftigeGeburtsdatum;
	}

	@Nonnull
	public Boolean getKeinPlatzInSchulhort() {
		return keinPlatzInSchulhort;
	}

	public void setKeinPlatzInSchulhort(@Nonnull Boolean keinPlatzInSchulhort) {
		this.keinPlatzInSchulhort = keinPlatzInSchulhort;
	}

	@Nullable
	public Boolean getUnterhaltspflichtig() {
		return unterhaltspflichtig;
	}

	public void setUnterhaltspflichtig(@Nullable Boolean unterhaltspflichtig) {
		this.unterhaltspflichtig = unterhaltspflichtig;
	}
}
