package Models;

import Enums.ObjectTypes;
import Enums.PlayerActions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class PlayerActionValuesList{
    private ArrayList<PlayerActionValues> values;
    private static final double TORPEDO_MULTIPLIER = 2;
    private static final PlayerActions[] actionsWithDirection = {
            PlayerActions.FORWARD,
            // PlayerActions.FIRE_SUPERNOVA, /* Direction dihitung,
            // PlayerActions.FIRE_TORPEDOES,    bukan dicari 0-359 */
            PlayerActions.FIRETELEPORTER
    };
    private static final PlayerActions[] actionsWithoutDirection = {
            PlayerActions.STOP,
            PlayerActions.STARTAFTERBURNER,
            PlayerActions.STOPAFTERBURNER,
            PlayerActions.DETONATESUPERNOVA,
            PlayerActions.TELEPORT,
            PlayerActions.USESHIELD
    };

    public PlayerActionValuesList() {
        values = new ArrayList<>();
    }

    public PlayerActionValuesList(PlayerAction playerAction, GameState gameState, GameObject bot) {
        values = new ArrayList<>();
        fill(playerAction);
        compute(gameState, bot);
    }

    private void fill(PlayerAction playerAction) {

        for (var currentPlayerAction : actionsWithDirection) {
            for (int heading = 0; heading < 360; heading++) {
                values.add(new PlayerActionValues(playerAction, currentPlayerAction, heading));
            }
        }
        for (var currentPlayerAction : actionsWithoutDirection) {
            values.add(new PlayerActionValues(playerAction, currentPlayerAction, 0));
        }
    }

    private void compute(GameState gameState, GameObject bot) {
        var otherPlayerList = gameState.getPlayerGameObjects()
                .stream().filter(item -> item.getId() != bot.getId())
                .sorted(Comparator.comparing(item -> item.getSize()))
                .collect(Collectors.toList());
        var foodList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                .collect(Collectors.toList());
        var superfoodList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERFOOD)
                .collect(Collectors.toList());
        var poisongasList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.GAS_CLOUD)
                .collect(Collectors.toList());
        var torpedoList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDO_SALVO)
                .collect(Collectors.toList());


        for (var playerAction : values) {
            playerAction.immediateValue = 0.0;
            playerAction.heuristicValue = 0.0;

            var pos = bot.getPosition();
            switch (playerAction.getAction()) {
                case FORWARD:
                    pos.setX(pos.getX() + (int) (bot.getSpeed() * Math.cos(playerAction.getHeading())));
                    pos.setY(pos.getY() + (int) (bot.getSpeed() * Math.sin(playerAction.getHeading())));
                    break;
                case STOP:
                    break;
                case STARTAFTERBURNER:
                    // Unused, set to dead
                    playerAction.setToDead();
                    break;
                case STOPAFTERBURNER:
                    playerAction.setToDead();
                    break;
                case FIRETORPEDOES:
                    playerAction.addHeuristicValue(
                            TORPEDO_MULTIPLIER * otherPlayerList.get(0).getSize());
                    break;
                case FIRESUPERNOVA:
                    playerAction.setToDead();
                    break;
                case DETONATESUPERNOVA:
                    playerAction.setToDead();
                    break;
                case FIRETELEPORTER:
                    playerAction.setToDead();
                    break;
                case TELEPORT:
                    playerAction.setToDead();
                    break;
                case USESHIELD:
                    playerAction.setToDead();
                    break;
            }

            playerAction.computePositionImmediateGain(pos, gameState, bot,
                    otherPlayerList, foodList, superfoodList, poisongasList, torpedoList);
            playerAction.computePositionHeuristicValue(pos, gameState, bot,
                    otherPlayerList, foodList, superfoodList, poisongasList);


        }
    }

    public PlayerAction bestAction() {
        assert (!values.isEmpty());
        values.sort(Comparator.comparingDouble(PlayerActionValues::getValue));
        System.out.println(values.get(values.size()-1).toString());
        return values.get(values.size() - 1);
    }

    public static boolean isCollide(GameObject object1, GameObject object2) {
        return getDistanceBetween(object1, object2) < object1.getSize() + object2.getSize();
    }

    public static double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    public static int getHeadingBetween(GameObject thisObject, GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - thisObject.getPosition().y,
                otherObject.getPosition().x - thisObject.getPosition().x));
        return (direction + 360) % 360;
    }

    public static int getOppositeDirection(int direction) {
        return (direction + 180) % 360;
    }

    public static int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }

}
