/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import ch.dvbern.ebegu.dto.neskovanp.Veranlagungsstand;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;

@Entity
public class SteuerdatenResponse extends AbstractEntity {

	private static final long serialVersionUID = -730016129801497214L;

	@Nullable
	@Column(nullable = true)
	private Integer zpvNrAntragsteller;

	@Nullable
	@Column(nullable = true)
	private LocalDate geburtsdatumAntragsteller;

	@Nullable
	@Column(nullable = true)
	private String kibonAntragId;

	@Nullable
	@Column(nullable = true)
	private Integer beginnGesuchsperiode;

	@Nullable
	@Column(nullable = true)
	private Integer zpvNrDossiertraeger;

	@Nullable
	@Column(nullable = true)
	private LocalDate geburtsdatumDossiertraeger;

	@Nullable
	@Column(nullable = true)
	private Integer zpvNrPartner;

	@Nullable
	@Column(nullable = true)
	private LocalDate geburtsdatumPartner;

	@Nullable
	@Column(nullable = true)
	private Integer fallId;

	@Nullable
	@Column(nullable = true)
	private LocalDate antwortdatum;

	@Nullable
	@Column(nullable = true)
	private Boolean synchroneAntwort;

	@Nullable
	@Column(nullable = true)
	@Enumerated(EnumType.STRING)
	private Veranlagungsstand veranlagungsstand;

	@Nullable
	@Column(nullable = true)
	private Boolean unterjaehrigerFall;

	@Nullable
	@Column(nullable = true)
	private BigDecimal erwerbseinkommenUnselbstaendigkeitDossiertraeger;

	@Nullable
	@Column(nullable = true)
	private BigDecimal erwerbseinkommenUnselbstaendigkeitPartner;

	@Nullable
	@Column(nullable = true)
	private BigDecimal steuerpflichtigesErsatzeinkommenDossiertraeger;

	@Nullable
	@Column(nullable = true)
	private BigDecimal steuerpflichtigesErsatzeinkommenPartner;

	@Nullable
	@Column(nullable = true)
	private BigDecimal erhalteneUnterhaltsbeitraegeDossiertraeger;

	@Nullable
	@Column(nullable = true)
	private BigDecimal erhalteneUnterhaltsbeitraegePartner;

	@Nullable
	@Column(nullable = true)
	private BigDecimal ausgewiesenerGeschaeftsertragDossiertraeger;

	@Nullable
	@Column(nullable = true)
	private BigDecimal ausgewiesenerGeschaeftsertragPartner;

	@Nullable
	@Column(nullable = true)
	private BigDecimal ausgewiesenerGeschaeftsertragVorperiodeDossiertraeger;

	@Nullable
	@Column(nullable = true)
	private BigDecimal ausgewiesenerGeschaeftsertragVorperiodePartner;

	@Nullable
	@Column(nullable = true)
	private BigDecimal ausgewiesenerGeschaeftsertragVorperiode2Dossiertraeger;

	@Nullable
	@Column(nullable = true)
	private BigDecimal ausgewiesenerGeschaeftsertragVorperiode2Partner;

	@Nullable
	@Column(nullable = true)
	private BigDecimal weitereSteuerbareEinkuenfteDossiertraeger;

	@Nullable
	@Column(nullable = true)
	private BigDecimal weitereSteuerbareEinkuenftePartner;

	@Nullable
	@Column(nullable = true)
	private BigDecimal bruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEgme;

	@Nullable
	@Column(nullable = true)
	private BigDecimal bruttoertraegeAusLiegenschaften;

	@Nullable
	@Column(nullable = true)
	private BigDecimal nettoertraegeAusEgmeDossiertraeger;

	@Nullable
	@Column(nullable = true)
	private BigDecimal nettoertraegeAusEgmePartner;

	@Nullable
	@Column(nullable = true)
	private BigDecimal geleisteteUnterhaltsbeitraege;

	@Nullable
	@Column(nullable = true)
	private BigDecimal schuldzinsen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal gewinnungskostenBeweglichesVermoegen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal liegenschaftsAbzuege;

	@Nullable
	@Column(nullable = true)
	private BigDecimal nettovermoegen;

	@Nullable
	public Integer getZpvNrAntragsteller() {
		return zpvNrAntragsteller;
	}

	public void setZpvNrAntragsteller(@Nullable Integer zpvNrAntragsteller) {
		this.zpvNrAntragsteller = zpvNrAntragsteller;
	}

	@Nullable
	public String getKibonAntragId() {
		return kibonAntragId;
	}

	public void setKiBonAntragId(@Nullable String kibonAntragId) {
		this.kibonAntragId = kibonAntragId;
	}

	@Nullable
	public Integer getBeginnGesuchsperiode() {
		return beginnGesuchsperiode;
	}

	public void setBeginnGesuchsperiode(@Nullable Integer beginnGesuchsperiode) {
		this.beginnGesuchsperiode = beginnGesuchsperiode;
	}

	@Nullable
	public Integer getZpvNrDossiertraeger() {
		return zpvNrDossiertraeger;
	}

	public void setZpvNrDossiertraeger(@Nullable Integer zpvNrDossiertraeger) {
		this.zpvNrDossiertraeger = zpvNrDossiertraeger;
	}

	@Nullable
	public Integer getZpvNrPartner() {
		return zpvNrPartner;
	}

	public void setZpvNrPartner(@Nullable Integer zpvNrPartner) {
		this.zpvNrPartner = zpvNrPartner;
	}

	@Nullable
	public Integer getFallId() {
		return fallId;
	}

	public void setFallId(@Nullable Integer fallId) {
		this.fallId = fallId;
	}

	@Nullable
	public Boolean getSynchroneAntwort() {
		return synchroneAntwort;
	}

	public void setSynchroneAntwort(@Nullable Boolean synchroneAntwort) {
		this.synchroneAntwort = synchroneAntwort;
	}

	@Nullable
	public Veranlagungsstand getVeranlagungsstand() {
		return veranlagungsstand;
	}

	public void setVeranlagungsstand(@Nullable Veranlagungsstand veranlagungsstand) {
		this.veranlagungsstand = veranlagungsstand;
	}

	@Nullable
	public Boolean getUnterjaehrigerFall() {
		return unterjaehrigerFall;
	}

	public void setUnterjaehrigerFall(@Nullable Boolean unterjaehrigerFall) {
		this.unterjaehrigerFall = unterjaehrigerFall;
	}

	@Nullable
	public BigDecimal getErwerbseinkommenUnselbstaendigkeitDossiertraeger() {
		return erwerbseinkommenUnselbstaendigkeitDossiertraeger;
	}

	public void setErwerbseinkommenUnselbstaendigkeitDossiertraeger(
		@Nullable BigDecimal erwerbseinkommenUnselbstaendigkeitDossiertraeger) {
		this.erwerbseinkommenUnselbstaendigkeitDossiertraeger = erwerbseinkommenUnselbstaendigkeitDossiertraeger;
	}

	@Nullable
	public BigDecimal getErwerbseinkommenUnselbstaendigkeitPartner() {
		return erwerbseinkommenUnselbstaendigkeitPartner;
	}

	public void setErwerbseinkommenUnselbstaendigkeitPartner(
		@Nullable BigDecimal erwerbseinkommenUnselbstaendigkeitPartner) {
		this.erwerbseinkommenUnselbstaendigkeitPartner = erwerbseinkommenUnselbstaendigkeitPartner;
	}

	@Nullable
	public BigDecimal getSteuerpflichtigesErsatzeinkommenDossiertraeger() {
		return steuerpflichtigesErsatzeinkommenDossiertraeger;
	}

	public void setSteuerpflichtigesErsatzeinkommenDossiertraeger(
		@Nullable BigDecimal steuerpflichtigesErsatzeinkommenDossiertraeger) {
		this.steuerpflichtigesErsatzeinkommenDossiertraeger = steuerpflichtigesErsatzeinkommenDossiertraeger;
	}

	@Nullable
	public BigDecimal getSteuerpflichtigesErsatzeinkommenPartner() {
		return steuerpflichtigesErsatzeinkommenPartner;
	}

	public void setSteuerpflichtigesErsatzeinkommenPartner(
		@Nullable BigDecimal steuerpflichtigesErsatzeinkommenPartner) {
		this.steuerpflichtigesErsatzeinkommenPartner = steuerpflichtigesErsatzeinkommenPartner;
	}

	@Nullable
	public BigDecimal getErhalteneUnterhaltsbeitraegeDossiertraeger() {
		return erhalteneUnterhaltsbeitraegeDossiertraeger;
	}

	public void setErhalteneUnterhaltsbeitraegeDossiertraeger(
		@Nullable BigDecimal erhalteneUnterhaltsbeitraegeDossiertraeger) {
		this.erhalteneUnterhaltsbeitraegeDossiertraeger = erhalteneUnterhaltsbeitraegeDossiertraeger;
	}

	@Nullable
	public BigDecimal getErhalteneUnterhaltsbeitraegePartner() {
		return erhalteneUnterhaltsbeitraegePartner;
	}

	public void setErhalteneUnterhaltsbeitraegePartner(@Nullable BigDecimal erhalteneUnterhaltsbeitraegePartner) {
		this.erhalteneUnterhaltsbeitraegePartner = erhalteneUnterhaltsbeitraegePartner;
	}

	@Nullable
	public BigDecimal getAusgewiesenerGeschaeftsertragDossiertraeger() {
		return ausgewiesenerGeschaeftsertragDossiertraeger;
	}

	public void setAusgewiesenerGeschaeftsertragDossiertraeger(
		@Nullable BigDecimal ausgewiesenerGeschaeftsertragDossiertraeger) {
		this.ausgewiesenerGeschaeftsertragDossiertraeger = ausgewiesenerGeschaeftsertragDossiertraeger;
	}

	@Nullable
	public BigDecimal getAusgewiesenerGeschaeftsertragPartner() {
		return ausgewiesenerGeschaeftsertragPartner;
	}

	public void setAusgewiesenerGeschaeftsertragPartner(@Nullable BigDecimal ausgewiesenerGeschaeftsertragPartner) {
		this.ausgewiesenerGeschaeftsertragPartner = ausgewiesenerGeschaeftsertragPartner;
	}

	@Nullable
	public BigDecimal getAusgewiesenerGeschaeftsertragVorperiodeDossiertraeger() {
		return ausgewiesenerGeschaeftsertragVorperiodeDossiertraeger;
	}

	public void setAusgewiesenerGeschaeftsertragVorperiodeDossiertraeger(
		@Nullable BigDecimal ausgewiesenerGeschaeftsertragVorperiodeDossiertraeger) {
		this.ausgewiesenerGeschaeftsertragVorperiodeDossiertraeger =
			ausgewiesenerGeschaeftsertragVorperiodeDossiertraeger;
	}

	@Nullable
	public BigDecimal getAusgewiesenerGeschaeftsertragVorperiodePartner() {
		return ausgewiesenerGeschaeftsertragVorperiodePartner;
	}

	public void setAusgewiesenerGeschaeftsertragVorperiodePartner(
		@Nullable BigDecimal ausgewiesenerGeschaeftsertragVorperiodePartner) {
		this.ausgewiesenerGeschaeftsertragVorperiodePartner = ausgewiesenerGeschaeftsertragVorperiodePartner;
	}

	@Nullable
	public BigDecimal getAusgewiesenerGeschaeftsertragVorperiode2Dossiertraeger() {
		return ausgewiesenerGeschaeftsertragVorperiode2Dossiertraeger;
	}

	public void setAusgewiesenerGeschaeftsertragVorperiode2Dossiertraeger(
		@Nullable BigDecimal ausgewiesenerGeschaeftsertragVorperiode2Dossiertraeger) {
		this.ausgewiesenerGeschaeftsertragVorperiode2Dossiertraeger =
			ausgewiesenerGeschaeftsertragVorperiode2Dossiertraeger;
	}

	@Nullable
	public BigDecimal getAusgewiesenerGeschaeftsertragVorperiode2Partner() {
		return ausgewiesenerGeschaeftsertragVorperiode2Partner;
	}

	public void setAusgewiesenerGeschaeftsertragVorperiode2Partner(
		@Nullable BigDecimal ausgewiesenerGeschaeftsertragVorperiode2Partner) {
		this.ausgewiesenerGeschaeftsertragVorperiode2Partner = ausgewiesenerGeschaeftsertragVorperiode2Partner;
	}

	@Nullable
	public BigDecimal getWeitereSteuerbareEinkuenfteDossiertraeger() {
		return weitereSteuerbareEinkuenfteDossiertraeger;
	}

	public void setWeitereSteuerbareEinkuenfteDossiertraeger(
		@Nullable BigDecimal weitereSteuerbareEinkuenfteDossiertraeger) {
		this.weitereSteuerbareEinkuenfteDossiertraeger = weitereSteuerbareEinkuenfteDossiertraeger;
	}

	@Nullable
	public BigDecimal getWeitereSteuerbareEinkuenftePartner() {
		return weitereSteuerbareEinkuenftePartner;
	}

	public void setWeitereSteuerbareEinkuenftePartner(@Nullable BigDecimal weitereSteuerbareEinkuenftePartner) {
		this.weitereSteuerbareEinkuenftePartner = weitereSteuerbareEinkuenftePartner;
	}

	@Nullable
	public BigDecimal getBruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEgme() {
		return bruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEgme;
	}

	public void setBruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEgme(
		@Nullable BigDecimal bruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEGME) {
		this.bruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEgme =
			bruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEGME;
	}

	@Nullable
	public BigDecimal getBruttoertraegeAusLiegenschaften() {
		return bruttoertraegeAusLiegenschaften;
	}

	public void setBruttoertraegeAusLiegenschaften(@Nullable BigDecimal bruttoertraegeAusLiegenschaften) {
		this.bruttoertraegeAusLiegenschaften = bruttoertraegeAusLiegenschaften;
	}

	@Nullable
	public BigDecimal getNettoertraegeAusEgmeDossiertraeger() {
		return nettoertraegeAusEgmeDossiertraeger;
	}

	public void setNettoertraegeAusEgmeDossiertraeger(@Nullable BigDecimal nettoertraegeAusEGMEDossiertraeger) {
		this.nettoertraegeAusEgmeDossiertraeger = nettoertraegeAusEGMEDossiertraeger;
	}

	@Nullable
	public BigDecimal getNettoertraegeAusEgmePartner() {
		return nettoertraegeAusEgmePartner;
	}

	public void setNettoertraegeAusEgmePartner(@Nullable BigDecimal nettoertraegeAusEGMEPartner) {
		this.nettoertraegeAusEgmePartner = nettoertraegeAusEGMEPartner;
	}

	@Nullable
	public BigDecimal getGeleisteteUnterhaltsbeitraege() {
		return geleisteteUnterhaltsbeitraege;
	}

	public void setGeleisteteUnterhaltsbeitraege(@Nullable BigDecimal geleisteteUnterhaltsbeitraege) {
		this.geleisteteUnterhaltsbeitraege = geleisteteUnterhaltsbeitraege;
	}

	@Nullable
	public BigDecimal getSchuldzinsen() {
		return schuldzinsen;
	}

	public void setSchuldzinsen(@Nullable BigDecimal schuldzinsen) {
		this.schuldzinsen = schuldzinsen;
	}

	@Nullable
	public BigDecimal getGewinnungskostenBeweglichesVermoegen() {
		return gewinnungskostenBeweglichesVermoegen;
	}

	public void setGewinnungskostenBeweglichesVermoegen(@Nullable BigDecimal gewinnungskostenBeweglichesVermoegen) {
		this.gewinnungskostenBeweglichesVermoegen = gewinnungskostenBeweglichesVermoegen;
	}

	@Nullable
	public BigDecimal getLiegenschaftsAbzuege() {
		return liegenschaftsAbzuege;
	}

	public void setLiegenschaftsAbzuege(@Nullable BigDecimal liegenschaftsAbzuege) {
		this.liegenschaftsAbzuege = liegenschaftsAbzuege;
	}

	@SuppressFBWarnings("NM_CONFUSING")
	@Nullable
	public BigDecimal getNettovermoegen() {
		return nettovermoegen;
	}

	@SuppressFBWarnings("NM_CONFUSING")
	public void setNettovermoegen(@Nullable BigDecimal nettovermoegen) {
		this.nettovermoegen = nettovermoegen;
	}

	@Nullable
	public LocalDate getGeburtsdatumAntragsteller() {
		return geburtsdatumAntragsteller;
	}

	public void setGeburtsdatumAntragsteller(@Nullable LocalDate geburtsdatumAntragsteller) {
		this.geburtsdatumAntragsteller = geburtsdatumAntragsteller;
	}

	@Nullable
	public LocalDate getGeburtsdatumDossiertraeger() {
		return geburtsdatumDossiertraeger;
	}

	public void setGeburtsdatumDossiertraeger(@Nullable LocalDate geburtsdatumDossiertraeger) {
		this.geburtsdatumDossiertraeger = geburtsdatumDossiertraeger;
	}

	@Nullable
	public LocalDate getAntwortdatum() {
		return antwortdatum;
	}

	public void setAntwortdatum(@Nullable LocalDate antwortdatum) {
		this.antwortdatum = antwortdatum;
	}

	@Nullable
	public LocalDate getGeburtsdatumPartner() {
		return geburtsdatumPartner;
	}

	public void setGeburtsdatumPartner(@Nullable LocalDate geburtsdatumPartner) {
		this.geburtsdatumPartner = geburtsdatumPartner;
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof SteuerdatenResponse)) {
			return false;
		}

		SteuerdatenResponse o = (SteuerdatenResponse) other;
		return Objects.equals(this.zpvNrAntragsteller, o.zpvNrAntragsteller) &&
			Objects.equals(this.geburtsdatumAntragsteller, o.geburtsdatumAntragsteller) &&
			StringUtils.equals(this.kibonAntragId, o.kibonAntragId) &&
			Objects.equals(this.beginnGesuchsperiode, o.beginnGesuchsperiode) &&
			Objects.equals(this.zpvNrDossiertraeger, o.zpvNrDossiertraeger) &&
			Objects.equals(this.geburtsdatumDossiertraeger, o.geburtsdatumDossiertraeger) &&
			Objects.equals(this.zpvNrPartner, o.zpvNrPartner) &&
			Objects.equals(this.geburtsdatumPartner, o.geburtsdatumPartner) &&
			Objects.equals(this.fallId, o.fallId) &&
			Objects.equals(this.synchroneAntwort, o.synchroneAntwort) &&
			this.veranlagungsstand == o.veranlagungsstand &&
			Objects.equals(this.unterjaehrigerFall, o.unterjaehrigerFall) &&
			isSame(this.erwerbseinkommenUnselbstaendigkeitDossiertraeger, o.erwerbseinkommenUnselbstaendigkeitDossiertraeger) &&
			isSame(this.erwerbseinkommenUnselbstaendigkeitPartner, o.erwerbseinkommenUnselbstaendigkeitPartner) &&
			isSame(this.steuerpflichtigesErsatzeinkommenDossiertraeger, o.steuerpflichtigesErsatzeinkommenDossiertraeger) &&
			isSame(this.steuerpflichtigesErsatzeinkommenPartner, o.steuerpflichtigesErsatzeinkommenPartner) &&
			isSame(this.erhalteneUnterhaltsbeitraegeDossiertraeger, o.erhalteneUnterhaltsbeitraegeDossiertraeger) &&
			isSame(this.erhalteneUnterhaltsbeitraegePartner, o.erhalteneUnterhaltsbeitraegePartner) &&
			isSame(this.ausgewiesenerGeschaeftsertragDossiertraeger, o.ausgewiesenerGeschaeftsertragDossiertraeger) &&
			isSame(this.ausgewiesenerGeschaeftsertragPartner, o.ausgewiesenerGeschaeftsertragPartner) &&
			isSame(this.ausgewiesenerGeschaeftsertragVorperiodeDossiertraeger, o.ausgewiesenerGeschaeftsertragVorperiodeDossiertraeger) &&
			isSame(this.ausgewiesenerGeschaeftsertragVorperiodePartner, o.ausgewiesenerGeschaeftsertragVorperiodePartner) &&
			isSame(this.ausgewiesenerGeschaeftsertragVorperiode2Dossiertraeger, o.ausgewiesenerGeschaeftsertragVorperiode2Dossiertraeger) &&
			isSame(this.ausgewiesenerGeschaeftsertragVorperiode2Partner, o.ausgewiesenerGeschaeftsertragVorperiode2Partner) &&
			isSame(this.weitereSteuerbareEinkuenfteDossiertraeger, o.weitereSteuerbareEinkuenfteDossiertraeger) &&
			isSame(this.weitereSteuerbareEinkuenftePartner, o.weitereSteuerbareEinkuenftePartner) &&
			isSame(this.bruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEgme, o.bruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEgme) &&
			isSame(this.bruttoertraegeAusLiegenschaften, o.bruttoertraegeAusLiegenschaften) &&
			isSame(this.nettoertraegeAusEgmeDossiertraeger, o.nettoertraegeAusEgmeDossiertraeger) &&
			isSame(this.nettoertraegeAusEgmePartner, o.nettoertraegeAusEgmePartner) &&
			isSame(this.geleisteteUnterhaltsbeitraege, o.geleisteteUnterhaltsbeitraege) &&
			isSame(this.schuldzinsen, o.schuldzinsen) &&
			isSame(this.gewinnungskostenBeweglichesVermoegen, o.gewinnungskostenBeweglichesVermoegen) &&
			isSame(this.liegenschaftsAbzuege, o.liegenschaftsAbzuege) &&
			isSame(this.nettovermoegen, o.nettovermoegen);
	}

	private boolean isSame(@Nullable BigDecimal that, @Nullable BigDecimal other) {
		if (that == null && other == null) {
			return true;
		}

		if(that == null) {
			return false;
		}

		return that.compareTo(other) == 0;
	}
}
