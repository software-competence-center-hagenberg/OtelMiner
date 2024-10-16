FROM node:20-alpine3.20 AS build

WORKDIR /frontend

RUN npm install -g pnpm

COPY frontend .

RUN npx next telemetry disable && pnpm install && pnpm build

FROM build

CMD ["sh", "-c", "DOTENV_CONFIG_PATH=.env.production pnpm start"]