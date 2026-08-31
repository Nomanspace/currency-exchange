FROM nginx:alpine
COPY nginx.conf /etc/nginx/nginx.conf
COPY index.html /usr/share/nginx/html/
COPY css/ /usr/share/nginx/html/css/
COPY js/ /usr/share/nginx/html/js/
COPY openapi.yaml /usr/share/nginx/html/
COPY swagger/ /usr/share/nginx/html/swagger/