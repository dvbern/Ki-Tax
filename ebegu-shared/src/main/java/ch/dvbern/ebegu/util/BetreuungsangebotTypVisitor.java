package ch.dvbern.ebegu.util;

public interface BetreuungsangebotTypVisitor<T> {

	T visitKita();
	T visitTagesfamilien();
	T visitMittagtisch();
	T visitTagesschule();

	T visitFerieninsel();

}
