FROM node:20-alpine3.20 AS build

WORKDIR /frontend

RUN npm install -g pnpm

COPY frontend-preprocessor .

#FIXME build fails due to linter errors
RUN pnpm install && pnpm build && npx next telemetry disable

FROM build

CMD DOTENV_CONFIG_PATH=.env.production pnpm start