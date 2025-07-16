# Gitlet - A Simple Version-Control System

## Overview

Gitlet is a simplified version-control system inspired by Git. It's designed to manage and track changes in your codebase. While smaller and simpler than Git, Gitlet provides essential version-control features, making it a useful tool for understanding fundamental version-control concepts.

Completed as part of the CS 61B course at UC Berkeley.

## Project Description

Gitlet allows you to:

- **Save Commits**: Commit changes to save snapshots of your entire project directory. These snapshots are referred to as "commits."

- **Restore Files**: Restore one or more files to a previous version from the commit history.

- **View Commit History**: Explore the history of commits, including messages and timestamps, to track the evolution of your project.

- **Manage Branches**: Maintain multiple branches of your project, enabling parallel development and experimentation.

- **Merge Branches**: Merge changes made in one branch into another to incorporate new features or bug fixes.

Gitlet is designed to be user-friendly, making it easier for individuals and teams to collaborate on software projects, track changes, and recover from mistakes.

## How to Use Gitlet

To use Gitlet, interact with it via the command line. Here are some common commands and how to use them:

- `gitlet init`: Initialize a new Gitlet repository in your current directory.

- `gitlet add <file>`: Stage a file for the next commit.

- `gitlet commit <message>`: Create a new commit with staged changes and a commit message.

- `gitlet log`: View a log of all commits, including unique identifiers, timestamps, and commit messages.

- `gitlet checkout <commit> <file>`: Restore a specific file to its state at a given commit.

- `gitlet branch <branch-name>`: Create a new branch for parallel development.

- `gitlet merge <branch-name>`: Merge changes from one branch into the current branch.

- `gitlet status`: Display the current repository status, including staged files and branch information.

## Getting Started

Before using Gitlet, ensure you have Java and a Java compiler installed on your system. Follow these steps:

1. Clone this repository to your local machine:

   ```bash
   git clone https://github.com/your-username/gitlet.git
   ```

2. Navigate to the project directory:

   ```bash
   cd gitlet
   ```

3. Compile the Java source files:

   ```bash
   javac gitlet/Main.java
   ```

4. Initialize a new Gitlet repository:

   ```bash
   java gitlet.Main init
   ```

Now you can start using Gitlet by running the various commands mentioned earlier.

## Project Structure

- `gitlet/`: The main directory containing the Java source code for the Gitlet project.

- `Main.java`: The entry point of the Gitlet application, handling command-line interactions and executing Gitlet commands.

- `Commit.java`: A class representing Gitlet commits, storing commit messages, timestamps, parent commits, and file references.

- `Repository.java`: A class managing Gitlet repositories, including commit history and file snapshots.

## Acknowledgments

This project was inspired by Git, the powerful version-control system used by developers worldwide. I also thank the creators of the Gitlet project for providing initial specifications and inspiration for this simplified version-control system.

Enjoy using Gitlet for your version-control needs!
