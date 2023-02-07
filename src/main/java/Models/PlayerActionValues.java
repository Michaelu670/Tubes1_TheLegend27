package Models;

import Enums.PlayerActions;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerActionValues extends PlayerAction{

    public double immediateValue;
    public double heuristicValue;
    public int dead; // 1 if Player will die
    private static final double immediateValueRate = 0.7;
    private static final double heuristicValueRate = 0.3;
    private static final double SUPERFOOD_CONSTANT = 3;

    public PlayerActionValues(PlayerAction playerAction, PlayerActions playerActions, int newHeading) {
        immediateValue = 0;
        heuristicValue = 0;
        dead = 0;
        playerId = playerAction.getPlayerId();
        action = playerActions;
        heading = newHeading;
    }

    public double getValue() {
        if(isDead()) return 0.0;
        return (immediateValue * immediateValueRate) +
                (heuristicValue * heuristicValueRate);
    }



    public void addImmediateValue(double addedValue) {
        this.immediateValue += addedValue;
    }

    public void addHeuristicValue(double addedValue) {
        this.heuristicValue += addedValue;
    }

    public void computePositionImmediateGain(Position pos, GameState gameState, GameObject bot,
                                              List<GameObject> otherPlayerList,
                                              List<GameObject> foodList,
                                              List<GameObject> superfoodList,
                                              List<GameObject> poisongasList,
                                              List<GameObject> torpedoList) {
        double gain = 0.0;
        // Check other player
        for (var player : otherPlayerList) {
            if(!PlayerActionValuesList.isCollide(bot, player)) continue;
            if (player.getSize() >= 2 * bot.getSize()) {
                setToDead();
                return;
            } else if (player.getSize() > bot.getSize()) {
                addImmediateValue(-player.getSize() / 2);
            }
            else {
                addImmediateValue(Math.min(bot.getSize() / 2, player.getSize()));
            }
        }
        // Check food
        for (var obj : foodList) {
            if(!PlayerActionValuesList.isCollide(bot, obj)) continue;
            addImmediateValue(obj.getSize());
            // TODO If superfood, addImmediateValue 2 * obj

        }
        // Check superfood
        for (var obj : superfoodList) {
            if(!PlayerActionValuesList.isCollide(bot, obj)) continue;
            addImmediateValue(obj.getSize());
            // If not superfood, add superfood effects
            addImmediateValue(SUPERFOOD_CONSTANT);
        }

        // Check gas
        for (var obj : poisongasList) {
            if(!PlayerActionValuesList.isCollide(bot, obj)) continue;
            addImmediateValue(-1);
            // TODO add heuristic value of radius - distance (not here)
        }

    }

    public void computePositionHeuristicValue(Position pos, GameState gameState, GameObject bot,
                                              List<GameObject> otherPlayerList,
                                              List<GameObject> foodList,
                                              List<GameObject> superfoodList,
                                              List<GameObject> poisongasList) {
        for (var obj : foodList) {
            addHeuristicValue(100 * obj.getSize() /
                    (Math.pow(PlayerActionValuesList.getDistanceBetween(bot, obj),2) + 1.0));
        }
    }

    public String toString() {
        String ret = new String();
        ret += "Move : " + getAction() + "\n";
        ret += "Heading : " + getHeading() + "\n";
        ret += "Value : " + getValue();
        return ret;
    }

    public boolean isDead() {
        return dead == 1;
    }

    public void setToDead() {
        dead = 1;
    }
}
