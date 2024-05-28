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
		Sie können ab sofort für das nächste Schuljahr ${gesuchsperiode.gesuchsperiodeString} ein Gesuch für Beiträge zur
		Kinderbetreuung stellen. Falls Sie nach dem 1. August ${gesuchsperiode.basisJahr} Beiträge erhalten wollen, müssen Sie in
		jedem Fall ein neues Gesuch stellen.
	</p>
	<p>
		Sie haben frühestens einen Monat nach Einreichung des vollständigen Gesuchs Anspruch auf Beiträge. Das heisst: Falls Sie
		ab dem 1. August Beiträge haben möchten, müssen Sie bis am 30. Juni ein neues Gesuch gestellt haben.
	</p>
	<p>
		<a href="https://www.kibon.ch/">Hier können Sie das neue Gesuch erfassen.</a>
	</p>
	<p>
		Bei Fragen melden Sie sich bei Ihrer Wohngemeinde. Die Kontaktangaben finden Sie unter folgendem Link: <a href="https://www.sz.ch/public/upload/assets/75905/240508_gemeindeverzeichnis_kiba.pdf">Gemeinden - Kanton Schwyz (sz.ch)</a>
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
