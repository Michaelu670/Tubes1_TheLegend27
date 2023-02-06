package Services;

import Enums.*;
import Models.*;
//import com.ctc.wstx.shaded.msv_core.datatype.xsd.Comparator;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    private int tickLeft;
    private int outputTeleport;

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
        this.outputTeleport = 0;
    }


    public GameObject getBot() {
        return this.bot;
    }

    public void setBot(GameObject bot) {
        this.bot = bot;
    }

    public PlayerAction getPlayerAction() {
        return this.playerAction;
    }

    public void setPlayerAction(PlayerAction playerAction) {
        this.playerAction = playerAction;
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);

        if(tickLeft == 0 && outputTeleport != 0){
            playerAction.action = PlayerActions.TELEPORT;
            outputTeleport--;
        }
        else{
            if(tickLeft > 0) tickLeft--;
            var enemyList = gameState.getPlayerGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER && item.getSize() + 3 < bot.getSize())
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());
            if(bot.getSize() >= 40 && !enemyList.isEmpty() && outputTeleport == 0){
                playerAction.action = PlayerActions.FIRETELEPORT;
                outputTeleport = 5;
                tickLeft = (int) getDistanceBetween(bot, enemyList.get(0)) / 10 + 3;
                playerAction.heading = getHeadingBetween(enemyList.get(0));
            }

            else if (!gameState.getGameObjects().isEmpty()) {
                var foodList = gameState.getGameObjects()
                        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList());

                playerAction.heading = getHeadingBetween(foodList.get(0));
                if(!enemyList.isEmpty() && getDistanceBetween(bot, enemyList.get(0)) < 30) {
                    playerAction.heading = getHeadingBetween(enemyList.get(0));
                }
            }
        }

        this.playerAction = playerAction;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    private void updateSelfState() {
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream().filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    private double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    private int getHeadingBetween(GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }


}
