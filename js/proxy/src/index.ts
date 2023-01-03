import { googleLogin } from "@cs124/koa-google-login"
import { Result, Status, Submission } from "@cs124/playground-types"
import cors from "@koa/cors"
import Router from "@koa/router"
import retryBuilder from "fetch-retry"
import originalFetch from "isomorphic-fetch"
import Koa, { Context } from "koa"
import koaBody from "koa-bodyparser"
import ratelimit from "koa-ratelimit"
import { MongoClient } from "mongodb"
import mongodbUri from "mongodb-uri"
import { String } from "runtypes"

const fetch = retryBuilder(originalFetch)

const BACKEND = String.check(process.env.PLAYGROUND_SERVER)

const { database } = String.guard(process.env.MONGODB) ? mongodbUri.parse(process.env.MONGODB) : { database: undefined }
const client = String.guard(process.env.MONGODB) ? MongoClient.connect(process.env.MONGODB) : undefined
const _collection = client?.then((c) => c.db(database).collection(process.env.MONGODB_COLLECTION || "playground"))
const validDomains = process.env.VALID_DOMAINS?.split(",").map((s) => s.trim())

const router = new Router<Record<string, unknown>, { email?: string }>()

const audience = process.env.GOOGLE_CLIENT_IDS?.split(",").map((s) => s.trim())

const PORT = process.env.PORT || 8888

const STATUS = Object.assign(
  {
    backend: BACKEND,
    what: "playground",
    started: new Date(),
    port: PORT,
  },
  audience ? { audience } : null,
  { mongoDB: client !== undefined }
)
const getStatus = async (retries = 0) => {
  return {
    ...STATUS,
    status: Status.check(await fetch(BACKEND, { retries, retryDelay: 2000 }).then((r: Response) => r.json())),
  }
}

router.get("/", async (ctx: Context) => {
  ctx.body = await getStatus()
})
router.post("/", async (ctx) => {
  const start = new Date()
  const collection = await _collection
  const request = Submission.check(ctx.request.body)
  request.timeout = 8000
  let response: Result
  try {
    response = await fetch(BACKEND, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    }).then(async (r: Response) => {
      if (r.status === 200) {
        return Result.check(await r.json())
      } else {
        throw await r.text()
      }
    })
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } catch (err: any) {
    collection?.insertOne({
      succeeded: false,
      ...request,
      start,
      end: new Date(),
      ip: ctx.request.ip,
      err,
      ...(String.guard(process.env.SEMESTER) && { semester: process.env.SEMESTER }),
      ...(ctx.email && { email: ctx.email }),
      ...(ctx.headers.origin && { origin: ctx.headers.origin }),
    })

    return ctx.throw(400, err.toString())
  }
  ctx.body = response
  collection?.insertOne({
    succeeded: true,
    ...response,
    start,
    end: new Date(),
    ip: ctx.request.ip,
    ...(String.guard(process.env.SEMESTER) && { semester: process.env.SEMESTER }),
    ...(ctx.email && { email: ctx.email }),
    ...(ctx.headers.origin && { origin: ctx.headers.origin }),
  })
})

const db = new Map()
const server = new Koa({ proxy: true })
  .use(
    cors({
      origin: (ctx) => {
        if (
          !ctx.headers.origin ||
          (validDomains &&
            !validDomains.includes(ctx.headers.origin) &&
            !validDomains.includes(ctx.headers.origin.split(".").slice(-2).join(".")))
        ) {
          return ""
        } else {
          return ctx.headers.origin
        }
      },
      maxAge: 86400,
      credentials: true,
    })
  )
  .use(
    ratelimit({
      driver: "memory",
      db: db,
      duration: process.env.RATE_LIMIT_MS ? parseInt(process.env.RATE_LIMIT_MS) : 10,
      headers: {
        remaining: "Rate-Limit-Remaining",
        reset: "Rate-Limit-Reset",
        total: "Rate-Limit-Total",
      },
      max: 1,
      whitelist: (ctx) => ctx.request.method === "GET",
    })
  )
  .use(audience ? googleLogin({ audience, required: false }) : (_, next) => next())
  .use(koaBody({ jsonLimit: "8mb" }))
  .use(router.routes())
  .use(router.allowedMethods())

Promise.resolve().then(async () => {
  await _collection
  getStatus(process.env.STARTUP_RETRY_COUNT ? parseInt(process.env.STARTUP_RETRY_COUNT) : 32).then((s) => {
    console.log(s)
  })
  server.listen(process.env.PORT || 8888)
  server.on("error", (err) => {
    console.error(err)
  })
})
process.on("uncaughtException", (err) => {
  console.error(err)
})
process.on("unhandledRejection", (err) => {
  console.error(err)
  process.exit(-1)
})
