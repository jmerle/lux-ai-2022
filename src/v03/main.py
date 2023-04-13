import atexit
import json
import sys
from argparse import Namespace
from pathlib import Path
from subprocess import PIPE, Popen

agents = {}

def kill_all_agents():
    for agent in agents.values():
        if agent.poll() is None:
            agent.kill()

atexit.register(kill_all_agents)

def agent_fn(obs, config):
    global agents

    is_kaggle = "__raw_path__" in config

    if obs.player not in agents:
        current_file = config["__raw_path__"] if is_kaggle else __file__

        agents[obs.player] = Popen(["java", "-jar", "agent.jar"],
                                   stdin=PIPE,
                                   stdout=PIPE,
                                   stderr=PIPE if is_kaggle else None,
                                   cwd=Path(current_file).parent)

        if not is_kaggle:
            print(f"Running {obs.player} on PID {agents[obs.player].pid}", file=sys.stderr)

    agent = agents[obs.player]

    if agent.poll() is not None:
        print("Agent process has stopped running", file=sys.stderr)
        if is_kaggle:
            raise RuntimeError()
        else:
            sys.exit(1)

    data = {**vars(obs), "info": config}
    data["obs"] = json.loads(data["obs"])

    agent.stdin.write((json.dumps(data) + "\n").encode("utf-8"))
    agent.stdin.flush()

    output = agent.stdout.readline().decode("utf-8")

    try:
        return json.loads(output)
    except ValueError:
        if is_kaggle:
            print(agent.stderr.read(), file=sys.stderr)
            raise RuntimeError()
        else:
            print(f"Agent responded with invalid output '{output}'", file=sys.stderr)
            sys.exit(1)

if __name__ == "__main__":
    while True:
        try:
            line = input()
        except EOFError as eof:
            raise SystemExit(eof)

        data = json.loads(line)
        data["obs"] = json.dumps(data["obs"])

        print(json.dumps(agent_fn(Namespace(**data), data["info"])))
