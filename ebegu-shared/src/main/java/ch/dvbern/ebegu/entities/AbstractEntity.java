/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.hibernate.StringUUIDType;
import ch.dvbern.ebegu.reporting.gesuchstichtag.GesuchStichtagDataRow;
import ch.dvbern.ebegu.reporting.gesuchzeitraum.GesuchZeitraumDataRow;
import ch.dvbern.ebegu.util.AbstractEntityListener;
import ch.dvbern.ebegu.util.Constants;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.de.GermanNormalizationFilterFactory;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;

@MappedSuperclass
@Audited
@EntityListeners(AbstractEntityListener.class)
//Mappings for the native quries used by the report
@SqlResultSetMappings({
	@SqlResultSetMapping(name = "GesuchStichtagDataRowMapping", classes = @ConstructorResult(targetClass = GesuchStichtagDataRow.class,
		columns = {
			@ColumnResult(name = "bgNummer", type = String.class),
			@ColumnResult(name = "gemeinde", type = String.class),
			@ColumnResult(name = "gesuchLaufNr", type = Integer.class),
			@ColumnResult(name = "institution", type = String.class),
			@ColumnResult(name = "betreuungsTyp", type = String.class),
			@ColumnResult(name = "periode", type = String.class),
			@ColumnResult(name = "nichtFreigegeben", type = Integer.class),
			@ColumnResult(name = "mahnungen", type = Integer.class),
			@ColumnResult(name = "beschwerde", type = Integer.class) }
	)),
	@SqlResultSetMapping(name = "GesuchZeitraumDataRowMapping", classes = @ConstructorResult(targetClass = GesuchZeitraumDataRow.class,
		columns = {
			@ColumnResult(name = "bgNummer", type = String.class),
			@ColumnResult(name = "gemeinde", type = String.class),
			@ColumnResult(name = "gesuchLaufNr", type = Integer.class),
			@ColumnResult(name = "institution", type = String.class),
			@ColumnResult(name = "betreuungsTyp", type = String.class),
			@ColumnResult(name = "periode", type = String.class),
			@ColumnResult(name = "anzahlGesuchOnline", type = Integer.class),
			@ColumnResult(name = "anzahlGesuchPapier", type = Integer.class),
			@ColumnResult(name = "anzahlMutationOnline", type = Integer.class),
			@ColumnResult(name = "anzahlMutationPapier", type = Integer.class),
			@ColumnResult(name = "anzahlMutationAbwesenheit", type = Integer.class),
			@ColumnResult(name = "anzahlMutationBetreuung", type = Integer.class),
			@ColumnResult(name = "anzahlMutationEV", type = Integer.class),
			@ColumnResult(name = "anzahlMutationEwerbspensum", type = Integer.class),
			@ColumnResult(name = "anzahlMutationFamilienSitutation", type = Integer.class),
			@ColumnResult(name = "anzahlMutationFinanzielleSituation", type = Integer.class),
			@ColumnResult(name = "anzahlMutationGesuchsteller", type = Integer.class),
			@ColumnResult(name = "anzahlMutationKinder", type = Integer.class),
			@ColumnResult(name = "anzahlMutationUmzug", type = Integer.class),
			@ColumnResult(name = "anzahlMahnungen", type = Integer.class),
			@ColumnResult(name = "anzahlSteueramtAusgeloest", type = Integer.class),
			@ColumnResult(name = "anzahlSteueramtGeprueft", type = Integer.class),
			@ColumnResult(name = "anzahlBeschwerde", type = Integer.class),
			@ColumnResult(name = "anzahlVerfuegungen", type = Integer.class),
			@ColumnResult(name = "anzahlVerfuegungenNormal", type = Integer.class),
			@ColumnResult(name = "anzahlVerfuegungenMaxEinkommen", type = Integer.class),
			@ColumnResult(name = "anzahlVerfuegungenKeinPensum", type = Integer.class),
			@ColumnResult(name = "anzahlVerfuegungenNichtEintreten", type = Integer.class) }
	))
})
@TypeDef(
	name = "string-uuid-binary",
	typeClass = StringUUIDType.class
)
@AnalyzerDef(
	name = "EBEGUGermanAnalyzer",
	tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
	filters = {
		@TokenFilterDef(factory = StandardFilterFactory.class),
		@TokenFilterDef(factory = LowerCaseFilterFactory.class),
		@TokenFilterDef(factory = GermanNormalizationFilterFactory.class)
	}
)
public abstract class AbstractEntity implements Serializable {

	private static final long serialVersionUID = -979317154050183445L;

	@Id
	@Column(unique = true, nullable = false, updatable = false, length = 16)
	@Size(min = Constants.UUID_LENGTH, max = Constants.UUID_LENGTH)
	@Type( type = "string-uuid-binary" )
	private String id;

	@Version
	@NotNull
	private long version;

	// Wert darf nicht leer sein, aber kein @NotNull, da Wert erst im @PrePersist gesetzt
	@Column(nullable = false)
	private LocalDateTime timestampErstellt;

	// Wert darf nicht leer sein, aber kein @NotNull, da Wert erst im @PrePersist gesetzt
	@Column(nullable = false)
	private LocalDateTime timestampMutiert;

	// Wert darf nicht leer sein, aber kein @NotNull, da Wert erst im @PrePersist gesetzt
	// wir verwenden hier die Hibernate spezifische Annotation, weil diese vererbt wird
	@Size(max = Constants.DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false, length = Constants.DB_DEFAULT_MAX_LENGTH)
	private String userErstellt;

	@Size(max = Constants.DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false, length = Constants.DB_DEFAULT_MAX_LENGTH)
	private String userMutiert;

	/**
	 * This variable is used to tell the AbstractEntityListener that it should skip the preUpdate method when saving
	 * this object. This is a transient field, so that it will be removed with the java-object.
	 * WARNING! set it to true only when you know what you are doing
	 */
	@Transient
	private boolean skipPreUpdate = false;

	protected AbstractEntity() {
		//da wir teilweise schon eine id brauchen bevor die Entities gespeichert werden initialisieren wir die uuid hier
		id = UUID.randomUUID().toString();
	}

	@Nonnull
	public String getId() {
		return id;
	}

	@SuppressWarnings("NullableProblems")
	public void setId(@Nonnull String id) {
		this.id = id;
	}

	// Nullable, da erst im PrePersist gesetzt
	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	@Nullable // Nullable, da erst im PrePersist gesetzt
	public LocalDateTime getTimestampErstellt() {
		return timestampErstellt;
	}

	public void setTimestampErstellt(LocalDateTime timestampErstellt) {
		this.timestampErstellt = timestampErstellt;
	}

	@Nullable // Nullable, da erst im PrePersist gesetzt
	public LocalDateTime getTimestampMutiert() {
		return timestampMutiert;
	}

	public void setTimestampMutiert(LocalDateTime timestampMutiert) {
		this.timestampMutiert = timestampMutiert;
	}

	@Nullable // Nullable, da erst im PrePersist gesetzt
	public String getUserErstellt() {
		return userErstellt;
	}

	public void setUserErstellt(@Nonnull String userErstellt) {
		this.userErstellt = userErstellt;
	}

	@Nullable // Nullable, da erst im PrePersist gesetzt
	public String getUserMutiert() {
		return userMutiert;
	}

	public void setUserMutiert(@Nonnull String userMutiert) {
		this.userMutiert = userMutiert;
	}

	public boolean isSkipPreUpdate() {
		return skipPreUpdate;
	}

	/**
	 * WARNING! set it to true only when you know what you are doing.
	 */
	public void setSkipPreUpdate(boolean skipPreUpdate) {
		this.skipPreUpdate = skipPreUpdate;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof AbstractEntity)) {
			return false;
		}

		if (!Hibernate.getClass(this).equals(Hibernate.getClass(o))) {
			return false;
		}

		AbstractEntity that = (AbstractEntity) o;

		Objects.requireNonNull(getId());
		Objects.requireNonNull(that.getId());

		return getId().equals(that.getId());
	}

	public int hashCode() {
		return getId().hashCode();
	}

	/**
	 * @return true wenn das entity noch nicht in der DB gespeichert wurde (i.e. keinen timestamp gesetzt hat)
	 */
	public boolean isNew() {
		return timestampErstellt == null;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("id", getId())
			.toString();
	}

	public abstract boolean isSame(AbstractEntity other);

	public String getMessageForAccessException() {
		return "No specific message set for this object type: " + this.getClass().getSimpleName();
	}
}
