# crossword-Puzzle

Fully implemented multiplayer crossword puzzle, which allows up to 3 clients to connect to the server and and play a crossword
puzzle. The game generates a board given a list of words, using a backtracking algorithm to auto generate random boards. 

Server connects all the players, and uses locks to ensure a round robin turn style during the game.
