<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="senderFullName" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="startDatum" type="java.lang.String" -->
<#-- @ftlvariable name="gesuchsperiode" type="ch.dvbern.ebegu.entities.Gesuchsperiode" -->
<#-- @ftlvariable name="gruss" type="ch.dvbern.ebegu.entities.Gesuchsperiode" -->
From: ${configuration.senderAddress}
To: <@base64Header>${senderFullName}</@base64Header> <${empfaengerMail}>
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem</#if> – Neue Periode freigeschaltet</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem</#if> – Neue Periode freigeschaltet</title>
</head>

<body>

<div>
	<p>
		Guten Tag
	</p>
	<p>
		Ab sofort können Sie den Antrag auf Betreuungsgutscheine für die neue Periode ${gesuchsperiode.gesuchsperiodeString} auf dem Onlineportal KiBon einreichen.
		<a href="https://www.kibon.ch/">Hier können Sie den neuen Antrag erfassen.</a>
	</p>
	<p>
		Bitte beachten Sie, dass der Betreuungsgutschein frühestens auf den Folgemonat nach Einreichung des vollständigen Antrags in der neuen Periode ausgestellt wird.
		Damit Sie die Betreuungsgutscheine ab 1. August erhalten, muss der vollständige Antrag und somit auch die Freigabequittung spätestens bis zum 31. Juli eingereicht werden.
		Wir empfehlen Ihnen aber den Antrag möglichst frühzeitig zu stellen und nicht bis Ende Juli zu warten.
		Falls Sie für die neue Periode keinen Antrag stellen möchten, können Sie dieses E-Mail ignorieren.
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
