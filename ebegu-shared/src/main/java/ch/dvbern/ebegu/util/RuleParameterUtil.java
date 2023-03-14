package ch.dvbern.ebegu.util;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.enums.DemoFeatureTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;

public class RuleParameterUtil {

	private final Map<EinstellungKey, Einstellung> einstellungen;

	private final List<DemoFeatureTyp> activatedDemoFeatures;

	private final KitaxUebergangsloesungParameter kitaxUebergangsloesungParameter;

	private final Locale locale;

	public RuleParameterUtil(
		Map<EinstellungKey, Einstellung> einstellungen,
		KitaxUebergangsloesungParameter kitaxUebergangsloesungParameter) {
		this.einstellungen = einstellungen;
		this.activatedDemoFeatures = Collections.emptyList();
		this.kitaxUebergangsloesungParameter = kitaxUebergangsloesungParameter;
		this.locale = Constants.DEFAULT_LOCALE;
	}

	public RuleParameterUtil(
		Map<EinstellungKey, Einstellung> einstellungen,
		List<DemoFeatureTyp> activatedDemoFeatures,
		KitaxUebergangsloesungParameter kitaxUebergangsloesungParameter,
		Locale locale) {
		this.einstellungen = einstellungen;
		this.activatedDemoFeatures = activatedDemoFeatures;
		this.kitaxUebergangsloesungParameter = kitaxUebergangsloesungParameter;
		this.locale = locale;
	}

	public Map<EinstellungKey, Einstellung> getEinstellungen() {
		return einstellungen;
	}

	public KitaxUebergangsloesungParameter getKitaxUebergangsloesungParameter() {
		return kitaxUebergangsloesungParameter;
	}

	@NotNull
	public Einstellung getEinstellung(EinstellungKey einstellungKey) {
		Einstellung einstellung = einstellungen.get(einstellungKey);
		Objects.requireNonNull(einstellung, "Parameter " + einstellungKey + " muss gesetzt sein");
		return einstellung;
	}

	public Locale getLocale() {
		return locale;
	}

	public boolean isDemoFeatureActivated(DemoFeatureTyp demoFeatureTyp) {
		return activatedDemoFeatures.contains(demoFeatureTyp);
	}
}
