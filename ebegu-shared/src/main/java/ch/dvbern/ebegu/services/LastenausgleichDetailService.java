package ch.dvbern.ebegu.services;

import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.LastenausgleichDetail;

/**
 * Service für LastenausgleichDetail
 */
public interface LastenausgleichDetailService {

	/**
	 * Gibt alle LastenausgleichDetails für eine Gemeinde zurück
	 */
	@Nonnull
	List<LastenausgleichDetail> getAllLastenausgleichDetailsForGemeinde(Gemeinde gemeinde);
}
