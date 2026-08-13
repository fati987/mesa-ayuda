# --- build ---
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# Vite resuelve import.meta.env.VITE_API_BASE_URL en build time, no en
# runtime — sin este ARG/ENV, un `docker build` sin --build-arg horneraría
# la imagen con la URL de API vacía. Pasar con:
#   docker build --build-arg VITE_API_BASE_URL=https://tu-backend.example --   .
ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
RUN npm run build

# --- runtime ---
FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
