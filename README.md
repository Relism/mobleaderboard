
# MobLeaderboard

MobLeaderboard is a Bukkit/Spigot plugin for Minecraft that introduces a leaderboard system based on special kills on mob which have a metadata attribute of "testPlugin"

## Features

- **Leaderboard**: Use `/test` to open a leaderboard GUI displaying the top 10 players based on special kills.

- **Admin Controls**: Admins can utilize `/atest` to perform special actions.

- **MongoDB Abstraction**: Custom MongoDB abstraction layer built on top of the MongoDB Java driver, providing enhanced functionality and error handling.

- **Safe Reward Distribution**: Rewards are safely given to players, accounting for scenarios where players have full inventories or are offline.

## Commands

- `/test`: Opens a GUI displaying the top 10 players based on special kills in a pyramid-like shape.

- `/atest`: Admin command for various actions, including resetting all player kills, rewarding top players, and spawning special zombies.

## Documentation

Javadocs for the project can be found at [https://relism.github.io/mobleaderboard/](https://relism.github.io/mobleaderboard/).

## Development

This plugin was developed in less than 5 and a half hours. Contributions and bug reports are welcome.

