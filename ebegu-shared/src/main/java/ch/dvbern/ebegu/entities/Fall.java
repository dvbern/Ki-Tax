package ch.dvbern.ebegu.entities;

import org.hibernate.envers.Audited;

import javax.persistence.Entity;

/**
 * Entitaet zum Speichern von Fall in der Datenbank.
 */
@Audited
@Entity
public class Fall extends AbstractEntity {

}
