package Models;

import Enums.Effects;
import Enums.PlayerActions;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerActionValues extends PlayerAction{

    public double immediateValue;
    public double heuristicValue;
    public int dead; // 1 if Player will die
    public GameObject target;
    private static final double immediateValueRate = 0.7;
    private static final double heuristicValueRate = 0.3;
    private static final double SUPERFOOD_CONSTANT = 3;
    private static final double WORLD_EDGE_CONSTANT = 10;

    public PlayerActionValues(GameObject bot, PlayerAction playerAction, PlayerActions playerActions, int newHeading) {
        immediateValue = 0;
        heuristicValue = 0;
        dead = 0;
        playerId = bot.getId();
        action = playerActions;
        heading = newHeading;
    }

    public double getValue() {
        if(isDead()) return -Double.MAX_VALUE;
        return (immediateValue * immediateValueRate) +
                (heuristicValue * heuristicValueRate);
    }

    public GameObject getTarget() {
        return target;
    }

    public void setTarget(GameObject target) {
        this.target = target;
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
        GameObject TempBot = new GameObject(bot.getId(),
                bot.getSize(),
                bot.getSpeed(),
                bot.currentHeading,
                pos,
                bot.getGameObjectType(),
                bot.getEffects(),
                bot.getTorpedoSalvoCount(),
                bot.getSupernovaAvailable(),
                bot.getTeleporterCount(),
                bot.getShieldCount());

        // Check other player
        for (var player : otherPlayerList) {
            if (PlayerActionValuesList.getDistanceBetween(player, TempBot) >
                player.getSize() + TempBot.getSize() + player.getSpeed()) continue;
            if (player.getSize() >= 2 * TempBot.getSize()) {
                setToDead();
                return;
            }
            if (PlayerActionValuesList.getDistanceBetween(player, TempBot) >
                    player.getSize() + TempBot.getSize() - player.getSpeed() / 3) continue;
            if (player.getSize() > TempBot.getSize()) {
                addImmediateValue(-player.getSize() / 2);
            }
            else {
                addImmediateValue(Math.min(TempBot.getSize() / 2, player.getSize()));
            }
        }
        // Check food
        for (var obj : foodList) {
            if(!PlayerActionValuesList.isCollide(TempBot, obj)) continue;
            addImmediateValue(obj.getSize());
            if (bot.getEffects().contains(Effects.SUPERFOOD)) {
                addImmediateValue(obj.getSize());
            }
        }
        // Check superfood
        for (var obj : superfoodList) {
            if(!PlayerActionValuesList.isCollide(TempBot, obj)) continue;
            addImmediateValue(obj.getSize());
        }

        // Check gas
        boolean isGas = false;
        for (var obj : poisongasList) {
            if(!PlayerActionValuesList.isCollide(TempBot, obj)) continue;
            isGas = true;
        }

        // Check outer gas
        if (PlayerActionValuesList.getDistanceBetween(pos, gameState.getWorld().getCenterPoint())
                > gameState.getWorld().getRadius() - bot.getSize() - gameState.getWorld().getDeltaRadius() - 1) {
            isGas = true;
        }

        if (isGas) addImmediateValue(-1);

    }

    public void computePositionHeuristicValue(Position pos, GameState gameState, GameObject bot,
                                              List<GameObject> otherPlayerList,
                                              List<GameObject> foodList,
                                              List<GameObject> superfoodList,
                                              List<GameObject> poisongasList) {
        GameObject TempBot = new GameObject(bot.getId(),
                bot.getSize(),
                bot.getSpeed(),
                bot.currentHeading,
                pos,
                bot.getGameObjectType(),
                bot.getEffects(),
                bot.getTorpedoSalvoCount(),
                bot.getSupernovaAvailable(),
                bot.getTeleporterCount(),
                bot.getShieldCount());

        addHeuristicValue(1000 / (Math.pow(PlayerActionValuesList.getDistanceBetween(
                TempBot.getPosition(), gameState.getWorld().getCenterPoint()), 3) + 1.0));

        for (var player : otherPlayerList) {
            if (player.getSize() > bot.getSize()) {
                addHeuristicValue(-700 * Math.min(bot.getSize(), player.getSize() / 2)/
                        (Math.pow(PlayerActionValuesList.getDistanceBetween(pos, player.getPosition())
                                - player.getSize() - bot.getSize(),3) + 1.0));
            }
            else {
                addHeuristicValue(400 * Math.min(bot.getSize() / 2, player.getSize())/
                        (Math.pow(PlayerActionValuesList.getDistanceBetween(pos, player.getPosition())
                                - player.getSize() - bot.getSize(),3) + 1.0));
            }
        }
        for (var obj : foodList) {
            addHeuristicValue(150 * obj.getSize() /
                    (Math.pow(PlayerActionValuesList.getDistanceBetween(
                            pos, obj.getPosition()),3) + 1.0));
        }

        for (var obj : superfoodList) {
            addHeuristicValue(400 * obj.getSize() /
                    (Math.pow(PlayerActionValuesList.getDistanceBetween(
                            pos, obj.getPosition()),3) + 1.0));
            if (PlayerActionValuesList.isCollide(TempBot, obj)) {
                addHeuristicValue(SUPERFOOD_CONSTANT);
            }
        }

        for (var obj : poisongasList) {
            if (PlayerActionValuesList.isCollide(TempBot, obj)) {
                addHeuristicValue(-TempBot.getSize() - obj.getSize()
                        + PlayerActionValuesList.getDistanceBetween(TempBot, obj));
            }
        }

        addHeuristicValue(Math.min(
                -PlayerActionValuesList.getDistanceBetween(pos, gameState.getWorld().getCenterPoint())
                - TempBot.getSize() + gameState.getWorld().getRadius() - WORLD_EDGE_CONSTANT, 0));
    }

    public void addTorpedoValue(GameObject bot, GameState gameState) {
        if(target.getEffects().contains(Effects.SHIELD) || bot.getSize() <= 25) {
            setToDead();
        }
        else {
            addHeuristicValue(target.getSize() /
                    Math.pow(PlayerActionValuesList.getDistanceBetween(target, bot), 2.5)  // TODO Bot distance
                    * Math.pow((bot.getTorpedoSalvoCount()), 3) * (target.getSize()/bot.getSize()) * gameState.getWorld().getCurrentTick() / 62500);
        }
    }

    public void addShieldValue(GameObject bot, List <GameObject> torpedoList) {
        if((bot.getSize() <= 50) || (bot.getShieldCount() == 0) || bot.getEffects().contains(Effects.SHIELD)) {
            setToDead();
        }
        else {
            double val = 0;
            boolean flag = false; // flag incoming torpedo, true if torpedo inbound
            for (var torpedo : torpedoList) {
                double distance = PlayerActionValuesList.getDistanceBetween(bot, torpedo) - bot.getSize();
                double relativeHeading = Math.abs(PlayerActionValuesList.getHeadingBetween(torpedo, bot) - torpedo.currentHeading);
                Double deviance = Math.toDegrees(Math.asin((bot.getSize() + torpedo.getSize()) / distance)); // use Double class to check for NaN
                if (relativeHeading < deviance /*&& !deviance.isNaN()*/) { // check if torpedo is possible to collide
                    if (relativeHeading == 0) relativeHeading = 0.001;
                    val += torpedo.getSize() * (deviance / relativeHeading) / Math.pow(distance/20, 4);
                    flag = true;
                }
            }
            if(flag) {
                val *= bot.size / 60;
                addHeuristicValue(val);
            }
            else setToDead();
        }
    }
    public String toString() {
        String ret = new String();
        ret += "Move : " + getAction() + "\n";
        ret += "Heading : " + getHeading() + "\n";
        ret += "Value : " + getValue() + "\n";
        ret += "Imm Value : " + immediateValue + " | Heu Value : " + heuristicValue;
        return ret;
    }

    public boolean isDead() {
        return dead == 1;
    }

    public void setToDead() {
        dead = 1;
    }
}
