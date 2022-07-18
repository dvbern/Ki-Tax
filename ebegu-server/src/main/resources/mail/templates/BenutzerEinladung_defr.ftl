<#-- @ftlvariable name="eingeladener" type="ch.dvbern.ebegu.entities.Benutzer" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="contentDE" type="java.lang.String" -->
<#-- @ftlvariable name="contentFR" type="java.lang.String" -->
<#-- @ftlvariable name="footerDE" type="java.lang.String" -->
<#-- @ftlvariable name="footerFR" type="java.lang.String" -->
<#-- @ftlvariable name="acceptLink" type="java.lang.String" -->
<#-- @ftlvariable name="acceptExpire" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${eingeladener.email}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Einladung / Confirmation</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Einladung / Confirmation</title>

</head>

<body>

<div>
    <p>
		${contentDE}
    </p>
	<p>
        <table cellspacing="0" cellpadding="0" width="100%">
            <tr>
                <td>
                    <table border="0" class="mobile-button" cellspacing="0" cellpadding="0">
                        <tr>
                            <td align="center" bgcolor="#d50025" style="background-color: #d50025; margin: auto; max-width: 600px; -webkit-border-radius: 2px; -moz-border-radius: 2px; border-radius: 2px; padding: 10px 15px; " width="100%">
                                <!--[if mso]>&nbsp;<![endif]-->
                                <a href="${acceptLink}" target="_blank" style="color: #ffffff; font-weight:normal; text-align:center; background-color: #d50025; text-decoration: none; border: none; -webkit-border-radius: 2px; -moz-border-radius: 2px; border-radius: 2px; display: inline-block;">
                                    <span style="color: #ffffff; font-weight:normal; line-height:1.5em; text-align:center;">EINLADUNG ANNEHMEN</span>
                                </a>
                                <!--[if mso]>&nbsp;<![endif]-->
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </p>
    <p>
        Diese Einladung ist bis zum <strong>${acceptExpire}</strong> gültig.
    </p>
    <p>
		${footerDE}
    </p>
    <p>
        <#if configuration.isDevmode>
		<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen verwendet. Über dieses System abgehandelte Anträge verfügen über keine Zahlungsberechtigung!</b><br><br>
        </#if>
		Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
    </p>

	<hr>

    <p>
		${contentFR}
    </p>
    <p>
    <table cellspacing="0" cellpadding="0" width="100%">
        <tr>
            <td>
                <table border="0" class="mobile-button" cellspacing="0" cellpadding="0">
                    <tr>
                        <td align="center" bgcolor="#d50025" style="background-color: #d50025; margin: auto; max-width: 600px; -webkit-border-radius: 2px; -moz-border-radius: 2px; border-radius: 2px; padding: 10px 15px; " width="100%">
                            <!--[if mso]>&nbsp;<![endif]-->
                            <a href="${acceptLink}" target="_blank" style="color: #ffffff; font-weight:normal; text-align:center; background-color: #d50025; text-decoration: none; border: none; -webkit-border-radius: 2px; -moz-border-radius: 2px; border-radius: 2px; display: inline-block;">
                                <span style="color: #ffffff; font-weight:normal; line-height:1.5em; text-align:center;">CONFIRMATION</span>
                            </a>
                            <!--[if mso]>&nbsp;<![endif]-->
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
    </p>
    <p>
		Ce lien est actif jusqu’au <strong>${acceptExpire}</strong>.
    </p>
    <p>
		${footerFR}
    </p>
    <p>
        <#if configuration.isDevmode>
		<b>Le présent message est envoyé par un système test utilisé pour les tutoriels. Les demandes via ce système ne donnent pas droit à un versement.</b><br><br>
        </#if>
		Merci de ne pas répondre à ce message automatique.
    </p>
</div>

</body>

</html>
