<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="senderFullName" type="java.lang.String" -->
<#-- @ftlvariable name="anzahlTage" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="tsOnlyAntrag" type="java.lang.Boolean" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="isSozialdienst" type="java.lang.Boolean" -->
From: ${configuration.senderAddress}
To: <@base64Header>${senderFullName}</@base64Header> <${empfaengerMail}>
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem</#if> – Gesuch <#if isSozialdienst>für ${gesuchsteller.fullName}</#if> nicht abgeschlossen</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem</#if> – Gesuch <#if isSozialdienst>für ${gesuchsteller.fullName} </#if>nicht abgeschlossen</title>

</head>

<body>

<div>
	<p>
		Guten Tag
	</p>
	<p>
        <#if isSozialdienst>Sie haben auf <a href="www.kibon.ch">www.kibon.ch</a> einen Antrag für ${gesuchsteller.fullName} erfasst<#else>Sie haben sich auf <a href="www.kibon.ch">www.kibon.ch</a> registriert.</#if> Sie haben Ihre Daten noch
		nicht freigegeben.
	</p>
	<p>
		Mit dieser Mail möchten wir Sie daran erinnern, Ihren Antrag<#if isSozialdienst> für ${gesuchsteller.fullName}</#if> rechtzeitig abzuschliessen. Das Gesuch gilt erst mit
		dem Einsenden der Freigabequittung als eingereicht und kann zuvor durch die Gemeinde nicht bearbeitet werden.
        Ohne eine Freigabe innert ${anzahlTage} Tagen erfolgt eine automatische Löschung.
	</p>
	<#if tsOnlyAntrag==false>
	<p>
        Bitte beachten Sie, dass der Betreuungsgutschein auf den Folgemonat nach Einreichung des vollständigen Gesuchs
        und ab Beginn des Betreuungsverhältnisses in der neuen Periode ausgestellt wird.
	</p>
    </#if>
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
