<#-- @ftlvariable name="betreuung" type="ch.dvbern.ebegu.entities.Betreuung" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: <@base64Header>${gesuchsteller.fullName}</@base64Header> <${empfaengerMail}>
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem</#if> – Anmeldung für ${betreuung.kind.kindJA.fullName} akzeptiert</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem</#if> – Anmeldung entgegengenommen</title>

</head>

<body>

<div>
	<p>
		Sehr geehrte Familie
	</p>
	<p>
		Wir bestätigen Ihnen Ihre Anmeldung für ${betreuung.kind.kindJA.fullName} an der Tagesschule "${betreuung.institutionStammdaten.institution.name}". Die Anmeldebestätigung kann <a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/gesuch/verfuegenView/${betreuung.extractGesuch().id}/${betreuung.betreuungNummer}/${betreuung.kind.kindNummer}">hier</a> eingesehen werden.
		Ihr Antrag auf einen vergünstigten Tarif wurde noch nicht gerprüft. Nach Abschluss der Prüfung erhalten Sie eine neue Bestätigung mit der definitiven Gebühr.
	</p>
	<p>
		Freundliche Grüsse <br/>
		Ihre Gemeinde ${betreuung.extractGesuch().dossier.gemeinde.name}
	</p>
	<p>
		<#if configuration.isDevmode>
		<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen verwendet. Über dieses System abgehandelte Gesuche verfügen über keine Zahlungsberechtigung!</b><br><br>
		</#if>
		Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
	</p>
</div>

</body>

</html>
