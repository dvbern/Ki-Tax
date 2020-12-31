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

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.Kinderabzug;

/**
 * DTO fuer Stammdaten der Kinder
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxKind extends JaxAbstractPersonDTO {

	private static final long serialVersionUID = -1297026881674137397L;

	@NotNull
	private Kinderabzug kinderabzugErstesHalbjahr;

	@NotNull
	private Kinderabzug kinderabzugZweitesHalbjahr;

	@NotNull
	private Boolean familienErgaenzendeBetreuung;

	@Nullable
	private Boolean sprichtAmtssprache;

	@Nullable
	private EinschulungTyp einschulungTyp;

	@Nullable
	private JaxPensumFachstelle pensumFachstelle;

	@Nullable
	private JaxPensumAusserordentlicherAnspruch pensumAusserordentlicherAnspruch;

	@Nullable
	private Boolean ausAsylwesen;

	@Nullable
	private String zemisNummer;

	@NotNull
	private Boolean zukunftigeGeburtsdatum = false;

	@NotNull
	public Kinderabzug getKinderabzugErstesHalbjahr() {
		return kinderabzugErstesHalbjahr;
	}

	public void setKinderabzugErstesHalbjahr(Kinderabzug kinderabzugErstesHalbjahr) {
		this.kinderabzugErstesHalbjahr = kinderabzugErstesHalbjahr;
	}

	public Kinderabzug getKinderabzugZweitesHalbjahr() {
		return kinderabzugZweitesHalbjahr;
	}

	public void setKinderabzugZweitesHalbjahr(Kinderabzug kinderabzugZweitesHalbjahr) {
		this.kinderabzugZweitesHalbjahr = kinderabzugZweitesHalbjahr;
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
	public JaxPensumFachstelle getPensumFachstelle() {
		return pensumFachstelle;
	}

	public void setPensumFachstelle(@Nullable JaxPensumFachstelle pensumFachstelle) {
		this.pensumFachstelle = pensumFachstelle;
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
}
