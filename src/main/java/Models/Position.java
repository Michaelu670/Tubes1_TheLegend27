package Models;

public class Position {

  public int x;
  public int y;

  public Position() {
    x = 0;
    y = 0;
  }

  public Position(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public void generateRandomPosition(GameState gameState) {
    Position center = gameState.getWorld().getCenterPoint();
    double r = gameState.getWorld().getRadius() * Math.sqrt(Math.random());
    double theta = Math.random() * 2 * Math.PI;
    setX((int) (center.getX() + r * Math.cos(theta)));
    setY((int) (center.getY() + r * Math.sin(theta)));
  }
}
