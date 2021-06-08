<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="senderFullName" type="java.lang.String" -->
<#-- @ftlvariable name="anzahlTage" type="java.lang.String" -->
<#-- @ftlvariable name="datumLoeschung" type="java.lang.String" -->
<#-- @ftlvariable name="adresse" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="tsOnlyAntrag" type="java.lang.Boolean" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="isSozialdienst" type="java.lang.Boolean" -->
From: ${configuration.senderAddress}
To: <@base64Header>${senderFullName}</@base64Header> <${empfaengerMail}>
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem</#if> – Freigabequittung ausstehend</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem</#if> – Freigabequittung ausstehend</title>

</head>

<body>

<div>
	<p>
		Guten Tag
	</p>
	<p>
		Sie haben via kiBon <#if isSozialdienst>den Antrag für ${gesuchsteller.fullName}<#else>Ihren Antrag</#if> vollständig erfasst, besten Dank!
	</p>
	<p>
		Leider ist Ihre Freigabequittung bisher nicht bei uns eingetroffen.
        <a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/gesuch/freigabe/${gesuch.id}">Hier</a>
		können Sie Ihre Freigabequittung nochmals herunterladen. Bitte schicken Sie uns die unterschriebene
        Freigabequittung umgehend per Post an ${adresse}.
		Andernfalls gilt Ihr Antrag als nicht eingereicht, kann nicht bearbeitet werden und wird am
		${datumLoeschung} automatisch gelöscht.
	</p>
    <#if tsOnlyAntrag == false>
		<p>
			Bitte beachten Sie, dass der Betreuungsgutschein auf den Folgemonat nach Einreichung des vollständigen Antrags
			und ab Beginn des Betreuungsverhältnisses in der neuen Periode ausgestellt wird.
		</p>
    </#if>
	<p>
		Dies ist eine automatisch generierte E-Mail. Falls Sie Ihre Freigabequittung in der Zwischenzeit eingesendet haben, betrachten Sie diese Aufforderung als gegenstandslos.
	</p>
	<p>
		Freundliche Grüsse <br/>
		Ihre Gemeinde ${gesuch.dossier.gemeinde.name}
	</p>
	<p>
		<#if configuration.isDevmode>
		<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen verwendet. Über dieses System abgehandelte Anträge verfügen über keine Zahlungsberechtigung!</b><br><br>
		</#if>
	</p>
</div>

</body>

</html>
