package ch.dvbern.ebegu.entities;

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.util.MathUtil;
import org.hibernate.envers.Audited;

@Audited
@Entity
public class Eingewoehnung extends AbstractDateRangedEntity {

	private static final long serialVersionUID = 5378127600682968595L;
	@NotNull
	@Column(nullable = false)
	private BigDecimal kosten = BigDecimal.ZERO;

	public BigDecimal getKosten() {
		return kosten;
	}

	public void setKosten(BigDecimal kosten) {
		this.kosten = kosten;
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this.equals(other)) {
			return true;
		}

		if (!(other instanceof Eingewoehnung)) {
			return false;
		}

		final Eingewoehnung otherEingewoehnung = (Eingewoehnung) other;

		return super.isSame(other)
			&& MathUtil.isSame(this.getKosten(), otherEingewoehnung.getKosten());
	}

	public Eingewoehnung copyEingewohnungEntity(@Nonnull Eingewoehnung target, @Nonnull AntragCopyType copyType) {
		super.copyAbstractDateRangedEntity(target, copyType);
		target.setKosten(this.getKosten());
		return target;
	}

	public void addPauschale(BigDecimal pauschaleToAdd) {
		this.kosten = this.kosten.add(pauschaleToAdd);
	}
}
