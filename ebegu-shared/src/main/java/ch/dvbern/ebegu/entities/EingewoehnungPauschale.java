package ch.dvbern.ebegu.entities;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.util.MathUtil;
import org.hibernate.envers.Audited;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Audited
@Entity
public class EingewoehnungPauschale extends AbstractDateRangedEntity {

	private static final long serialVersionUID = 5378127600682968595L;
	@NotNull
	@Column(nullable = false)
	private BigDecimal pauschale = BigDecimal.ZERO;

	public BigDecimal getPauschale() {
		return pauschale;
	}

	public void setPauschale(BigDecimal pauschale) {
		this.pauschale = pauschale;
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this.equals(other)) {
			return true;
		}

		if (!(other instanceof EingewoehnungPauschale)) {
			return false;
		}

		final EingewoehnungPauschale otherEingewoehnung = (EingewoehnungPauschale) other;

		return super.isSame(other)
			&& MathUtil.isSame(this.getPauschale(), otherEingewoehnung.getPauschale());
	}

	public EingewoehnungPauschale copyEingewohnungEntity(@Nonnull EingewoehnungPauschale target, @Nonnull AntragCopyType copyType) {
		super.copyAbstractDateRangedEntity(target, copyType);
		target.setPauschale(this.getPauschale());
		return target;
	}
}
