package Models;

import Enums.Effects;
import Enums.PlayerActions;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerActionValues extends PlayerAction {

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
        if (isDead())
            return -Double.MAX_VALUE;
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
            if (PlayerActionValuesList.getDistanceBetween(player, TempBot) > player.getSize() + TempBot.getSize()
                    + player.getSpeed())
                continue;
            if (player.getSize() >= 2 * TempBot.getSize()) {
                setToDead();
                return;
            }
            if (player.getSize() > TempBot.getSize()) {
                addImmediateValue(-player.getSize() / 2);
            }
            if (PlayerActionValuesList.getDistanceBetween(player, TempBot) > player.getSize() + TempBot.getSize()
                    - player.getSpeed() / 3)
                continue;

            if (player.getSize() + 3 < TempBot.getSize()) {
                addImmediateValue(Math.min(TempBot.getSize() / 2, player.getSize()));
            }
        }

        // Check food
        for (var obj : foodList) {
            if (!PlayerActionValuesList.isCollide(TempBot, obj))
                continue;
            addImmediateValue(obj.getSize());
            if (bot.getEffects().contains(Effects.SUPERFOOD)) {
                addImmediateValue(obj.getSize());
            }
        }

        // Check superfood
        for (var obj : superfoodList) {
            if (!PlayerActionValuesList.isCollide(TempBot, obj))
                continue;
            addImmediateValue(obj.getSize());
        }

        // Check gas
        boolean isGas = false;
        for (var obj : poisongasList) {
            if (!PlayerActionValuesList.isCollide(TempBot, obj))
                continue;
            isGas = true;
        }

        // Check outer gas
        if (PlayerActionValuesList.getDistanceBetween(pos, gameState.getWorld().getCenterPoint()) > gameState.getWorld()
                .getRadius() - bot.getSize() - gameState.getWorld().getDeltaRadius() - 1) {
            isGas = true;
        }

        if (isGas)
            addImmediateValue(-1);

        if (bot.getSize() + immediateValue < 5)
            setToDead();
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
            if (player.getSize() + 6 >= bot.getSize()) {
                addHeuristicValue(-4 * Math.min(bot.getSize(), player.getSize() / 2)
                        / Math.pow(PlayerActionValuesList.getTickDistance(TempBot, player,
                                bot.getSpeed() + player.getSpeed()), 2));
            } else {
                addHeuristicValue(Math.min(bot.getSize() / 2, player.getSize())
                        / Math.pow(PlayerActionValuesList.getTickDistance(TempBot, player,
                                Math.max(bot.getSpeed() - player.getSpeed() / 3, 1)), 2));
            }
        }
        for (var obj : foodList) {
            addHeuristicValue(obj.getSize() /
                    Math.pow(PlayerActionValuesList.getTickDistance(TempBot, obj, bot.getSpeed()), 2));
        }

        for (var obj : superfoodList) {
            addHeuristicValue(obj.getSize() /
                    Math.pow(PlayerActionValuesList.getTickDistance(TempBot, obj, bot.getSpeed()), 2));
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
                        - TempBot.getSize() + gameState.getWorld().getRadius() - WORLD_EDGE_CONSTANT,
                0));
    }

    public void addTorpedoValue(GameObject bot, GameState gameState) {
        if (target.getEffects().contains(Effects.SHIELD) || bot.getSize() <= 25 || bot.getTorpedoSalvoCount() == 0) {
            setToDead();
        } else {
            /*
             * Will hit if cos(cumulative player move degree) < maxcos
             * Means : if maxcos >= 1 || maxcos < 0, guaranteed to hit
             * else chance = 1 - arccos(maxcos) / PI
             */
            Double maxcos = (target.getSize() + 10) * 20 / (target.getSpeed())
                    / (PlayerActionValuesList.getDistanceBetween(target, bot)
                            - bot.getSize() - 20 - target.getSize());
            Double p = maxcos >= 0 && maxcos <= 1 ? 1 - Math.acos(maxcos) / Math.PI : 1.0;

            Double areaLintasan = 20 * (PlayerActionValuesList.getDistanceBetween(target, bot)
                    - bot.getSize() - target.getSize());
            Double totalArea = Math.PI * gameState.getWorld().getRadius() * gameState.getWorld().getRadius();
            var objectInArea = gameState.getGameObjects()
                    .stream().filter(item -> PlayerActionValuesList.getDistanceBetween(
                            gameState.getWorld().getCenterPoint(),
                            item.getPosition()) < gameState.getWorld().getRadius())
                    .collect(Collectors.toList());
            Double totalObjectInArea = Double.valueOf(objectInArea.size());

            Double predictedObjectInArea = totalObjectInArea * areaLintasan / totalArea;

            System.out.println("chance : " + p + ", predict obj : " + predictedObjectInArea);
            addHeuristicValue(
                    20 * p * Math.max(0, 1 - predictedObjectInArea / 4));
            addImmediateValue(-5);
        }
    }

    public void addShieldValue(GameObject bot, List<GameObject> torpedoList) {
        if ((bot.getSize() <= 50) || (bot.getShieldCount() == 0) || bot.getEffects().contains(Effects.SHIELD)) {
            setToDead();
        } else {
            boolean flag = false; // flag incoming torpedo, true if torpedo inbound
            for (var torpedo : torpedoList) {
                System.out.println("Torpedo speed : " + torpedo.getSpeed());
                double distance = PlayerActionValuesList.getDistanceBetween(bot, torpedo);
                double relativeHeading = Math
                        .abs(PlayerActionValuesList.getHeadingBetween(torpedo, bot) - torpedo.currentHeading);
                Double deviance = Math.toDegrees(
                        Math.asin((bot.getSize() + torpedo.getSize()) / distance)); // use Double class to check for NaN
                if (deviance.isNaN()) {
                    System.out.println("deviance isNaN");
                }
                if (relativeHeading < deviance && !deviance.isNaN()) { // check if torpedo is possible to collide
                    if (relativeHeading == 0)
                        relativeHeading = 0.001;
                    if (4 <= distance / torpedo.getSpeed() && distance / torpedo.getSpeed() <= 5) {
                        flag = true;
                    }
                }
            }
            if (flag) {
                addHeuristicValue(1000);
            } else
                setToDead();
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
