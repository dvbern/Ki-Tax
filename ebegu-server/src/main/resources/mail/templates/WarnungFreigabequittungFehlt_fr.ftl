<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="anzahlTage" type="java.lang.String" -->
<#-- @ftlvariable name="datumLoeschung" type="java.lang.String" -->
<#-- @ftlvariable name="adresse" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
From: ${configuration.senderAddress}
To: " ${gesuchsteller.fullName} <${gesuchsteller.mail}>
Subject: <@base64Header>kiBon – Freigabequittung ausstehend</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>kiBon – Freigabequittung ausstehend</title>

</head>

<body>

<div>
    <p>
        FR_Guten Tag
    </p>
    <p>
        FR_Sie haben via kiBon Ihr Gesuch vollständig erfasst, besten Dank!
    </p>
    <p>
        FR_Leider ist Ihre Freigabequittung bisher nicht bei uns eingetroffen.
        <a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/gesuch/freigabe/${gesuch.id}">Hier</a>
        können Sie Ihre Freigabequittung nochmals herunterladen. Bitte schicken Sie uns die unterschriebene
        Freigabequittung umgehend per Post an ${adresse}.
        Andernfalls gilt Ihr Gesuch nicht nicht als eingereicht, kann nicht bearbeitet werden und wird am
	${datumLoeschung} automatisch gelöscht.
    </p>
    <p>
        FR_Bitte beachten Sie, dass der Betreuungsgutschein auf den Folgemonat nach Einreichung des vollständigen Gesuchs
        und ab Beginn des Betreuungsverhältnisses in der neuen Periode ausgestellt wird.
    </p>
    <p>
        FR_Freundliche Grüsse <br/>
        Ihre Gemeinde ${gesuch.dossier.gemeinde.name}
    </p>
    <p>
        FR_Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
    </p>
</div>

</body>

</html>
