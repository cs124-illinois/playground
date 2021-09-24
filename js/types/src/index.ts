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
}).And(
  Partial({
    filesystem: Array(FakeFile),
    timeout: Number,
  })
)
export type Submission = Static<typeof Submission>

export const Result = Record({
  started: String.withConstraint((s) => !isNaN(Date.parse(s))),
  ended: String.withConstraint((s) => !isNaN(Date.parse(s))),
  outputLines: Array(OutputLine),
  timedOut: Boolean,
  exitValue: Number,
})
export type Result = Static<typeof Result>

export const Status = Record({
  started: String.withConstraint((s) => !isNaN(Date.parse(s))),
  version: String,
})
export type Status = Static<typeof Status>
