<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="senderFullName" type="java.lang.String" -->
<#-- @ftlvariable name="anzahlTage" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="tsOnlyAntrag" type="java.lang.Boolean" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="isSozialdienst" type="java.lang.Boolean" -->
<#-- @ftlvariable name="gruss" type="java.lang.Boolean" -->
<#-- @ftlvariable name="fall" type="ch.dvbern.ebegu.entities.Fall"-->
<#-- @ftlvariable name="gesuchsperiode" type="ch.dvbern.ebegu.entities.Gesuchsperiode"-->
From: ${configuration.senderAddress}
To: <@base64Header>${senderFullName}</@base64Header> <${empfaengerMail}>
Subject: ${fall.getPaddedFallnummer()}, ${gesuchsperiode.getGesuchsperiodeString()}, <@base64Header>kiBon <#if configuration.isDevmode>Testsystem</#if> – Antrag <#if isSozialdienst>für ${gesuchsteller.fullName}</#if> nicht abgeschlossen</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
    ${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem</#if> – Gesuch <#if isSozialdienst>für ${gesuchsteller.fullName} </#if>
		nicht abgeschlossen</title>

</head>

<body>

<div>
	<p>
		Guten Tag
	</p>
	<p>
        <#if isSozialdienst>Sie haben auf
			<a href="www.kibon.ch">www.kibon.ch</a> einen Antrag für ${gesuchsteller.fullName} erfasst,<#else>Sie haben sich auf <a href="www.kibon.ch">www.kibon.ch</a> registriert,</#if> aber Ihr Gesuch für Beiträge zur Kinderbetreuung noch nicht eingereicht.
	</p>
	<p>
		Mit dieser Mail möchten wir Sie daran erinnern, Ihr Gesuch<#if isSozialdienst> für ${gesuchsteller.fullName}</#if>
		rechtzeitig einzureichen. Ihr Gesuch wird erst mit dem Klick auf «Gesuch einreichen» der Gemeinde übermittelt.
		Sie haben keinen Anspruch auf Beiträge, solange Sie das Gesuch nicht einreichen.
	</p>
	<p>
		Bitte holen Sie diesen Schritt nach, wenn Sie Beiträge möchten. Wenn Sie das nicht machen, löschen wir Ihr Gesuch in ${anzahlTage} Tagen.
	</p>
	<p>
		Wenn nur noch die Platzbestätigung ausstehend ist, setzen Sie sich bitte möglichst schnell mit Ihrer Betreuungsinstitution in Verbindung, damit Sie das Gesuch einreichen können.
	</p>
	<p>
		Beiträge erhalten Sie frühestens einen Monat nach Einreichung des vollständigen Gesuchs.
	</p>
	<p>
		Bei Fragen melden Sie sich bei Ihrer Wohngemeinde. Die Kontaktangaben finden Sie unter folgendem Link: <a href="https://www.sz.ch/verwaltung/finanzdepartement/steuerverwaltung/steuerbezug-steuerrechnung/gemeinden-bezirke.html/8756-8758-8802-10332-10354-10494-10498">Gemeinden - Kanton Schwyz (sz.ch)</a>
	</p>
	<p>
		Freundliche Grüsse <br/>
        ${gruss}
	</p>
	<p>
        <#if configuration.isDevmode>
			<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen verwendet.
				Über dieses System abgehandelte Anträge verfügen über keine Zahlungsberechtigung!</b><br><br>
        </#if>
		Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
	</p>
</div>

</body>

</html>
