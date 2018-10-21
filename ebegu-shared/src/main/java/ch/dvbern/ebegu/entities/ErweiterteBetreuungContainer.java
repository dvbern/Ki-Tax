package ch.dvbern.ebegu.entities;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.util.EbeguUtil;
import org.hibernate.envers.Audited;

@Audited
@Entity
@Table(
	uniqueConstraints = @UniqueConstraint(columnNames = "betreuung_id", name = "UK_erweiterte_betreuung_betreuung")
)
public class ErweiterteBetreuungContainer extends AbstractMutableEntity {

	private static final long serialVersionUID = 4847428166714262413L;

	@NotNull
	@OneToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_erweiterte_betreuung_container_betreuung_id"), nullable = false)
	private Betreuung betreuung;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_erweiterte_betreuung_container_erweiterte_betreuung_gs"))
	private ErweiterteBetreuung erweiterteBetreuungGS;

	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_erweiterte_betreuung_container_erweiterte_betreuung_ja"))
	private ErweiterteBetreuung erweiterteBetreuungJA;

	public Betreuung getBetreuung() {
		return betreuung;
	}

	public void setBetreuung(@NotNull Betreuung betreuung) {
		this.betreuung = betreuung;
	}

	@Nullable
	public ErweiterteBetreuung getErweiterteBetreuungGS() {
		return erweiterteBetreuungGS;
	}

	public void setErweiterteBetreuungGS(@Nullable ErweiterteBetreuung erweiterteBetreuungGS) {
		this.erweiterteBetreuungGS = erweiterteBetreuungGS;
	}

	public ErweiterteBetreuung getErweiterteBetreuungJA() {
		return erweiterteBetreuungJA;
	}

	public void setErweiterteBetreuungJA(ErweiterteBetreuung erweiterteBetreuungJA) {
		this.erweiterteBetreuungJA = erweiterteBetreuungJA;
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
		if (!(other instanceof ErweiterteBetreuungContainer)) {
			return false;
		}
		final ErweiterteBetreuungContainer otherErwBetrContainer = (ErweiterteBetreuungContainer) other;
		return EbeguUtil.isSameObject(getErweiterteBetreuungJA(), otherErwBetrContainer.getErweiterteBetreuungJA());
	}

	@Nonnull
	public ErweiterteBetreuungContainer copyErweiterteBetreuungContainer(
		@Nonnull ErweiterteBetreuungContainer target, @Nonnull AntragCopyType copyType, @Nonnull Betreuung targetErweiterteBetreuung) {
		super.copyAbstractEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
			target.setBetreuung(targetErweiterteBetreuung);
			target.setErweiterteBetreuungGS(null);
			target.setErweiterteBetreuungJA(this.getErweiterteBetreuungJA().copyErweiterteBetreuung(new ErweiterteBetreuung(), copyType));
			break;
		case ERNEUERUNG:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}
}
