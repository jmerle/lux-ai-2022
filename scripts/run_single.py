import random
import subprocess
import webbrowser
from argparse import ArgumentParser
from common import get_agent_file
from datetime import datetime
from pathlib import Path

def main() -> None:
    parser = ArgumentParser(description="Run a single match between two agents.")
    parser.add_argument("agent1", type=str, help="name of the first agent")
    parser.add_argument("agent2", type=str, help="name of the second agent")
    parser.add_argument("-s", "--seed", type=int, help="the seed to use, defaults to a random seed")
    parser.add_argument("-o", "--open", action="store_true", help="open the replay in Lux Eye 2022 afterwards (assumes the replays directory is served on port 8000)")

    args = parser.parse_args()

    agent1_file = get_agent_file(args.agent1)
    agent2_file = get_agent_file(args.agent2)

    seed = args.seed or random.randint(0, 1e9)

    print(f"Running {args.agent1} against {args.agent2} on seed {seed}")

    timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    replay_file = Path(__file__).parent.parent / "replays" / f"{args.agent1}-vs-{args.agent2}-at-{timestamp}.json"
    replay_file.parent.mkdir(parents=True, exist_ok=True)

    process_args = [
        "luxai2022",
        str(agent1_file),
        str(agent2_file),
        "--replay.save_format", "json",
        "--output", str(replay_file),
        "--seed", str(seed),
        "--verbose", "3"
    ]

    process = subprocess.run(process_args)
    if process.returncode != 0:
        raise RuntimeError(f"luxai2022 exited with error code {process.returncode} while running {args.agent1} against {args.agent2} to {replay_file}")

    if args.open:
        webbrowser.open(f"https://jmerle.github.io/lux-eye-2022/?input=http://localhost:8000/{replay_file.name}")

if __name__ == "__main__":
    main()
