package ch.dvbern.ebegu.util;

public interface AnspruchBeschaeftigungAbhangigkeitTypVisitor<T> {

	T visitUnabhaengig();
	T visitAbhaengig();
	T visitMinimum();
	T visitSchwyz();

}
