package ch.dvbern.ebegu.entities;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

@Audited
@Entity
@Table(
	uniqueConstraints = @UniqueConstraint(columnNames = "betreuung_id", name = "UK_erweiterte_betreuung_betreuung")
)
public class ErweiterteBetreuungContainer extends AbstractMutableEntity implements Comparable<ErweiterteBetreuungContainer> {

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

	//TODO
	@Override
	public boolean isSame(AbstractEntity other) {
		return false;
	}

	//TODO
	@Override
	public int compareTo(ErweiterteBetreuungContainer o) {
		return 0;
	}
}
