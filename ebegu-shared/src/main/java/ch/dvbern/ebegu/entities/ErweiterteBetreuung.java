package ch.dvbern.ebegu.entities;

import java.math.BigDecimal;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.util.MathUtil;
import org.hibernate.envers.Audited;

@SuppressWarnings("InstanceVariableMayNotBeInitialized")
@Audited
@Entity
public class ErweiterteBetreuung extends AbstractMutableEntity {

	private static final long serialVersionUID = -2859349895821767525L;

	@NotNull
	@Column(nullable = false)
	private Boolean erweiterteBeduerfnisse = false;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_erweiterte_betreuung_fachstelle_id"))
	private Fachstelle fachstelle;

	@Column(nullable = false)
	private boolean erweiterteBeduerfnisseBestaetigt = false;

	@Nullable
	private Boolean betreuungInGemeinde;

	@NotNull
	@Column(nullable = false)
	private Boolean keineKesbPlatzierung = false;

	@Nullable
	@Column(nullable = true)
	private Boolean kitaPlusZuschlag;

	@Column(nullable = false)
	private boolean kitaPlusZuschlagBestaetigt = false;

	@Nullable
	@Column(nullable = true)
	private BigDecimal erweitereteBeduerfnisseBetrag;

	@Nullable
	public Boolean getBetreuungInGemeinde() {
		return betreuungInGemeinde;
	}

	public void setBetreuungInGemeinde(@Nullable Boolean betreuungInGemeinde) {
		this.betreuungInGemeinde = betreuungInGemeinde;
	}

	@Nonnull
	public Boolean getErweiterteBeduerfnisse() {
		return erweiterteBeduerfnisse;
	}

	public void setErweiterteBeduerfnisse(@Nonnull Boolean erweiterteBeduerfnisse) {
		this.erweiterteBeduerfnisse = erweiterteBeduerfnisse;
	}

	@Nullable
	public Fachstelle getFachstelle() {
		return fachstelle;
	}

	public void setFachstelle(@Nullable Fachstelle fachstelle) {
		this.fachstelle = fachstelle;
	}

	public boolean isErweiterteBeduerfnisseBestaetigt() {
		return erweiterteBeduerfnisseBestaetigt;
	}

	public void setErweiterteBeduerfnisseBestaetigt(boolean erweiterteBeduerfnisseBestaetigt) {
		this.erweiterteBeduerfnisseBestaetigt = erweiterteBeduerfnisseBestaetigt;
	}

	@Nonnull
	public Boolean getKeineKesbPlatzierung() {
		return keineKesbPlatzierung;
	}

	public void setKeineKesbPlatzierung(@Nonnull Boolean keineKesbPlatzierung) {
		this.keineKesbPlatzierung = keineKesbPlatzierung;
	}

	@Nullable
	public Boolean getKitaPlusZuschlag() {
		return kitaPlusZuschlag;
	}

	public void setKitaPlusZuschlag(@Nullable Boolean kitaPlusZuschlag) {
		this.kitaPlusZuschlag = kitaPlusZuschlag;
	}

	@Nullable
	public BigDecimal getErweitereteBeduerfnisseBetrag() {
		return erweitereteBeduerfnisseBetrag;
	}

	public void setErweitereteBeduerfnisseBetrag(@Nullable BigDecimal erweitereteBeduerfnisseBetrag) {
		this.erweitereteBeduerfnisseBetrag = erweitereteBeduerfnisseBetrag;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof ErweiterteBetreuung)) {
			return false;
		}
		final ErweiterteBetreuung otherErwBetr = (ErweiterteBetreuung) other;
		boolean erwBeduerfnisseSame = Objects.equals(
			getErweiterteBeduerfnisse(),
			otherErwBetr.getErweiterteBeduerfnisse());

		boolean bestAussBetrAufwand = Objects.equals(
			isErweiterteBeduerfnisseBestaetigt(),
			otherErwBetr.isErweiterteBeduerfnisseBestaetigt());

		boolean fachstelleSame = Objects.equals(
			getFachstelle(),
			otherErwBetr.getFachstelle());

		boolean kesbSame = Objects.equals(
			getKeineKesbPlatzierung(),
			otherErwBetr.getKeineKesbPlatzierung());

		boolean kitaPlusSame = Objects.equals(
			getKitaPlusZuschlag(),
			otherErwBetr.getKitaPlusZuschlag());

		boolean erweitereteBeduerfnisseSame = MathUtil.isSame(
			getErweitereteBeduerfnisseBetrag(),
			otherErwBetr.getErweitereteBeduerfnisseBetrag());

		return erwBeduerfnisseSame && bestAussBetrAufwand && fachstelleSame && kesbSame && kitaPlusSame && erweitereteBeduerfnisseSame;
	}

	@Nonnull
	public ErweiterteBetreuung copyErweiterteBetreuung(
		@Nonnull ErweiterteBetreuung target,
		@Nonnull AntragCopyType copyType) {
		super.copyAbstractEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
			target.setErweiterteBeduerfnisse(this.getErweiterteBeduerfnisse());
			target.setErweiterteBeduerfnisseBestaetigt(
				this.isErweiterteBeduerfnisseBestaetigt());
			target.setFachstelle(this.getFachstelle());
			target.setKeineKesbPlatzierung(this.getKeineKesbPlatzierung());
			target.setBetreuungInGemeinde(this.getBetreuungInGemeinde());
			target.setKitaPlusZuschlag(this.getKitaPlusZuschlag());
			target.setKitaPlusZuschlagBestaetigt(this.isKitaPlusZuschlagBestaetigt());
			target.setErweitereteBeduerfnisseBetrag(this.getErweitereteBeduerfnisseBetrag());
			break;
		case ERNEUERUNG:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	public boolean isKitaPlusZuschlagBestaetigt() {
		return kitaPlusZuschlagBestaetigt;
	}

	public void setKitaPlusZuschlagBestaetigt(boolean kitaPlusZuschlagBestaetigt) {
		this.kitaPlusZuschlagBestaetigt = kitaPlusZuschlagBestaetigt;
	}
}
