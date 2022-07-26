## Mail templates

The mail templates are localized and mandantfähig. This means we have base files for the default
(german, "de-CH") and for the french ("fr-CH") translations of these mails. There are also some 
mails that have a custom locale which contain texts both in german and french with the locale "defr-CH".

### Template Lookup explanation

We're using freemarker as template engine. This engigne looks up the templates by the name and the
locale. It starts by looking for the most precise variation according to the locale and then goes down
to the least precise, meaning the filename itself. 

An example: We have the filename <i>InfoBetreuungVerfuegt.ftl</i> and the locale "de-CH-lu". In this locale,
de is the language, CH is the country and lu is the variant. freemarker no searches first for
<i>InfoBetreuungVerfuegt_de_CH_lu.ftl</i>, then <i>InfoBetreuungVerfuegt_de_CH.ftl</i>, then 
<i>InfoBetreuungVerfuegt_de.ftl</i> and finally <i>InfoBetreuungVerfuegt.ftl</i>

## Creating a new template

Create the XYZ_de.ftl template. This should always be added since this is the default and therefore
the fallback. If needed, create the XYZ_fr.ftl template.

To get the correct template, make sure to pass the correct locale to the 
<b>MailTemplateConfiguration.doProcessTemplate</b> method.

### Overwriting a template for a mandant

We have custom locales for the mandants and the languages which we create in a visitor (See MandantLocaleVisitor.java).
These locals have the language, the country and the variant set, where the variant determines the mandant.

If you want to overwrite a template for a mandant, copy the template and add the country and variant codes
separated by an underscore (_) as descriven in the section above. 

For example, a lucerne template overwrite for the template XYZ_de.tfl would look like this:
"XYZ_de_CH_lu.tfl". Note that the CH is necessary. 
For example, a lucerne locale would look like this: "de-CH-lu"