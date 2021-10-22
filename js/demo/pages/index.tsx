import { PlaygroundProvider, usePlayground } from "@cs124/playground-react"
import { Result, Submission } from "@cs124/playground-types"
import { GoogleLoginProvider, useGoogleLogin, WithGoogleTokens } from "@cs124/react-google-login"
import dynamic from "next/dynamic"
import { useCallback, useEffect, useMemo, useRef, useState } from "react"
import { IAceEditor } from "react-ace/lib/types"

const AceEditor = dynamic(() => import("react-ace"), { ssr: false })

const DEFAULT_PYTHON_CODE = `print("Hello, Python!")`
const DEFAULT_CPP_CODE = `
#include <iostream>
int main() {
    std::cout << "Hello, CPP!\\n";
    return 0;
}`.trim()
const DEFAULT_HASKELL_CODE = `
main = putStrLn "Hello, Haskell!"
`.trim()
const DEFAULT_JAVA_CODE = `
public class Main {
  public static void main(String[] unused) {
    System.out.println("Hello, Java!");
  }
}
`.trim()

const LoginButton: React.FC = () => {
  const { isSignedIn, auth, ready } = useGoogleLogin()
  if (!ready) {
    return null
  }
  return (
    <button onClick={() => (isSignedIn ? auth?.signOut() : auth?.signIn())}>{isSignedIn ? "Signout" : "Signin"}</button>
  )
}

const PlaygroundDemo: React.FC = () => {
  const [value, setValue] = useState("")
  const [mode, setMode] = useState<string>("python")
  const [result, setResult] = useState<{ result?: Result; error?: string } | undefined>()
  const { run: runPlayground } = usePlayground()
  const aceRef = useRef<IAceEditor>()
  const [running, setRunning] = useState(false)

  const run = useCallback(async () => {
    if (!aceRef.current) {
      return
    }
    const content = aceRef.current.getValue()
    if (content.trim() === "") {
      setResult(undefined)
      return
    }

    let path = "main.py"
    if (mode === "python") {
      path = "main.py"
    } else if (mode === "cpp") {
      path = "main.cpp"
    } else if (mode === "haskell") {
      path = "main.hs"
    } else if (mode === "java") {
      path = "Main.java"
    }

    const submission: Submission = {
      image: `cs124/playground-${mode}`,
      filesystem: [{ path, contents: content }],
    }
    try {
      setRunning(true)
      const result = await runPlayground(submission, true)
      if (result.timedOut) {
        setResult({ error: "Timeout" })
      } else {
        setResult({ result })
      }
    } catch (error: any) {
      setResult({ error: error.toString() })
    } finally {
      setRunning(false)
    }
  }, [mode, runPlayground])

  const output = useMemo(
    () => result?.error || result?.result?.outputLines.map(({ line }) => line).join("\n") || "",
    [result]
  )

  const commands = useMemo(() => {
    return [
      {
        name: "gotoline",
        exec: (): boolean => {
          return false
        },
        bindKey: { win: "", mac: "" },
      },
      {
        name: "run",
        bindKey: { win: "Ctrl-Enter", mac: "Ctrl-Enter" },
        exec: () => run(),
        readOnly: true,
      },
      {
        name: "close",
        bindKey: { win: "Esc", mac: "Esc" },
        exec: () => setResult(undefined),
        readOnly: true,
      },
    ]
  }, [run])

  useEffect(() => {
    commands.forEach((command) => {
      if (!aceRef.current) {
        return
      }
      aceRef.current?.commands.addCommand(command)
    })
  }, [commands])

  useEffect(() => {
    if (mode === "python") {
      setValue(DEFAULT_PYTHON_CODE)
    } else if (mode === "cpp") {
      setValue(DEFAULT_CPP_CODE)
    } else if (mode === "haskell") {
      setValue(DEFAULT_HASKELL_CODE)
    } else if (mode === "java") {
      setValue(DEFAULT_JAVA_CODE)
    }
    setResult(undefined)
  }, [mode])

  return (
    <>
      <AceEditor
        mode={mode}
        theme="github"
        width="100%"
        height="16rem"
        minLines={16}
        maxLines={Infinity}
        value={value}
        showPrintMargin={false}
        onBeforeLoad={(ace) => {
          ace.config.set("basePath", `https://cdn.jsdelivr.net/npm/ace-builds@${ace.version}/src-min-noconflict`)
        }}
        onLoad={(ace) => {
          aceRef.current = ace
        }}
        onChange={setValue}
        commands={commands}
        setOptions={{ tabSize: 2 }}
      />
      <div style={{ marginTop: 8 }}>
        <button
          onClick={() => {
            run()
          }}
          style={{ marginRight: 8 }}
        >
          Run
        </button>
        <div style={{ float: "right" }}>
          <select id="language" onChange={(e) => setMode(e.target.value)} value={mode}>
            <option value="python">Python</option>
            <option value="cpp">C++</option>
            <option value="java">Java</option>
            <option value="haskell">Haskell</option>
          </select>
        </div>
      </div>
      <div className="output">
        {running ? <span>Running...</span> : output !== undefined ? <span>{output}</span> : <span />}
      </div>
      {result?.result && (
        <AceEditor readOnly theme="github" mode="json" height="32rem" value={JSON.stringify(result.result, null, 2)} />
      )}
    </>
  )
}

export default function Home() {
  return (
    <GoogleLoginProvider clientConfig={{ client_id: process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID as string }}>
      <WithGoogleTokens>
        {({ idToken }) => (
          <PlaygroundProvider googleToken={idToken} server={process.env.NEXT_PUBLIC_PLAYGROUND_SERVER as string}>
            <h1><kbd>playground</kbd></h1>
            <p>Visit the <a href="https://github.com/cs124-illinois/playground">project homepage</a></p>
            <h2>Playground Demo</h2>
            <div style={{ marginBottom: 8 }}>
              <LoginButton />
            </div>
            <PlaygroundDemo />
          </PlaygroundProvider>
        )}
      </WithGoogleTokens>
    </GoogleLoginProvider>
  )
}
