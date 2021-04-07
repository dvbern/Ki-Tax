<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="senderFullName" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="isSozialdienst" type="java.lang.Boolean" -->
From: ${configuration.senderAddress}
To: <@base64Header>${senderFullName}</@base64Header> <${empfaengerMail}>
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem</#if> – <#if isSozialdienst>Der Antrag für ${gesuchsteller.fullName}<#else>Ihr Antrag</#if> wurde bearbeitet</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem</#if> – <#if isSozialdienst>Der Antrag für ${gesuchsteller.fullName}<#else>Ihr Antrag</#if> wurde bearbeitet</title>

</head>

<body>

<div>
	<p>
		Sehr geehrte Familie
	</p>
	<p>
		Die Prüfung und Berechnung <#if isSozialdienst>des Antrags für ${gesuchsteller.fullName}<#else>Ihres Antrags</#if> wurde bearbeitet. Sie können die Ergebnisse
		<a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/gesuch/verfuegen/${gesuch.id}">hier</a>
		einsehen.
	</p>
	<p>
		Freundliche Grüsse <br/>
		Ihre Gemeinde ${gesuch.dossier.gemeinde.name}
	</p>
	<p>
		<#if configuration.isDevmode>
		<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen verwendet. Über dieses System abgehandelte Anträge verfügen über keine Zahlungsberechtigung!</b><br><br>
		</#if>
		Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
	</p>
</div>

</body>

</html>
