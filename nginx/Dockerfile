FROM nginx:1.25-alpine

COPY nginx.conf /etc/nginx/nginx.conf

# 추후에 추가 (HTTPS-운영단계)
# RUN mkdir -p /etc/nginx/ssl
RUN mkdir -p /usr/share/nginx/html/static
RUN apk add --no-cache curl

# 추후에 443 포트 추가 (HTTPS-운영단계)
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]