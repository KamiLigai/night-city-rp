FROM node:lts-alpine AS build
WORKDIR /usr/src/app
COPY package.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:stable-alpine
COPY --from=build /usr/src/app/dist /bin/www
COPY nginx.conf /etc/nginx/conf.d/default.conf
#EXPOSE 80
CMD [ "nginx", "-g", "daemon off;" ]
