<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="senderFullName" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="isSozialdienst" type="java.lang.Boolean" -->
<#-- @ftlvariable name="hostname" type="java.lang.String" -->
<#-- @ftlvariable name="gruss" type="java.lang.String" -->
<#-- @ftlvariable name="fall" type="ch.dvbern.ebegu.entities.Fall"-->
<#-- @ftlvariable name="gesuchsperiode" type="ch.dvbern.ebegu.entities.Gesuchsperiode"-->
From: ${configuration.senderAddress}
To: <@base64Header>${senderFullName}</@base64Header> <${empfaengerMail}>
Subject: ${fall.getPaddedFallnummer()}, ${gesuchsperiode.getGesuchsperiodeString()}, <@base64Header>kiBon <#if configuration.isDevmode>Testsystem</#if> – Unvollständige Unterlagen</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem</#if> – Unvollständige Unterlagen</title>

</head>

<body>

<div>
	<p>
		Sehr geehrte Familie
	</p>
	<p>
		Am ${gesuch.getEingangsdatumFormated()} haben Sie einen Antrag <#if isSozialdienst>für ${gesuchsteller.fullName}</#if> via kiBon eingereicht.
		Leider sind die eingereichten Unterlagen unvollständig. Die fehlenden Dokumente
		müssen nachgereicht werden. Unser Schreiben kann
		<a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${hostname}/gesuch/verfuegen/${gesuch.id}">hier</a>
		eingesehen werden.
	</p>
	<p>
		Freundliche Grüsse <br/>
		${gruss}
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
