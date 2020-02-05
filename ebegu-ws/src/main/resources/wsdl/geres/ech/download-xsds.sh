# Since the GERES and eCH Weblocations for xsds seem not to be available at all times download the xsds manually and
# check them in, also change the wsdls to use the local versions

# Renewal Service
wget https://a6hu-www-sts-b.be.ch:443/securityService/samlrenew/schemas/sv-context.xsd
wget https://a6hu-www-sts-b.be.ch:443/securityService/samlrenew/schemas/sv-faults.xsd
wget https://a6hu-www-sts-b.be.ch:443/securityService/samlrenew/schemas/samlrenew.xsd

# ZertSTS Service
wget https://a6hu-www-sts-b.be.ch:443/securityService/zertsts/schemas/sv-context.xsd
wget https://a6hu-www-sts-b.be.ch:443/securityService/zertsts/schemas/sv-faults.xsd
wget https://a6hu-www-sts-b.be.ch:443/securityService/zertsts/schemas/zertsts.xsd

# Assertion
wget https://a6hu-www-sts-b.be.ch/securityService/samlrenew/schemas/oasis-sstc-saml-schema-assertion-1.1.xsd
wget https://a6hu-www-sts-b.be.ch/securityService/samlrenew/schemas/xmldsig-core-schema.xsd


# ech standard xsds
wget http://www.ech.ch/xmlns/eCH-0020-f/3/eCH-0020-3-0f.xsd
wget http://www.ech.ch/xmlns/eCH-0011-f/8/eCH-0011-8-1f.xsd
wget http://www.ech.ch/xmlns/eCH-0010-f/5/eCH-0010-5-1f.xsd
wget http://www.ech.ch/xmlns/eCH-0008-f/3/eCH-0008-3-0f.xsd
wget http://www.ech.ch/xmlns/eCH-0021-f/7/eCH-0021-7-0f.xsd
wget http://www.ech.ch/xmlns/eCH-0006/2/eCH-0006-2-0.xsd
wget http://www.ech.ch/xmlns/eCH-0044-f/4/eCH-0044-4-1f.xsd
wget http://www.ech.ch/xmlns/eCH-0007-f/5/eCH-0007-5-0f.xsd
wget http://www.ech.ch/xmlns/eCH-0058/5/eCH-0058-5-0.xsd
wget http://www.ech.ch/xmlns/eCH-0135/1/eCH-0135-1-0.xsd

