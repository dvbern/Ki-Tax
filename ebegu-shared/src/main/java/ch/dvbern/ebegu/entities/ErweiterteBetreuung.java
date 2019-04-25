package ch.dvbern.ebegu.entities;

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
	@NotNull
	private boolean bestaetigungAusserordentlicherBetreuungsaufwand = false;

	public Boolean getErweiterteBeduerfnisse() {
		return erweiterteBeduerfnisse;
	}

	public void setErweiterteBeduerfnisse(Boolean erweiterteBeduerfnisse) {
		this.erweiterteBeduerfnisse = erweiterteBeduerfnisse;
	}

	@Nullable
	public Fachstelle getFachstelle() {
		return fachstelle;
	}

	public void setFachstelle(@Nullable Fachstelle fachstelle) {
		this.fachstelle = fachstelle;
	}

	@Nonnull
	public boolean isBestaetigungAusserordentlicherBetreuungsaufwand() {
		return bestaetigungAusserordentlicherBetreuungsaufwand;
	}

	public void setBestaetigungAusserordentlicherBetreuungsaufwand(
		@Nonnull boolean bestaetigungAusserordentlicherBetreuungsaufwand) {
		this.bestaetigungAusserordentlicherBetreuungsaufwand = bestaetigungAusserordentlicherBetreuungsaufwand;
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
			isBestaetigungAusserordentlicherBetreuungsaufwand(),
			otherErwBetr.isBestaetigungAusserordentlicherBetreuungsaufwand());

		boolean fachstelleSame = Objects.equals(
			getFachstelle(),
			otherErwBetr.getFachstelle());

		return erwBeduerfnisseSame && bestAussBetrAufwand && fachstelleSame;
	}

	@Nonnull
	public ErweiterteBetreuung copyErweiterteBetreuung(
		@Nonnull ErweiterteBetreuung target,
		@Nonnull AntragCopyType copyType) {
		super.copyAbstractEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
			target.setErweiterteBeduerfnisse(this.getErweiterteBeduerfnisse());
			target.setBestaetigungAusserordentlicherBetreuungsaufwand(
				this.isBestaetigungAusserordentlicherBetreuungsaufwand());
			target.setFachstelle(this.getFachstelle());
			break;
		case ERNEUERUNG:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}
}
