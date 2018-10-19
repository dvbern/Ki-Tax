package ch.dvbern.ebegu.entities;

import java.util.Objects;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

@Audited
@Entity
public class ErweiterteBetreuung extends AbstractMutableEntity {

	private static final long serialVersionUID = -2859349895821767525L;

	@NotNull
	@Column(nullable = false)
	private Boolean erweiterteBeduerfnisse = false;

	@Nullable
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_erweiterte_betreuung_fachstelle_id"))
	private Fachstelle fachstelle;

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
		boolean erwBeduerfnisseSame = Objects.equals(getErweiterteBeduerfnisse(),
			((ErweiterteBetreuung) other).erweiterteBeduerfnisse);

		return erwBeduerfnisseSame;
	}
}
