package ch.dvbern.ebegu.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.util.Constants;
import org.hibernate.annotations.Type;

@Entity
@Table(indexes ={ @Index(columnList = "kindId", name = "IX_alle_faelle_view_kind_kind_id"), })
public class AlleFaelleViewKind {

	@Id
	@Column(unique = true, nullable = false, updatable = false, length = 16)
	@Size(min = Constants.UUID_LENGTH, max = Constants.UUID_LENGTH)
	@Type(type = "string-uuid-binary")
	private String kindId;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_alle_faelle_view_kind_antrag_id"), nullable = false, name="antrag_id")
	private AlleFaelleView alleFaelleView;

	@NotNull
	@Column(nullable = false)
	private String name;

	public String getKindId() {
		return kindId;
	}

	public void setKindId(String kindId) {
		this.kindId = kindId;
	}

	public AlleFaelleView getAlleFaelleView() {
		return alleFaelleView;
	}

	public void setAlleFaelleView(AlleFaelleView alleFaelleView) {
		this.alleFaelleView = alleFaelleView;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
