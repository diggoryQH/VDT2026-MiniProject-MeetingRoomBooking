# Stage 1: Build stage
FROM node:20-alpine AS build

# Build-time environment variables
ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

WORKDIR /app

# Install dependencies first to leverage caching
COPY package*.json ./
RUN npm ci

# Copy all source files and build the Vue SPA
COPY . .
RUN npm run build

# Stage 2: Production runtime stage
FROM nginx:1.25-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
