package Models;

import Enums.ObjectTypes;
import Enums.PlayerActions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class PlayerActionValuesList{
    private ArrayList<PlayerActionValues> values;
    private static final PlayerActions[] actionsWithDirection = {
            PlayerActions.FORWARD,
            // PlayerActions.FIRE_SUPERNOVA, /* Direction dihitung,
            // PlayerActions.FIRE_TORPEDOES,    bukan dicari 0-359 */
            // PlayerActions.FIRETELEPORTER
    };
    private static final PlayerActions[] actionsWithoutDirection = {
            PlayerActions.STOP,
            PlayerActions.STARTAFTERBURNER,
            PlayerActions.STOPAFTERBURNER,
            PlayerActions.DETONATESUPERNOVA,
            // PlayerActions.TELEPORT,
            PlayerActions.ACTIVATESHIELD
    };

    public PlayerActionValuesList() {
        values = new ArrayList<>();
    }

    public PlayerActionValuesList(PlayerAction playerAction, GameState gameState, GameObject bot, Teleporter myTeleporter) {
        values = new ArrayList<>();
        fill(playerAction, gameState, bot, myTeleporter);
        compute(gameState, bot);
    }

    private void fill(PlayerAction playerAction, GameState gameState, GameObject bot, Teleporter myTeleporter) {
        
        for (var currentPlayerAction : actionsWithDirection) {
            for (int heading = 0; heading < 360; heading++) {
                values.add(new PlayerActionValues(bot, playerAction, currentPlayerAction, heading));
            }
        }
        for (var currentPlayerAction : actionsWithoutDirection) {
            values.add(new PlayerActionValues(bot, playerAction, currentPlayerAction, 0));
        }
        var otherPlayerList = gameState.getPlayerGameObjects()
                .stream().filter(item -> item.getId() != bot.getId())
                .sorted(Comparator.comparing(item -> item.getSize()))
                .collect(Collectors.toList());
        for (var player : otherPlayerList) {
            values.add(new PlayerActionValues(bot, playerAction, PlayerActions.FIRETORPEDOES, getHeadingBetween(bot, player)));
            values.get(values.size() - 1).setTarget(player);
        }
        if(bot.getTeleporterCount() > 0 && bot.getSize() > 45 && myTeleporter.isTeleporterActive() == false){
            for (var player : otherPlayerList) {
                if(bot.getSize() - 20 > player.getSize()){
                    values.add(new PlayerActionValues(bot, playerAction, PlayerActions.FIRETELEPORT, getHeadingBetween(bot, player)));
                    values.get(values.size() - 1).setTarget(player);
                }
            }
            for(int i = 0; i < 20; i++){
                Position randPos = new Position();
                randPos.generateRandomPosition(gameState);
                GameObject target = new GameObject(null, null, null, null, randPos, null, null, null, null, null, null);
                values.add(new PlayerActionValues(bot, playerAction, PlayerActions.FIRETELEPORT, getHeadingBetween(bot, target)));
                values.get(values.size() - 1).setTarget(target);
            }
        }

        if(myTeleporter.isTeleporterActive()){
            var teleporterList = gameState.getGameObjects().stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER).collect(Collectors.toList());
            if(myTeleporter.getId() == null){
                for(var teleporter : teleporterList){
                    if(teleporter.currentHeading == myTeleporter.getHeading()){
                        myTeleporter.setId(teleporter.getId());
                    }
                }
            }
            // System.out.println("Teleporter ID: " + myTeleporter.getId());
            boolean found = false;
            for(var teleporter : teleporterList){
                if(teleporter.getId().equals(myTeleporter.getId())){
                    found = true;
                    double dist = getDistanceBetween(teleporter.getPosition(), myTeleporter.getTargetPosition());
                    System.out.println("Distance teleporter to target: " + dist);
                    if(dist <= 10){
                        System.out.println("Teleporter target position: (" + myTeleporter.getTargetPosition().getX() + ", " + myTeleporter.getTargetPosition().getY() + ")");
                        System.out.println("Teleporter position: (" + teleporter.getPosition().getX() + ", " + teleporter.getPosition().getY() + ")");
                        GameObject target = new GameObject(null, null, null, null, teleporter.getPosition(), null, null, null, null, null, null);
                        values.add(new PlayerActionValues(bot, playerAction, PlayerActions.TELEPORT, 0));
                        values.get(values.size() - 1).setTarget(target);
                    }
                    else{
                        double distBotToTeleporter = getDistanceBetween(bot.getPosition(), teleporter.getPosition());
                        double distBotToTarget = getDistanceBetween(bot.getPosition(), myTeleporter.getTargetPosition());
                        
                        if(distBotToTeleporter > distBotToTarget){
                            // Teleporter telah melewati target
                            System.out.println("Teleporter target position (kelewatan): (" + myTeleporter.getTargetPosition().getX() + ", " + myTeleporter.getTargetPosition().getY() + ")");
                            System.out.println("Teleporter position (kelewatan): (" + teleporter.getPosition().getX() + ", " + teleporter.getPosition().getY() + ")");
                            GameObject target = new GameObject(null, null, null, null, teleporter.getPosition(), null, null, null, null, null, null);
                            values.add(new PlayerActionValues(bot, playerAction, PlayerActions.TELEPORT, 0));
                            values.get(values.size() - 1).setTarget(target);
                        }
                        else{
                            // Teleporter belum melewati target
                            System.out.println("Teleporter target position (belum kelewatan): (" + myTeleporter.getTargetPosition().getX() + ", " + myTeleporter.getTargetPosition().getY() + ")");
                            System.out.println("Teleporter position (belum kelewatan): (" + teleporter.getPosition().getX() + ", " + teleporter.getPosition().getY() + ")");

                            // PlayerActionValues valuesRealTarget = new PlayerActionValues(bot, playerAction, PlayerActions.TELEPORT, 0);
                            // PlayerActionValues valuesCurrentTarget = new PlayerActionValues(bot, playerAction, PlayerActions.TELEPORT, 0);

                            // var foodList = gameState.getGameObjects()
                            //         .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                            //         .collect(Collectors.toList());
                            // var superfoodList = gameState.getGameObjects()
                            //         .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERFOOD)
                            //         .collect(Collectors.toList());
                            // var poisongasList = gameState.getGameObjects()
                            //         .stream().filter(item -> item.getGameObjectType() == ObjectTypes.GASCLOUD)
                            //         .collect(Collectors.toList());
                            // var torpedoList = gameState.getGameObjects()
                            //         .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDOSALVO)
                            //         .collect(Collectors.toList());
                            
                            // valuesRealTarget.computePositionImmediateGain(myTeleporter.getTargetPosition(), gameState, bot,
                            //         otherPlayerList, foodList, superfoodList, poisongasList, torpedoList);
                            // valuesRealTarget.computePositionHeuristicValue(myTeleporter.getTargetPosition(), gameState, bot,
                            //         otherPlayerList, foodList, superfoodList, poisongasList);

                            // valuesCurrentTarget.computePositionImmediateGain(teleporter.getPosition(), gameState, bot,
                            //         otherPlayerList, foodList, superfoodList, poisongasList, torpedoList);
                            // valuesCurrentTarget.computePositionHeuristicValue(teleporter.getPosition(), gameState, bot,
                            //         otherPlayerList, foodList, superfoodList, poisongasList);

                            // if(valuesCurrentTarget.getValue() > valuesRealTarget.getValue()){
                            //     System.out.println("Values real target: " + valuesRealTarget.getValue());
                            //     System.out.println("Values current target: " + valuesCurrentTarget.getValue());
                            //     GameObject target = new GameObject(null, null, null, null, teleporter.getPosition(), null, null, null, null, null, null);
                            //     values.add(new PlayerActionValues(bot, playerAction, PlayerActions.TELEPORT, 0));
                            //     values.get(values.size() - 1).setTarget(target);
                            // }
                            for(var player : otherPlayerList){
                                if(bot.getSize() > player.getSize() && getDistanceBetween(teleporter, player) <= player.getSize() + bot.getSize()){
                                    GameObject target = new GameObject(null, null, null, null, teleporter.getPosition(), null, null, null, null, null, null);
                                    values.add(new PlayerActionValues(bot, playerAction, PlayerActions.TELEPORT, 0));
                                    values.get(values.size() - 1).setTarget(target);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if(!found) {
                myTeleporter.cntNotFound++;
            }
            if(myTeleporter.cntNotFound == 2) {
                myTeleporter.reset();
            }
        }
    }

    private void compute(GameState gameState, GameObject bot) {
        /* Precompute GameObject List to save computational power */
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
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.GASCLOUD)
                .collect(Collectors.toList());
        var torpedoList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDOSALVO)
                .collect(Collectors.toList());


        for (var playerAction : values) {
            playerAction.immediateValue = 0.0;
            playerAction.heuristicValue = 0.0;

            var pos = new Position(bot.getPosition().getX(), bot.getPosition().getY());
            switch (playerAction.getAction()) {
                case FORWARD:
                    pos.setX(Math.round(pos.getX() + Math.round((double) bot.getSpeed() *
                            Math.cos(Math.toRadians(playerAction.getHeading())))));
                    pos.setY(Math.round(pos.getY() + Math.round((double) bot.getSpeed() *
                            Math.sin(Math.toRadians(playerAction.getHeading())))));
                    break;
                case STOP:
                    playerAction.setToDead();
                    break;
                case STARTAFTERBURNER:
                    // Unused, set to dead
                    playerAction.setToDead();
                    break;
                case STOPAFTERBURNER:
                    playerAction.setToDead();
                    break;
                case FIRETORPEDOES:
                    playerAction.addTorpedoValue(bot, gameState);
                    break;
                case FIRESUPERNOVA:
                    playerAction.setToDead();
                    break;
                case DETONATESUPERNOVA:
                    playerAction.setToDead();
                    break;
                case FIRETELEPORT:
                    bot.setSize(bot.getSize() - 20);
                    pos.setX(playerAction.target.getPosition().getX());
                    pos.setY(playerAction.target.getPosition().getY());
                    // playerAction.addTeleporterValue(bot, gameState);
                    break;
                case TELEPORT:
                    pos.setX(playerAction.getTarget().getPosition().getX());
                    pos.setY(playerAction.getTarget().getPosition().getY());
                    playerAction.addHeuristicValue(-10);
                    break;
                case ACTIVATESHIELD:
                    playerAction.addShieldValue(bot, torpedoList);
                    break;
                default:
                    playerAction.setToDead();
                    break;
            }

            playerAction.computePositionImmediateGain(pos, gameState, bot,
                    otherPlayerList, foodList, superfoodList, poisongasList, torpedoList);
            playerAction.computePositionHeuristicValue(pos, gameState, bot,
                    otherPlayerList, foodList, superfoodList, poisongasList);

            if (playerAction.getAction() == PlayerActions.FIRETELEPORT) {
                playerAction.addHeuristicValue(playerAction.immediateValue);
                playerAction.heuristicValue /= 1.5;
                playerAction.immediateValue = -20;
                bot.setSize(bot.getSize() + 20);
            }


        }
    }

    public PlayerActionValues bestAction() {
        assert (!values.isEmpty());
        values.sort(Comparator.comparingDouble(PlayerActionValues::getValue));
        System.out.println(values.get(values.size()-1).toString());
        return values.get(values.size() - 1);
    }

    public static boolean isCollide(GameObject object1, GameObject object2) {
        return ((int) Math.round(getDistanceBetween(object1, object2))) < object1.getSize() + object2.getSize();
    }

    public static Double getTickDistance(GameObject obj1, GameObject obj2, Integer speed) {
        if(speed == 0) return Double.MAX_VALUE;
        return Math.max(Math.ceil((getDistanceBetween(obj1, obj2) -
                obj1.getSize() - obj2.getSize()) / speed), 1.0);
    }

    public static double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    public static double getDistanceBetween(Position pos1, Position pos2) {
        var triangleX = Math.abs(pos1.x - pos2.x);
        var triangleY = Math.abs(pos1.y - pos2.y);
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
