package Models;

import Enums.*;
import java.util.*;

public class GameObject {
  public UUID id;
  public Integer size;
  public Integer speed;
  public Integer currentHeading;
  public Position position;
  public ObjectTypes gameObjectType;
  public EnumSet<Effects> effects;
  public Integer torpedoSalvoCount;
  public Integer supernovaAvailable;
  public Integer teleporterCount;
  public Integer shieldCount;


  public EnumSet<Effects> getEffects() {
    return effects;
  }

  public void setEffects(EnumSet<Effects> effects) {
    this.effects = effects;
  }


  public GameObject(UUID id, Integer size, Integer speed, Integer currentHeading, Position position, ObjectTypes gameObjectType, EnumSet<Effects> effects, Integer torpedoSalvoCount, Integer supernovaAvailable, Integer teleporterCount, Integer shieldCount) {
    this.id = id;
    this.size = size;
    this.speed = speed;
    this.currentHeading = currentHeading;
    this.position = position;
    this.gameObjectType = gameObjectType;
    this.effects = effects;
    this.torpedoSalvoCount = torpedoSalvoCount;
    this.supernovaAvailable = supernovaAvailable;
    this.teleporterCount = teleporterCount;
    this.shieldCount = shieldCount;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getSpeed() {
    return speed;
  }

  public void setSpeed(int speed) {
    this.speed = speed;
  }

  public Position getPosition() {
    return position;
  }

  public void setPosition(Position position) {
    this.position = position;
  }

  public ObjectTypes getGameObjectType() {
    return gameObjectType;
  }

  public void setGameObjectType(ObjectTypes gameObjectType) {
    this.gameObjectType = gameObjectType;
  }

  public Integer getTorpedoSalvoCount() {
    return torpedoSalvoCount;
  }

  public void setTorpedoSalvoCount(Integer torpedoSalvoCount) {
    this.torpedoSalvoCount = torpedoSalvoCount;
  }

  public Integer getSupernovaAvailable() {
    return supernovaAvailable;
  }

  public void setSupernovaAvailable(Integer supernovaAvailable) {
    this.supernovaAvailable = supernovaAvailable;
  }

  public Integer getTeleporterCount() {
    return teleporterCount;
  }

  public void setTeleporterCount(Integer teleporterCount) {
    this.teleporterCount = teleporterCount;
  }

  public Integer getShieldCount() {
    return shieldCount;
  }

  public void setShieldCount(Integer shieldCount) {
    this.shieldCount = shieldCount;
  }

  public static GameObject FromStateList(UUID id, List<Integer> stateList)
  {
    Position position = new Position(stateList.get(4), stateList.get(5));
    return new GameObject(id, stateList.get(0), stateList.get(1), stateList.get(2), position, ObjectTypes.valueOf(stateList.get(3)), Effects.getEffects(stateList.get(6)),
            (stateList.size() > 7 ? stateList.get(7) : 0),
            (stateList.size() > 8 ? stateList.get(8) : 0),
            (stateList.size() > 9 ? stateList.get(9) : 0),
            (stateList.size() > 10 ? stateList.get(10) : 0)
    );
  }
}
