package ch.dvbern.ebegu.entities;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

@Audited
@Entity
public class ErweiterteBetreuung extends AbstractMutableEntity implements Comparable<ErweiterteBetreuung> {

	private static final long serialVersionUID = -2859349895821767525L;

	@NotNull
	@Column(nullable = false)
	private Boolean erweiterteBeduerfnisse = false;


	public Boolean getErweiterteBeduerfnisse() {
		return erweiterteBeduerfnisse;
	}

	public void setErweiterteBeduerfnisse(Boolean erweiterteBeduerfnisse) {
		this.erweiterteBeduerfnisse = erweiterteBeduerfnisse;
	}

	//TODO
	@Override
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
		if (!(other instanceof ErweiterteBetreuung)) {
			return false;
		}
		boolean erwBeduerfnisseSame = Objects.equals(getErweiterteBeduerfnisse(), other
			.getErweiterteBeduerfnisse());
		return false;
	}

	//TODO
	@Override
	public int compareTo(ErweiterteBetreuung o) {
		return 0;
	}
}
