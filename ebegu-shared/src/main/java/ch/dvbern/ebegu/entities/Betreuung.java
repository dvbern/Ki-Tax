package ch.dvbern.ebegu.entities;

import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.validators.CheckBetreuungspensum;
import ch.dvbern.ebegu.validators.CheckBetreuungspensumDatesOverlapping;
import org.hibernate.envers.Audited;

import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entity fuer Betreuungen.
 */
@Audited
@Entity
@CheckBetreuungspensum
@CheckBetreuungspensumDatesOverlapping
public class Betreuung extends AbstractEntity {

	private static final long serialVersionUID = -6776987863150835840L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_betreuung_kind_id"), nullable = false)
	private KindContainer kind;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_betreuung_institution_stammdaten_id"), nullable = false)
	private InstitutionStammdaten institutionStammdaten;

	@Enumerated(value = EnumType.STRING)
	@Column(nullable = false)
	@NotNull
	private Betreuungsstatus betreuungsstatus;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "betreuung")
	private Set<BetreuungspensumContainer> betreuungspensumContainers = new HashSet<>();

	@Nullable
	@Column(nullable = true)
	private Boolean schulpflichtig = false;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungen;


	public KindContainer getKind() {
		return kind;
	}

	public void setKind(KindContainer kind) {
		this.kind = kind;
	}

	public InstitutionStammdaten getInstitutionStammdaten() {
		return institutionStammdaten;
	}

	public void setInstitutionStammdaten(InstitutionStammdaten institutionStammdaten) {
		this.institutionStammdaten = institutionStammdaten;
	}

	public Betreuungsstatus getBetreuungsstatus() {
		return betreuungsstatus;
	}

	public void setBetreuungsstatus(Betreuungsstatus betreuungsstatus) {
		this.betreuungsstatus = betreuungsstatus;
	}

	public Set<BetreuungspensumContainer> getBetreuungspensumContainers() {
		return betreuungspensumContainers;
	}

	public void setBetreuungspensumContainers(Set<BetreuungspensumContainer> betreuungspensumContainers) {
		this.betreuungspensumContainers = betreuungspensumContainers;
	}

	@Nullable
	public Boolean getSchulpflichtig() {
		return schulpflichtig;
	}

	public void setSchulpflichtig(@Nullable Boolean schulpflichtig) {
		this.schulpflichtig = schulpflichtig;
	}

	@Nullable
	public String getBemerkungen() {
		return bemerkungen;
	}

	public void setBemerkungen(@Nullable String bemerkungen) {
		this.bemerkungen = bemerkungen;
	}

	public boolean isSame(Betreuung otherBetreuung) {
		if (this == otherBetreuung) {
			return true;
		}
		if (otherBetreuung == null || getClass() != otherBetreuung.getClass()) {
			return false;
		}

		boolean bemSame = Objects.equals(this.getBemerkungen(), otherBetreuung.getBemerkungen());
		boolean pensenSame =  this.getBetreuungspensumContainers().stream().allMatch(
			(pensCont) -> otherBetreuung.getBetreuungspensumContainers().stream().anyMatch(otherPensenCont -> otherPensenCont.isSame(pensCont)));
		boolean statusSame = Objects.equals(this.getBetreuungsstatus(), otherBetreuung.getBetreuungsstatus());
		boolean stammdatenSame = this.getInstitutionStammdaten().isSame(otherBetreuung.getInstitutionStammdaten());
		return bemSame && pensenSame && statusSame && stammdatenSame;
	}
}
