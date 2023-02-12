package Services;

import Enums.*;
import Models.*;
//import com.ctc.wstx.shaded.msv_core.datatype.xsd.Comparator;

import java.lang.reflect.Array;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    private Integer currentTick;
    private Integer prevRadius;
    private Teleporter teleporter;

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
        currentTick = -1;
        prevRadius = -1;
        this.teleporter = new Teleporter();
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
        // Tick counting
        if (gameState.getWorld().getCurrentTick() == null ||
                currentTick >= gameState.getWorld().getCurrentTick())
            return;
        tickOutput();
        if (gameState.getWorld().getCurrentTick() != null) {
            currentTick = gameState.getWorld().getCurrentTick();
        }

        // Update delta
        if (prevRadius != -1)
            gameState.getWorld().setDeltaRadius(prevRadius - gameState.getWorld().getRadius());


        // Time start
        Timestamp start_time = new Timestamp(System.currentTimeMillis());

        // Compute action
        PlayerActionValuesList valuesList = new PlayerActionValuesList(playerAction, gameState, bot, teleporter);


        // Time end
        Timestamp end_time = new Timestamp(System.currentTimeMillis());
        System.out.print("Tick: " + gameState.getWorld().getCurrentTick());
        System.out.println(", Time needed: " + (double)(end_time.getNanos() - start_time.getNanos()) / 1000000.0 + " ms");

        this.playerAction = valuesList.bestAction();
        playerAction = this.playerAction;

        // Set isTeleporterActive = true, if current action "FIRETELEPORT"
        if(playerAction.getAction() == PlayerActions.FIRETELEPORT){
            teleporter.setTeleporterActive(true);
            teleporter.setHeading(playerAction.getHeading());
            teleporter.setTargetPosition(valuesList.bestAction().target.getPosition());
        }

        // Keluarin action "TELEPORT" 2x in case tidak keteleport yg awal
        if(playerAction.getAction() == PlayerActions.TELEPORT){
            teleporter.cntOutput++;
        }
        // Reset teleporter, if current action "TELEPORT"
        if(teleporter.cntOutput == 2){
            teleporter.reset();
        }

        System.out.println("Size : " + bot.getSize());
        System.out.println("Pos : " + bot.getPosition().getX() + " " + bot.getPosition().getY());
        System.out.println("Effects : " + bot.getEffects().toString());
        System.out.println("Salvo count : " + bot.getTorpedoSalvoCount());
        System.out.println("Teleporter count: " + bot.getTeleporterCount());
        System.out.println("");
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

    private void tickOutput() {
        if (currentTick + 1 < gameState.getWorld().getCurrentTick()) {
            System.out.println("Skipped on tick " +
                    String.valueOf(currentTick + 1) + " - " +
                    String.valueOf(gameState.getWorld().getCurrentTick() - 1));
        }
    }




}
