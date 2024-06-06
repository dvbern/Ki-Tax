package ch.dvbern.ebegu.rechner;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rechner.rules.RechnerRule;
import ch.dvbern.ebegu.util.BetreuungsangebotTypVisitor;
import com.sun.istack.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class BetreuungsangebotRechnerVisitor implements BetreuungsangebotTypVisitor<AbstractRechner> {

	private final Mandant mandant;
	private final List<RechnerRule> rechnerRulesForGemeinde;

	public BetreuungsangebotRechnerVisitor(Mandant mandant, List<RechnerRule> rechnerRulesForGemeinde) {
		this.mandant = mandant;
		this.rechnerRulesForGemeinde = rechnerRulesForGemeinde;
	}

	public AbstractRechner getRechnerForBetreuungsTyp(@NotNull BetreuungsangebotTyp betreuungsangebotTyp) {
		return betreuungsangebotTyp.accept(this);
	}
	@Override
	public AbstractRechner visitKita() {
		return new KitaRechnerVisitor(rechnerRulesForGemeinde).getKitaRechnerForMandant(mandant);
	}

	@Override
	public AbstractRechner visitTagesfamilien() {
		return new TageselternRechnerVisitor(rechnerRulesForGemeinde).getTageselternRechnerForMandant(mandant);
	}

	@Override
	public AbstractRechner visitMittagtisch() {
		return new MittagstischRechnerVisitor().visit(mandant);
	}

	@Override
	public AbstractRechner visitTagesschule() {
		return new TagesschuleRechnerVisitor(rechnerRulesForGemeinde).getTagesschuleRechnerForMandant(mandant);
	}

	@Nullable
	@Override
	public AbstractRechner visitFerieninsel() {
		return null;
	}
}
