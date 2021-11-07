import { Array, Boolean, Literal, Number, Partial, Record, Static, String, Union } from "runtypes"

export const Console = Union(Literal("STDOUT"), Literal("STDERR"))
export type Console = Static<typeof Console>

export const OutputLine = Record({
  console: Console,
  timestamp: String.withConstraint((s) => !isNaN(Date.parse(s))),
  line: String,
})
export type OutputLine = Static<typeof OutputLine>

export const FakeFile = Record({
  path: String,
  contents: String,
})
export type FakeFile = Static<typeof FakeFile>

export const Submission = Record({
  image: String,
  timeout: Number,
}).And(
  Partial({
    filesystem: Array(FakeFile),
  })
)
export type Submission = Static<typeof Submission>

export const Timings = Record({
  started: String.withConstraint((s) => !isNaN(Date.parse(s))),
  tempCreated: String.withConstraint((s) => !isNaN(Date.parse(s))),
  imagePulled: String.withConstraint((s) => !isNaN(Date.parse(s))),
  containerStarted: String.withConstraint((s) => !isNaN(Date.parse(s))),
}).And(
  Partial({
    executionStarted: String.withConstraint((s) => !isNaN(Date.parse(s))),
    completed: String.withConstraint((s) => !isNaN(Date.parse(s))),
  })
)
export type Timings = Static<typeof Timings>

export const Result = Record({
  outputLines: Array(OutputLine),
  timeout: Number,
  timedOut: Boolean,
  exitValue: Number,
  timings: Timings,
})
export type Result = Static<typeof Result>

export const Status = Record({
  started: String.withConstraint((s) => !isNaN(Date.parse(s))),
  version: String,
})
export type Status = Static<typeof Status>
