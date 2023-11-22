FROM docker-registry.dvbern.ch/kibon/nginx-base

ARG KIBON_POM_VERSION

ADD target/ebegu-web-${KIBON_POM_VERSION}-client.tar.gz /usr/share/nginx/html/web
