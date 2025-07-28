FROM node:20-alpine3.20 AS build

WORKDIR /dashboard

RUN npm install -g pnpm

COPY dashboard .

#FIXME build fails due to linter errors
RUN pnpm install && pnpm build && npx next telemetry disable

FROM build

CMD DOTENV_CONFIG_PATH=.env.production pnpm start