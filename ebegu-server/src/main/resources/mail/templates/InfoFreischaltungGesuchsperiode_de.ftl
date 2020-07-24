<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="startDatum" type="java.lang.String" -->
<#-- @ftlvariable name="gesuchsperiode" type="ch.dvbern.ebegu.entities.Gesuchsperiode" -->
From: ${configuration.senderAddress}
To: <@base64Header>${gesuchsteller.fullName}</@base64Header> <${empfaengerMail}>
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem</#if> – Neue Gesuchsperiode freigeschaltet</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem</#if> – Neue Gesuchsperiode freigeschaltet</title>
</head>

<body>

<div>
	<p>
		Guten Tag
	</p>
	<p>
		Gerne möchten wir Sie mit dieser Mail informieren, dass die Gesuchperiode ${gesuchsperiode.gesuchsperiodeString} ab sofort
		für die Erfassung Ihrer Daten in kiBon offen steht. <br>
		<a href="https://www.kibon.ch/">Hier</a> können Sie das neue Gesuch erfassen.
	</p>
	<p>
		Bitte beachten Sie, dass der Betreuungsgutschein auf den Folgemonat nach Einreichung des vollständigen Gesuchs
		und ab Beginn des Betreuungsverhältnisses in der neuen Periode ausgestellt wird.
	</p>
	<p>
		Falls Sie für die Periode ${gesuchsperiode.gesuchsperiodeString} kein Gesuch stellen möchten, sind für Sie keine weiteren Schritte notwendig.
	<p>
		Freundliche Grüsse <br/>
		Ihre Gemeinde ${gesuch.dossier.gemeinde.name}
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
