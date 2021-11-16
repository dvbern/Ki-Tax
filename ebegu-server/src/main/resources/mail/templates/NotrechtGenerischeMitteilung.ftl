<#-- @ftlvariable name="empfaenger" type="java.lang.String" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="betreff" type="java.lang.String" -->
<#-- @ftlvariable name="inhalt" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${empfaenger}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> –
    ${betreff}</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – ${betreff}</title>

</head>

<body>

<div>
    <p>
${inhalt}
    </p>
    <p>
		Den aktuellen Stand Ihrer Rückforderungsformulare finden Sie unter <a
                href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/corona-finanzierung/list">https://kibon.ch/corona-finanzierung/list</a>
    </p>
    <p>
        <#if configuration.isDevmode>
		<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen verwendet. Über dieses System abgehandelte Anträge verfügen über keine Zahlungsberechtigung!</b><br><br>
        </#if>
		Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
    </p>
    <p>


</div>

</body>

</html>
