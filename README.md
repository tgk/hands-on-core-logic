# Hands-on core.logic

## Having a go with the material from the session

Start a repl and start copying things in from src/hands_on.clj

    lein repl

Alternatively, start a nREPL session in Emacs.

## Setting up a session for several people

Clone [the session project from github](https://github.com/kovasb/session).

From the hands-on project, start the nrepls

    bin/start_nrepls.sh 5

Start the session servers

    bin/start_sessions.sh 5 /path/of/session/clone

When you're done, kill the session servers

    bin/stop_sessions.sh

And kill the nrepls by executin `C-c` in the nrepl window started by `start_nrepls.sh`.
