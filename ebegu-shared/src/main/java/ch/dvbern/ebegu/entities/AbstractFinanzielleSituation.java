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

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.Min;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.util.MathUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.envers.Audited;

/**
 * Gemeinsame Basisklasse für FinanzielleSituation und Einkommensverschlechterung
 *
 * @author gapa
 * @version 1.0
 */
@Audited
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractFinanzielleSituation extends AbstractMutableEntity {

	private static final long serialVersionUID = 2596930494846119259L;

	@Nullable
	@Column(nullable = true)
	private BigDecimal bruttoLohn;

	@Nullable
	@Column(nullable = true)
	private BigDecimal nettolohn;

	@Nullable
	@Column(nullable = true)
	private BigDecimal familienzulage;

	@Nullable
	@Column(nullable = true)
	private BigDecimal ersatzeinkommen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal erhalteneAlimente;

	@Nullable
	@Column(nullable = true)
	private BigDecimal bruttovermoegen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal schulden;

	@Nullable
	@Column(nullable = true)
	private BigDecimal geschaeftsgewinnBasisjahr;

	@Nullable
	@Column(nullable = true)
	private BigDecimal geschaeftsgewinnBasisjahrMinus1;

	@Nullable
	@Column(nullable = true)
	private BigDecimal geleisteteAlimente;

	@Nullable
	@Column(nullable = true)
	private BigDecimal steuerbaresEinkommen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal steuerbaresVermoegen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzuegeLiegenschaft;

	@Nullable
	@Column(nullable = true)
	private BigDecimal geschaeftsverlust;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einkaeufeVorsorge;

	@Nullable
	@Column(nullable = true)
	private BigDecimal bruttoertraegeVermoegen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal nettoertraegeErbengemeinschaft;

	@Nullable
	@Column(nullable = true)
	private BigDecimal nettoVermoegen;

	@Nullable
	@Column(nullable = true)
	private Boolean einkommenInVereinfachtemVerfahrenAbgerechnet;

	@Nullable
	@Column(nullable = true)
	private BigDecimal amountEinkommenInVereinfachtemVerfahrenAbgerechnet;

	@Nullable
	@Column(nullable = true)
	private BigDecimal gewinnungskosten;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzugSchuldzinsen;

	@Nullable
	@Transient
	private BigDecimal durchschnittlicherGeschaeftsgewinn;

	@Nullable
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
	@JoinColumn(nullable = true)
	protected FinanzielleSituationSelbstdeklaration selbstdeklaration;

	@Nullable
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
	@JoinColumn(nullable = true)
	private FinSitZusatzangabenAppenzell finSitZusatzangabenAppenzell;

	@Nullable
	@Column(nullable = true)
	@Min(0)
	private BigDecimal ersatzeinkommenSelbststaendigkeitBasisjahr;

	@Nullable
	@Column(nullable = true)
	@Min(0)
	private BigDecimal ersatzeinkommenSelbststaendigkeitBasisjahrMinus1;

	public AbstractFinanzielleSituation() {
	}

	public abstract Boolean getSteuerveranlagungErhalten();

	public abstract Boolean getSteuererklaerungAusgefuellt();

	public abstract Boolean getSteuerdatenZugriff();

	public abstract SteuerdatenAnfrageStatus getSteuerdatenAbfrageStatus();

	@Nullable
	public BigDecimal getNettolohn() {
		return nettolohn;
	}

	public void setNettolohn(@Nullable final BigDecimal nettolohn) {
		this.nettolohn = nettolohn;
	}

	@Nullable
	public BigDecimal getFamilienzulage() {
		return familienzulage;
	}

	public void setFamilienzulage(@Nullable final BigDecimal familienzulage) {
		this.familienzulage = familienzulage;
	}

	@Nullable
	public BigDecimal getErsatzeinkommen() {
		return ersatzeinkommen;
	}

	public void setErsatzeinkommen(@Nullable final BigDecimal ersatzeinkommen) {
		this.ersatzeinkommen = ersatzeinkommen;
	}

	@Nullable
	public BigDecimal getErhalteneAlimente() {
		return erhalteneAlimente;
	}

	public void setErhalteneAlimente(@Nullable final BigDecimal erhalteneAlimente) {
		this.erhalteneAlimente = erhalteneAlimente;
	}

	@Nullable
	public BigDecimal getBruttovermoegen() {
		return bruttovermoegen;
	}

	public void setBruttovermoegen(@Nullable final BigDecimal bruttovermoegen) {
		this.bruttovermoegen = bruttovermoegen;
	}

	@Nullable
	public BigDecimal getSchulden() {
		return schulden;
	}

	public void setSchulden(@Nullable final BigDecimal schulden) {
		this.schulden = schulden;
	}

	@Nullable
	public BigDecimal getGeschaeftsgewinnBasisjahr() {
		return geschaeftsgewinnBasisjahr;
	}

	public void setGeschaeftsgewinnBasisjahr(@Nullable final BigDecimal geschaeftsgewinnBasisjahr) {
		this.geschaeftsgewinnBasisjahr = geschaeftsgewinnBasisjahr;
	}
	@Nullable
	public BigDecimal getGeschaeftsgewinnBasisjahrMinus1() {
		return geschaeftsgewinnBasisjahrMinus1;
	}

	public void setGeschaeftsgewinnBasisjahrMinus1(@Nullable BigDecimal geschaeftsgewinnBasisjahrMinus1) {
		this.geschaeftsgewinnBasisjahrMinus1 = geschaeftsgewinnBasisjahrMinus1;
	}
	@Nullable
	public BigDecimal getGeleisteteAlimente() {
		return geleisteteAlimente;
	}

	public void setGeleisteteAlimente(@Nullable final BigDecimal geleisteteAlimente) {
		this.geleisteteAlimente = geleisteteAlimente;
	}

	@Nullable
	public BigDecimal getDurchschnittlicherGeschaeftsgewinn() {
		return durchschnittlicherGeschaeftsgewinn;
	}

	public void setDurchschnittlicherGeschaeftsgewinn(@Nullable BigDecimal durchschnittlicherGeschaeftsgewinn) {
		this.durchschnittlicherGeschaeftsgewinn = durchschnittlicherGeschaeftsgewinn;
	}

	@Nullable
	public BigDecimal getSteuerbaresEinkommen() {
		if (this.getFinSitZusatzangabenAppenzell() != null) {
			return this.getFinSitZusatzangabenAppenzell().getSteuerbaresEinkommen();
		}
		return steuerbaresEinkommen;
	}

	public void setSteuerbaresEinkommen(@Nullable BigDecimal steuerbaresEinkommen) {
		if (this.getFinSitZusatzangabenAppenzell() != null) {
			this.getFinSitZusatzangabenAppenzell().setSteuerbaresEinkommen(steuerbaresEinkommen);
			return;
		}
		this.steuerbaresEinkommen = steuerbaresEinkommen;
	}

	@Nullable
	public BigDecimal getSteuerbaresVermoegen() {
		if (this.getFinSitZusatzangabenAppenzell() != null) {
			return this.getFinSitZusatzangabenAppenzell().getSteuerbaresVermoegen();
		}

		return steuerbaresVermoegen;
	}

	public void setSteuerbaresVermoegen(@Nullable BigDecimal steuerbaresVermoegen) {
		if (this.getFinSitZusatzangabenAppenzell() != null) {
			this.getFinSitZusatzangabenAppenzell().setSteuerbaresVermoegen(steuerbaresVermoegen);
			return;
		}
		this.steuerbaresVermoegen = steuerbaresVermoegen;
	}

	@Nullable
	public BigDecimal getAbzuegeLiegenschaft() {
		return abzuegeLiegenschaft;
	}

	public void setAbzuegeLiegenschaft(@Nullable BigDecimal abzuegeLiegenschaft) {
		this.abzuegeLiegenschaft = abzuegeLiegenschaft;
	}

	@Nullable
	public BigDecimal getGeschaeftsverlust() {
		return geschaeftsverlust;
	}

	public void setGeschaeftsverlust(@Nullable BigDecimal geschaeftsverlust) {
		this.geschaeftsverlust = geschaeftsverlust;
	}

	@Nullable
	public BigDecimal getEinkaeufeVorsorge() {
		return einkaeufeVorsorge;
	}

	public void setEinkaeufeVorsorge(@Nullable BigDecimal einkaeufeVorsorge) {
		this.einkaeufeVorsorge = einkaeufeVorsorge;
	}

	@Nullable
	public BigDecimal getBruttoertraegeVermoegen() {
		return bruttoertraegeVermoegen;
	}

	public void setBruttoertraegeVermoegen(@Nullable BigDecimal bruttoertraegeVermoegen) {
		this.bruttoertraegeVermoegen = bruttoertraegeVermoegen;
	}

	@Nullable
	public BigDecimal getNettoertraegeErbengemeinschaft() {
		return nettoertraegeErbengemeinschaft;
	}

	public void setNettoertraegeErbengemeinschaft(@Nullable BigDecimal nettoertraegeErbengemeinschaft) {
		this.nettoertraegeErbengemeinschaft = nettoertraegeErbengemeinschaft;
	}

	@SuppressFBWarnings("NM_CONFUSING")
	@Nullable
	public BigDecimal getNettoVermoegen() {
		return nettoVermoegen;
	}

	@SuppressFBWarnings("NM_CONFUSING")
	public void setNettoVermoegen(@Nullable BigDecimal nettoVermoegen) {
		this.nettoVermoegen = nettoVermoegen;
	}

	@Nullable
	public Boolean getEinkommenInVereinfachtemVerfahrenAbgerechnet() {
		return einkommenInVereinfachtemVerfahrenAbgerechnet;
	}

	public void setEinkommenInVereinfachtemVerfahrenAbgerechnet(
			@Nullable Boolean einkommenInVereinfachtemVerfahrenAbgerechnet) {
		this.einkommenInVereinfachtemVerfahrenAbgerechnet = einkommenInVereinfachtemVerfahrenAbgerechnet;
	}

	@Nullable
	public BigDecimal getAmountEinkommenInVereinfachtemVerfahrenAbgerechnet() {
		return amountEinkommenInVereinfachtemVerfahrenAbgerechnet;
	}

	public void setAmountEinkommenInVereinfachtemVerfahrenAbgerechnet(
			@Nullable BigDecimal amountEinkommenInVereinfachtemVerfahrenAbgerechnet) {
		this.amountEinkommenInVereinfachtemVerfahrenAbgerechnet = amountEinkommenInVereinfachtemVerfahrenAbgerechnet;
	}

	@Nullable
	public BigDecimal getGewinnungskosten() {
		return gewinnungskosten;
	}

	public void setGewinnungskosten(@Nullable BigDecimal gewinnungskosten) {
		this.gewinnungskosten = gewinnungskosten;
	}

	@Nullable
	public BigDecimal getAbzugSchuldzinsen() {
		return abzugSchuldzinsen;
	}

	public void setAbzugSchuldzinsen(@Nullable BigDecimal abzugSchuldzinsen) {
		this.abzugSchuldzinsen = abzugSchuldzinsen;
	}

	@Nullable
	public FinanzielleSituationSelbstdeklaration getSelbstdeklaration() {
		return selbstdeklaration;
	}

	public void setSelbstdeklaration(@Nullable FinanzielleSituationSelbstdeklaration selbstdeklaration) {
		this.selbstdeklaration = selbstdeklaration;
	}

	@Nullable
	public FinSitZusatzangabenAppenzell getFinSitZusatzangabenAppenzell() {
		return finSitZusatzangabenAppenzell;
	}

	public void setFinSitZusatzangabenAppenzell(@Nullable FinSitZusatzangabenAppenzell finSitZusatzangabenAppenzell) {
		this.finSitZusatzangabenAppenzell = finSitZusatzangabenAppenzell;
	}

	@Nullable
	public BigDecimal getErsatzeinkommenSelbststaendigkeitBasisjahr() {
		return ersatzeinkommenSelbststaendigkeitBasisjahr;
	}

	public void setErsatzeinkommenSelbststaendigkeitBasisjahr(@Nullable BigDecimal ersatzeinkommenSelbststaendigkeitBasisjahr) {
		this.ersatzeinkommenSelbststaendigkeitBasisjahr = ersatzeinkommenSelbststaendigkeitBasisjahr;
	}

	@Nullable
	public BigDecimal getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1() {
		return ersatzeinkommenSelbststaendigkeitBasisjahrMinus1;
	}

	public void setErsatzeinkommenSelbststaendigkeitBasisjahrMinus1(
		@Nullable BigDecimal ersatzeinkommenSelbststaendigkeitBasisjahrMinus1) {
		this.ersatzeinkommenSelbststaendigkeitBasisjahrMinus1 = ersatzeinkommenSelbststaendigkeitBasisjahrMinus1;
	}

	@Nullable
	public BigDecimal getBruttoLohn() {
		return bruttoLohn;
	}

	public void setBruttoLohn(@Nullable BigDecimal bruttoLohn) {
		this.bruttoLohn = bruttoLohn;
	}


	@Nonnull
	public AbstractFinanzielleSituation copyAbstractFinanzielleSituation(
		@Nonnull AbstractFinanzielleSituation target,
		@Nonnull AntragCopyType copyType) {
		super.copyAbstractEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_AR_2023:
			target.setNettolohn(this.getNettolohn());
			target.setFamilienzulage(this.getFamilienzulage());
			target.setErsatzeinkommen(this.getErsatzeinkommen());
			target.setErhalteneAlimente(this.getErhalteneAlimente());
			target.setBruttovermoegen(this.getBruttovermoegen());
			target.setSchulden(this.getSchulden());
			target.setGeschaeftsgewinnBasisjahr(this.getGeschaeftsgewinnBasisjahr());
			target.setGeschaeftsgewinnBasisjahrMinus1(this.getGeschaeftsgewinnBasisjahrMinus1());
			target.setGeleisteteAlimente(this.getGeleisteteAlimente());
			target.setSteuerbaresEinkommen(this.getSteuerbaresEinkommen());
			target.setSteuerbaresVermoegen(this.getSteuerbaresVermoegen());
			target.setAbzuegeLiegenschaft(this.getAbzuegeLiegenschaft());
			target.setGeschaeftsverlust(this.getGeschaeftsverlust());
			target.setEinkaeufeVorsorge(this.getEinkaeufeVorsorge());
			target.setNettoertraegeErbengemeinschaft(this.getNettoertraegeErbengemeinschaft());
			target.setGewinnungskosten(this.getGewinnungskosten());
			target.setAbzugSchuldzinsen(this.getAbzugSchuldzinsen());
			target.setNettoVermoegen(this.getNettoVermoegen());
			target.setEinkommenInVereinfachtemVerfahrenAbgerechnet(this.getEinkommenInVereinfachtemVerfahrenAbgerechnet());
			target.setAmountEinkommenInVereinfachtemVerfahrenAbgerechnet(this.getAmountEinkommenInVereinfachtemVerfahrenAbgerechnet());
			target.setBruttoertraegeVermoegen(this.getBruttoertraegeVermoegen());
			target.setBruttoLohn(this.getBruttoLohn());
			if (this.getSelbstdeklaration() != null) {
				target.setSelbstdeklaration(this.getSelbstdeklaration().copySelbsteklaration(new FinanzielleSituationSelbstdeklaration(), copyType));
			}
			if (this.getFinSitZusatzangabenAppenzell() != null) {
				target.setFinSitZusatzangabenAppenzell(this.getFinSitZusatzangabenAppenzell().copyFinSitZusatzangabenAppenzell(new FinSitZusatzangabenAppenzell(), copyType));
			}
			target.setErsatzeinkommenSelbststaendigkeitBasisjahr(this.getErsatzeinkommenSelbststaendigkeitBasisjahr());
			target.setErsatzeinkommenSelbststaendigkeitBasisjahrMinus1(this.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1());
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	@Override
	@SuppressWarnings({"OverlyComplexMethod", "PMD.CompareObjectsWithEquals"})
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(AbstractEntity other) {
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final AbstractFinanzielleSituation otherFinSituation = (AbstractFinanzielleSituation) other;
		return MathUtil.isSame(getNettolohn(), otherFinSituation.getNettolohn()) &&
			MathUtil.isSame(getFamilienzulage(), otherFinSituation.getFamilienzulage()) &&
			MathUtil.isSame(getErsatzeinkommen(), otherFinSituation.getErsatzeinkommen()) &&
			MathUtil.isSame(getErhalteneAlimente(), otherFinSituation.getErhalteneAlimente()) &&
			MathUtil.isSame(getBruttovermoegen(), otherFinSituation.getBruttovermoegen()) &&
			MathUtil.isSame(getSchulden(), otherFinSituation.getSchulden()) &&
			MathUtil.isSame(getGeschaeftsgewinnBasisjahr(), otherFinSituation.getGeschaeftsgewinnBasisjahr()) &&
			MathUtil.isSame(getGeschaeftsgewinnBasisjahrMinus1(), otherFinSituation.getGeschaeftsgewinnBasisjahrMinus1()) &&
			MathUtil.isSame(getGeleisteteAlimente(), otherFinSituation.getGeleisteteAlimente()) &&
			MathUtil.isSame(getSteuerbaresEinkommen(), otherFinSituation.getSteuerbaresEinkommen()) &&
			MathUtil.isSame(getSteuerbaresVermoegen(), otherFinSituation.getSteuerbaresVermoegen()) &&
			MathUtil.isSame(getAbzuegeLiegenschaft(), otherFinSituation.getAbzuegeLiegenschaft()) &&
			MathUtil.isSame(getGeschaeftsverlust(), otherFinSituation.getGeschaeftsverlust()) &&
			MathUtil.isSame(getEinkaeufeVorsorge(), otherFinSituation.getEinkaeufeVorsorge()) &&
			MathUtil.isSame(getAbzugSchuldzinsen(), otherFinSituation.getAbzugSchuldzinsen()) &&
			MathUtil.isSame(getGewinnungskosten(), otherFinSituation.getGewinnungskosten()) &&
			MathUtil.isSame(getNettoertraegeErbengemeinschaft(), otherFinSituation.getNettoertraegeErbengemeinschaft()) &&
			MathUtil.isSame(getNettoVermoegen(), otherFinSituation.getNettoVermoegen()) &&
			MathUtil.isSame(
					getAmountEinkommenInVereinfachtemVerfahrenAbgerechnet(),
					otherFinSituation.getAmountEinkommenInVereinfachtemVerfahrenAbgerechnet()) &&
			MathUtil.isSame(getDurchschnittlicherGeschaeftsgewinn(), otherFinSituation.getDurchschnittlicherGeschaeftsgewinn()) &&
		    MathUtil.isSame(getErsatzeinkommenSelbststaendigkeitBasisjahr(), otherFinSituation.getErsatzeinkommenSelbststaendigkeitBasisjahr()) &&
			MathUtil.isSame(getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1(), otherFinSituation.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1()) &&
			MathUtil.isSame(getBruttoLohn(), otherFinSituation.getBruttoLohn());
	}

	public boolean isVollstaendig(FinanzielleSituationTyp finSitTyp) {
		switch (finSitTyp) {
		case LUZERN:
			return (this.getSteuerbaresEinkommen() != null
					&& this.getSteuerbaresVermoegen() != null
					&& this.getAbzuegeLiegenschaft() != null
					&& this.getGeschaeftsverlust() != null
					&& this.getEinkaeufeVorsorge() != null) ||
			 this.getSelbstdeklaration() != null && this.getSelbstdeklaration().isVollstaendig();
		case APPENZELL:
		case APPENZELL_FOLGEMONAT:
			return this.getFinSitZusatzangabenAppenzell() != null
					&& this.getFinSitZusatzangabenAppenzell().isVollstaendig();
		case SOLOTHURN:
		case BERN_FKJV:
		case BERN:
		default:
			return isVollstaendingBern();
		}

	}

	private boolean isVollstaendingBern() {
		boolean isVollstaendig = this.getNettolohn() != null && this.getFamilienzulage() != null
			&& this.getErsatzeinkommen() != null && this.getErhalteneAlimente() != null
			&& this.getGeleisteteAlimente() != null;

		//Wenn Daten von Steuerschnittstelle abgefragt wurden, sind die Schulden und das Bruttovermögen nicht gesetzt,
		//dafür das Nettovermögen. Die Vollständigkeit der FinSit muss entsprechend validiert werden
		if (this.getSteuerdatenAbfrageStatus() != null
		&& this.getSteuerdatenAbfrageStatus().isSteuerdatenAbfrageErfolgreich()) {
			return isVollstaendig && this.getNettoVermoegen() != null;
		}

		return isVollstaendig && this.getSchulden() != null && this.getBruttovermoegen() != null;
	}

}
