To test search algorithms, run ChessClient.java.

To change the algorithm that you're testing, between line 79 and 83 are all the different algorithms.
The default is for MiniMax of depth 4 to be playing as black against the player, who is white.
One can comment out the line that sets minimax as black and uncomment one of the other AIs to test another AI.

The AI takes two parameters in the constructor. The first is its color, which it needs to know. 
If the AI is initialized as black put in 1, 0 if white. The second parameter is the depth it's limited to.
Currently, the depths are all set at a default of 4, but they can in increased/decreased.