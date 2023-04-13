import math
import multiprocessing
import random
import re
import shutil
import subprocess
import time
from argparse import ArgumentParser
from common import get_agent_file
from dataclasses import dataclass
from pathlib import Path
from rich.live import Live
from rich.table import Table
from threading import Lock, Thread
from typing import List

@dataclass
class Opponent:
    file: Path
    wins: int = 0
    losses: int = 0
    draws: int = 0
    reverse: bool = False

    @property
    def name(self) -> str:
        return self.file.parent.name

    @property
    def win_rate(self) -> float:
        return (self.wins + 0.5 * self.draws) / self.games if self.games > 0 else 0

    @property
    def games(self) -> int:
        return self.wins + self.losses + self.draws

class Manager:
    def __init__(self, me: Path, opponents: List[Opponent]) -> None:
        self.me = me
        self.opponents = opponents
        self.lock = Lock()
        self.running = True

    def start(self, worker_count: int) -> None:
        worker_threads = []

        for _ in range(worker_count):
            worker_thread = Thread(target=self.worker_target)
            worker_thread.start()
            worker_threads.append(worker_thread)

        try:
            with Live(self.create_table()) as live:
                while True:
                    time.sleep(0.25)
                    live.update(self.create_table())
        except KeyboardInterrupt:
            print("Shutting down...")
            self.running = False

    def create_table(self) -> Table:
        table = Table()

        _, terminal_height = shutil.get_terminal_size((80, 20))
        opponents_per_section = terminal_height - 4
        sections = math.ceil(len(self.opponents) / opponents_per_section)

        for _ in range(sections):
            table.add_column("#")
            table.add_column("Opponent")
            table.add_column("Win rate")
            table.add_column("Wins")
            table.add_column("Losses")
            table.add_column("Draws")
            table.add_column("Games")

        with self.lock:
            sorted_opponents = sorted(self.opponents, key=lambda opponent: opponent.win_rate)

            for y in range(opponents_per_section):
                columns = []

                for x in range(sections):
                    opponent_idx = x * opponents_per_section + y
                    if opponent_idx >= len(sorted_opponents):
                        break

                    opponent = sorted_opponents[opponent_idx]
                    columns.extend([str(opponent_idx + 1),
                                    opponent.name,
                                    f"{opponent.win_rate * 100:.2f}%",
                                    str(opponent.wins),
                                    str(opponent.losses),
                                    str(opponent.draws),
                                    str(opponent.games)])

                if len(columns) == 0:
                    break

                table.add_row(*columns)

        return table

    def worker_target(self) -> None:
        while self.running:
            with self.lock:
                min_games = min(opponent.games for opponent in self.opponents)
                opponent = random.choice([opponent for opponent in self.opponents if opponent.games == min_games])

                reverse = opponent.reverse
                opponent.reverse = not opponent.reverse

            agent1, agent2 = str(self.me), str(opponent.file)
            if reverse:
                agent1, agent2 = agent2, agent1

            process = subprocess.Popen(["luxai2022", agent1, agent2, "--seed", str(random.randint(0, 1e9)), "--verbose", "3"],
                                        stdout=subprocess.PIPE,
                                        stderr=subprocess.DEVNULL)

            while self.running:
                try:
                    stdout, _ = process.communicate(timeout=1)
                    break
                except subprocess.TimeoutExpired:
                    pass

            if not self.running:
                process.kill()
                return

            stdout = stdout.decode("utf-8")

            matches = re.search(r"'player_0': ([0-9.-]+), 'player_1': ([0-9.-]+)", stdout)
            if matches is None:
                raise RuntimeError(f"Cannot find result of match between {agent1} and {agent2}, output:\n{stdout}")

            score1 = float(matches.group(1))
            score2 = float(matches.group(2))

            with self.lock:
                if score1 == score2:
                    opponent.draws += 1
                elif score1 > score2:
                    if reverse:
                        opponent.losses += 1
                    else:
                        opponent.wins += 1
                else:
                    if reverse:
                        opponent.wins += 1
                    else:
                        opponent.losses += 1

def main() -> None:
    parser = ArgumentParser(description="Run multiple matches between an agent and one or more other agents.")
    parser.add_argument("me", type=str, help="name of the primary agent")
    parser.add_argument("opponents", type=str, nargs="*", help="names of the other agents, defaults to all other agents")

    args = parser.parse_args()

    me = get_agent_file(args.me)

    opponents = list(set(get_agent_file(agent) for agent in args.opponents))
    if len(opponents) == 0:
        opponents = list((Path.cwd() / "build" / "agents").rglob("main.py"))
        opponents.remove(me)

    if len(opponents) < 1:
        raise RuntimeError("At least one opponent is required")

    opponents = [Opponent(file) for file in opponents]
    worker_count = math.floor(multiprocessing.cpu_count() / 4)

    manager = Manager(me, opponents)
    manager.start(worker_count)

if __name__ == "__main__":
    main()
