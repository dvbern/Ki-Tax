package ch.dvbern.ebegu.entities;


import ch.dvbern.ebegu.enums.EbeguParameterKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import org.hibernate.envers.Audited;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Entitaet zum Speichern von zeitabhängigen Parametern in E-BEGU
 */
@Audited
@Entity
public class EbeguParameter extends AbstractDateRangedEntity {


	private static final long serialVersionUID = 8704632842261673111L;

	@NotNull
	@Column(nullable = false, length = Constants.DB_DEFAULT_MAX_LENGTH)
	@Enumerated(EnumType.STRING)
	private EbeguParameterKey name;

	@NotNull
	@Column(nullable = false, length = Constants.DB_DEFAULT_MAX_LENGTH)
	private String value;


	@Nonnull
	public EbeguParameterKey getName() {
		return name;
	}

	public void setName(@Nonnull EbeguParameterKey name) {
		this.name = name;
	}

	@Nonnull
	public String getValue() {
		return value;
	}

	public void setValue(@Nonnull String value) {
		this.value = value;
	}

	public EbeguParameter copy(DateRange gueltigkeit) {
		EbeguParameter that = new EbeguParameter();
		that.setGueltigkeit(gueltigkeit);
		that.setName(this.getName());
		that.setValue(this.getValue());
		return that;
	}

	public BigDecimal getAsBigDecimal() {
		return new BigDecimal(value);
	}

	public Integer getAsInteger() {
		return Integer.valueOf(value);
	}
}
