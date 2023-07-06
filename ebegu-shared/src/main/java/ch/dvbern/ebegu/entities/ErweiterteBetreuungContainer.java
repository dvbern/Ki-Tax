package ch.dvbern.ebegu.entities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.util.EbeguUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.envers.Audited;

@Audited
@Entity
public class ErweiterteBetreuungContainer extends AbstractMutableEntity {

	private static final long serialVersionUID = 4847428166714262413L;


	@NotNull
	@OneToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_erweiterte_betreuung_container_betreuung_id"))
	@MapsId //foreign key of betreuung is primary key of this entity
	private Betreuung betreuung;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_erweiterte_betreuung_container_erweiterte_betreuung_gs"))
	private ErweiterteBetreuung erweiterteBetreuungGS;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_erweiterte_betreuung_container_erweiterte_betreuung_ja"))
	private ErweiterteBetreuung erweiterteBetreuungJA;

	@SuppressWarnings("NullableProblems")
	@SuppressFBWarnings(value = "NP_NONNULL_PARAM_VIOLATION", justification = "ErweiterteBetreuungContainer shares id with betreuung, it can not exist alone")
	public ErweiterteBetreuungContainer() {
		//noinspection ConstantConditions
		setId(null);    // ErweiterteBetreuungContainer shares id with betreuung, it can not exist alone
	}

	public ErweiterteBetreuungContainer(Betreuung betreuung) {
		this.betreuung = betreuung; //maintain relation for hibernate
		setId(betreuung.getId());
		betreuung.setErweiterteBetreuungContainer(this); // maintain relation for java
	}

	/**
	 * MapsId fuehrt dazu, dass als PK in der Datenbank der FK der Betreuung verwendet wird. Damit wir im Code trotzdem getId() verwenden
	 * koennen wird die Methode hier ueberschrieben
	 */
	@Nonnull
	@Override
	public String getId() {
		return betreuung.getId();
	}

	@Nullable
	public Betreuung getBetreuung() {
		return betreuung;
	}

	public void setBetreuung(@Nullable Betreuung betreuung) {
		this.betreuung = betreuung;
	}

	@Nullable
	public ErweiterteBetreuung getErweiterteBetreuungGS() {
		return erweiterteBetreuungGS;
	}

	public void setErweiterteBetreuungGS(@Nullable ErweiterteBetreuung erweiterteBetreuungGS) {
		this.erweiterteBetreuungGS = erweiterteBetreuungGS;
	}

	@Nullable
	public ErweiterteBetreuung getErweiterteBetreuungJA() {
		return erweiterteBetreuungJA;
	}

	public void setErweiterteBetreuungJA(@Nullable ErweiterteBetreuung erweiterteBetreuungJA) {
		this.erweiterteBetreuungJA = erweiterteBetreuungJA;
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final ErweiterteBetreuungContainer otherErwBetrContainer = (ErweiterteBetreuungContainer) other;
		return EbeguUtil.isSame(getErweiterteBetreuungJA(), otherErwBetrContainer.getErweiterteBetreuungJA());
	}

	@Nonnull
	public ErweiterteBetreuungContainer copyErweiterteBetreuungContainer(
		@Nonnull ErweiterteBetreuungContainer target, @Nonnull AntragCopyType copyType, @Nonnull Betreuung targetErweiterteBetreuung) {
		super.copyAbstractEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
			target.setBetreuung(targetErweiterteBetreuung);
			target.setErweiterteBetreuungGS(null);
			target.setErweiterteBetreuungJA(
				this.getErweiterteBetreuungJA() != null
					? this.getErweiterteBetreuungJA().copyErweiterteBetreuung(new ErweiterteBetreuung(), copyType)
					: null
			);
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_AR_2023:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}
}
