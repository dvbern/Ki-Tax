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

package ch.dvbern.ebegu.api.dtos;

import java.util.Collection;
import java.util.LinkedHashSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.Kinderabzug;

/**
 * DTO fuer Stammdaten der Kinder
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxKind extends JaxAbstractPersonDTO {

	private static final long serialVersionUID = -1297026881674137397L;

	@Nullable
	private Geschlecht geschlecht;

	@Nullable
	private Kinderabzug kinderabzugErstesHalbjahr;

	@Nullable
	private Kinderabzug kinderabzugZweitesHalbjahr;

	@Nullable
	private Boolean isPflegekind;

	@Nullable
	private Boolean pflegeEntschaedigungErhalten;

	@Nullable
	private Boolean obhutAlternierendAusueben;

	@Nullable
	private Boolean gemeinsamesGesuch;

	@Nullable
	private Boolean inErstausbildung;

	@Nullable
	private Boolean lebtKindAlternierend;

	@Nullable
	private Boolean alimenteErhalten;

	@Nullable
	private Boolean alimenteBezahlen;

	@NotNull
	private Boolean familienErgaenzendeBetreuung;

	@Nullable
	private Boolean sprichtAmtssprache;

	@Nullable
	private EinschulungTyp einschulungTyp;

	@Nonnull
	private Boolean keinPlatzInSchulhort = false;

	@Nonnull
	private Collection<@Valid JaxPensumFachstelle> pensumFachstellen = new LinkedHashSet<>();

	@Nullable
	private JaxPensumAusserordentlicherAnspruch pensumAusserordentlicherAnspruch;

	@Nullable
	private Boolean ausAsylwesen;

	@Nullable
	private String zemisNummer;

	@NotNull
	private Boolean zukunftigeGeburtsdatum = false;

	@Nonnull
	private Boolean inPruefung = false;

	@Nullable
	private Boolean unterhaltspflichtig;

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

	@Nullable
	public Boolean getPflegekind() {
		return isPflegekind;
	}

	public void setPflegekind(@Nullable Boolean pflegekind) {
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


	@Nonnull
	public Collection<JaxPensumFachstelle> getPensumFachstellen() {
		return pensumFachstellen;
	}

	public void setPensumFachstellen(@Nonnull Collection<JaxPensumFachstelle> pensumFachstellen) {
		this.pensumFachstellen = pensumFachstellen;
	}

	@Nullable
	public EinschulungTyp getEinschulungTyp() {
		return einschulungTyp;
	}

	public void setEinschulungTyp(@Nullable EinschulungTyp einschulungTyp) {
		this.einschulungTyp = einschulungTyp;
	}

	@Nullable
	public JaxPensumAusserordentlicherAnspruch getPensumAusserordentlicherAnspruch() {
		return pensumAusserordentlicherAnspruch;
	}

	public void setPensumAusserordentlicherAnspruch(
		@Nullable JaxPensumAusserordentlicherAnspruch pensumAusserordentlicherAnspruch) {
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

	public Boolean getZukunftigeGeburtsdatum() {
		return zukunftigeGeburtsdatum;
	}

	public void setZukunftigeGeburtsdatum(Boolean zukunftigeGeburtsdatum) {
		this.zukunftigeGeburtsdatum = zukunftigeGeburtsdatum;
	}

	@Nonnull
	public Boolean getInPruefung() {
		return inPruefung;
	}

	public void setInPruefung(@Nonnull Boolean inPruefung) {
		this.inPruefung = inPruefung;
	}

	@Nonnull
	public Boolean getKeinPlatzInSchulhort() {
		return keinPlatzInSchulhort;
	}

	public void setKeinPlatzInSchulhort(@Nonnull Boolean keinPlatzInSchulhort) {
		this.keinPlatzInSchulhort = keinPlatzInSchulhort;
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
	public Boolean getUnterhaltspflichtig() {
		return unterhaltspflichtig;
	}

	public void setUnterhaltspflichtig(@Nullable Boolean unterhaltspflichtig) {
		this.unterhaltspflichtig = unterhaltspflichtig;
	}
}
