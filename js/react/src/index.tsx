import { Result, Status, Submission } from "@cs124/playground-types"
import React, { PropsWithChildren, useCallback, useContext, useEffect, useState } from "react"

export interface PlaygroundContext {
  available: boolean
  connected: boolean
  status: Status | undefined
  run: (submission: Submission, validate?: boolean) => Promise<Result>
}

interface PlaygroundProviderProps {
  server: string
  googleToken?: string | undefined
}

export const PlaygroundProvider: React.FC<PropsWithChildren & PlaygroundProviderProps> = ({
  googleToken,
  server,
  children,
}) => {
  const [status, setStatus] = useState<Status | undefined>(undefined)

  useEffect(() => {
    fetch(server)
      .then((response) => response.json())
      .then((response) => setStatus(response.status))
      .catch(() => setStatus(undefined))
  }, [server])

  const run = useCallback(
    async (submission: Submission, validate = false): Promise<Result> => {
      submission = validate ? Submission.check(submission) : submission
      const result = await fetch(server, {
        method: "post",
        body: JSON.stringify(submission),
        headers: {
          "Content-Type": "application/json",
          ...(googleToken && { "google-token": googleToken }),
        },
      })
        .then(async (result) => {
          if (result.status === 200) {
            const r = await result.json()
            setStatus(r.status)
            return r
          } else {
            throw await result.text()
          }
        })
        .catch((err) => {
          setStatus(undefined)
          throw err
        })
      return validate ? Result.check(result) : (result as Result)
    },
    [googleToken, server]
  )

  return (
    <PlaygroundContext.Provider value={{ available: true, status, connected: status !== undefined, run }}>
      {children}
    </PlaygroundContext.Provider>
  )
}

export const usePlayground = (): PlaygroundContext => {
  return useContext(PlaygroundContext)
}

export const PlaygroundContext = React.createContext<PlaygroundContext>({
  available: false,
  connected: false,
  status: undefined,
  run: () => {
    throw new Error("Playground context not available")
  },
})
