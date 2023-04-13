from pathlib import Path

def get_agent_file(name: str) -> Path:
    for file in [Path(name), Path(__file__).parent.parent / "build" / "agents" / name / "main.py"]:
        if file.is_file():
            return file

    raise ValueError(f"Agent {name} does not exist")
