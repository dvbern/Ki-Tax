<#-- @ftlvariable name="betreuung" type="ch.dvbern.ebegu.entities.Betreuung" -->
<#-- @ftlvariable name="senderFullName" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: <@base64Header>${senderFullName}</@base64Header> <${empfaengerMail}>
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
		Ihre Anmeldung für ${betreuung.kind.kindJA.fullName} an der ${betreuung.institutionStammdaten.institution
		.name} wurde entgegengenommen. Sie erhalten die definitive Bestätigung der Anmeldung von der gewählten Institution. <br/>
		Die Betreuungen können <a href="<#if configuration
		.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/gesuch/verfuegen/${betreuung.extractGesuch().id}">hier</a> eingesehen werden.
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
