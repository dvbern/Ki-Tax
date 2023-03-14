package ch.dvbern.ebegu.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.enums.DemoFeatureTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;

public class RuleParameterUtil {

	private final Map<EinstellungKey, Einstellung> einstellungen;

	private final List<DemoFeatureTyp> activatedDemoFeatures;

	private final KitaxUebergangsloesungParameter kitaxUebergangsloesungParameter;

	public RuleParameterUtil(
		Map<EinstellungKey, Einstellung> einstellungen,
		KitaxUebergangsloesungParameter kitaxUebergangsloesungParameter) {
		this.einstellungen = einstellungen;
		this.activatedDemoFeatures = Collections.emptyList();
		this.kitaxUebergangsloesungParameter = kitaxUebergangsloesungParameter;
	}

	public RuleParameterUtil(
		Map<EinstellungKey, Einstellung> einstellungen,
		List<DemoFeatureTyp> activatedDemoFeatures,
		KitaxUebergangsloesungParameter kitaxUebergangsloesungParameter) {
		this.einstellungen = einstellungen;
		this.activatedDemoFeatures = activatedDemoFeatures;
		this.kitaxUebergangsloesungParameter = kitaxUebergangsloesungParameter;
	}

	public Map<EinstellungKey, Einstellung> getEinstellungen() {
		return einstellungen;
	}

	public KitaxUebergangsloesungParameter getKitaxUebergangsloesungParameter() {
		return kitaxUebergangsloesungParameter;
	}
}
